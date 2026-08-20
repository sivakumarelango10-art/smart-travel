import React, { useState } from 'react';
import { AlertCircle, Check } from 'lucide-react';
import { Seat } from '../types/api';

interface SeatMapProps {
  flightId: string;
  cabinClass?: string;
  seats: Seat[];
  requiredCount: number;
  selectedSeats: string[];
  onSeatSelect: (seats: string[]) => void;
  onRefreshSeats: () => void;
}

export const SeatMap: React.FC<SeatMapProps> = ({
  cabinClass,
  seats,
  requiredCount,
  selectedSeats = [],
  onSeatSelect,
  onRefreshSeats,
}) => {
  const [conflictError, setConflictError] = useState<string | null>(null);

  // Safely extract seats array whether passed as an array or a SeatMapResponse object
  const safeSeats: Seat[] = Array.isArray(seats)
    ? seats
    : (seats as any)?.seats && Array.isArray((seats as any).seats)
    ? (seats as any).seats
    : [];

  // Group seats by row
  const rowsMap = new Map<number, Seat[]>();
  safeSeats.forEach((seat) => {
    if (seat && typeof seat.row === 'number') {
      if (!rowsMap.has(seat.row)) {
        rowsMap.set(seat.row, []);
      }
      rowsMap.get(seat.row)!.push(seat);
    }
  });

  const sortedRows = Array.from(rowsMap.keys()).sort((a, b) => a - b);

  const handleSeatClick = (seat: Seat) => {
    if (seat.status !== 'AVAILABLE' && !selectedSeats.includes(seat.seatNumber)) {
      return;
    }

    setConflictError(null);
    const isCurrentlySelected = selectedSeats.includes(seat.seatNumber);

    if (isCurrentlySelected) {
      // Deselect
      onSeatSelect(selectedSeats.filter((s) => s !== seat.seatNumber));
    } else {
      // Check count limit
      if (selectedSeats.length >= requiredCount) {
        setConflictError(`You can select up to ${requiredCount} seat(s) for ${requiredCount} passenger(s).`);
        return;
      }
      onSeatSelect([...selectedSeats, seat.seatNumber]);
    }
  };

  return (
    <div className="space-y-6">
      {/* Seat Map Legend */}
      <div className="p-4 sm:p-5 rounded-3xl bg-slate-900/90 border border-slate-800 flex flex-wrap items-center justify-between gap-4 text-xs shadow-xl backdrop-blur-xl">
        <div className="flex flex-wrap items-center gap-4">
          <div className="flex items-center gap-2">
            <div className="w-5 h-5 rounded-lg bg-slate-800 border border-slate-700"></div>
            <span className="text-slate-300 font-semibold">Available</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-5 h-5 rounded-lg bg-sky-500 border border-sky-400 text-white flex items-center justify-center font-bold text-[10px] shadow-sm shadow-sky-500/50">
              <Check className="w-3.5 h-3.5" />
            </div>
            <span className="text-slate-200 font-bold">Selected</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-5 h-5 rounded-lg bg-slate-950 border border-slate-800 opacity-40 cursor-not-allowed"></div>
            <span className="text-slate-500 font-medium">Occupied</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-5 h-5 rounded-lg bg-indigo-950/80 border border-indigo-500/40 flex items-center justify-center text-[10px] text-indigo-300 font-bold">
              ★
            </div>
            <span className="text-slate-300 font-semibold">Extra Legroom</span>
          </div>
        </div>

        <div className="flex items-center gap-2">
          {cabinClass && (
            <span className="hidden sm:inline-block px-3 py-1 rounded-full bg-slate-800 border border-slate-700 text-slate-300 font-bold text-[11px] uppercase">
              Cabin: {cabinClass.replace('_', ' ')}
            </span>
          )}
          <div className="px-3.5 py-1.5 rounded-full bg-sky-950/60 border border-sky-800/40 text-sky-300 font-black text-xs">
            Selected: {selectedSeats.length} / {requiredCount} required
          </div>
        </div>
      </div>

      {conflictError && (
        <div className="p-3.5 rounded-2xl bg-rose-500/15 border border-rose-500/30 text-rose-400 text-xs font-semibold flex items-center gap-2 animate-fade-in">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{conflictError}</span>
        </div>
      )}

      {/* Airplane Fuselage Layout */}
      <div className="max-w-md mx-auto p-6 sm:p-8 rounded-[40px] bg-slate-900 border-2 border-slate-800 shadow-2xl relative">
        {/* Cockpit Front Nose Curve */}
        <div className="w-32 h-14 mx-auto mb-8 rounded-t-full border-t-2 border-x-2 border-slate-700 bg-slate-950/80 flex items-center justify-center shadow-inner">
          <span className="text-[10px] uppercase font-black text-slate-400 tracking-widest">Cockpit</span>
        </div>

        {/* Column Labels Header */}
        <div className="flex items-center justify-between px-3 pb-3 text-xs font-mono font-black text-slate-400 border-b border-slate-800 mb-4">
          <div className="flex gap-2">
            <span className="w-8 text-center">A</span>
            <span className="w-8 text-center">B</span>
            <span className="w-8 text-center">C</span>
          </div>
          <span className="text-[10px] uppercase text-slate-500 font-bold">Aisle</span>
          <div className="flex gap-2">
            <span className="w-8 text-center">D</span>
            <span className="w-8 text-center">E</span>
            <span className="w-8 text-center">F</span>
          </div>
        </div>

        {/* Rows Container */}
        {sortedRows.length === 0 ? (
          <div className="py-12 text-center space-y-3">
            <p className="text-sm font-semibold text-slate-300">Loading physical seat configuration...</p>
            <p className="text-xs text-slate-500">Retrieving real-time seat inventory for this aircraft.</p>
            {onRefreshSeats && (
              <button
                type="button"
                onClick={onRefreshSeats}
                className="mt-2 px-4 py-2 rounded-xl bg-sky-500/20 hover:bg-sky-500/30 text-sky-400 border border-sky-500/30 text-xs font-bold transition inline-flex items-center gap-1.5"
              >
                Refresh Seat Layout
              </button>
            )}
          </div>
        ) : (
          <div className="space-y-3">
            {sortedRows.map((rowNum) => {
              const rowSeats = rowsMap.get(rowNum) || [];
              const leftSeats = rowSeats.filter((s) => ['A', 'B', 'C'].includes(s.column));
              const rightSeats = rowSeats.filter((s) => ['D', 'E', 'F'].includes(s.column));
              const isExitRow = rowSeats.some((s) => s.isEmergencyExit);

              return (
                <div key={rowNum} className="space-y-1">
                  {isExitRow && (
                    <div className="py-1 text-center text-[9px] uppercase tracking-wider font-bold text-amber-400 bg-amber-500/10 border-y border-amber-500/20 rounded-xl my-2">
                      ⚠️ Emergency Exit Row
                    </div>
                  )}

                  <div className="flex items-center justify-between gap-2">
                    {/* Left Side (ABC) */}
                    <div className="flex gap-2">
                      {['A', 'B', 'C'].map((col) => {
                        const seat = leftSeats.find((s) => s.column === col);
                        if (!seat) return <div key={col} className="w-8 h-8"></div>;

                        const isSelected = selectedSeats.includes(seat.seatNumber);
                        const isAvailable = seat.status === 'AVAILABLE';

                        return (
                          <button
                            key={seat.seatNumber}
                            type="button"
                            disabled={!isAvailable && !isSelected}
                            onClick={() => handleSeatClick(seat)}
                            title={`${seat.seatNumber} • ${seat.cabinClass} ${
                              seat.extraLegroom ? '(Extra Legroom)' : ''
                            }`}
                            className={`w-8 h-8 rounded-xl font-mono text-xs font-black transition-all duration-150 flex items-center justify-center ${
                              isSelected
                                ? 'bg-sky-500 border border-sky-400 text-white shadow-lg shadow-sky-500/40 scale-105'
                                : isAvailable
                                ? seat.extraLegroom
                                  ? 'bg-indigo-950/80 hover:bg-indigo-900 border border-indigo-500/40 text-indigo-300 hover:scale-105'
                                  : 'bg-slate-800/90 hover:bg-slate-750 border border-slate-700 text-slate-200 hover:scale-105'
                                : 'bg-slate-950 border border-slate-800 text-slate-600 opacity-40 cursor-not-allowed'
                            }`}
                          >
                            {isSelected ? <Check className="w-3.5 h-3.5" /> : seat.column}
                          </button>
                        );
                      })}
                    </div>

                    {/* Aisle & Row Number */}
                    <span className="w-8 text-center text-xs font-mono font-black text-slate-400">
                      {rowNum}
                    </span>

                    {/* Right Side (DEF) */}
                    <div className="flex gap-2">
                      {['D', 'E', 'F'].map((col) => {
                        const seat = rightSeats.find((s) => s.column === col);
                        if (!seat) return <div key={col} className="w-8 h-8"></div>;

                        const isSelected = selectedSeats.includes(seat.seatNumber);
                        const isAvailable = seat.status === 'AVAILABLE';

                        return (
                          <button
                            key={seat.seatNumber}
                            type="button"
                            disabled={!isAvailable && !isSelected}
                            onClick={() => handleSeatClick(seat)}
                            title={`${seat.seatNumber} • ${seat.cabinClass} ${
                              seat.extraLegroom ? '(Extra Legroom)' : ''
                            }`}
                            className={`w-8 h-8 rounded-xl font-mono text-xs font-black transition-all duration-150 flex items-center justify-center ${
                              isSelected
                                ? 'bg-sky-500 border border-sky-400 text-white shadow-lg shadow-sky-500/40 scale-105'
                                : isAvailable
                                ? seat.extraLegroom
                                  ? 'bg-indigo-950/80 hover:bg-indigo-900 border border-indigo-500/40 text-indigo-300 hover:scale-105'
                                  : 'bg-slate-800/90 hover:bg-slate-750 border border-slate-700 text-slate-200 hover:scale-105'
                                : 'bg-slate-950 border border-slate-800 text-slate-600 opacity-40 cursor-not-allowed'
                            }`}
                          >
                            {isSelected ? <Check className="w-3.5 h-3.5" /> : seat.column}
                          </button>
                        );
                      })}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}

        {/* Fuselage Rear Curve */}
        <div className="w-28 h-8 mx-auto mt-8 rounded-b-3xl border-b-2 border-x-2 border-slate-700 bg-slate-950/80 flex items-center justify-center">
          <span className="text-[9px] uppercase font-black text-slate-400 tracking-widest">Galley & Restrooms</span>
        </div>
      </div>
    </div>
  );
};
