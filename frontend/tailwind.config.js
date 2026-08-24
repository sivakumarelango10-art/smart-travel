/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // Luxury Obsidian & Gold Palette (Zero Blue)
        primary: {
          DEFAULT: '#0B0C10', // Deep Obsidian Black
          hover: '#14161F',
          light: '#1F222E',
          dark: '#050608',
          50: '#F8F9FA',
          100: '#F1F3F5',
          200: '#E9ECEF',
          300: '#DEE2E6',
          400: '#9499A6',
          500: '#636875',
          600: '#434752',
          700: '#2C2F38',
          800: '#1A1C23',
          900: '#0B0C10',
          950: '#050608',
        },
        secondary: {
          DEFAULT: '#F59E0B', // Radiant Gold / Amber
          hover: '#D97706',
          light: '#FBBF24',
          50: '#FFFBEB',
          100: '#FEF3C7',
          200: '#FDE68A',
          300: '#FCD34D',
          400: '#FBBF24',
          500: '#F59E0B',
          600: '#D97706',
          700: '#B45309',
          800: '#92400E',
          900: '#78350F',
        },
        accent: {
          DEFAULT: '#FF6B35', // Warm Sunset Coral / Terracotta Accent
          hover: '#EA580C',
          light: '#FB923C',
          50: '#FFF7ED',
          100: '#FFEDD5',
          200: '#FED7AA',
          300: '#FDBA74',
          400: '#FB923C',
          500: '#FF6B35',
          600: '#EA580C',
          700: '#C2410C',
          800: '#9A3412',
          900: '#7C2D12',
        },
        emerald: {
          DEFAULT: '#10B981', // Jade / Emerald for Live Status & Confirmations
          hover: '#059669',
          light: '#34D399',
          50: '#ECFDF5',
          100: '#D1FAE5',
          200: '#A7F3D0',
          300: '#6EE7B7',
          400: '#34D399',
          500: '#10B981',
          600: '#059669',
          700: '#047857',
        },
        neutral: {
          DEFAULT: '#F8F9FA',
          surface: '#FFFFFF',
          muted: '#F1F3F5',
          border: '#E9ECEF',
        },
        surface: {
          light: '#FFFFFF',
          subtle: '#F8F9FA',
          card: '#FFFFFF',
          dark: '#0B0C10',
          'dark-card': '#14161F',
          obsidian: '#12131A',
        },
        brand: {
          obsidian: '#0B0C10',
          gold: '#F59E0B',
          coral: '#FF6B35',
          emerald: '#10B981',
          slate: '#1A1C24',
        }
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'BlinkMacSystemFont', 'Segoe UI', 'Roboto', 'sans-serif'],
        display: ['Inter', 'system-ui', 'sans-serif'],
      },
      borderRadius: {
        'card': '16px',
        'panel': '24px',
        'button': '10px',
        'input': '10px',
      },
      boxShadow: {
        'subtle': '0 1px 3px 0 rgba(11, 12, 16, 0.06), 0 1px 2px -1px rgba(11, 12, 16, 0.04)',
        'card': '0 4px 20px -2px rgba(11, 12, 16, 0.08), 0 2px 6px -2px rgba(11, 12, 16, 0.04)',
        'card-hover': '0 14px 32px -4px rgba(11, 12, 16, 0.14), 0 4px 12px -2px rgba(11, 12, 16, 0.08)',
        'dropdown': '0 12px 36px -5px rgba(11, 12, 16, 0.22)',
        'glow-gold': '0 0 24px -4px rgba(245, 158, 11, 0.45)',
        'glow-coral': '0 0 24px -4px rgba(255, 107, 53, 0.45)',
        'glow-emerald': '0 0 24px -4px rgba(16, 185, 129, 0.45)',
      },
      animation: {
        'pulse-subtle': 'pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'fade-in': 'fadeIn 0.2s ease-in-out',
        'slide-up': 'slideUp 0.25s cubic-bezier(0.16, 1, 0.3, 1)',
        'scale-in': 'scaleIn 0.2s cubic-bezier(0.16, 1, 0.3, 1)',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0', transform: 'translateY(4px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        slideUp: {
          '0%': { opacity: '0', transform: 'translateY(16px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        scaleIn: {
          '0%': { opacity: '0', transform: 'scale(0.97)' },
          '100%': { opacity: '1', transform: 'scale(1)' },
        }
      }
    },
  },
  plugins: [],
}
