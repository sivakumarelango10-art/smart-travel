import React from 'react';
import { SlidersHorizontal, Sun, Sunset, Moon, Sunrise, RotateCcw } from 'lucide-react';

interface FlightFiltersProps {
  availableAirlines: string[];
  selectedAirlines: string[];
  onAirlineToggle: (airline: string) => void;
  maxPrice: number;
  priceLimit: number;
  onPriceChange: (price: number) => void;
  nonStopOnly: boolean;
  onNonStopToggle: (val: boolean) => void;
  timeWindow?: string;
  onTimeWindowChange: (window: string) => void;
  sortBy: string;
  onSortChange: (sort: string) => void;
  onReset: () => void;
}

export const FlightFilters: React.FC<FlightFiltersProps> = ({
  availableAirlines,
  selectedAirlines,
  onAirlineToggle,
  maxPrice,
  priceLimit,
  onPriceChange,
  nonStopOnly,
  onNonStopToggle,
  timeWindow = 'ALL',
  onTimeWindowChange,
  sortBy,
  onSortChange,
  onReset,
}) => {
  return (
    <div className="rounded-3xl bg-slate-900/90 border border-slate-800 p-6 shadow-2xl space-y-6 sticky top-24 backdrop-blur-xl">
      <div className="flex items-center justify-between pb-4 border-b border-slate-800">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-xl bg-sky-500/10 text-sky-400 border border-sky-500/20 flex items-center justify-center">
            <SlidersHorizontal className="w-4 h-4" />
          </div>
          <div>
            <h3 className="font-extrabold text-white text-sm">Filters & Sort</h3>
            <p className="text-[10px] text-slate-400">Refine flight results</p>
          </div>
        </div>
        <button
          type="button"
          onClick={onReset}
          className="text-xs text-sky-400 hover:text-sky-300 font-bold flex items-center gap-1 hover:underline transition"
        >
          <RotateCcw className="w-3 h-3" />
          Reset All
        </button>
      </div>

      {/* Sort By */}
      <div className="space-y-2">
        <label className="block text-xs font-bold text-slate-200">Sort Flights By</label>
        <select
          value={sortBy}
          onChange={(e) => onSortChange(e.target.value)}
          className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2.5 text-xs font-semibold text-white focus:outline-none focus:border-sky-500 transition cursor-pointer"
        >
          <option value="CHEAPEST">Cheapest (Lowest Fare First)</option>
          <option value="FASTEST">Fastest (Shortest Duration)</option>
          <option value="EARLIEST_DEPARTURE">Earliest Departure</option>
          <option value="LATEST_DEPARTURE">Latest Departure</option>
        </select>
      </div>

      {/* Stops */}
      <div className="space-y-2">
        <label className="block text-xs font-bold text-slate-200">Stops</label>
        <label className="flex items-center justify-between p-3 rounded-2xl bg-slate-950 border border-slate-800 hover:border-slate-700 cursor-pointer transition group">
          <span className="text-xs text-slate-300 font-semibold group-hover:text-white">Non-Stop Flights Only</span>
          <input
            type="checkbox"
            checked={nonStopOnly}
            onChange={(e) => onNonStopToggle(e.target.checked)}
            className="w-4 h-4 rounded text-sky-500 focus:ring-0 bg-slate-800 border-slate-700 cursor-pointer accent-sky-500"
          />
        </label>
      </div>

      {/* Departure Time Slots */}
      <div className="space-y-2">
        <label className="block text-xs font-bold text-slate-200">Departure Time</label>
        <div className="grid grid-cols-2 gap-2">
          {[
            { id: 'MORNING', label: 'Morning', sub: '6 AM - 12 PM', icon: Sunrise },
            { id: 'AFTERNOON', label: 'Afternoon', sub: '12 PM - 6 PM', icon: Sun },
            { id: 'EVENING', label: 'Evening', sub: '6 PM - 12 AM', icon: Sunset },
            { id: 'NIGHT', label: 'Night', sub: '12 AM - 6 AM', icon: Moon },
          ].map((item) => {
            const Icon = item.icon;
            const isSelected = timeWindow === item.id;

            return (
              <button
                key={item.id}
                type="button"
                onClick={() => onTimeWindowChange(isSelected ? 'ALL' : item.id)}
                className={`p-3 rounded-2xl border text-left transition flex flex-col gap-1 ${
                  isSelected
                    ? 'bg-sky-500/15 border-sky-500/40 text-white shadow-md shadow-sky-500/10'
                    : 'bg-slate-950 border-slate-800/80 text-slate-400 hover:border-slate-700'
                }`}
              >
                <div className="flex items-center gap-1.5">
                  <Icon className={`w-3.5 h-3.5 ${isSelected ? 'text-sky-400' : 'text-slate-500'}`} />
                  <span className="text-xs font-bold">{item.label}</span>
                </div>
                <span className="text-[10px] text-slate-500 font-medium">{item.sub}</span>
              </button>
            );
          })}
        </div>
      </div>

      {/* Price Slider */}
      <div className="space-y-2.5">
        <div className="flex justify-between items-baseline">
          <label className="text-xs font-bold text-slate-200">Max Budget</label>
          <span className="text-xs font-extrabold text-sky-400 bg-sky-950/60 px-2 py-0.5 rounded border border-sky-800/40 font-mono">
            ₹{priceLimit.toLocaleString('en-IN')}
          </span>
        </div>
        <input
          type="range"
          min="1000"
          max={maxPrice > 1000 ? maxPrice : 50000}
          step="500"
          value={priceLimit}
          onChange={(e) => onPriceChange(Number(e.target.value))}
          className="w-full h-2 bg-slate-800 rounded-lg appearance-none cursor-pointer accent-sky-500"
        />
        <div className="flex justify-between text-[10px] text-slate-500 font-mono font-bold">
          <span>₹1,000</span>
          <span>₹{(maxPrice > 1000 ? maxPrice : 50000).toLocaleString('en-IN')}</span>
        </div>
      </div>

      {/* Airlines */}
      {availableAirlines.length > 0 && (
        <div className="space-y-2 pt-2 border-t border-slate-800">
          <label className="block text-xs font-bold text-slate-200">Airlines ({availableAirlines.length})</label>
          <div className="space-y-1.5 max-h-48 overflow-y-auto pr-1">
            {availableAirlines.map((airline) => {
              const isChecked = selectedAirlines.includes(airline);
              return (
                <label
                  key={airline}
                  className={`flex items-center justify-between p-2.5 rounded-xl border cursor-pointer transition ${
                    isChecked
                      ? 'bg-sky-500/10 border-sky-500/30 text-white'
                      : 'bg-slate-950 border-slate-800/80 text-slate-300 hover:border-slate-700'
                  }`}
                >
                  <span className="text-xs font-semibold">{airline}</span>
                  <input
                    type="checkbox"
                    checked={isChecked}
                    onChange={() => onAirlineToggle(airline)}
                    className="w-4 h-4 rounded text-sky-500 focus:ring-0 bg-slate-800 border-slate-700 cursor-pointer accent-sky-500"
                  />
                </label>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
};

