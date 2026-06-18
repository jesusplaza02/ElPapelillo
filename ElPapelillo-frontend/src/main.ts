
import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';
import { AES, Utf8, SHA256 } from 'crypto-es';

const SECRET_KEY = 'ClaveSecretaTFG_ElPapelillo_2026';

if (typeof window !== 'undefined' && typeof window.localStorage !== 'undefined') {
  
  const originalSetItem = window.localStorage.setItem;
  const originalGetItem = window.localStorage.getItem;

  // 1. Interceptamos para GUARDAR
  window.localStorage.setItem = function (key: string, value: string): void {
    try {
      if (value === null || value === undefined) return;

      // Ciframos el valor normal
      const encryptedValue = AES.encrypt(String(value), SECRET_KEY).toString();
      originalSetItem.apply(this, [key, encryptedValue]);


      const sello = SHA256(key + String(value) + SECRET_KEY).toString();
      originalSetItem.apply(this, [key + '_sello', sello]);

      window.dispatchEvent(new Event('local-storage-cambiado'));

    } catch (e) {
      originalSetItem.apply(this, [key, value]);
    }
  };

  // 2. Interceptamos para LEER
  window.localStorage.getItem = function (key: string): string | null {
    const rawValue = originalGetItem.apply(this, [key]);
    
    if (key.endsWith('_sello')) {
      return rawValue;
    }

    const guardadoSello = originalGetItem.apply(this, [key + '_sello']);
    if (!rawValue) return null;

    try {
      const bytes = AES.decrypt(rawValue, SECRET_KEY);
      const decryptedText = bytes.toString(Utf8);
      
      if (decryptedText) {
        const selloEsperado = SHA256(key + decryptedText + SECRET_KEY).toString();
        
        if (guardadoSello !== selloEsperado) {
          console.warn("Intento de manipulación detectado en: " + key);
          originalSetItem.apply(this, ['clear_in_progress', 'true']);
          window.localStorage.clear();
          window.location.href = '/login';
          return null;
        }

        return decryptedText;
      }
    } catch (e) {
      if (!originalGetItem.apply(this, ['clear_in_progress'])) {
        window.localStorage.clear();
        window.location.href = '/login';
      }
    }
    
    return null;
  };
}

bootstrapApplication(App, appConfig).catch((err) => console.error(err));