import { Variants, Transition } from 'framer-motion';

/**
 * SmartTravel Motion Design System
 * 
 * Strict Performance Guidelines:
 * - Animate only GPU-composited properties (`transform`, `opacity`).
 * - Max duration capped at 700ms to preserve crisp, instantaneous responsiveness.
 * - Easing: Ultra-smooth exponential ease-out `[0.22, 1, 0.36, 1]`.
 */

// ─── EASING CONSTANTS ────────────────────────────────────────────────────────
export const EASING_SMOOTH = [0.22, 1, 0.36, 1] as const;
export const EASING_IN_OUT = [0.4, 0, 0.2, 1] as const;

// ─── SPRING PRESETS ──────────────────────────────────────────────────────────
export const SPRING_SNAPPY: Transition = {
  type: 'spring',
  stiffness: 450,
  damping: 35,
  mass: 0.8,
};

export const SPRING_SMOOTH: Transition = {
  type: 'spring',
  stiffness: 320,
  damping: 28,
  mass: 0.9,
};

export const SPRING_BOUNCY: Transition = {
  type: 'spring',
  stiffness: 400,
  damping: 22,
};

// ─── DURATIONS (Seconds) ─────────────────────────────────────────────────────
export const DURATION_MICRO = 0.18;
export const DURATION_FAST = 0.22;
export const DURATION_NORMAL = 0.32;
export const DURATION_PAGE = 0.36;
export const DURATION_HERO = 0.55;

// ─── BASE TRANSITION PRESETS ─────────────────────────────────────────────────
export const transitionMicro: Transition = {
  duration: DURATION_MICRO,
  ease: EASING_SMOOTH,
};

export const transitionNormal: Transition = {
  duration: DURATION_NORMAL,
  ease: EASING_SMOOTH,
};

export const transitionPage: Transition = {
  duration: DURATION_PAGE,
  ease: EASING_SMOOTH,
};

export const transitionHero: Transition = {
  duration: DURATION_HERO,
  ease: EASING_SMOOTH,
};

// ─── REUSABLE MOTION VARIANTS ────────────────────────────────────────────────

/**
 * Top-level page transition (Fade + Subtle Upward Slide)
 */
export const pageVariants: Variants = {
  initial: {
    opacity: 0,
    y: 10,
  },
  animate: {
    opacity: 1,
    y: 0,
    transition: {
      duration: DURATION_PAGE,
      ease: EASING_SMOOTH,
    },
  },
  exit: {
    opacity: 0,
    y: -8,
    transition: {
      duration: 0.2,
      ease: EASING_IN_OUT,
    },
  },
};

/**
 * Simple fade in/out
 */
export const fadeInVariants: Variants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: transitionNormal,
  },
  exit: {
    opacity: 0,
    transition: transitionMicro,
  },
};

/**
 * Slide up and fade in
 */
export const slideUpVariants: Variants = {
  hidden: {
    opacity: 0,
    y: 16,
  },
  visible: {
    opacity: 1,
    y: 0,
    transition: transitionNormal,
  },
  exit: {
    opacity: 0,
    y: -12,
    transition: transitionMicro,
  },
};

/**
 * Scale in and fade (for Dialogs, Popovers, Badges)
 */
export const scaleInVariants: Variants = {
  hidden: {
    opacity: 0,
    scale: 0.95,
  },
  visible: {
    opacity: 1,
    scale: 1,
    transition: SPRING_SMOOTH,
  },
  exit: {
    opacity: 0,
    scale: 0.96,
    transition: { duration: 0.15, ease: EASING_IN_OUT },
  },
};

/**
 * Modal Backdrop Variant
 */
export const modalBackdropVariants: Variants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: { duration: 0.25, ease: 'easeOut' },
  },
  exit: {
    opacity: 0,
    transition: { duration: 0.2, ease: 'easeIn' },
  },
};

/**
 * Modal Dialog Variant (scale + subtle lift)
 */
export const modalDialogVariants: Variants = {
  hidden: {
    opacity: 0,
    scale: 0.95,
    y: 12,
  },
  visible: {
    opacity: 1,
    scale: 1,
    y: 0,
    transition: SPRING_SMOOTH,
  },
  exit: {
    opacity: 0,
    scale: 0.96,
    y: 8,
    transition: { duration: 0.18, ease: EASING_IN_OUT },
  },
};

/**
 * Toast notification slide-in from bottom-right
 */
export const toastVariants: Variants = {
  hidden: {
    opacity: 0,
    x: 40,
    scale: 0.92,
  },
  visible: {
    opacity: 1,
    x: 0,
    scale: 1,
    transition: SPRING_SNAPPY,
  },
  exit: {
    opacity: 0,
    x: 30,
    scale: 0.94,
    transition: { duration: 0.18, ease: EASING_IN_OUT },
  },
};

/**
 * Stagger container for card grids (search results, hotels, recommendations)
 */
export const staggerContainerVariants: Variants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: {
      staggerChildren: 0.05,
      delayChildren: 0.04,
    },
  },
};

/**
 * Stagger child item (Cards entrance)
 */
export const cardEntranceVariants: Variants = {
  hidden: {
    opacity: 0,
    y: 14,
    scale: 0.98,
  },
  visible: {
    opacity: 1,
    y: 0,
    scale: 1,
    transition: {
      duration: DURATION_NORMAL,
      ease: EASING_SMOOTH,
    },
  },
};

/**
 * Dropdown Popover Variant
 */
export const dropdownVariants: Variants = {
  hidden: {
    opacity: 0,
    scale: 0.96,
    y: -6,
  },
  visible: {
    opacity: 1,
    scale: 1,
    y: 0,
    transition: SPRING_SNAPPY,
  },
  exit: {
    opacity: 0,
    scale: 0.96,
    y: -4,
    transition: { duration: 0.12, ease: EASING_IN_OUT },
  },
};

/**
 * Mobile navigation drawer expansion
 */
export const mobileDrawerVariants: Variants = {
  closed: {
    opacity: 0,
    height: 0,
    transition: {
      duration: 0.22,
      ease: EASING_IN_OUT,
    },
  },
  open: {
    opacity: 1,
    height: 'auto',
    transition: SPRING_SMOOTH,
  },
};

/**
 * Shared active pill indicator transition
 */
export const activePillTransition: Transition = {
  type: 'spring',
  stiffness: 480,
  damping: 38,
};
