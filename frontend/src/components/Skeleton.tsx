import React from 'react';
import clsx from 'clsx';

interface SkeletonProps extends React.HTMLAttributes<HTMLDivElement> {
  className?: string;
  variant?: 'rectangular' | 'circular' | 'rounded';
  width?: string | number;
  height?: string | number;
}

/**
 * High-performance GPU-friendly shimmer skeleton loader.
 */
export const Skeleton: React.FC<SkeletonProps> = ({
  className,
  variant = 'rounded',
  width,
  height,
  style,
  ...props
}) => {
  const variantClass = {
    rectangular: 'rounded-none',
    circular: 'rounded-full',
    rounded: 'rounded-xl',
  }[variant];

  return (
    <div
      className={clsx(
        'relative overflow-hidden bg-slate-800/60 dark:bg-slate-800/80',
        'before:absolute before:inset-0 before:-translate-x-full',
        'before:animate-[shimmer_1.8s_infinite]',
        'before:bg-gradient-to-r before:from-transparent before:via-slate-700/30 before:to-transparent',
        variantClass,
        className
      )}
      style={{
        width,
        height,
        ...style,
      }}
      aria-hidden="true"
      {...props}
    />
  );
};
