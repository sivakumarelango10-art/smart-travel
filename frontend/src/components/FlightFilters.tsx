import React from 'react';
import { SlidersHorizontal, Sun, Sunset, Moon, Sunrise } from 'lucide-react';

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
    <div className="rounded-2xl bg-slate-900/90 border border-slate-800 p-5 shadow-xl space-y-6">
      <div className="flex items-center justify-between pb-3 border-b border-slate-800">
        <div className="flex items-center gap-2">
          <SlidersHorizontal className="w-4 h-4 text-sky-400" />
          <h3 className="font-bold text-white text-sm">Filters & Sort</h3>
        </div>
        <button
          type="button"
          onClick={onReset}
          className="text-xs text-sky-400 hover:text-sky-300 font-medium"
        >
          Reset All
        </button>
      </div>

      {/* Sort By */}
      <div className="space-y-2">
        <label className="block text-xs font-semibold text-slate-300">Sort Flights By</label>
        <select
          value={sortBy}
          onChange={(e) => onSortChange(e.target.value)}
          className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs font-medium text-white focus:outline-none focus:border-sky-500 transition"
        >
          <option value="CHEAPEST">Cheapest (Lowest Fare First)</option>
          <option value="FASTEST">Fastest (Shortest Duration)</option>
          <option value="EARLIEST_DEPARTURE">Earliest Departure</option>
          <option value="LATEST_DEPARTURE">Latest Departure</option>
        </select>
      </div>

      {/* Stops */}
      <div className="space-y-2">
        <label className="block text-xs font-semibold text-slate-300">Stops</label>
        <label className="flex items-center gap-2.5 p-2 rounded-xl bg-slate-950 border border-slate-800 cursor-pointer hover:border-slate-700 transition">
          <input
            type="checkbox"
            checked={nonStopOnly}
            onChange={(e) => onNonStopToggle(e.target.checked)}
            className="w-4 h-4 rounded text-sky-500 focus:ring-0 bg-slate-800 border-slate-700 cursor-pointer"
          />
          <span className="text-xs text-slate-300 font-medium">Non-Stop Flights Only</span>
        </label>
      </div>

      {/* Departure Time Slots */}
      <div className="space-y-2">
        <label className="block text-xs font-semibold text-slate-300">Departure Time</label>
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
                className={`p-2.5 rounded-xl border text-left transition flex flex-col gap-1 ${
                  isSelected
                    ? 'bg-sky-500/10 border-sky-500/40 text-white'
                    : 'bg-slate-950 border-slate-800 text-slate-400 hover:border-slate-700'
                }`}
              >
                <div className="flex items-center gap-1.5">
                  <Icon className={`w-3.5 h-3.5 ${isSelected ? 'text-sky-400' : 'text-slate-500'}`} />
                  <span className="text-xs font-semibold">{item.label}</span>
                </div>
                <span className="text-[10px] text-slate-500">{item.sub}</span>
              </button>
            );
          })}
        </div>
      </div>

      {/* Price Slider */}
      <div className="space-y-2">
        <div className="flex justify-between items-baseline">
          <label className="text-xs font-semibold text-slate-300">Max Price</label>
          <span className="text-xs font-bold text-sky-400">₹{priceLimit.toLocaleString('en-IN')}</span>
        </div>
        <input
          type="range"
          min="1000"
          max={maxPrice > 1000 ? maxPrice : 50000}
          step="500"
          value={priceLimit}
          onChange={(e) => onPriceChange(Number(e.target.value))}
          className="w-full h-1.5 bg-slate-800 rounded-lg appearance-none cursor-pointer accent-sky-500"
        />
        <div className="flex justify-between text-[10px] text-slate-500">
          <span>₹1,000</span>
          <span>₹{(maxPrice > 1000 ? maxPrice : 50000).toLocaleString('en-IN')}</span>
        </div>
      </div>

      {/* Airlines */}
      {availableAirlines.length > 0 && (
        <div className="space-y-2">
          <label className="block text-xs font-semibold text-slate-300">Airlines</label>
          <div className="space-y-1.5 max-h-48 overflow-y-auto">
            {availableAirlines.map((airline) => {
              const isChecked = selectedAirlines.includes(airline);
              return (
                <label
                  key={airline}
                  className="flex items-center justify-between p-2 rounded-xl bg-slate-950 border border-slate-800 cursor-pointer hover:border-slate-700 transition"
                >
                  <span className="text-xs text-slate-300">{airline}</span>
                  <input
                    type="checkbox"
                    checked={isChecked}
                    onChange={() => onAirlineToggle(airline)}
                    className="w-4 h-4 rounded text-sky-500 focus:ring-0 bg-slate-800 border-slate-700 cursor-pointer"
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
