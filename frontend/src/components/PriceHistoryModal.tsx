import React, { useState, useEffect } from 'react';
import { TrendingUp, X, Calendar, Activity } from 'lucide-react';
import { CabinClass, FlightPriceHistory } from '../types/api';
import { pricingService } from '../services/pricingService';

interface PriceHistoryModalProps {
  flightId: string;
  flightNumber: string;
  cabinClass?: CabinClass;
  onClose: () => void;
}

export const PriceHistoryModal: React.FC<PriceHistoryModalProps> = ({
  flightId,
  flightNumber,
  cabinClass = 'ECONOMY',
  onClose,
}) => {
  const [history, setHistory] = useState<FlightPriceHistory[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    pricingService
      .getPriceHistory(flightId, cabinClass)
      .then((data) => {
        // Sort ascending for chart
        const sorted = [...data].reverse();
        setHistory(sorted);
      })
      .catch((err) => {
        console.error('Failed to load price history', err);
      })
      .finally(() => {
        setLoading(false);
      });
  }, [flightId, cabinClass]);

  // Compute SVG chart coordinates
  const prices = history.map((h) => h.finalPrice);
  const minPrice = prices.length ? Math.min(...prices) * 0.9 : 3000;
  const maxPrice = prices.length ? Math.max(...prices) * 1.1 : 8000;
  const range = maxPrice - minPrice || 1;

  const chartWidth = 500;
  const chartHeight = 180;
  const padding = 30;

  const points = history.map((h, index) => {
    const x = padding + (index / Math.max(1, history.length - 1)) * (chartWidth - padding * 2);
    const y = chartHeight - padding - ((h.finalPrice - minPrice) / range) * (chartHeight - padding * 2);
    return { x, y, price: h.finalPrice, date: h.capturedAt, reason: h.reason };
  });

  const pathD = points.length
    ? `M ${points.map((p) => `${p.x},${p.y}`).join(' L ')}`
    : '';

  return (
    <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-xl w-full p-6 shadow-2xl animate-in fade-in zoom-in duration-150">
        <div className="flex items-center justify-between pb-4 border-b border-slate-800">
          <div className="flex items-center gap-2">
            <div className="p-2 bg-indigo-500/10 border border-indigo-500/20 rounded-xl">
              <TrendingUp className="w-5 h-5 text-indigo-400" />
            </div>
            <div>
              <h3 className="text-base font-bold text-white">Price History & Trends</h3>
              <p className="text-xs text-slate-400">{flightNumber} · {cabinClass}</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {loading ? (
          <div className="py-16 text-center text-slate-500">
            <div className="w-6 h-6 border-2 border-cyan-500 border-t-transparent rounded-full animate-spin mx-auto mb-2" />
            Loading price trend data...
          </div>
        ) : history.length === 0 ? (
          <div className="py-12 text-center text-slate-400">
            <Activity className="w-10 h-10 text-slate-700 mx-auto mb-2" />
            <p className="font-medium text-slate-300">No historical price snapshots recorded yet</p>
            <p className="text-xs text-slate-500 mt-1">Price fluctuations are captured as demand shifts.</p>
          </div>
        ) : (
          <div className="mt-4 space-y-4">
            {/* SVG Trend Chart */}
            <div className="p-4 bg-slate-950/60 rounded-xl border border-slate-800">
              <div className="flex items-center justify-between text-xs text-slate-400 mb-2">
                <span>Fare Trend</span>
                <span className="font-semibold text-cyan-400">
                  Latest: ₹{history[history.length - 1]?.finalPrice.toLocaleString()}
                </span>
              </div>

              <svg viewBox={`0 0 ${chartWidth} ${chartHeight}`} className="w-full h-44 overflow-visible">
                {/* Horizontal Grid lines */}
                <line
                  x1={padding}
                  y1={padding}
                  x2={chartWidth - padding}
                  y2={padding}
                  stroke="#334155"
                  strokeDasharray="4"
                />
                <line
                  x1={padding}
                  y1={chartHeight - padding}
                  x2={chartWidth - padding}
                  y2={chartHeight - padding}
                  stroke="#334155"
                  strokeDasharray="4"
                />

                {/* Y-axis labels */}
                <text x={padding - 5} y={padding + 4} fill="#64748b" fontSize="10" textAnchor="end">
                  ₹{Math.round(maxPrice)}
                </text>
                <text x={padding - 5} y={chartHeight - padding + 4} fill="#64748b" fontSize="10" textAnchor="end">
                  ₹{Math.round(minPrice)}
                </text>

                {/* Trend Line */}
                {pathD && (
                  <>
                    <path
                      d={pathD}
                      fill="none"
                      stroke="url(#trendGradient)"
                      strokeWidth="2.5"
                      strokeLinecap="round"
                    />
                    <defs>
                      <linearGradient id="trendGradient" x1="0%" y1="0%" x2="100%" y2="0%">
                        <stop offset="0%" stopColor="#06b6d4" />
                        <stop offset="100%" stopColor="#6366f1" />
                      </linearGradient>
                    </defs>
                  </>
                )}

                {/* Data Points */}
                {points.map((p, i) => (
                  <circle
                    key={i}
                    cx={p.x}
                    cy={p.y}
                    r="4"
                    className="fill-cyan-400 stroke-slate-900 stroke-2 hover:scale-125 transition-transform cursor-pointer"
                  >
                    <title>₹{p.price.toLocaleString()} ({p.reason || 'Normal'})</title>
                  </circle>
                ))}
              </svg>
            </div>

            {/* Snapshot Log Table */}
            <div className="max-h-48 overflow-y-auto space-y-1.5 pr-1">
              {history.map((h, i) => (
                <div
                  key={h.id || i}
                  className="flex items-center justify-between p-2.5 bg-slate-800/30 rounded-lg text-xs border border-slate-800/60 hover:bg-slate-800/60 transition-colors"
                >
                  <div className="flex items-center gap-2">
                    <Calendar className="w-3.5 h-3.5 text-slate-500" />
                    <span className="text-slate-300">
                      {new Date(h.capturedAt).toLocaleDateString([], {
                        month: 'short',
                        day: 'numeric',
                      })}{' '}
                      {new Date(h.capturedAt).toLocaleTimeString([], {
                        hour: '2-digit',
                        minute: '2-digit',
                      })}
                    </span>
                    <span className="text-[11px] text-slate-500">· {h.reason || 'Standard'}</span>
                  </div>
                  <span className="font-bold text-white">₹{h.finalPrice.toLocaleString()}</span>
                </div>
              ))}
            </div>
          </div>
        )}

        <div className="mt-4 pt-3 border-t border-slate-800 flex justify-end">
          <button
            onClick={onClose}
            className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-sm font-semibold text-white rounded-xl transition-colors"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
};
