import { lazy, ComponentType } from 'react';

/**
 * Wraps dynamic component imports with automatic chunk fetch retry.
 * Handles single-page app deployment updates where previous chunk hashes no longer exist on CDN.
 */
export function lazyWithRetry<T extends ComponentType<any>>(
  factory: () => Promise<{ default: T }>
) {
  return lazy(async () => {
    const pageHasBeenForceRefreshed = JSON.parse(
      window.sessionStorage.getItem('smarttravel_chunk_reload') || 'false'
    );

    try {
      const component = await factory();
      window.sessionStorage.setItem('smarttravel_chunk_reload', 'false');
      return component;
    } catch (error: any) {
      const isChunkError = /Failed to fetch dynamically imported module|Importing a module script failed|Loading chunk/i.test(
        error?.message || ''
      );

      if (isChunkError && !pageHasBeenForceRefreshed && typeof window !== 'undefined') {
        window.sessionStorage.setItem('smarttravel_chunk_reload', 'true');
        window.location.reload();
        return new Promise<{ default: T }>(() => {}); // wait for reload
      }

      throw error;
    }
  });
}
