// AutoFuel Models & Presets (models.js)

export const VehiclePowertrain = {
  ELECTRIC: '100% Elétrico',
  HYBRID: 'Híbrido (HEV/PHEV)',
  COMBUSTION: 'Combustão (Gasolina)'
};

export const MaintenanceStatusLevel = {
  OVERDUE: 'OVERDUE',   // Vencido (Vermelho)
  DUE_SOON: 'DUE_SOON', // Atenção / Próximo (Amarelo)
  OK: 'OK'              // Em dia (Verde)
};

export const MAINTENANCE_CATEGORIES = [
  'Motor',
  'Filtros',
  'Freios',
  'Suspensão',
  'Transmissão',
  'Pneus',
  'Elétrica',
  'Outros'
];

export const FUEL_TYPES = [
  'Gasolina Comum',
  'Gasolina Aditivada',
  'Etanol',
  'Diesel',
  'GNV'
];

export const STANDARD_MAINTENANCE_PRESETS = [
  {
    title: 'Troca de Óleo do Motor e Filtro',
    category: 'Motor',
    intervalKm: 10000,
    intervalMonths: 6,
    notes: 'Utilizar especificação 0W20 ou 5W30 100% sintético.'
  },
  {
    title: 'Filtro de Ar do Motor e Combustível',
    category: 'Filtros',
    intervalKm: 15000,
    intervalMonths: 12,
    notes: 'Substituição preventiva para proteger bicos injetores e admissão.'
  },
  {
    title: 'Pastilhas de Freio Dianteiras',
    category: 'Freios',
    intervalKm: 25000,
    intervalMonths: 24,
    notes: 'Verificar espessura das pastilhas e condição dos discos.'
  },
  {
    title: 'Alinhamento, Balanceamento e Rodízio',
    category: 'Pneus',
    intervalKm: 10000,
    intervalMonths: 6,
    notes: 'Evita desgaste irregular da banda de rodagem dos pneus.'
  },
  {
    title: 'Fluido de Freio (DOT 4 / 5.1)',
    category: 'Freios',
    intervalKm: 40000,
    intervalMonths: 24,
    notes: 'Substituição por higroscopia (umidade acumulada no sistema).'
  },
  {
    title: 'Velas de Ignição',
    category: 'Motor',
    intervalKm: 40000,
    intervalMonths: 36,
    notes: 'Velas de Iridium/Platina garantem queima ideal e menor consumo.'
  }
];

export const PRESET_MARKET_CARS = [
  // 1. 100% Elétricos
  {
    id: 'dolphin_mini',
    name: 'BYD Dolphin Mini (EV)',
    category: '100% Elétrico',
    powertrain: VehiclePowertrain.ELECTRIC,
    efficiencyKmPerKwh: 9.6, // ~10.4 kWh/100km
    efficiencyKmL: 0,
    description: 'Compacto urbano elétrico mais vendido do Brasil. Altíssima eficiência energética.',
    badge: 'Super Econômico'
  },
  {
    id: 'byd_dolphin',
    name: 'BYD Dolphin GS (EV)',
    category: '100% Elétrico',
    powertrain: VehiclePowertrain.ELECTRIC,
    efficiencyKmPerKwh: 7.8, // ~12.8 kWh/100km
    efficiencyKmL: 0,
    description: 'Hatch elétrico espaçoso com excelente relação custo-benefício.',
    badge: 'Mais Popular'
  },
  {
    id: 'gwm_ora03',
    name: 'GWM Ora 03 Skin (EV)',
    category: '100% Elétrico',
    powertrain: VehiclePowertrain.ELECTRIC,
    efficiencyKmPerKwh: 7.2, // ~13.8 kWh/100km
    efficiencyKmL: 0,
    description: 'Hatch elétrico premium com 171 cv e visual retrô-futurista.',
    badge: 'Desempenho'
  },
  {
    id: 'volvo_ex30',
    name: 'Volvo EX30 (EV)',
    category: '100% Elétrico',
    powertrain: VehiclePowertrain.ELECTRIC,
    efficiencyKmPerKwh: 6.5, // ~15.3 kWh/100km
    efficiencyKmL: 0,
    description: 'SUV compacto elétrico premium com alta potência e tecnologia de segurança.',
    badge: 'SUV Premium'
  },

  // 2. Híbridos
  {
    id: 'corolla_hybrid',
    name: 'Toyota Corolla Hybrid (HEV)',
    category: 'Híbrido',
    powertrain: VehiclePowertrain.HYBRID,
    efficiencyKmPerKwh: 0,
    efficiencyKmL: 18.5,
    description: 'Referência em eficiência na cidade combinando motor 1.8 a combustão com motor elétrico.',
    badge: 'Alta Eficiência'
  },
  {
    id: 'byd_song_plus',
    name: 'BYD Song Plus DM-i (PHEV)',
    category: 'Híbrido',
    powertrain: VehiclePowertrain.HYBRID,
    efficiencyKmPerKwh: 0,
    efficiencyKmL: 25.0,
    description: 'SUV híbrido plug-in com autonomia superior a 1.000 km combinados.',
    badge: 'Plug-in'
  },
  {
    id: 'haval_h6_hev',
    name: 'GWM Haval H6 HEV',
    category: 'Híbrido',
    powertrain: VehiclePowertrain.HYBRID,
    efficiencyKmPerKwh: 0,
    efficiencyKmL: 13.8,
    description: 'SUV médio híbrido convencional com 243 cv e bom espaço para a família.',
    badge: 'SUV Médio'
  },

  // 3. Carros a Combustão Mais Econômicos
  {
    id: 'renault_kwid',
    name: 'Renault Kwid 1.0',
    category: 'Combustão Econômico',
    powertrain: VehiclePowertrain.COMBUSTION,
    efficiencyKmPerKwh: 0,
    efficiencyKmL: 15.5,
    description: 'Um dos compactos a combustão mais econômicos do ranking do Inmetro (PBEV).',
    badge: 'Inmetro Top'
  },
  {
    id: 'onix_plus',
    name: 'Chevrolet Onix Plus 1.0',
    category: 'Combustão Econômico',
    powertrain: VehiclePowertrain.COMBUSTION,
    efficiencyKmPerKwh: 0,
    efficiencyKmL: 15.2,
    description: 'Sedan compacto com excelente aerodinâmica e baixo consumo na estrada (até 17,5 km/L).',
    badge: 'Sedan Top'
  },
  {
    id: 'vw_polo_tsi',
    name: 'Volkswagen Polo 170 TSI',
    category: 'Combustão Econômico',
    powertrain: VehiclePowertrain.COMBUSTION,
    efficiencyKmPerKwh: 0,
    efficiencyKmL: 14.8,
    description: 'Motor 1.0 Turbo com injeção direta que une bom torque e economia.',
    badge: 'Turbo Eficiente'
  },

  // 4. SUV Médio Turbo Padrão
  {
    id: 'suv_medio_padrao',
    name: 'SUV Médio Turbo Padrão',
    category: 'SUV Médio (Combustão)',
    powertrain: VehiclePowertrain.COMBUSTION,
    efficiencyKmPerKwh: 0,
    efficiencyKmL: 11.0,
    description: 'Média de consumo de SUVs médios turboflex (porte Compass/Corolla Cross 2.0/Taos).',
    badge: 'Média de Mercado'
  }
];

export function getDemoInitialData() {
  const now = Date.now();
  const dayMs = 24 * 60 * 60 * 1000;

  const vehicleId = 1;
  const initialVehicle = {
    id: vehicleId,
    name: 'Tracker Premier 1.2 Turbo',
    brand: 'Chevrolet',
    modelYear: 2023,
    plate: 'BRA2E25',
    currentOdometerKm: 28450,
    fuelCapacityLiters: 44.0,
    preferredFuel: 'Gasolina Comum',
    isActive: true
  };

  const fuelLogs = [
    {
      id: 1,
      vehicleId: vehicleId,
      odometerKm: 26900,
      liters: 40.5,
      pricePerLiter: 5.89,
      totalCost: 238.54,
      fuelType: 'Gasolina Comum',
      isFullTank: true,
      dateEpochMillis: now - (42 * dayMs),
      gasStation: 'Posto Shell Morumbi',
      notes: 'Primeiro abastecimento registrado.'
    },
    {
      id: 2,
      vehicleId: vehicleId,
      odometerKm: 27420,
      liters: 38.0,
      pricePerLiter: 5.92,
      totalCost: 224.96,
      fuelType: 'Gasolina Comum',
      isFullTank: true,
      dateEpochMillis: now - (28 * dayMs),
      gasStation: 'Posto Ipiranga Nações',
      notes: 'Uso rodoviário 70%, cidade 30%.'
    },
    {
      id: 3,
      vehicleId: vehicleId,
      odometerKm: 27910,
      liters: 41.2,
      pricePerLiter: 3.89,
      totalCost: 160.27,
      fuelType: 'Etanol',
      isFullTank: true,
      dateEpochMillis: now - (14 * dayMs),
      gasStation: 'Posto BR Petrobras',
      notes: 'Testando autonomia com Etanol.'
    },
    {
      id: 4,
      vehicleId: vehicleId,
      odometerKm: 28450,
      liters: 39.5,
      pricePerLiter: 6.05,
      totalCost: 238.97,
      fuelType: 'Gasolina Aditivada',
      isFullTank: true,
      dateEpochMillis: now - (2 * dayMs),
      gasStation: 'Posto Shell Marginal',
      notes: 'Abastecimento recente para viagem.'
    }
  ];

  const maintenanceItems = [
    {
      id: 1,
      vehicleId: vehicleId,
      title: 'Troca de Óleo do Motor e Filtro',
      category: 'Motor',
      intervalKm: 10000,
      intervalMonths: 6,
      lastServiceKm: 20000,
      lastServiceDateEpochMillis: now - (150 * dayMs),
      notes: '0W-20 sintético Dexos 1 Gen 3.'
    },
    {
      id: 2,
      vehicleId: vehicleId,
      title: 'Filtro de Ar e Combustível',
      category: 'Filtros',
      intervalKm: 15000,
      intervalMonths: 12,
      lastServiceKm: 15000,
      lastServiceDateEpochMillis: now - (320 * dayMs),
      notes: 'Substituição preventiva.'
    },
    {
      id: 3,
      vehicleId: vehicleId,
      title: 'Pastilhas de Freio Dianteiras',
      category: 'Freios',
      intervalKm: 25000,
      intervalMonths: 24,
      lastServiceKm: 0,
      lastServiceDateEpochMillis: now - (500 * dayMs),
      notes: 'Item original de fábrica que precisa de revisão urgente.'
    },
    {
      id: 4,
      vehicleId: vehicleId,
      title: 'Alinhamento e Balanceamento',
      category: 'Pneus',
      intervalKm: 10000,
      intervalMonths: 6,
      lastServiceKm: 20000,
      lastServiceDateEpochMillis: now - (120 * dayMs),
      notes: 'Recomendado a cada 10.000 km.'
    },
    {
      id: 5,
      vehicleId: vehicleId,
      title: 'Fluido de Freio DOT 4',
      category: 'Freios',
      intervalKm: 40000,
      intervalMonths: 24,
      lastServiceKm: 0,
      lastServiceDateEpochMillis: now - (360 * dayMs),
      notes: 'Verificar ponto de ebulição do fluido.'
    }
  ];

  const maintenanceHistory = [
    {
      id: 1,
      vehicleId: vehicleId,
      maintenanceItemId: 1,
      title: 'Troca de Óleo do Motor e Filtro',
      category: 'Motor',
      serviceKm: 20000,
      serviceDateEpochMillis: now - (150 * dayMs),
      cost: 320.00,
      workshop: 'Concessionária Vigorito',
      notes: 'Revisão dos 20.000 km concluída.'
    },
    {
      id: 2,
      vehicleId: vehicleId,
      maintenanceItemId: 4,
      title: 'Alinhamento e Balanceamento',
      category: 'Pneus',
      serviceKm: 20000,
      serviceDateEpochMillis: now - (120 * dayMs),
      cost: 140.00,
      workshop: 'DPaschoal Centro',
      notes: 'Geometria 3D e rodízio em X dos 4 pneus.'
    }
  ];

  return {
    vehicles: [initialVehicle],
    activeVehicleId: vehicleId,
    fuelLogs,
    maintenanceItems,
    maintenanceHistory
  };
}
