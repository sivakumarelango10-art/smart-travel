import React, { useEffect, useRef, useState } from 'react';
import { AlertCircle } from 'lucide-react';

interface GoogleSignInButtonProps {
  onSuccess: (credential: string) => void;
  onError?: (errorMessage: string) => void;
  disabled?: boolean;
  text?: 'signin_with' | 'signup_with' | 'continue_with';
  rememberMe?: boolean;
}

declare global {
  interface Window {
    google?: {
      accounts: {
        id: {
          initialize: (config: any) => void;
          renderButton: (parent: HTMLElement, options: any) => void;
          prompt: (notification?: (notification: any) => void) => void;
          disableAutoSelect: () => void;
        };
      };
    };
  }
}

export const GoogleSignInButton: React.FC<GoogleSignInButtonProps> = ({
  onSuccess,
  onError,
  disabled = false,
  text = 'continue_with',
  rememberMe = true,
}) => {
  const googleBtnContainerRef = useRef<HTMLDivElement>(null);
  const [scriptLoaded, setScriptLoaded] = useState<boolean>(false);
  const [isProcessing, setIsProcessing] = useState<boolean>(false);

  const clientId = (import.meta.env.VITE_GOOGLE_CLIENT_ID || '').trim();

  useEffect(() => {
    // Check if Google GSI script is already in the document
    if (window.google?.accounts?.id) {
      setScriptLoaded(true);
      return;
    }

    const existingScript = document.getElementById('google-gsi-script');
    if (!existingScript) {
      const script = document.createElement('script');
      script.id = 'google-gsi-script';
      script.src = 'https://accounts.google.com/gsi/client';
      script.async = true;
      script.defer = true;
      script.onload = () => {
        setScriptLoaded(true);
      };
      script.onerror = () => {
        if (onError) {
          onError('Failed to load Google Identity Services SDK.');
        }
      };
      document.head.appendChild(script);
    } else {
      existingScript.addEventListener('load', () => setScriptLoaded(true));
    }
  }, [onError]);

  useEffect(() => {
    if (!scriptLoaded || !window.google?.accounts?.id || !clientId) {
      return;
    }

    try {
      window.google.accounts.id.initialize({
        client_id: clientId,
        callback: (response: { credential?: string; select_by?: string }) => {
          if (response.credential) {
            setIsProcessing(true);
            try {
              onSuccess(response.credential);
            } catch (err: any) {
              if (onError) onError(err?.message || 'Google authentication failed.');
            } finally {
              setIsProcessing(false);
            }
          } else {
            if (onError) onError('Google login was cancelled or no credential returned.');
          }
        },
        auto_select: false,
        cancel_on_tap_outside: true,
      });

      if (googleBtnContainerRef.current) {
        googleBtnContainerRef.current.innerHTML = '';
        window.google.accounts.id.renderButton(googleBtnContainerRef.current, {
          type: 'standard',
          theme: 'filled_black',
          size: 'large',
          text: text,
          shape: 'pill',
          logo_alignment: 'left',
          width: 380,
        });
      }
    } catch (err: any) {
      console.warn('Google GSI initialization notice:', err);
    }
  }, [scriptLoaded, clientId, text, onSuccess, onError]);

  const handleCustomButtonClick = () => {
    if (disabled || isProcessing) return;

    if (clientId && window.google?.accounts?.id) {
      try {
        window.google.accounts.id.prompt((notification: any) => {
          if (notification.isNotDisplayed() || notification.isSkippedMoment()) {
            // Popup blocked or not displayed
          }
        });
      } catch (err: any) {
        if (onError) onError(err?.message || 'Could not trigger Google prompt.');
      }
    } else {
      // Demo / simulated mode for local development when VITE_GOOGLE_CLIENT_ID is not set yet
      handleSimulatedGoogleLogin();
    }
  };

  const handleSimulatedGoogleLogin = () => {
    setIsProcessing(true);
    // Generate a secure mock JWT ID Token for developer testing without Google Cloud credentials
    const header = btoa(JSON.stringify({ alg: 'RS256', typ: 'JWT', kid: 'sim-google-key' }));
    const payload = btoa(
      JSON.stringify({
        iss: 'https://accounts.google.com',
        sub: 'google-sim-user-' + Math.floor(Math.random() * 1000000),
        email: 'google.traveler@gmail.com',
        email_verified: true,
        name: 'Google Traveler',
        given_name: 'Google',
        family_name: 'Traveler',
        picture: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80',
        iat: Math.floor(Date.now() / 1000),
        exp: Math.floor(Date.now() / 1000) + 3600,
        aud: clientId || 'mock-google-client-id',
      })
    );
    const signature = btoa('simulated-google-signature-for-development');
    const mockToken = `${header}.${payload}.${signature}`;

    setTimeout(() => {
      try {
        onSuccess(mockToken);
      } finally {
        setIsProcessing(false);
      }
    }, 400);
  };

  return (
    <div className="w-full space-y-2">
      {/* Hidden container where official GSI renders if configured */}
      <div ref={googleBtnContainerRef} className="hidden" aria-hidden="true" />

      {/* Styled Obsidian & Amber Gold Native Google Sign-In Button */}
      <button
        type="button"
        disabled={disabled || isProcessing}
        onClick={handleCustomButtonClick}
        className="w-full py-2.5 px-4 rounded-xl bg-[#181A22] hover:bg-[#1F222E] text-white text-xs font-semibold border border-white/15 hover:border-amber-400/40 shadow-md transition-all flex items-center justify-center gap-2.5 disabled:opacity-50 cursor-pointer group"
      >
        {isProcessing ? (
          <span className="flex items-center gap-2 text-slate-300 text-xs">
            <span className="w-3.5 h-3.5 border-2 border-amber-400/30 border-t-amber-400 rounded-full animate-spin"></span>
            <span>Connecting to Google...</span>
          </span>
        ) : (
          <>
            {/* Official Google Vector Logo */}
            <svg className="w-3.5 h-3.5 shrink-0" viewBox="0 0 24 24">
              <path
                fill="#4285F4"
                d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
              />
              <path
                fill="#34A853"
                d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
              />
              <path
                fill="#FBBC05"
                d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"
              />
              <path
                fill="#EA4335"
                d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"
              />
            </svg>
            <span className="text-slate-200 group-hover:text-white transition text-xs font-semibold">
              Continue with Google
            </span>
          </>
        )}
      </button>
    </div>
  );
};
