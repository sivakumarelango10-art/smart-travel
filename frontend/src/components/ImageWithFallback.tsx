import React, { useState } from 'react';
import { ImageOff, Sparkles } from 'lucide-react';

interface ImageWithFallbackProps extends React.ImgHTMLAttributes<HTMLImageElement> {
  fallbackSrc?: string;
  fallbackText?: string;
  containerClassName?: string;
}

const DEFAULT_FALLBACK = 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=800&q=80';

export const ImageWithFallback: React.FC<ImageWithFallbackProps> = ({
  src,
  alt = 'Travel stay photo',
  className = '',
  containerClassName = '',
  fallbackSrc = DEFAULT_FALLBACK,
  fallbackText,
  ...props
}) => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  const effectiveSrc = error ? fallbackSrc : src || fallbackSrc;

  return (
    <div className={`relative overflow-hidden bg-slate-900 ${containerClassName}`}>
      {/* Loading Skeleton */}
      {loading && (
        <div className="absolute inset-0 bg-slate-800 animate-pulse flex items-center justify-center">
          <Sparkles className="w-5 h-5 text-slate-600 animate-spin" />
        </div>
      )}

      {/* Image */}
      <img
        src={effectiveSrc}
        alt={alt}
        loading={props.loading || 'lazy'}
        decoding="async"
        onLoad={() => setLoading(false)}
        onError={() => {
          if (!error) {
            setError(true);
            setLoading(false);
          }
        }}
        className={`w-full h-full object-cover transition-opacity duration-300 ${
          loading ? 'opacity-0' : 'opacity-100'
        } ${className}`}
        {...props}
      />

      {/* Extreme fallback overlay if even fallbackSrc fails */}
      {error && !fallbackSrc && (
        <div className="absolute inset-0 bg-slate-900 flex flex-col items-center justify-center text-slate-500 p-4 text-center">
          <ImageOff className="w-8 h-8 mb-1 opacity-60" />
          <span className="text-[11px] font-medium">{fallbackText || 'Image unavailable'}</span>
        </div>
      )}
    </div>
  );
};
