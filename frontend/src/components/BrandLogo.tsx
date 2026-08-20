import React from 'react';
import { Link } from 'react-router-dom';

interface BrandLogoProps {
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl' | '2xl';
  withLink?: boolean;
  className?: string;
  showBadge?: boolean;
  badgeText?: string;
}

export const BrandLogo: React.FC<BrandLogoProps> = ({
  size = 'md',
  withLink = true,
  className = '',
  showBadge = false,
  badgeText = 'PREMIUM',
}) => {
  const sizeClasses = {
    xs: 'h-7',
    sm: 'h-8 sm:h-9',
    md: 'h-10 sm:h-11',
    lg: 'h-12 sm:h-14',
    xl: 'h-16 sm:h-20',
    '2xl': 'h-24 sm:h-28',
  }[size];

  const content = (
    <div className={`inline-flex items-center gap-2.5 group transition-transform duration-300 ${className}`}>
      <div className="relative flex items-center shrink-0">
        <img
          src="/logo.png"
          alt="SmartTravel Logo - Explore • Book • Journey"
          className={`${sizeClasses} w-auto object-contain drop-shadow-md group-hover:scale-[1.03] transition-transform duration-300 select-none`}
          loading="eager"
        />
      </div>
      {showBadge && (
        <span className="hidden sm:inline-flex text-[9px] uppercase font-black px-2 py-0.5 rounded-full bg-sky-500/15 text-sky-400 border border-sky-500/30 tracking-wider">
          {badgeText}
        </span>
      )}
    </div>
  );

  if (withLink) {
    return (
      <Link to="/" className="inline-flex items-center focus:outline-none focus:ring-2 focus:ring-sky-500/40 rounded-xl">
        {content}
      </Link>
    );
  }

  return content;
};
