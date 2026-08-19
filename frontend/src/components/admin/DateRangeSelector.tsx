import React, { useState } from 'react';
import { Calendar, RefreshCw, ChevronDown } from 'lucide-react';
import { AnalyticsPeriod } from '../../types/analytics';

interface DateRangeSelectorProps {
  period: AnalyticsPeriod;
  onPeriodChange: (period: AnalyticsPeriod, from?: string, to?: string) => void;
  onRefresh: () => void;
  loading?: boolean;
  lastUpdated?: Date | null;
}

const PERIOD_LABELS: Record<AnalyticsPeriod, string> = {
  today: 'Today',
  yesterday: 'Yesterday',
  last7days: 'Last 7 Days',
  last30days: 'Last 30 Days',
  thisMonth: 'This Month',
  lastMonth: 'Last Month',
  custom: 'Custom Range',
};

export const DateRangeSelector: React.FC<DateRangeSelectorProps> = ({
  period,
  onPeriodChange,
  onRefresh,
  loading = false,
  lastUpdated,
}) => {
  const [isOpen, setIsOpen] = useState(false);
  const [customFrom, setCustomFrom] = useState('');
  const [customTo, setCustomTo] = useState('');
  const [customError, setCustomError] = useState<string | null>(null);

  const handleSelectPeriod = (p: AnalyticsPeriod) => {
    if (p !== 'custom') {
      onPeriodChange(p);
      setIsOpen(false);
    } else {
      onPeriodChange('custom', customFrom ? `${customFrom}T00:00:00Z` : undefined, customTo ? `${customTo}T23:59:59Z` : undefined);
    }
  };

  const handleApplyCustom = (e: React.FormEvent) => {
    e.preventDefault();
    if (!customFrom || !customTo) {
      setCustomError('Please select both start and end dates');
      return;
    }
    if (customFrom > customTo) {
      setCustomError('Start date must be before end date');
      return;
    }
    setCustomError(null);
    onPeriodChange('custom', `${customFrom}T00:00:00Z`, `${customTo}T23:59:59Z`);
    setIsOpen(false);
  };

  return (
    <div className="flex flex-wrap items-center justify-between gap-3">
      {/* Date Range Selector Dropdown */}
      <div className="relative">
        <button
          type="button"
          onClick={() => setIsOpen(!isOpen)}
          className="flex items-center gap-2 px-3.5 py-2 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg text-sm font-medium text-slate-700 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-700/50 shadow-sm transition-colors"
        >
          <Calendar className="w-4 h-4 text-primary-500" />
          <span>{PERIOD_LABELS[period]}</span>
          <ChevronDown className="w-4 h-4 text-slate-400" />
        </button>

        {isOpen && (
          <div className="absolute left-0 mt-2 w-72 bg-white dark:bg-slate-800 rounded-xl shadow-xl border border-slate-200 dark:border-slate-700 z-50 p-2">
            <div className="space-y-1 mb-2">
              {(Object.keys(PERIOD_LABELS) as AnalyticsPeriod[]).map((p) => (
                <button
                  key={p}
                  type="button"
                  onClick={() => handleSelectPeriod(p)}
                  className={`w-full text-left px-3 py-1.5 text-sm rounded-md transition-colors ${
                    period === p
                      ? 'bg-primary-50 dark:bg-primary-950/50 text-primary-600 dark:text-primary-400 font-semibold'
                      : 'text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-700'
                  }`}
                >
                  {PERIOD_LABELS[p]}
                </button>
              ))}
            </div>

            {/* Custom Range Input fields */}
            {period === 'custom' && (
              <form onSubmit={handleApplyCustom} className="pt-2 border-t border-slate-200 dark:border-slate-700 space-y-2">
                <div>
                  <label className="block text-[11px] font-semibold text-slate-500 dark:text-slate-400 uppercase mb-1">
                    From
                  </label>
                  <input
                    type="date"
                    value={customFrom}
                    onChange={(e) => setCustomFrom(e.target.value)}
                    className="w-full text-xs px-2.5 py-1.5 border border-slate-200 dark:border-slate-700 rounded-md bg-slate-50 dark:bg-slate-900 text-slate-900 dark:text-white"
                  />
                </div>
                <div>
                  <label className="block text-[11px] font-semibold text-slate-500 dark:text-slate-400 uppercase mb-1">
                    To
                  </label>
                  <input
                    type="date"
                    value={customTo}
                    onChange={(e) => setCustomTo(e.target.value)}
                    className="w-full text-xs px-2.5 py-1.5 border border-slate-200 dark:border-slate-700 rounded-md bg-slate-50 dark:bg-slate-900 text-slate-900 dark:text-white"
                  />
                </div>
                {customError && (
                  <p className="text-[11px] text-rose-500">{customError}</p>
                )}
                <button
                  type="submit"
                  className="w-full py-1.5 bg-primary-600 text-white text-xs font-semibold rounded-md hover:bg-primary-700 transition-colors"
                >
                  Apply Range
                </button>
              </form>
            )}
          </div>
        )}
      </div>

      {/* Refresh Button & Last Updated Timestamp */}
      <div className="flex items-center gap-3">
        {lastUpdated && (
          <span className="text-xs text-slate-400 dark:text-slate-500">
            Updated {lastUpdated.toLocaleTimeString()}
          </span>
        )}
        <button
          type="button"
          onClick={onRefresh}
          disabled={loading}
          className="flex items-center gap-1.5 px-3 py-2 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg text-sm font-medium text-slate-700 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-700/50 shadow-sm transition-colors disabled:opacity-50"
          title="Refresh dashboard data"
        >
          <RefreshCw className={`w-4 h-4 text-slate-500 ${loading ? 'animate-spin text-primary-500' : ''}`} />
          <span className="hidden sm:inline">Refresh</span>
        </button>
      </div>
    </div>
  );
};
