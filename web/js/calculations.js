// AutoFuel Calculation Engine (calculations.js)
import { MaintenanceStatusLevel, VehiclePowertrain } from './models.js';

/**
 * Calculates per-refill statistics (distance, km/L, cost/km)
 */
export function computeFuelRefillsWithStats(logs) {
  if (!logs || !logs.length) return [];

  // Sort ascending by odometer to calculate intervals
  const sorted = [...logs].sort((a, b) => a.odometerKm - b.odometerKm);

  const results = [];
  for (let i = 0; i < sorted.length; i++) {
    const current = sorted[i];
    const prev = i > 0 ? sorted[i - 1] : null;

    let distanceTraveledKm = null;
    let kmPerLiter = null;
    let costPerKm = null;

    if (prev && current.odometerKm > prev.odometerKm) {
      distanceTraveledKm = current.odometerKm - prev.odometerKm;

      // Fuel consumption is calculated when this is a full tank refill
      if (current.isFullTank && current.liters > 0) {
        kmPerLiter = distanceTraveledKm / current.liters;
      }

      if (distanceTraveledKm > 0 && current.totalCost > 0) {
        costPerKm = current.totalCost / distanceTraveledKm;
      }
    }

    results.push({
      log: current,
      distanceTraveledKm,
      kmPerLiter,
      costPerKm
    });
  }

  // Return sorted descending for display (most recent first)
  return results.sort((a, b) => b.log.odometerKm - a.log.odometerKm);
}

/**
 * Computes dashboard aggregate statistics for a vehicle
 */
export function computeDashboardStats(vehicle, refillsWithStats, maintenanceItemsWithStatus) {
  if (!vehicle) {
    return {
      currentOdometerKm: 0,
      totalDistanceLoggedKm: 0,
      averageKmPerLiter: 0,
      averageCostPerKm: 0,
      totalFuelSpent: 0,
      totalLitersFilled: 0,
      lastKmPerLiter: null,
      gasolineAvgKmPerL: null,
      ethanolAvgKmPerL: null,
      monthlySpent: 0,
      overdueCount: 0,
      dueSoonCount: 0
    };
  }

  const logs = (refillsWithStats || []).map(r => r.log);
  const totalFuelSpent = logs.reduce((sum, l) => sum + (Number(l.totalCost) || 0), 0);
  const totalLitersFilled = logs.reduce((sum, l) => sum + (Number(l.liters) || 0), 0);

  // Calculate distance between min and max logged odometers
  let totalDistanceLoggedKm = 0;
  if (logs.length >= 2) {
    const odometers = logs.map(l => Number(l.odometerKm));
    totalDistanceLoggedKm = Math.max(...odometers) - Math.min(...odometers);
  }

  // Valid refills with km/L calculation
  const validRefills = refillsWithStats.filter(r => r.kmPerLiter !== null && r.kmPerLiter > 0);

  // Overall average km/L (weighted by total distance / total liters among valid refills)
  let averageKmPerLiter = 0;
  if (validRefills.length > 0) {
    const sumKm = validRefills.reduce((sum, r) => sum + (r.distanceTraveledKm || 0), 0);
    const sumL = validRefills.reduce((sum, r) => sum + (r.log.liters || 0), 0);
    averageKmPerLiter = sumL > 0 ? (sumKm / sumL) : 0;
  }

  // Cost per km
  let averageCostPerKm = 0;
  const validCosts = refillsWithStats.filter(r => r.costPerKm !== null && r.costPerKm > 0);
  if (validCosts.length > 0) {
    const sumCost = validCosts.reduce((sum, r) => sum + (r.log.totalCost || 0), 0);
    const sumDist = validCosts.reduce((sum, r) => sum + (r.distanceTraveledKm || 0), 0);
    averageCostPerKm = sumDist > 0 ? (sumCost / sumDist) : 0;
  }

  // Most recent km/L
  const lastWithKmL = validRefills.length > 0 ? validRefills[0].kmPerLiter : null;

  // Gasoline vs Ethanol specific averages
  const gasRefills = validRefills.filter(r => r.log.fuelType.toLowerCase().includes('gasolina'));
  let gasolineAvgKmPerL = null;
  if (gasRefills.length > 0) {
    const km = gasRefills.reduce((s, r) => s + (r.distanceTraveledKm || 0), 0);
    const l = gasRefills.reduce((s, r) => s + (r.log.liters || 0), 0);
    gasolineAvgKmPerL = l > 0 ? (km / l) : null;
  }

  const ethRefills = validRefills.filter(r => r.log.fuelType.toLowerCase().includes('etanol'));
  let ethanolAvgKmPerL = null;
  if (ethRefills.length > 0) {
    const km = ethRefills.reduce((s, r) => s + (r.distanceTraveledKm || 0), 0);
    const l = ethRefills.reduce((s, r) => s + (r.log.liters || 0), 0);
    ethanolAvgKmPerL = l > 0 ? (km / l) : null;
  }

  // Current month spent
  const now = new Date();
  const currentYear = now.getFullYear();
  const currentMonth = now.getMonth();
  const monthlySpent = logs.filter(l => {
    const d = new Date(l.dateEpochMillis);
    return d.getFullYear() === currentYear && d.getMonth() === currentMonth;
  }).reduce((sum, l) => sum + (Number(l.totalCost) || 0), 0);

  // Maintenance counts
  const overdueCount = (maintenanceItemsWithStatus || []).filter(m => m.status === MaintenanceStatusLevel.OVERDUE).length;
  const dueSoonCount = (maintenanceItemsWithStatus || []).filter(m => m.status === MaintenanceStatusLevel.DUE_SOON).length;

  return {
    currentOdometerKm: Number(vehicle.currentOdometerKm) || 0,
    totalDistanceLoggedKm,
    averageKmPerLiter,
    averageCostPerKm,
    totalFuelSpent,
    totalLitersFilled,
    lastKmPerLiter: lastWithKmL,
    gasolineAvgKmPerL,
    ethanolAvgKmPerL,
    monthlySpent,
    overdueCount,
    dueSoonCount
  };
}

/**
 * Computes maintenance item status, remaining km, days, progress and level
 */
export function computeMaintenanceItemStatus(item, currentVehicleKm, currentTimeMillis = Date.now()) {
  const targetKm = Number(item.lastServiceKm) + Number(item.intervalKm);
  const intervalMillis = Number(item.intervalMonths) * 30 * 24 * 60 * 60 * 1000;
  const targetDateMillis = Number(item.lastServiceDateEpochMillis) + intervalMillis;

  const remainingKm = targetKm - currentVehicleKm;
  const remainingMillis = targetDateMillis - currentTimeMillis;
  const remainingDays = Math.floor(remainingMillis / (1000 * 60 * 60 * 24));

  // Calculate progress based on whichever is closer (km or time)
  const kmUsed = currentVehicleKm - item.lastServiceKm;
  const kmProgress = item.intervalKm > 0 ? Math.min(Math.max(kmUsed / item.intervalKm, 0), 1.5) : 0;

  const timeUsed = currentTimeMillis - item.lastServiceDateEpochMillis;
  const timeProgress = intervalMillis > 0 ? Math.min(Math.max(timeUsed / intervalMillis, 0), 1.5) : 0;

  const maxProgress = Math.min(Math.max(Math.max(kmProgress, timeProgress), 0), 1);

  let status = MaintenanceStatusLevel.OK;
  let statusMessage = '';

  if (remainingKm <= 0 || remainingDays <= 0) {
    status = MaintenanceStatusLevel.OVERDUE;
    const overdueKm = Math.abs(Math.round(remainingKm));
    const overdueDays = Math.abs(remainingDays);
    if (remainingKm <= 0 && remainingDays <= 0) {
      statusMessage = `Vencido há ${overdueKm.toLocaleString('pt-BR')} km e ${overdueDays} dias!`;
    } else if (remainingKm <= 0) {
      statusMessage = `Vencido há ${overdueKm.toLocaleString('pt-BR')} km!`;
    } else {
      statusMessage = `Vencido há ${overdueDays} dias!`;
    }
  } else if (remainingKm <= 1000 || remainingDays <= 15) {
    status = MaintenanceStatusLevel.DUE_SOON;
    if (remainingKm <= 1000 && remainingDays <= 15) {
      statusMessage = `Atenção: faltam ${Math.round(remainingKm).toLocaleString('pt-BR')} km ou ${remainingDays} dias`;
    } else if (remainingKm <= 1000) {
      statusMessage = `Atenção: faltam apenas ${Math.round(remainingKm).toLocaleString('pt-BR')} km`;
    } else {
      statusMessage = `Atenção: faltam apenas ${remainingDays} dias`;
    }
  } else {
    status = MaintenanceStatusLevel.OK;
    statusMessage = `Em dia (faltam ${Math.round(remainingKm).toLocaleString('pt-BR')} km ou ${remainingDays} dias)`;
  }

  return {
    item,
    targetKm,
    targetDateEpochMillis: targetDateMillis,
    remainingKm,
    remainingDays,
    progress: maxProgress,
    status,
    statusMessage
  };
}

/**
 * Flex Calculator: Ethanol vs Gasoline
 */
export function computeFlexCalculation(gasPrice, ethPrice, gasAvgKmL = null, ethAvgKmL = null, tankCapacity = 50.0) {
  const gPrice = Number(gasPrice) || 0;
  const ePrice = Number(ethPrice) || 0;

  if (gPrice <= 0 || ePrice <= 0) {
    return null;
  }

  // Price ratio (e.g. 3.89 / 5.89 = 66.04%)
  const priceRatioPercent = (ePrice / gPrice) * 100;

  // Check if user has vehicle-specific consumption data
  const hasVehicleData = (gasAvgKmL !== null && gasAvgKmL > 0 && ethAvgKmL !== null && ethAvgKmL > 0);
  const breakEvenPercent = hasVehicleData ? ((ethAvgKmL / gasAvgKmL) * 100) : 70.0;

  // Advantage determination
  const isEthanolBetter = priceRatioPercent <= breakEvenPercent;

  // Cost per kilometer
  const effectiveGasKmL = (gasAvgKmL && gasAvgKmL > 0) ? gasAvgKmL : 12.0;
  const effectiveEthKmL = (ethAvgKmL && ethAvgKmL > 0) ? ethAvgKmL : (effectiveGasKmL * 0.70);

  const costPerKmGas = gPrice / effectiveGasKmL;
  const costPerKmEth = ePrice / effectiveEthKmL;

  const costDiffPerKm = Math.abs(costPerKmGas - costPerKmEth);
  const savingsPer1000Km = costDiffPerKm * 1000;

  // Full tank savings calculation
  const tankTotalGasCost = gPrice * tankCapacity;
  const tankTotalEthCost = ePrice * tankCapacity;
  // Distance you can run with 1 full tank of each
  const distWithGasTank = tankCapacity * effectiveGasKmL;
  const costGasToRunThatDist = tankTotalGasCost;
  const costEthToRunThatDist = distWithGasTank * costPerKmEth;
  const tankSavings = Math.abs(costGasToRunThatDist - costEthToRunThatDist);

  return {
    priceRatioPercent,
    breakEvenPercent,
    isEthanolBetter,
    hasVehicleData,
    costPerKmGas,
    costPerKmEth,
    savingsPer1000Km,
    tankSavings,
    winnerFuel: isEthanolBetter ? 'ETANOL' : 'GASOLINA',
    diffPercent: Math.abs(priceRatioPercent - breakEvenPercent)
  };
}

/**
 * Market Benchmark Comparison (EVs, Hybrids, Efficient ICE)
 */
export function computeMarketComparison(cars, userAverageKmL, gasPrice = 5.99, kwhPrice = 0.85, monthlyKm = 1200) {
  const gPrice = Number(gasPrice) || 5.99;
  const elecPrice = Number(kwhPrice) || 0.85;
  const kmPerMonth = Number(monthlyKm) || 1200;

  // User baseline cost per km
  const userKmL = (Number(userAverageKmL) > 0) ? Number(userAverageKmL) : 10.5;
  const userCostPerKm = gPrice / userKmL;
  const userMonthlyCost = userCostPerKm * kmPerMonth;
  const userAnnualCost = userMonthlyCost * 12;

  const comparedCars = cars.map(car => {
    let costPerKm = 0;
    if (car.powertrain === VehiclePowertrain.ELECTRIC) {
      costPerKm = (car.efficiencyKmPerKwh > 0) ? (elecPrice / car.efficiencyKmPerKwh) : 0.12;
    } else {
      costPerKm = (car.efficiencyKmL > 0) ? (gPrice / car.efficiencyKmL) : 0.40;
    }

    const monthlyCost = costPerKm * kmPerMonth;
    const annualCost = monthlyCost * 12;
    const annualSavings = userAnnualCost - annualCost;

    return {
      ...car,
      costPerKm,
      monthlyCost,
      annualCost,
      annualSavings,
      efficiencyLabel: car.powertrain === VehiclePowertrain.ELECTRIC
        ? `${car.efficiencyKmPerKwh.toFixed(1)} km/kWh`
        : `${car.efficiencyKmL.toFixed(1)} km/L`
    };
  });

  return {
    userCostPerKm,
    userMonthlyCost,
    userAnnualCost,
    userKmL,
    gPrice,
    elecPrice,
    kmPerMonth,
    comparedCars: comparedCars.sort((a, b) => b.annualSavings - a.annualSavings)
  };
}
