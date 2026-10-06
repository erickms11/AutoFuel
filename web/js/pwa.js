// AutoFuel PWA Controller (pwa.js)

let deferredInstallPrompt = null;

export function initPWA() {
  // 1. Register Service Worker
  if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
      navigator.serviceWorker.register('./sw.js')
        .then((reg) => {
          console.log('[PWA] Service Worker registrado com sucesso:', reg.scope);
        })
        .catch((err) => {
          console.warn('[PWA] Falha ao registrar Service Worker:', err);
        });
    });
  }

  // 2. Intercept beforeinstallprompt for Android / Desktop Chrome
  const installBtn = document.getElementById('btn-install-pwa');
  const installBanner = document.getElementById('pwa-install-banner');

  window.addEventListener('beforeinstallprompt', (e) => {
    // Prevent mini-infobar on mobile
    e.preventDefault();
    deferredInstallPrompt = e;

    if (installBtn) {
      installBtn.style.display = 'inline-flex';
    }
    if (installBanner) {
      installBanner.classList.remove('hidden');
    }
  });

  window.addEventListener('appinstalled', () => {
    console.log('[PWA] Aplicativo instalado!');
    deferredInstallPrompt = null;
    if (installBtn) installBtn.style.display = 'none';
    if (installBanner) installBanner.classList.add('hidden');
    showToast('AutoFuel instalado com sucesso!');
  });

  if (installBtn) {
    installBtn.addEventListener('click', promptPWAInstallation);
  }

  const bannerInstallBtn = document.getElementById('pwa-banner-btn-install');
  if (bannerInstallBtn) {
    bannerInstallBtn.addEventListener('click', promptPWAInstallation);
  }

  const bannerCloseBtn = document.getElementById('pwa-banner-btn-close');
  if (bannerCloseBtn && installBanner) {
    bannerCloseBtn.addEventListener('click', () => {
      installBanner.classList.add('hidden');
    });
  }

  // 3. Detect iOS Safari standalone vs browser
  const isIOS = /iPad|iPhone|iPod/.test(navigator.userAgent) && !window.MSStream;
  const isStandalone = window.matchMedia('(display-mode: standalone)').matches || window.navigator.standalone;

  if (isIOS && !isStandalone) {
    // Show iOS install instructions when user taps install button
    if (installBtn) {
      installBtn.style.display = 'inline-flex';
      installBtn.removeEventListener('click', promptPWAInstallation);
      installBtn.addEventListener('click', showIOSInstallModal);
    }
  }

  // 4. Online/Offline indicator
  window.addEventListener('online', () => {
    showToast('Conexão restabelecida. Modo online.');
    updateConnectionStatus(true);
  });

  window.addEventListener('offline', () => {
    showToast('Você está sem conexão. Operando 100% offline.');
    updateConnectionStatus(false);
  });

  updateConnectionStatus(navigator.onLine);
}

export function promptPWAInstallation() {
  if (deferredInstallPrompt) {
    deferredInstallPrompt.prompt();
    deferredInstallPrompt.userChoice.then((choiceResult) => {
      if (choiceResult.outcome === 'accepted') {
        console.log('[PWA] Usuário aceitou a instalação.');
      } else {
        console.log('[PWA] Usuário cancelou a instalação.');
      }
      deferredInstallPrompt = null;
    });
  } else {
    // Fallback if not ready or in iOS
    showIOSInstallModal();
  }
}

export function showIOSInstallModal() {
  const modal = document.getElementById('modal-ios-install');
  if (modal) {
    modal.classList.add('active');
  }
}

function updateConnectionStatus(isOnline) {
  const badge = document.getElementById('connection-status-badge');
  if (badge) {
    if (isOnline) {
      badge.textContent = 'Online';
      badge.className = 'status-badge online';
    } else {
      badge.textContent = 'Offline';
      badge.className = 'status-badge offline';
    }
  }
}

export function showToast(message, duration = 3200) {
  let toastContainer = document.getElementById('toast-container');
  if (!toastContainer) {
    toastContainer = document.createElement('div');
    toastContainer.id = 'toast-container';
    toastContainer.className = 'toast-container';
    document.body.appendChild(toastContainer);
  }

  const toast = document.createElement('div');
  toast.className = 'toast-card';
  toast.textContent = message;

  toastContainer.appendChild(toast);
  setTimeout(() => toast.classList.add('show'), 20);

  setTimeout(() => {
    toast.classList.remove('show');
    setTimeout(() => toast.remove(), 300);
  }, duration);
}
