// AutoFuel Offline Storage Engine (storage.js)
import { getDemoInitialData, STANDARD_MAINTENANCE_PRESETS } from './models.js';

const STORAGE_KEY = 'autofuel_db_v1';
const THEME_KEY = 'autofuel_theme_preference';

let listeners = [];

export const Storage = {
  getTheme() {
    return localStorage.getItem(THEME_KEY) || 'dark';
  },

  setTheme(theme) {
    localStorage.setItem(THEME_KEY, theme);
    document.documentElement.setAttribute('data-theme', theme);
  },

  loadData() {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) {
        const initial = getDemoInitialData();
        this.saveData(initial);
        return initial;
      }
      const parsed = JSON.parse(raw);
      if (!parsed.vehicles || !parsed.vehicles.length) {
        const initial = getDemoInitialData();
        this.saveData(initial);
        return initial;
      }
      return parsed;
    } catch (e) {
      console.error('Error loading data from localStorage, resetting:', e);
      const fallback = getDemoInitialData();
      this.saveData(fallback);
      return fallback;
    }
  },

  saveData(data) {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
      this.notifyListeners();
    } catch (e) {
      console.error('Error saving data to localStorage:', e);
    }
  },

  subscribe(callback) {
    listeners.push(callback);
    return () => {
      listeners = listeners.filter(l => l !== callback);
    };
  },

  notifyListeners() {
    const data = this.loadData();
    listeners.forEach(cb => {
      try { cb(data); } catch (err) { console.error('Listener callback error:', err); }
    });
  },

  getActiveVehicle() {
    const data = this.loadData();
    const active = data.vehicles.find(v => v.id === data.activeVehicleId);
    return active || data.vehicles[0] || null;
  },

  getAllVehicles() {
    return this.loadData().vehicles || [];
  },

  setActiveVehicle(vehicleId) {
    const data = this.loadData();
    data.activeVehicleId = Number(vehicleId);
    data.vehicles.forEach(v => {
      v.isActive = (v.id === Number(vehicleId));
    });
    this.saveData(data);
  },

  addVehicle(vehicle) {
    const data = this.loadData();
    const newId = Date.now();
    const newVehicle = {
      ...vehicle,
      id: newId,
      isActive: true
    };
    data.vehicles.push(newVehicle);
    data.activeVehicleId = newId;

    // Seed standard maintenance presets for the new vehicle
    const now = Date.now();
    const items = STANDARD_MAINTENANCE_PRESETS.map((preset, idx) => ({
      id: newId + idx + 1,
      vehicleId: newId,
      title: preset.title,
      category: preset.category,
      intervalKm: preset.intervalKm,
      intervalMonths: preset.intervalMonths,
      lastServiceKm: Number(vehicle.currentOdometerKm) || 0,
      lastServiceDateEpochMillis: now,
      notes: preset.notes
    }));

    data.maintenanceItems = [...(data.maintenanceItems || []), ...items];
    this.saveData(data);
    return newVehicle;
  },

  updateVehicle(updatedVehicle) {
    const data = this.loadData();
    data.vehicles = data.vehicles.map(v => v.id === updatedVehicle.id ? { ...v, ...updatedVehicle } : v);
    this.saveData(data);
  },

  deleteVehicle(vehicleId) {
    const data = this.loadData();
    if (data.vehicles.length <= 1) {
      throw new Error('Não é possível excluir o único veículo cadastrado.');
    }
    data.vehicles = data.vehicles.filter(v => v.id !== vehicleId);
    data.fuelLogs = (data.fuelLogs || []).filter(l => l.vehicleId !== vehicleId);
    data.maintenanceItems = (data.maintenanceItems || []).filter(m => m.vehicleId !== vehicleId);
    data.maintenanceHistory = (data.maintenanceHistory || []).filter(h => h.vehicleId !== vehicleId);

    if (data.activeVehicleId === vehicleId) {
      data.activeVehicleId = data.vehicles[0].id;
    }
    this.saveData(data);
  },

  addFuelLog(log) {
    const data = this.loadData();
    const newLog = {
      ...log,
      id: Date.now()
    };
    data.fuelLogs = data.fuelLogs || [];
    data.fuelLogs.push(newLog);

    // If odometer entered is greater than vehicle odometer, update vehicle
    const vehicle = data.vehicles.find(v => v.id === log.vehicleId);
    if (vehicle && Number(log.odometerKm) > Number(vehicle.currentOdometerKm)) {
      vehicle.currentOdometerKm = Number(log.odometerKm);
    }

    this.saveData(data);
    return newLog;
  },

  deleteFuelLog(logId) {
    const data = this.loadData();
    data.fuelLogs = (data.fuelLogs || []).filter(l => l.id !== logId);
    this.saveData(data);
  },

  addMaintenanceItem(item) {
    const data = this.loadData();
    const newItem = {
      ...item,
      id: Date.now()
    };
    data.maintenanceItems = data.maintenanceItems || [];
    data.maintenanceItems.push(newItem);
    this.saveData(data);
    return newItem;
  },

  updateMaintenanceItem(item) {
    const data = this.loadData();
    data.maintenanceItems = (data.maintenanceItems || []).map(m => m.id === item.id ? item : m);
    this.saveData(data);
  },

  deleteMaintenanceItem(itemId) {
    const data = this.loadData();
    data.maintenanceItems = (data.maintenanceItems || []).filter(m => m.id !== itemId);
    this.saveData(data);
  },

  recordMaintenancePerformed(itemId, serviceKm, serviceDateMillis, cost, workshop, notes) {
    const data = this.loadData();
    const item = (data.maintenanceItems || []).find(m => m.id === itemId);
    if (!item) return;

    // 1. Update the item's last service info
    item.lastServiceKm = Number(serviceKm);
    item.lastServiceDateEpochMillis = Number(serviceDateMillis);

    // 2. Insert into maintenance history
    const historyEntry = {
      id: Date.now(),
      vehicleId: item.vehicleId,
      maintenanceItemId: item.id,
      title: item.title,
      category: item.category,
      serviceKm: Number(serviceKm),
      serviceDateEpochMillis: Number(serviceDateMillis),
      cost: Number(cost) || 0,
      workshop: workshop || '',
      notes: notes || ''
    };
    data.maintenanceHistory = data.maintenanceHistory || [];
    data.maintenanceHistory.unshift(historyEntry);

    // 3. Update vehicle odometer if service was performed at a higher km
    const vehicle = data.vehicles.find(v => v.id === item.vehicleId);
    if (vehicle && Number(serviceKm) > Number(vehicle.currentOdometerKm)) {
      vehicle.currentOdometerKm = Number(serviceKm);
    }

    this.saveData(data);
    return historyEntry;
  },

  deleteMaintenanceHistory(historyId) {
    const data = this.loadData();
    data.maintenanceHistory = (data.maintenanceHistory || []).filter(h => h.id !== historyId);
    this.saveData(data);
  },

  exportToJson() {
    const data = this.loadData();
    const jsonStr = JSON.stringify(data, null, 2);
    const blob = new Blob([jsonStr], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    const dateStr = new Date().toISOString().slice(0, 10);
    a.href = url;
    a.download = `autofuel_backup_${dateStr}.json`;
    a.click();
    URL.revokeObjectURL(url);
  },

  importFromJson(jsonString) {
    const parsed = JSON.parse(jsonString);
    if (!parsed.vehicles || !Array.isArray(parsed.vehicles) || parsed.vehicles.length === 0) {
      throw new Error('Arquivo de backup inválido: lista de veículos não encontrada.');
    }
    this.saveData(parsed);
  },

  resetToDemo() {
    const demo = getDemoInitialData();
    this.saveData(demo);
  },

  clearAllData(createStarterVehicle = true) {
    if (createStarterVehicle) {
      const now = Date.now();
      const starter = {
        vehicles: [{
          id: 1,
          name: 'Meu Carro',
          brand: 'Geral',
          modelYear: new Date().getFullYear(),
          plate: 'ABC1D23',
          currentOdometerKm: 0,
          fuelCapacityLiters: 50,
          preferredFuel: 'Gasolina Comum',
          isActive: true
        }],
        activeVehicleId: 1,
        fuelLogs: [],
        maintenanceItems: STANDARD_MAINTENANCE_PRESETS.map((p, idx) => ({
          id: idx + 1,
          vehicleId: 1,
          title: p.title,
          category: p.category,
          intervalKm: p.intervalKm,
          intervalMonths: p.intervalMonths,
          lastServiceKm: 0,
          lastServiceDateEpochMillis: now,
          notes: p.notes
        })),
        maintenanceHistory: []
      };
      this.saveData(starter);
    } else {
      localStorage.removeItem(STORAGE_KEY);
      this.notifyListeners();
    }
  }
};
