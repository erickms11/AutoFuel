// AutoFuel App Core Controller (app.js)
import { Storage } from './storage.js';
import {
  computeFuelRefillsWithStats,
  computeDashboardStats,
  computeMaintenanceItemStatus,
  computeFlexCalculation,
  computeMarketComparison
} from './calculations.js';
import {
  PRESET_MARKET_CARS,
  MAINTENANCE_CATEGORIES,
  FUEL_TYPES,
  MaintenanceStatusLevel,
  VehiclePowertrain
} from './models.js';
import { renderConsumptionChart } from './chart.js';
import { initPWA, showToast } from './pwa.js';

let activeTab = 'dashboard';
let activeMaintenanceSubTab = 'items';
let activeComparatorSubTab = 'flex';
let editingMaintenanceItemId = null;

// Formatters
const brCurrency = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
const brDecimal = new Intl.NumberFormat('pt-BR', { minimumFractionDigits: 1, maximumFractionDigits: 2 });

document.addEventListener('DOMContentLoaded', () => {
  // 1. Initialize Theme
  const savedTheme = Storage.getTheme();
  Storage.setTheme(savedTheme);
  updateThemeIcon(savedTheme);

  // 2. Initialize PWA
  initPWA();

  // 3. Setup Navigation & Tabs
  setupNavigation();

  // 4. Setup Modals & Action Handlers
  setupModals();

  // 5. Setup Flex Calculator & Market Inputs
  setupComparatorInputs();

  // 6. Listen to Storage changes to re-render
  Storage.subscribe(() => {
    renderCurrentTab();
    updateVehicleSelector();
  });

  // Initial render
  updateVehicleSelector();
  renderCurrentTab();

  // Listen to window resize for chart responsiveness
  window.addEventListener('resize', () => {
    if (activeTab === 'dashboard') {
      renderDashboard();
    }
  });
});

/* ==========================================================================
   Navigation
   ========================================================================== */
function setupNavigation() {
  const navButtons = document.querySelectorAll('.nav-item');
  navButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      const targetTab = btn.getAttribute('data-tab');
      switchTab(targetTab);
    });
  });

  // Top theme toggle
  const themeBtn = document.getElementById('btn-toggle-theme');
  if (themeBtn) {
    themeBtn.addEventListener('click', () => {
      const current = Storage.getTheme();
      const next = current === 'dark' ? 'light' : 'dark';
      Storage.setTheme(next);
      updateThemeIcon(next);
      if (activeTab === 'dashboard') renderDashboard();
    });
  }

  // Active Vehicle chip click -> Open vehicle manager modal
  const vehicleChip = document.getElementById('vehicle-chip-btn');
  if (vehicleChip) {
    vehicleChip.addEventListener('click', () => {
      openModal('modal-vehicle-manage');
    });
  }

  // Data management icon click
  const dataBtn = document.getElementById('btn-data-management');
  if (dataBtn) {
    dataBtn.addEventListener('click', () => {
      openModal('modal-data-management');
    });
  }
}

export function switchTab(tabName, subTab = null) {
  activeTab = tabName;

  // Update nav item buttons
  document.querySelectorAll('.nav-item').forEach(btn => {
    btn.classList.toggle('active', btn.getAttribute('data-tab') === tabName);
  });

  // Update tab content displays
  document.querySelectorAll('.tab-content').forEach(content => {
    content.classList.toggle('active', content.id === `tab-${tabName}`);
  });

  if (subTab && tabName === 'comparator') {
    activeComparatorSubTab = subTab;
    document.querySelectorAll('.comp-tab-btn').forEach(b => {
      b.classList.toggle('active', b.getAttribute('data-subtab') === subTab);
    });
    document.querySelectorAll('.comp-subtab-content').forEach(c => {
      c.classList.toggle('active', c.id === `comp-subtab-${subTab}`);
    });
  }

  renderCurrentTab();
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

function updateThemeIcon(theme) {
  const themeBtn = document.getElementById('btn-toggle-theme');
  if (!themeBtn) return;
  themeBtn.innerHTML = theme === 'dark'
    ? `<svg width="20" height="20" fill="currentColor" viewBox="0 0 24 24"><path d="M12 3c-4.97 0-9 4.03-9 9 0 4.17 2.84 7.67 6.69 8.69a.75.75 0 00.86-.83A6.75 6.75 0 0118.14 8.45a.75.75 0 00.83-.86A9.004 9.004 0 0012 3z"/></svg>`
    : `<svg width="20" height="20" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2.25a.75.75 0 01.75.75v2.25a.75.75 0 01-1.5 0V3a.75.75 0 01.75-.75zM7.5 12a4.5 4.5 0 119 0 4.5 4.5 0 01-9 0zm10.742-5.492a.75.75 0 010 1.06l-1.591 1.592a.75.75 0 11-1.06-1.061l1.59-1.591a.75.75 0 011.061 0zm-12.484 0a.75.75 0 011.06 0l1.592 1.59a.75.75 0 11-1.06 1.061L5.758 7.569a.75.75 0 010-1.061zM12 18.75a.75.75 0 01.75.75V21a.75.75 0 01-1.5 0v-1.5a.75.75 0 01.75-.75zm6.818 2.068a.75.75 0 01-1.06 0l-1.592-1.591a.75.75 0 111.061-1.06l1.591 1.59a.75.75 0 010 1.061zM6.818 19.758a.75.75 0 010-1.06l1.591-1.592a.75.75 0 111.06 1.06l-1.59 1.592a.75.75 0 01-1.061 0zM3 12a.75.75 0 01.75-.75h2.25a.75.75 0 010 1.5H3.75A.75.75 0 013 12zm15 0a.75.75 0 01.75-.75h2.25a.75.75 0 010 1.5H18.75A.75.75 0 0118 12z"/></svg>`;
}

function updateVehicleSelector() {
  const vehicle = Storage.getActiveVehicle();
  const label = document.getElementById('active-vehicle-name-label');
  if (label && vehicle) {
    label.textContent = vehicle.name;
  }
}

function renderCurrentTab() {
  switch (activeTab) {
    case 'dashboard':
      renderDashboard();
      break;
    case 'fuel':
      renderFuelHistory();
      break;
    case 'maintenance':
      renderMaintenance();
      break;
    case 'comparator':
      renderComparator();
      break;
  }
  updateMaintenanceBadge();
}

function updateMaintenanceBadge() {
  const vehicle = Storage.getActiveVehicle();
  if (!vehicle) return;
  const data = Storage.loadData();
  const items = (data.maintenanceItems || []).filter(m => m.vehicleId === vehicle.id);
  const withStatus = items.map(i => computeMaintenanceItemStatus(i, vehicle.currentOdometerKm));
  const alertsCount = withStatus.filter(s => s.status !== MaintenanceStatusLevel.OK).length;

  const badge = document.getElementById('nav-maintenance-badge');
  if (badge) {
    if (alertsCount > 0) {
      badge.textContent = alertsCount;
      badge.style.display = 'block';
    } else {
      badge.style.display = 'none';
    }
  }
}

/* ==========================================================================
   Tab 1: Dashboard Rendering
   ========================================================================== */
function renderDashboard() {
  const vehicle = Storage.getActiveVehicle();
  if (!vehicle) return;

  const data = Storage.loadData();
  const vehicleLogs = (data.fuelLogs || []).filter(l => l.vehicleId === vehicle.id);
  const refillsWithStats = computeFuelRefillsWithStats(vehicleLogs);

  const vehicleItems = (data.maintenanceItems || []).filter(m => m.vehicleId === vehicle.id);
  const maintWithStatus = vehicleItems.map(i => computeMaintenanceItemStatus(i, vehicle.currentOdometerKm));

  const stats = computeDashboardStats(vehicle, refillsWithStats, maintWithStatus);

  // 1. Vehicle Hero Card
  const heroVehicleName = document.getElementById('dash-vehicle-name');
  const heroVehicleSub = document.getElementById('dash-vehicle-sub');
  const heroPlate = document.getElementById('dash-vehicle-plate');
  const heroOdometer = document.getElementById('dash-vehicle-odometer');

  if (heroVehicleName) heroVehicleName.textContent = vehicle.name;
  if (heroVehicleSub) heroVehicleSub.textContent = `${vehicle.brand} • ${vehicle.modelYear} • Tanque ${vehicle.fuelCapacityLiters}L`;
  if (heroPlate) heroPlate.textContent = vehicle.plate || '---';
  if (heroOdometer) heroOdometer.textContent = `${Number(vehicle.currentOdometerKm).toLocaleString('pt-BR')} km`;

  // 2. Maintenance Alert Banner
  const alertContainer = document.getElementById('dash-maintenance-alert-container');
  if (alertContainer) {
    if (stats.overdueCount > 0 || stats.dueSoonCount > 0) {
      const isOverdue = stats.overdueCount > 0;
      const title = isOverdue
        ? `${stats.overdueCount} revisão(ões) vencida(s)!`
        : `${stats.dueSoonCount} revisão(ões) próxima(s)!`;
      const subtitle = isOverdue
        ? 'Atenção imediata recomendada para evitar danos mecânicos.'
        : 'Agende preventivamente a revisão do seu veículo.';

      alertContainer.innerHTML = `
        <div class="alert-banner ${isOverdue ? 'overdue' : 'due-soon'}" id="dash-banner-click">
          <div class="alert-banner-left">
            <svg width="24" height="24" fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/>
            </svg>
            <div>
              <div class="alert-banner-title">${title}</div>
              <div class="alert-banner-subtitle">${subtitle}</div>
            </div>
          </div>
          <svg width="18" height="18" fill="currentColor" viewBox="0 0 24 24"><path d="M8.59 16.59L13.17 12 8.59 7.41 10 6l6 6-6 6-1.41-1.41z"/></svg>
        </div>
      `;
      alertContainer.style.display = 'block';

      document.getElementById('dash-banner-click')?.addEventListener('click', () => {
        switchTab('maintenance');
      });
    } else {
      alertContainer.style.display = 'none';
    }
  }

  // 3. Metrics Grid
  document.getElementById('metric-avg-kml').textContent = stats.averageKmPerLiter > 0
    ? brDecimal.format(stats.averageKmPerLiter)
    : '--';

  document.getElementById('metric-cost-km').textContent = stats.averageCostPerKm > 0
    ? brCurrency.format(stats.averageCostPerKm)
    : '--';

  document.getElementById('metric-monthly-spent').textContent = brCurrency.format(stats.monthlySpent);

  document.getElementById('metric-total-liters').textContent = stats.totalLitersFilled > 0
    ? `${brDecimal.format(stats.totalLitersFilled)} L`
    : '--';

  // Last consumption badge in card footnote
  const lastKmLBadge = document.getElementById('metric-last-kml');
  if (lastKmLBadge) {
    lastKmLBadge.textContent = stats.lastKmPerLiter
      ? `Último: ${brDecimal.format(stats.lastKmPerLiter)} km/L`
      : 'Aguardando abastecimento';
  }

  // 4. Render Consumption Chart Canvas
  const canvas = document.getElementById('consumption-chart-canvas');
  if (canvas) {
    renderConsumptionChart(canvas, refillsWithStats, stats.averageKmPerLiter);
  }

  // 5. Recent Refills list (up to 3)
  const recentList = document.getElementById('dash-recent-refills-list');
  if (recentList) {
    const recent = refillsWithStats.slice(0, 3);
    if (recent.length === 0) {
      recentList.innerHTML = `<div class="card" style="text-align: center; color: var(--text-muted); font-size: 0.9rem;">Nenhum abastecimento cadastrado ainda. Clique em "Abastecer" acima para começar!</div>`;
    } else {
      recentList.innerHTML = recent.map(r => renderRefillCardHtml(r, false)).join('');
    }
  }
}

/* ==========================================================================
   Tab 2: Fuel History Rendering
   ========================================================================== */
function renderFuelHistory() {
  const vehicle = Storage.getActiveVehicle();
  if (!vehicle) return;

  const data = Storage.loadData();
  const vehicleLogs = (data.fuelLogs || []).filter(l => l.vehicleId === vehicle.id);
  const refillsWithStats = computeFuelRefillsWithStats(vehicleLogs);

  const container = document.getElementById('fuel-history-list');
  if (!container) return;

  if (refillsWithStats.length === 0) {
    container.innerHTML = `
      <div class="card" style="text-align: center; padding: 40px 20px;">
        <svg width="48" height="48" fill="var(--accent-teal)" viewBox="0 0 24 24" style="margin-bottom: 12px; opacity: 0.8;">
          <path d="M19.77 7.23l.01-.01-3.72-3.72L15 4.56l2.11 2.11c-.94.36-1.61 1.26-1.61 2.33a2.5 2.5 0 002.5 2.5c.36 0 .69-.08 1-.21v7.21c0 .55-.45 1-1 1s-1-.45-1-1V14c0-1.1-.9-2-2-2h-1V5c0-1.1-.9-2-2-2H6c-1.1 0-2 .9-2 2v16h10v-7.5h1.5v5c0 1.38 1.12 2.5 2.5 2.5s2.5-1.12 2.5-2.5V9c0-.69-.28-1.32-.73-1.77zM12 10H6V5h6v5zm6 0c-.55 0-1-.45-1-1s.45-1 1-1 1 .45 1 1-.45 1-1 1z"/>
        </svg>
        <h3 style="margin-bottom: 6px;">Nenhum abastecimento</h3>
        <p style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 16px;">Registre os abastecimentos do seu veículo para acompanhar médias e custos.</p>
        <button class="btn-primary" style="margin: 0 auto;" onclick="document.getElementById('btn-quick-add-fuel').click()">
          Cadastrar Primeiro Abastecimento
        </button>
      </div>
    `;
    return;
  }

  container.innerHTML = refillsWithStats.map(r => renderRefillCardHtml(r, true)).join('');

  // Attach delete buttons
  container.querySelectorAll('.btn-delete-refill').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      const id = Number(btn.getAttribute('data-id'));
      if (confirm('Deseja realmente excluir este registro de abastecimento?')) {
        Storage.deleteFuelLog(id);
        showToast('Abastecimento removido.');
      }
    });
  });
}

function renderRefillCardHtml(refillItem, showDelete = false) {
  const log = refillItem.log;
  const dateStr = new Date(log.dateEpochMillis).toLocaleDateString('pt-BR');
  const fuelPillClass = log.fuelType.toLowerCase().includes('etanol') ? 'etanol' : log.fuelType.toLowerCase().includes('diesel') ? 'diesel' : 'gasolina';

  return `
    <div class="refill-item-card">
      <div class="refill-item-left">
        <div>
          <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 4px;">
            <span class="fuel-pill ${fuelPillClass}">${log.fuelType}</span>
            <span class="refill-station-name">${log.gasStation || 'Posto'}</span>
            ${log.isFullTank ? '<span style="font-size: 0.7rem; background: rgba(255,255,255,0.08); padding: 2px 6px; border-radius: 4px; color: var(--text-muted);">Tanque Cheio</span>' : ''}
          </div>
          <div class="refill-meta">
            ${dateStr} • Odômetro: <strong>${log.odometerKm.toLocaleString('pt-BR')} km</strong>
            ${refillItem.distanceTraveledKm ? ` (+${refillItem.distanceTraveledKm.toLocaleString('pt-BR')} km)` : ''}
          </div>
        </div>
      </div>
      <div class="refill-item-right">
        <div class="refill-price">${brCurrency.format(log.totalCost)}</div>
        <div style="font-size: 0.78rem; color: var(--text-secondary);">
          ${brDecimal.format(log.liters)} L • ${brCurrency.format(log.pricePerLiter)}/L
        </div>
        ${refillItem.kmPerLiter ? `<div class="refill-kml">${brDecimal.format(refillItem.kmPerLiter)} km/L</div>` : ''}
        ${showDelete ? `
          <button class="btn-link btn-delete-refill" data-id="${log.id}" style="color: var(--accent-rose); font-size: 0.75rem; margin-top: 4px;">
            Excluir
          </button>
        ` : ''}
      </div>
    </div>
  `;
}

/* ==========================================================================
   Tab 3: Maintenance Rendering
   ========================================================================== */
function renderMaintenance() {
  const vehicle = Storage.getActiveVehicle();
  if (!vehicle) return;

  const data = Storage.loadData();
  const vehicleItems = (data.maintenanceItems || []).filter(m => m.vehicleId === vehicle.id);
  const itemsWithStatus = vehicleItems.map(i => computeMaintenanceItemStatus(i, vehicle.currentOdometerKm));

  // Sort: Overdue first, then due soon, then OK
  itemsWithStatus.sort((a, b) => {
    const priority = { OVERDUE: 0, DUE_SOON: 1, OK: 2 };
    const pDiff = priority[a.status] - priority[b.status];
    return pDiff !== 0 ? pDiff : a.remainingKm - b.remainingKm;
  });

  const history = (data.maintenanceHistory || []).filter(h => h.vehicleId === vehicle.id);

  // Subtabs toggler
  const itemsSubTabContent = document.getElementById('maint-subtab-items');
  const historySubTabContent = document.getElementById('maint-subtab-history');

  document.querySelectorAll('.maint-subtab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const target = btn.getAttribute('data-subtab');
      activeMaintenanceSubTab = target;
      document.querySelectorAll('.maint-subtab-btn').forEach(b => b.classList.toggle('active', b === btn));
      if (itemsSubTabContent) itemsSubTabContent.style.display = target === 'items' ? 'block' : 'none';
      if (historySubTabContent) historySubTabContent.style.display = target === 'history' ? 'block' : 'none';
    });
  });

  // Render items
  const itemsContainer = document.getElementById('maint-items-list');
  if (itemsContainer) {
    if (itemsWithStatus.length === 0) {
      itemsContainer.innerHTML = `<div class="card" style="text-align: center; color: var(--text-muted);">Nenhum plano de manutenção cadastrado.</div>`;
    } else {
      itemsContainer.innerHTML = itemsWithStatus.map(st => {
        const item = st.item;
        const statusClass = st.status.toLowerCase().replace('_', '-');
        const statusLabel = st.status === MaintenanceStatusLevel.OVERDUE
          ? 'VENCIDO'
          : st.status === MaintenanceStatusLevel.DUE_SOON ? 'ATENÇÃO' : 'EM DIA';

        const progressPercent = Math.min(Math.round(st.progress * 100), 100);

        return `
          <div class="maint-card ${statusClass}">
            <div class="maint-top-row">
              <div>
                <div class="maint-title">${item.title}</div>
                <div class="maint-category">${item.category} • Intervalo: a cada ${item.intervalKm.toLocaleString('pt-BR')} km ou ${item.intervalMonths} meses</div>
              </div>
              <span class="maint-status-chip ${statusClass}">${statusLabel}</span>
            </div>

            <div class="progress-container">
              <div class="progress-bar-fill ${statusClass}" style="width: ${progressPercent}%"></div>
            </div>

            <div style="font-size: 0.85rem; font-weight: 600; color: ${st.status === MaintenanceStatusLevel.OVERDUE ? 'var(--accent-rose)' : st.status === MaintenanceStatusLevel.DUE_SOON ? 'var(--accent-amber)' : 'var(--text-secondary)'};">
              ${st.statusMessage}
            </div>

            ${item.notes ? `<div style="font-size: 0.78rem; color: var(--text-muted); margin-top: 4px;">Obs: ${item.notes}</div>` : ''}

            <div class="maint-footer-row">
              <span>Última revisão: ${item.lastServiceKm.toLocaleString('pt-BR')} km</span>
              <div style="display: flex; gap: 6px;">
                <button class="btn-small btn-secondary btn-edit-maint-item" data-id="${item.id}">Editar</button>
                <button class="btn-small btn-primary btn-record-service" data-id="${item.id}">Registrar Serviço</button>
              </div>
            </div>
          </div>
        `;
      }).join('');

      // Attach clicks
      itemsContainer.querySelectorAll('.btn-record-service').forEach(btn => {
        btn.addEventListener('click', () => {
          const id = Number(btn.getAttribute('data-id'));
          openRecordServiceModal(id);
        });
      });

      itemsContainer.querySelectorAll('.btn-edit-maint-item').forEach(btn => {
        btn.addEventListener('click', () => {
          const id = Number(btn.getAttribute('data-id'));
          openEditMaintenanceModal(id);
        });
      });
    }
  }

  // Render history
  const historyContainer = document.getElementById('maint-history-list');
  if (historyContainer) {
    if (history.length === 0) {
      historyContainer.innerHTML = `<div class="card" style="text-align: center; color: var(--text-muted);">Nenhum histórico de serviço realizado registrado ainda.</div>`;
    } else {
      historyContainer.innerHTML = history.map(h => {
        const dateStr = new Date(h.serviceDateEpochMillis).toLocaleDateString('pt-BR');
        return `
          <div class="card" style="margin-bottom: 10px;">
            <div style="display: flex; justify-content: space-between; align-items: flex-start;">
              <div>
                <h4 style="font-size: 1rem; font-weight: 700;">${h.title}</h4>
                <div style="font-size: 0.8rem; color: var(--text-muted);">${h.category} • ${dateStr} • ${h.serviceKm.toLocaleString('pt-BR')} km</div>
                ${h.workshop ? `<div style="font-size: 0.8rem; color: var(--text-secondary); margin-top: 2px;">Oficina: <strong>${h.workshop}</strong></div>` : ''}
                ${h.notes ? `<div style="font-size: 0.78rem; color: var(--text-muted); margin-top: 2px;">${h.notes}</div>` : ''}
              </div>
              <div style="text-align: right;">
                <div style="font-family: var(--font-mono); font-weight: 700; color: var(--accent-emerald); font-size: 1.1rem;">
                  ${h.cost ? brCurrency.format(h.cost) : 'R$ 0,00'}
                </div>
                <button class="btn-link btn-del-history" data-id="${h.id}" style="color: var(--accent-rose); font-size: 0.75rem; margin-top: 6px;">Excluir</button>
              </div>
            </div>
          </div>
        `;
      }).join('');

      historyContainer.querySelectorAll('.btn-del-history').forEach(btn => {
        btn.addEventListener('click', () => {
          const id = Number(btn.getAttribute('data-id'));
          if (confirm('Deseja excluir este registro de histórico?')) {
            Storage.deleteMaintenanceHistory(id);
            showToast('Histórico excluído.');
          }
        });
      });
    }
  }
}

/* ==========================================================================
   Tab 4: Comparator Rendering (Flex + Market)
   ========================================================================== */
function setupComparatorInputs() {
  // Subtab buttons
  document.querySelectorAll('.comp-tab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const target = btn.getAttribute('data-subtab');
      activeComparatorSubTab = target;
      document.querySelectorAll('.comp-tab-btn').forEach(b => b.classList.toggle('active', b === btn));
      document.querySelectorAll('.comp-subtab-content').forEach(c => {
        c.classList.toggle('active', c.id === `comp-subtab-${target}`);
      });
      renderComparator();
    });
  });

  // Flex calculator inputs
  const gasPriceInput = document.getElementById('flex-input-gas-price');
  const ethPriceInput = document.getElementById('flex-input-eth-price');

  [gasPriceInput, ethPriceInput].forEach(inp => {
    if (inp) {
      inp.addEventListener('input', renderFlexVerdict);
    }
  });

  // Market comparison inputs
  const mktGasInput = document.getElementById('mkt-input-gas-price');
  const mktKwhInput = document.getElementById('mkt-input-kwh-price');
  const mktKmInput = document.getElementById('mkt-input-monthly-km');

  [mktGasInput, mktKwhInput, mktKmInput].forEach(inp => {
    if (inp) {
      inp.addEventListener('input', renderMarketComparisonList);
    }
  });

  // Filter category buttons for market
  document.querySelectorAll('.mkt-filter-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.mkt-filter-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      renderMarketComparisonList();
    });
  });
}

function renderComparator() {
  if (activeComparatorSubTab === 'flex') {
    renderFlexVerdict();
  } else {
    renderMarketComparisonList();
  }
}

function renderFlexVerdict() {
  const vehicle = Storage.getActiveVehicle();
  const gasInput = document.getElementById('flex-input-gas-price');
  const ethInput = document.getElementById('flex-input-eth-price');
  const verdictContainer = document.getElementById('flex-verdict-container');

  if (!gasInput || !ethInput || !verdictContainer) return;

  const gasPrice = parseFloat(gasInput.value) || 0;
  const ethPrice = parseFloat(ethInput.value) || 0;

  if (gasPrice <= 0 || ethPrice <= 0) {
    verdictContainer.innerHTML = `<div class="card" style="text-align: center; color: var(--text-muted);">Insira os preços da gasolina e do etanol para calcular o veredito.</div>`;
    return;
  }

  const data = Storage.loadData();
  const vehicleLogs = (data.fuelLogs || []).filter(l => l.vehicleId === vehicle?.id);
  const refills = computeFuelRefillsWithStats(vehicleLogs);
  const stats = computeDashboardStats(vehicle, refills, []);

  const result = computeFlexCalculation(
    gasPrice,
    ethPrice,
    stats.gasolineAvgKmPerL,
    stats.ethanolAvgKmPerL,
    vehicle?.fuelCapacityLiters || 50.0
  );

  if (!result) return;

  const verdictClass = result.isEthanolBetter ? 'etanol' : 'gasolina';
  const winnerFuelName = result.winnerFuel === 'ETANOL' ? 'Etanol' : 'Gasolina Comum';
  const winnerPrice = result.winnerFuel === 'ETANOL' ? ethPrice : gasPrice;

  verdictContainer.innerHTML = `
    <div class="verdict-box ${verdictClass}">
      <div class="verdict-tag">Veredito Mais Econômico</div>
      <div class="verdict-title">ABASTEÇA COM ${result.winnerFuel}</div>
      <div class="verdict-ratio">
        Relação: <strong>${result.priceRatioPercent.toFixed(1)}%</strong>
        (Equilíbrio: ${result.breakEvenPercent.toFixed(1)}%)
      </div>
      <div class="verdict-description">
        ${result.hasVehicleData
          ? `Cálculo com <strong>consumo real medido</strong> (${stats.gasolineAvgKmPerL?.toFixed(1)} km/L Gas vs ${stats.ethanolAvgKmPerL?.toFixed(1)} km/L Etanol).`
          : 'Cálculo com a regra padrão de paridade de 70%.'}
      </div>

      <div class="verdict-savings-list">
        <div class="verdict-stat-row">
          <span class="verdict-stat-lbl">Economia por Tanque Cheio</span>
          <span class="verdict-stat-num">${brCurrency.format(result.tankSavings)}</span>
        </div>
        <div class="verdict-stat-row">
          <span class="verdict-stat-lbl">Economia a cada 1.000 km</span>
          <span class="verdict-stat-num">${brCurrency.format(result.savingsPer1000Km)}</span>
        </div>
        <div class="verdict-stat-row">
          <span class="verdict-stat-lbl">Custo por km rodado</span>
          <div class="verdict-stat-dual">
            <span class="dual-eth">Etanol: ${brCurrency.format(result.costPerKmEth)}/km</span>
            <span class="dual-gas">Gasolina: ${brCurrency.format(result.costPerKmGas)}/km</span>
          </div>
        </div>
      </div>

      <button type="button" class="btn-primary btn-verdict-action" id="btn-verdict-apply" data-fuel="${winnerFuelName}" data-price="${winnerPrice}">
        <svg width="18" height="18" fill="currentColor" viewBox="0 0 24 24">
          <path d="M19.77 7.23l.01-.01-3.72-3.72L15 4.56l2.11 2.11c-.94.36-1.61 1.26-1.61 2.33a2.5 2.5 0 002.5 2.5c.36 0 .69-.08 1-.21v7.21c0 .55-.45 1-1 1s-1-.45-1-1V14c0-1.1-.9-2-2-2h-1V5c0-1.1-.9-2-2-2H6c-1.1 0-2 .9-2 2v16h10v-7.5h1.5v5c0 1.38 1.12 2.5 2.5 2.5s2.5-1.12 2.5-2.5V9c0-.69-.28-1.32-.73-1.77zM12 10H6V5h6v5zm6 0c-.55 0-1-.45-1-1s.45-1 1-1 1 .45 1 1-.45 1-1 1z"/>
        </svg>
        Abastecer com ${winnerFuelName}
      </button>
    </div>
  `;

  const btnApply = document.getElementById('btn-verdict-apply');
  if (btnApply) {
    btnApply.addEventListener('click', () => {
      openAddFuelModalWithPrefill(winnerFuelName, winnerPrice);
    });
  }
}

function openAddFuelModalWithPrefill(fuelType, price) {
  const vehicle = Storage.getActiveVehicle();
  if (!vehicle) return;

  document.getElementById('fuel-input-odometer').value = vehicle.currentOdometerKm || '';
  document.getElementById('fuel-input-liters').value = '';
  document.getElementById('fuel-input-price-per-l').value = price ? Number(price).toFixed(2) : '';
  document.getElementById('fuel-input-total-cost').value = '';
  document.getElementById('fuel-input-station').value = '';
  document.getElementById('fuel-input-notes').value = '';
  document.getElementById('fuel-checkbox-fulltank').checked = true;

  const selectType = document.getElementById('fuel-select-type');
  if (selectType) {
    selectType.value = fuelType;
  }

  openModal('modal-add-fuel');
  setTimeout(() => {
    document.getElementById('fuel-input-liters')?.focus();
  }, 150);
}

function renderMarketComparisonList() {
  const vehicle = Storage.getActiveVehicle();
  const data = Storage.loadData();
  const vehicleLogs = (data.fuelLogs || []).filter(l => l.vehicleId === vehicle?.id);
  const refills = computeFuelRefillsWithStats(vehicleLogs);
  const stats = computeDashboardStats(vehicle, refills, []);

  const gasInput = document.getElementById('mkt-input-gas-price');
  const kwhInput = document.getElementById('mkt-input-kwh-price');
  const kmInput = document.getElementById('mkt-input-monthly-km');
  const container = document.getElementById('market-comparison-cards');

  if (!container) return;

  const gasPrice = parseFloat(gasInput?.value) || 5.99;
  const kwhPrice = parseFloat(kwhInput?.value) || 0.85;
  const monthlyKm = parseFloat(kmInput?.value) || 1200;

  const activeFilterBtn = document.querySelector('.mkt-filter-btn.active');
  const filterCat = activeFilterBtn?.getAttribute('data-filter') || 'ALL';

  let cars = PRESET_MARKET_CARS;
  if (filterCat === 'ELECTRIC') {
    cars = cars.filter(c => c.powertrain === VehiclePowertrain.ELECTRIC);
  } else if (filterCat === 'HYBRID') {
    cars = cars.filter(c => c.powertrain === VehiclePowertrain.HYBRID);
  } else if (filterCat === 'COMBUSTION') {
    cars = cars.filter(c => c.powertrain === VehiclePowertrain.COMBUSTION);
  }

  const comparison = computeMarketComparison(cars, stats.averageKmPerLiter, gasPrice, kwhPrice, monthlyKm);

  // Update User Baseline Header Box
  const baselineBox = document.getElementById('mkt-user-baseline');
  if (baselineBox) {
    baselineBox.innerHTML = `
      <div class="baseline-mobile-card">
        <div class="baseline-header-row">
          <div>
            <span class="baseline-tag">Seu Veículo Atual</span>
            <div class="baseline-title">${vehicle?.name || 'Seu Carro'}</div>
            <div class="baseline-meta">Média: <strong>${comparison.userKmL.toFixed(1)} km/L</strong> • ${monthlyKm.toLocaleString('pt-BR')} km/mês</div>
          </div>
          <div class="baseline-cost-badge">
            <div class="baseline-cost-val">${brCurrency.format(comparison.userCostPerKm)}/km</div>
            <div class="baseline-cost-annual">Gasto Anual: ${brCurrency.format(comparison.userAnnualCost)}</div>
          </div>
        </div>
      </div>
    `;
  }

  container.innerHTML = comparison.comparedCars.map(car => {
    const isSave = car.annualSavings > 0;
    const savingsColor = isSave ? 'var(--accent-emerald)' : 'var(--accent-rose)';

    return `
      <div class="market-car-card">
        <div class="car-header-row">
          <div>
            <div class="car-name">${car.name}</div>
            <div style="font-size: 0.78rem; color: var(--text-muted);">${car.category}</div>
          </div>
          ${car.badge ? `<span class="car-badge">${car.badge}</span>` : ''}
        </div>

        <div style="font-size: 0.82rem; color: var(--text-secondary); line-height: 1.4;">
          ${car.description}
        </div>

        <div class="car-metrics-row">
          <div class="car-metric-box">
            <span class="car-metric-label">Eficiência</span>
            <span class="car-efficiency">${car.efficiencyLabel}</span>
          </div>
          <div class="car-metric-box">
            <span class="car-metric-label">Custo/km</span>
            <span class="car-cost-km">${brCurrency.format(car.costPerKm)}</span>
          </div>
        </div>

        <div class="car-savings-banner">
          <span>Economia Anual Estimada:</span>
          <span class="car-savings-val" style="color: ${savingsColor};">
            ${isSave ? '+' : ''}${brCurrency.format(car.annualSavings)}/ano
          </span>
        </div>
      </div>
    `;
  }).join('');
}

/* ==========================================================================
   Modals Setup & Logic
   ========================================================================== */
function setupModals() {
  // Generic close buttons
  document.querySelectorAll('.modal-close, .btn-modal-cancel').forEach(btn => {
    btn.addEventListener('click', () => {
      closeAllModals();
    });
  });

  // Close modal when tapping overlay backdrop
  document.querySelectorAll('.modal-overlay').forEach(overlay => {
    overlay.addEventListener('click', (e) => {
      if (e.target === overlay) {
        closeAllModals();
      }
    });
  });

  // 1. Add Fuel Modal
  const addFuelBtn = document.getElementById('btn-quick-add-fuel');
  if (addFuelBtn) {
    addFuelBtn.addEventListener('click', () => {
      const vehicle = Storage.getActiveVehicle();
      if (!vehicle) return;

      document.getElementById('fuel-input-odometer').value = vehicle.currentOdometerKm || '';
      document.getElementById('fuel-input-liters').value = '';
      document.getElementById('fuel-input-price-per-l').value = '';
      document.getElementById('fuel-input-total-cost').value = '';
      document.getElementById('fuel-input-station').value = '';
      document.getElementById('fuel-input-notes').value = '';
      document.getElementById('fuel-checkbox-fulltank').checked = true;

      openModal('modal-add-fuel');
    });
  }

  // Auto calculate total in fuel modal
  const litersInp = document.getElementById('fuel-input-liters');
  const priceLInp = document.getElementById('fuel-input-price-per-l');
  const totalCostInp = document.getElementById('fuel-input-total-cost');

  function updateFuelTotal() {
    const l = parseFloat(litersInp.value) || 0;
    const p = parseFloat(priceLInp.value) || 0;
    if (l > 0 && p > 0) {
      totalCostInp.value = (l * p).toFixed(2);
    }
  }
  litersInp?.addEventListener('input', updateFuelTotal);
  priceLInp?.addEventListener('input', updateFuelTotal);

  document.getElementById('form-add-fuel')?.addEventListener('submit', (e) => {
    e.preventDefault();
    const vehicle = Storage.getActiveVehicle();
    if (!vehicle) return;

    const odo = parseFloat(document.getElementById('fuel-input-odometer').value) || 0;
    const l = parseFloat(document.getElementById('fuel-input-liters').value) || 0;
    const p = parseFloat(document.getElementById('fuel-input-price-per-l').value) || 0;
    const cost = parseFloat(document.getElementById('fuel-input-total-cost').value) || (l * p);
    const fuelType = document.getElementById('fuel-select-type').value;
    const isFullTank = document.getElementById('fuel-checkbox-fulltank').checked;
    const station = document.getElementById('fuel-input-station').value.trim();
    const notes = document.getElementById('fuel-input-notes').value.trim();

    Storage.addFuelLog({
      vehicleId: vehicle.id,
      odometerKm: odo,
      liters: l,
      pricePerLiter: p,
      totalCost: cost,
      fuelType,
      isFullTank,
      dateEpochMillis: Date.now(),
      gasStation: station,
      notes
    });

    closeAllModals();
    showToast('Abastecimento registrado com sucesso!');
  });

  // 2. Update Odometer Modal
  document.getElementById('dash-vehicle-odometer')?.addEventListener('click', () => {
    const vehicle = Storage.getActiveVehicle();
    if (!vehicle) return;
    document.getElementById('input-quick-odometer').value = vehicle.currentOdometerKm;
    openModal('modal-update-odometer');
  });

  document.getElementById('form-update-odometer')?.addEventListener('submit', (e) => {
    e.preventDefault();
    const vehicle = Storage.getActiveVehicle();
    if (!vehicle) return;
    const newKm = parseFloat(document.getElementById('input-quick-odometer').value) || 0;
    Storage.updateVehicle({
      ...vehicle,
      currentOdometerKm: newKm
    });
    closeAllModals();
    showToast(`Odômetro atualizado para ${newKm.toLocaleString('pt-BR')} km`);
  });

  // 3. Record Performed Service Modal
  document.getElementById('form-record-service')?.addEventListener('submit', (e) => {
    e.preventDefault();
    const itemId = Number(document.getElementById('record-service-item-id').value);
    const km = parseFloat(document.getElementById('record-service-km').value) || 0;
    const dateVal = document.getElementById('record-service-date').value;
    const dateMillis = dateVal ? new Date(dateVal).getTime() : Date.now();
    const cost = parseFloat(document.getElementById('record-service-cost').value) || 0;
    const workshop = document.getElementById('record-service-workshop').value.trim();
    const notes = document.getElementById('record-service-notes').value.trim();

    Storage.recordMaintenancePerformed(itemId, km, dateMillis, cost, workshop, notes);
    closeAllModals();
    showToast('Revisão registrada no histórico com sucesso!');
  });

  // 4. Add/Edit Maintenance Item Modal
  document.getElementById('btn-add-maintenance-item')?.addEventListener('click', () => {
    const vehicle = Storage.getActiveVehicle();
    if (!vehicle) return;
    editingMaintenanceItemId = null;
    document.getElementById('modal-maint-item-title').textContent = 'Novo Item de Manutenção';
    document.getElementById('maint-item-input-title').value = '';
    document.getElementById('maint-item-input-category').value = 'Motor';
    document.getElementById('maint-item-input-interval-km').value = '10000';
    document.getElementById('maint-item-input-interval-months').value = '6';
    document.getElementById('maint-item-input-last-km').value = vehicle.currentOdometerKm;
    document.getElementById('maint-item-input-notes').value = '';
    document.getElementById('btn-del-maint-item').style.display = 'none';

    openModal('modal-add-edit-maint-item');
  });

  document.getElementById('form-add-edit-maint-item')?.addEventListener('submit', (e) => {
    e.preventDefault();
    const vehicle = Storage.getActiveVehicle();
    if (!vehicle) return;

    const title = document.getElementById('maint-item-input-title').value.trim();
    const category = document.getElementById('maint-item-input-category').value;
    const intervalKm = parseFloat(document.getElementById('maint-item-input-interval-km').value) || 10000;
    const intervalMonths = parseInt(document.getElementById('maint-item-input-interval-months').value) || 6;
    const lastKm = parseFloat(document.getElementById('maint-item-input-last-km').value) || vehicle.currentOdometerKm;
    const notes = document.getElementById('maint-item-input-notes').value.trim();

    if (editingMaintenanceItemId) {
      const data = Storage.loadData();
      const existing = (data.maintenanceItems || []).find(m => m.id === editingMaintenanceItemId);
      if (existing) {
        Storage.updateMaintenanceItem({
          ...existing,
          title,
          category,
          intervalKm,
          intervalMonths,
          lastServiceKm: lastKm,
          notes
        });
        showToast('Item de manutenção atualizado.');
      }
    } else {
      Storage.addMaintenanceItem({
        vehicleId: vehicle.id,
        title,
        category,
        intervalKm,
        intervalMonths,
        lastServiceKm: lastKm,
        lastServiceDateEpochMillis: Date.now(),
        notes
      });
      showToast('Item de manutenção cadastrado.');
    }
    closeAllModals();
  });

  document.getElementById('btn-del-maint-item')?.addEventListener('click', () => {
    if (editingMaintenanceItemId && confirm('Deseja excluir este plano de manutenção?')) {
      Storage.deleteMaintenanceItem(editingMaintenanceItemId);
      closeAllModals();
      showToast('Item de manutenção removido.');
    }
  });

  // 5. Vehicle Manage Modal
  setupVehicleManageModal();

  // 6. Data Management Modal
  setupDataManagementModal();
}

function openModal(modalId) {
  closeAllModals();
  const m = document.getElementById(modalId);
  if (m) m.classList.add('active');
}

function closeAllModals() {
  document.querySelectorAll('.modal-overlay').forEach(m => m.classList.remove('active'));
}

function openRecordServiceModal(itemId) {
  const vehicle = Storage.getActiveVehicle();
  const data = Storage.loadData();
  const item = (data.maintenanceItems || []).find(m => m.id === itemId);
  if (!item || !vehicle) return;

  document.getElementById('record-service-item-id').value = item.id;
  document.getElementById('record-service-title').textContent = item.title;
  document.getElementById('record-service-km').value = vehicle.currentOdometerKm;
  document.getElementById('record-service-date').value = new Date().toISOString().slice(0, 10);
  document.getElementById('record-service-cost').value = '';
  document.getElementById('record-service-workshop').value = '';
  document.getElementById('record-service-notes').value = '';

  openModal('modal-record-service');
}

function openEditMaintenanceModal(itemId) {
  const data = Storage.loadData();
  const item = (data.maintenanceItems || []).find(m => m.id === itemId);
  if (!item) return;

  editingMaintenanceItemId = item.id;
  document.getElementById('modal-maint-item-title').textContent = 'Editar Item de Manutenção';
  document.getElementById('maint-item-input-title').value = item.title;
  document.getElementById('maint-item-input-category').value = item.category;
  document.getElementById('maint-item-input-interval-km').value = item.intervalKm;
  document.getElementById('maint-item-input-interval-months').value = item.intervalMonths;
  document.getElementById('maint-item-input-last-km').value = item.lastServiceKm;
  document.getElementById('maint-item-input-notes').value = item.notes || '';
  document.getElementById('btn-del-maint-item').style.display = 'block';

  openModal('modal-add-edit-maint-item');
}

function setupVehicleManageModal() {
  const listContainer = document.getElementById('vehicle-modal-list');
  const addFormContainer = document.getElementById('vehicle-modal-add-form');
  const btnShowAdd = document.getElementById('btn-show-add-vehicle');

  function renderVehicleList() {
    const vehicles = Storage.getAllVehicles();
    const active = Storage.getActiveVehicle();
    if (!listContainer) return;

    listContainer.innerHTML = vehicles.map(v => {
      const isActive = v.id === active?.id;
      return `
        <div class="card" style="margin-bottom: 8px; display: flex; justify-content: space-between; align-items: center; border: 1px solid ${isActive ? 'var(--accent-teal)' : 'var(--border-subtle)'};">
          <div>
            <div style="font-weight: 700; font-size: 1rem;">${v.name} ${isActive ? '<span style="font-size: 0.72rem; color: var(--accent-teal); font-weight: 800;">(ATIVO)</span>' : ''}</div>
            <div style="font-size: 0.8rem; color: var(--text-muted);">${v.brand} • ${v.modelYear} • Placa: ${v.plate || '---'}</div>
            <div style="font-size: 0.8rem; color: var(--text-secondary);">Odômetro: <strong>${v.currentOdometerKm.toLocaleString('pt-BR')} km</strong> • Tanque: ${v.fuelCapacityLiters}L</div>
          </div>
          <div>
            ${!isActive ? `<button class="btn-small btn-primary btn-select-vehicle" data-id="${v.id}">Selecionar</button>` : ''}
          </div>
        </div>
      `;
    }).join('');

    listContainer.querySelectorAll('.btn-select-vehicle').forEach(btn => {
      btn.addEventListener('click', () => {
        const id = Number(btn.getAttribute('data-id'));
        Storage.setActiveVehicle(id);
        closeAllModals();
        showToast('Veículo ativo alterado.');
      });
    });
  }

  // Open modal hook
  document.getElementById('vehicle-chip-btn')?.addEventListener('click', () => {
    if (addFormContainer) addFormContainer.style.display = 'none';
    if (listContainer) listContainer.style.display = 'block';
    renderVehicleList();
    openModal('modal-vehicle-manage');
  });

  btnShowAdd?.addEventListener('click', () => {
    if (listContainer) listContainer.style.display = 'none';
    if (addFormContainer) addFormContainer.style.display = 'block';
  });

  document.getElementById('btn-cancel-add-vehicle')?.addEventListener('click', () => {
    if (addFormContainer) addFormContainer.style.display = 'none';
    if (listContainer) listContainer.style.display = 'block';
  });

  document.getElementById('form-add-new-vehicle')?.addEventListener('submit', (e) => {
    e.preventDefault();
    const name = document.getElementById('new-veh-name').value.trim();
    const brand = document.getElementById('new-veh-brand').value.trim();
    const year = parseInt(document.getElementById('new-veh-year').value) || new Date().getFullYear();
    const plate = document.getElementById('new-veh-plate').value.trim().toUpperCase();
    const odo = parseFloat(document.getElementById('new-veh-odometer').value) || 0;
    const cap = parseFloat(document.getElementById('new-veh-capacity').value) || 50;
    const fuel = document.getElementById('new-veh-fuel').value;

    Storage.addVehicle({
      name,
      brand,
      modelYear: year,
      plate,
      currentOdometerKm: odo,
      fuelCapacityLiters: cap,
      preferredFuel: fuel
    });

    closeAllModals();
    showToast(`Veículo "${name}" adicionado com sucesso!`);
  });
}

function setupDataManagementModal() {
  document.getElementById('btn-export-json')?.addEventListener('click', () => {
    Storage.exportToJson();
    showToast('Download do backup JSON iniciado.');
  });

  const fileInput = document.getElementById('input-import-json-file');
  document.getElementById('btn-trigger-import-json')?.addEventListener('click', () => {
    fileInput?.click();
  });

  fileInput?.addEventListener('change', (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (event) => {
      try {
        const content = event.target?.result;
        Storage.importFromJson(content);
        closeAllModals();
        showToast('Backup importado com sucesso!');
      } catch (err) {
        alert('Erro ao importar backup: ' + err.message);
      }
    };
    reader.readAsText(file);
  });

  document.getElementById('btn-reset-demo-data')?.addEventListener('click', () => {
    if (confirm('Restaurar dados demonstrativos (Tracker 1.2 Turbo com histórico e revisões)? Seus dados atuais serão substituídos.')) {
      Storage.resetToDemo();
      closeAllModals();
      showToast('Dados de demonstração restaurados!');
    }
  });

  document.getElementById('btn-clear-all-data')?.addEventListener('click', () => {
    if (confirm('ATENÇÃO: Deseja apagar todos os registros e reiniciar o AutoFuel do zero?')) {
      Storage.clearAllData(true);
      closeAllModals();
      showToast('Todos os dados foram resetados.');
    }
  });
}
