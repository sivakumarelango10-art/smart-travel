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
    <div className="rounded-2xl bg-white border border-slate-200 p-5 shadow-sm space-y-6 sticky top-20">
      {/* Header */}
      <div className="flex items-center justify-between pb-3 border-b border-slate-100">
        <div className="flex items-center gap-2">
          <div className="w-7 h-7 rounded-lg bg-secondary/10 text-secondary flex items-center justify-center">
            <SlidersHorizontal className="w-4 h-4" />
          </div>
          <div>
            <h3 className="font-bold text-primary text-sm">Filter Flights</h3>
          </div>
        </div>
        <button
          type="button"
          onClick={onReset}
          className="text-xs text-secondary hover:underline font-semibold flex items-center gap-1 transition"
        >
          <RotateCcw className="w-3 h-3" />
          Reset
        </button>
      </div>

      {/* Sort By */}
      <div className="space-y-1.5">
        <label className="block text-xs font-bold text-slate-700">Sort By</label>
        <select
          value={sortBy}
          onChange={(e) => onSortChange(e.target.value)}
          className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3 py-2 text-xs font-semibold text-primary focus:outline-none focus:border-secondary transition cursor-pointer"
        >
          <option value="CHEAPEST">Cheapest (Lowest Fare)</option>
          <option value="FASTEST">Fastest (Shortest Duration)</option>
          <option value="EARLIEST_DEPARTURE">Earliest Departure</option>
          <option value="LATEST_DEPARTURE">Latest Departure</option>
        </select>
      </div>

      {/* Stops */}
      <div className="space-y-1.5">
        <label className="block text-xs font-bold text-slate-700">Stops</label>
        <label className="flex items-center justify-between p-2.5 rounded-xl bg-slate-50 border border-slate-200 hover:border-slate-300 cursor-pointer transition">
          <span className="text-xs text-slate-700 font-semibold">Non-Stop Flights Only</span>
          <input
            type="checkbox"
            checked={nonStopOnly}
            onChange={(e) => onNonStopToggle(e.target.checked)}
            className="w-4 h-4 rounded text-secondary focus:ring-0 bg-white border-slate-300 cursor-pointer accent-secondary"
          />
        </label>
      </div>

      {/* Departure Time Slots */}
      <div className="space-y-1.5">
        <label className="block text-xs font-bold text-slate-700">Departure Time</label>
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
                className={`p-2.5 rounded-xl border text-left transition flex flex-col justify-between ${
                  isSelected
                    ? 'bg-secondary/10 border-secondary text-secondary'
                    : 'bg-slate-50 border-slate-200 text-slate-600 hover:bg-slate-100'
                }`}
              >
                <div className="flex items-center justify-between mb-1">
                  <Icon className={`w-3.5 h-3.5 ${isSelected ? 'text-secondary' : 'text-slate-400'}`} />
                  <span className="text-[10px] font-bold">{item.label}</span>
                </div>
                <span className="text-[9px] text-slate-400 font-medium">{item.sub}</span>
              </button>
            );
          })}
        </div>
      </div>

      {/* Max Price Slider */}
      {maxPrice > 0 && (
        <div className="space-y-2">
          <div className="flex items-center justify-between text-xs">
            <span className="font-bold text-slate-700">Max Budget</span>
            <span className="font-black text-primary">₹{priceLimit.toLocaleString('en-IN')}</span>
          </div>
          <input
            type="range"
            min="2000"
            max={maxPrice}
            step="500"
            value={priceLimit}
            onChange={(e) => onPriceChange(Number(e.target.value))}
            className="w-full h-1.5 bg-slate-200 rounded-lg appearance-none cursor-pointer accent-secondary"
          />
        </div>
      )}

      {/* Airlines Filter */}
      {availableAirlines.length > 0 && (
        <div className="space-y-2">
          <label className="block text-xs font-bold text-slate-700">Airlines</label>
          <div className="space-y-1.5 max-h-48 overflow-y-auto pr-1">
            {availableAirlines.map((airline) => {
              const isChecked = selectedAirlines.includes(airline);
              return (
                <label
                  key={airline}
                  className="flex items-center justify-between p-2 rounded-lg bg-slate-50 hover:bg-slate-100 border border-slate-200 cursor-pointer text-xs transition"
                >
                  <span className="font-medium text-slate-700 truncate pr-2">{airline}</span>
                  <input
                    type="checkbox"
                    checked={isChecked}
                    onChange={() => onAirlineToggle(airline)}
                    className="w-3.5 h-3.5 rounded text-secondary focus:ring-0 cursor-pointer accent-secondary"
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
