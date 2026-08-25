import React, { useEffect, useState, useRef } from 'react';
import { motion, useSpring, useTransform, useReducedMotion } from 'framer-motion';

interface AnimatedPriceProps {
  value: number;
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
  const [displayValue, setDisplayValue] = useState(value);
  const prevValueRef = useRef(value);

  const spring = useSpring(value, {
    stiffness: 120,
    damping: 18,
    mass: 0.5,
  });

  const formatted = useTransform(spring, (current) => {
    return Math.round(current).toLocaleString('en-IN');
  });

  useEffect(() => {
    spring.set(value);
    const unsubscribe = formatted.on('change', (latest) => {
      setDisplayValue(Number(latest.replace(/,/g, '')));
    });

    const isDifferent = prevValueRef.current !== value;
    prevValueRef.current = value;

    return () => unsubscribe();
  }, [value, spring, formatted]);

  if (shouldReduceMotion) {
    return (
      <span className={className}>
        {prefix}{currency}{value.toLocaleString('en-IN')}{suffix}
      </span>
    );
  }

  const isChanged = prevValueRef.current !== value;

  return (
    <motion.span
      key={value}
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
