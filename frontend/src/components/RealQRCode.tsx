import React, { useState, useEffect, useRef } from 'react';
import QRCode from 'qrcode';

interface RealQRCodeProps {
  value: string;
  size?: number;
  className?: string;
  darkColor?: string;
  lightColor?: string;
  includeMargin?: boolean;
}

/**
 * Enterprise RealQRCode component that generates genuine, scannable
 * 2D matrix barcodes readable by iOS/Android camera apps, Google Lens, and airport scanners.
 */
export const RealQRCode: React.FC<RealQRCodeProps> = ({
  value,
  size = 160,
  className = '',
  darkColor = '#000000',
  lightColor = '#ffffff',
  includeMargin = true,
}) => {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const [dataUrl, setDataUrl] = useState<string>('');
  const [error, setError] = useState<boolean>(false);

  useEffect(() => {
    if (!value) return;

    QRCode.toDataURL(value, {
      width: size * 2, // 2x resolution for retina and scanner precision
      margin: includeMargin ? 2 : 1,
      errorCorrectionLevel: 'M',
      color: {
        dark: darkColor,
        light: lightColor,
      },
    })
      .then((url) => {
        setDataUrl(url);
        setError(false);
      })
      .catch((err) => {
        console.error('Failed to generate real QR code:', err);
        setError(true);
      });
  }, [value, size, darkColor, lightColor, includeMargin]);

  if (error || !dataUrl) {
    return (
      <div
        style={{ width: size, height: size }}
        className={`bg-white rounded-xl flex items-center justify-center p-2 border border-slate-200 ${className}`}
      >
        <canvas ref={canvasRef} className="w-full h-full" />
      </div>
    );
  }

  return (
    <img
      src={dataUrl}
      alt="Scannable Boarding Pass QR Code"
      style={{ width: size, height: size }}
      className={`rounded-xl block object-contain select-none shadow-sm ${className}`}
      loading="eager"
    />
  );
};
