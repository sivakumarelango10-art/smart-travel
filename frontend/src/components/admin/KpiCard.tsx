import React from 'react';
import { LucideIcon } from 'lucide-react';

interface KpiCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  icon: LucideIcon;
  iconColor?: string;
  iconBg?: string;
  trend?: {
    value: string;
    isPositive?: boolean;
    label?: string;
  };
  className?: string;
  loading?: boolean;
}

export const KpiCard: React.FC<KpiCardProps> = ({
  title,
  value,
  subtitle,
  icon: Icon,
  iconColor = 'text-amber-400',
  iconBg = 'bg-amber-400/10 border border-amber-400/20',
  trend,
  className = '',
  loading = false,
}) => {
  if (loading) {
    return (
      <div className={`bg-[#14161F] rounded-2xl border border-white/10 p-5 animate-pulse ${className}`}>
        <div className="flex items-center justify-between mb-3">
          <div className="h-4 bg-[#181A22] rounded w-24" />
          <div className="w-10 h-10 bg-[#181A22] rounded-lg" />
        </div>
        <div className="h-7 bg-[#181A22] rounded w-32 mb-2" />
        <div className="h-3 bg-[#181A22] rounded w-20" />
      </div>
    );
  }

  return (
    <div
      className={`bg-[#14161F] rounded-2xl border border-white/10 p-5 shadow-xl hover:border-amber-500/30 transition-all duration-300 ${className}`}
    >
      <div className="flex items-start justify-between">
        <div className="flex-1">
          <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1">
            {title}
          </p>
          <p className="text-2xl font-black text-white tracking-tight">
            {value}
          </p>
          {subtitle && (
            <p className="text-xs text-slate-400 mt-1">
              {subtitle}
            </p>
          )}
          {trend && (
            <div className="flex items-center gap-1.5 mt-2">
              <span
                className={`text-xs font-bold px-2 py-0.5 rounded-full ${
                  trend.isPositive
                    ? 'text-emerald-400 bg-emerald-500/10 border border-emerald-500/20 shadow-glow-emerald'
                    : 'text-rose-400 bg-rose-500/10 border border-rose-500/20'
                }`}
              >
                {trend.value}
              </span>
              {trend.label && (
                <span className="text-xs text-slate-400">
                  {trend.label}
                </span>
              )}
            </div>
          )}
        </div>
        <div className={`p-2.5 rounded-xl shrink-0 ${iconBg}`}>
          <Icon className={`w-5 h-5 ${iconColor}`} />
        </div>
      </div>
    </div>
  );
};
