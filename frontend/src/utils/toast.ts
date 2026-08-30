/**
 * Global application toast notification dispatcher.
 * Bridges application events to InAppNotificationToast without blocking the browser thread.
 */
export function notify(
  title: string,
  body: string,
  eventType: 'INFO' | 'SUCCESS' | 'ERROR' | 'WARNING' | 'CANCEL' | 'DELAY' = 'INFO',
  url?: string
): void {
  if (typeof window !== 'undefined') {
    window.dispatchEvent(
      new CustomEvent('app:notification', {
        detail: {
          title,
          body,
          eventType,
          url,
        },
      })
    );
  }
}
