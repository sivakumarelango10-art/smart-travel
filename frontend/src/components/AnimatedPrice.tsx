import React, { useEffect, useState, useRef } from 'react';
import { motion, useSpring, useTransform, useReducedMotion } from 'framer-motion';

interface AnimatedPriceProps {
  value: number | string;
  currency?: string;
  className?: string;
  prefix?: string;
  suffix?: string;
}

/**
 * AnimatedPrice creates a smooth, continuous rolling number animation
 * whenever dynamic pricing or fare calculations update.
 */
export const AnimatedPrice: React.FC<AnimatedPriceProps> = ({
  value,
  currency = '₹',
  className = '',
  prefix = '',
  suffix = '',
}) => {
  const shouldReduceMotion = useReducedMotion();
  const numValue = typeof value === 'number' ? (isNaN(value) ? 0 : value) : Number(value) || 0;
  const [displayValue, setDisplayValue] = useState(numValue);
  const prevValueRef = useRef(numValue);

  const spring = useSpring(numValue, {
    stiffness: 120,
    damping: 18,
    mass: 0.5,
  });

  const formatted = useTransform(spring, (current) => {
    return Math.round(current).toLocaleString('en-IN');
  });

  useEffect(() => {
    spring.set(numValue);
    const unsubscribe = formatted.on('change', (latest) => {
      setDisplayValue(Number(latest.replace(/,/g, '')) || 0);
    });

    prevValueRef.current = numValue;

    return () => unsubscribe();
  }, [numValue, spring, formatted]);

  if (shouldReduceMotion) {
    return (
      <span className={className}>
        {prefix}{currency}{numValue.toLocaleString('en-IN')}{suffix}
      </span>
    );
  }

  const isChanged = prevValueRef.current !== numValue;

  return (
    <motion.span
      key={numValue}
      initial={isChanged ? { scale: 1.08, color: '#FBBF24' } : false}
      animate={{ scale: 1, color: 'inherit' }}
      transition={{ duration: 0.35, ease: [0.22, 1, 0.36, 1] }}
      className={`inline-flex items-center tabular-nums ${className}`}
    >
      <span>{prefix}{currency}</span>
      <motion.span>{formatted}</motion.span>
      {suffix && <span>{suffix}</span>}
    </motion.span>
  );
};

