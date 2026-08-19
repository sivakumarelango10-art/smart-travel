import React, { useState } from 'react';
import { AlertCircle } from 'lucide-react';
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
  seats,
  requiredCount,
  selectedSeats,
  onSeatSelect,
}) => {
  const [conflictError, setConflictError] = useState<string | null>(null);

  // Group seats by row
  const rowsMap = new Map<number, Seat[]>();
  seats.forEach((seat) => {
    if (!rowsMap.has(seat.row)) {
      rowsMap.set(seat.row, []);
    }
    rowsMap.get(seat.row)!.push(seat);
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
      <div className="p-4 rounded-xl bg-slate-950/60 border border-slate-800 flex flex-wrap items-center justify-between gap-4 text-xs">
        <div className="flex flex-wrap items-center gap-4">
          <div className="flex items-center gap-2">
            <div className="w-5 h-5 rounded-md bg-slate-800 border border-slate-700"></div>
            <span className="text-slate-300">Available</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-5 h-5 rounded-md bg-sky-500 border border-sky-400 text-white flex items-center justify-center font-bold text-[10px]">
              ✓
            </div>
            <span className="text-slate-300">Selected</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-5 h-5 rounded-md bg-slate-900/60 border border-slate-800 opacity-40 cursor-not-allowed"></div>
            <span className="text-slate-500">Booked / Occupied</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-5 h-5 rounded-md bg-indigo-950/60 border border-indigo-500/30 flex items-center justify-center text-[10px] text-indigo-400 font-bold">
              +
            </div>
            <span className="text-slate-300">Extra Legroom</span>
          </div>
        </div>

        <div className="text-sky-400 font-semibold">
          Selected: {selectedSeats.length} / {requiredCount} required
        </div>
      </div>

      {conflictError && (
        <div className="p-3 rounded-lg bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs font-medium flex items-center gap-2">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{conflictError}</span>
        </div>
      )}

      {/* Airplane Fuselage Layout */}
      <div className="max-w-md mx-auto p-6 rounded-3xl bg-slate-900 border-2 border-slate-800 shadow-2xl relative">
        {/* Cockpit Front Curve */}
        <div className="w-24 h-12 mx-auto mb-6 rounded-t-full border-t-2 border-x-2 border-slate-700 bg-slate-800/40 flex items-center justify-center">
          <span className="text-[10px] uppercase font-bold text-slate-500 tracking-wider">Cockpit</span>
        </div>

        {/* Rows Container */}
        <div className="space-y-3">
          {sortedRows.map((rowNum) => {
            const rowSeats = rowsMap.get(rowNum) || [];
            rowSeats.sort((a, b) => a.column.localeCompare(b.column));

            const leftSeats = rowSeats.filter((s) => ['A', 'B', 'C'].includes(s.column));
            const rightSeats = rowSeats.filter((s) => ['D', 'E', 'F'].includes(s.column));
            const isExitRow = rowSeats.some((s) => s.isEmergencyExit);

            return (
              <div key={rowNum} className="space-y-1">
                {isExitRow && (
                  <div className="py-1 text-center text-[9px] uppercase tracking-wider font-bold text-amber-400 bg-amber-500/10 border-y border-amber-500/20 rounded">
                    ⚠️ Emergency Exit Row
                  </div>
                )}

                <div className="flex items-center justify-between gap-2">
                  {/* Left Side (ABC) */}
                  <div className="flex items-center gap-1.5">
                    {leftSeats.map((seat) => {
                      const isSelected = selectedSeats.includes(seat.seatNumber);
                      const isAvail = seat.status === 'AVAILABLE';

                      return (
                        <button
                          key={seat.seatNumber}
                          type="button"
                          disabled={!isAvail && !isSelected}
                          onClick={() => handleSeatClick(seat)}
                          className={`w-9 h-10 rounded-lg text-xs font-semibold flex flex-col items-center justify-center transition-all ${
                            isSelected
                              ? 'bg-gradient-to-tr from-sky-500 to-indigo-600 text-white shadow-lg shadow-sky-500/30 scale-105 border border-sky-400'
                              : isAvail
                              ? seat.extraLegroom
                                ? 'bg-indigo-950/40 hover:bg-indigo-900/60 border border-indigo-500/30 text-indigo-200'
                                : 'bg-slate-800 hover:bg-slate-700 border border-slate-700 text-slate-200'
                              : 'bg-slate-950 border border-slate-900 text-slate-600 opacity-40 cursor-not-allowed'
                          }`}
                          title={`${seat.seatNumber} (${seat.cabinClass}) - ${seat.status}`}
                        >
                          <span>{seat.column}</span>
                          {seat.extraLegroom && !isSelected && (
                            <span className="text-[8px] text-indigo-400 font-bold">+₹</span>
                          )}
                        </button>
                      );
                    })}
                  </div>

                  {/* Aisle Row Number */}
                  <div className="w-6 text-center font-mono text-[11px] font-bold text-slate-500">
                    {rowNum}
                  </div>

                  {/* Right Side (DEF) */}
                  <div className="flex items-center gap-1.5">
                    {rightSeats.map((seat) => {
                      const isSelected = selectedSeats.includes(seat.seatNumber);
                      const isAvail = seat.status === 'AVAILABLE';

                      return (
                        <button
                          key={seat.seatNumber}
                          type="button"
                          disabled={!isAvail && !isSelected}
                          onClick={() => handleSeatClick(seat)}
                          className={`w-9 h-10 rounded-lg text-xs font-semibold flex flex-col items-center justify-center transition-all ${
                            isSelected
                              ? 'bg-gradient-to-tr from-sky-500 to-indigo-600 text-white shadow-lg shadow-sky-500/30 scale-105 border border-sky-400'
                              : isAvail
                              ? seat.extraLegroom
                                ? 'bg-indigo-950/40 hover:bg-indigo-900/60 border border-indigo-500/30 text-indigo-200'
                                : 'bg-slate-800 hover:bg-slate-700 border border-slate-700 text-slate-200'
                              : 'bg-slate-950 border border-slate-900 text-slate-600 opacity-40 cursor-not-allowed'
                          }`}
                          title={`${seat.seatNumber} (${seat.cabinClass}) - ${seat.status}`}
                        >
                          <span>{seat.column}</span>
                          {seat.extraLegroom && !isSelected && (
                            <span className="text-[8px] text-indigo-400 font-bold">+₹</span>
                          )}
                        </button>
                      );
                    })}
                  </div>
                </div>
              </div>
            );
          })}
        </div>

        {/* Galley & Rear Tail */}
        <div className="mt-8 pt-4 border-t border-slate-800 text-center text-[10px] uppercase font-bold text-slate-500 tracking-wider">
          Galley & Lavatories
        </div>
      </div>
    </div>
  );
};
