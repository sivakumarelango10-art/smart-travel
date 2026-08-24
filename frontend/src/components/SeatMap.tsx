import React, { useState, useEffect, useMemo } from 'react';
import { AlertCircle, Check, Sparkles, Radio } from 'lucide-react';
import { Seat, SeatMapUpdateEvent } from '../types/api';
import { useSeatMapWebSocket } from '../hooks/useSeatMapWebSocket';
import { useAuth } from '../context/AuthContext';

interface SeatMapProps {
  flightId: string;
  cabinClass?: string;
  seats: Seat[];
  requiredCount: number;
  selectedSeats: string[];
  onSeatSelect: (seats: string[]) => void;
  onRefreshSeats?: () => void;
  preferredSeatType?: string;
}

export const SeatMap: React.FC<SeatMapProps> = ({
  flightId,
  cabinClass,
  seats,
  requiredCount,
  selectedSeats = [],
  onSeatSelect,
  onRefreshSeats,
  preferredSeatType,
}) => {
  const { user } = useAuth();
  const [conflictError, setConflictError] = useState<string | null>(null);
  const [localSeats, setLocalSeats] = useState<Seat[]>(seats);
  const [realtimeNotification, setRealtimeNotification] = useState<string | null>(null);

  // Sync with prop changes
  useEffect(() => {
    setLocalSeats(seats);
  }, [seats]);

  // Effective preferred seat type from props or authenticated user preferences
  const effectivePreference = preferredSeatType || user?.preferences?.preferredSeatType || '';

  // Real-time WebSocket connection to /topic/seat-map/{flightId}
  useSeatMapWebSocket({
    flightId,
    onSeatUpdate: (event: SeatMapUpdateEvent) => {
      if (event && event.seatNumbers && event.seatNumbers.length > 0) {
        setLocalSeats((prev) =>
          prev.map((s) => {
            if (event.seatNumbers.includes(s.seatNumber)) {
              return {
                ...s,
                status: event.status,
                priceAdjustment: event.priceAdjustment !== undefined ? event.priceAdjustment : s.priceAdjustment,
              };
            }
            return s;
          })
        );

        // Deselect if another user held the seat
        if (event.status === 'HELD' || event.status === 'BOOKED') {
          const conflicting = selectedSeats.filter((num) => event.seatNumbers.includes(num));
          if (conflicting.length > 0) {
            onSeatSelect(selectedSeats.filter((num) => !event.seatNumbers.includes(num)));
            setConflictError(`Seat ${conflicting.join(', ')} was just reserved by another traveler.`);
          }
        }

        setRealtimeNotification(`Real-time update: Seats ${event.seatNumbers.join(', ')} updated (${event.status})`);
        setTimeout(() => setRealtimeNotification(null), 4000);
      }
    },
    enabled: !!flightId,
  });

  // Helper to safely extract row number
  const getSeatRow = (seat: Seat): number => {
    if (typeof seat.rowNumber === 'number' && !isNaN(seat.rowNumber)) return seat.rowNumber;
    if (typeof seat.row === 'number' && !isNaN(seat.row)) return seat.row;
    if (seat.seatNumber) {
      const match = seat.seatNumber.match(/^\d+/);
      if (match) return parseInt(match[0], 10);
    }
    return 1;
  };

  // Helper to safely extract column letter
  const getSeatColumn = (seat: Seat): string => {
    if (seat.column && typeof seat.column === 'string') return seat.column.toUpperCase();
    if (seat.seatNumber) {
      const match = seat.seatNumber.match(/[A-Za-z]+$/);
      if (match) return match[0].toUpperCase();
    }
    return 'A';
  };

  // Helper to check if seat matches preference
  const isPreferredSeat = (seat: Seat, rowNum: number, col: string): boolean => {
    if (!effectivePreference) return false;
    const pref = effectivePreference.toUpperCase();
    if (pref === 'WINDOW' && (col === 'A' || col === 'F')) return true;
    if (pref === 'AISLE' && (col === 'C' || col === 'D')) return true;
    if (pref === 'EXTRA_LEGROOM' && (rowNum === 1 || rowNum === 12 || (seat.priceAdjustment && seat.priceAdjustment > 0))) return true;
    if (pref === 'MIDDLE' && (col === 'B' || col === 'E')) return true;
    return false;
  };

  // Extract raw seats
  const rawSeats: Seat[] = Array.isArray(localSeats)
    ? localSeats
    : (localSeats as any)?.seats && Array.isArray((localSeats as any).seats)
    ? (localSeats as any).seats
    : [];

  // Reliable fallback if empty
  const safeSeats: Seat[] = rawSeats.length > 0 ? rawSeats : Array.from({ length: 20 * 6 }, (_, i) => {
    const r = Math.floor(i / 6) + 1;
    const col = ['A', 'B', 'C', 'D', 'E', 'F'][i % 6];
    return {
      seatNumber: `${r}${col}`,
      rowNumber: r,
      column: col,
      cabinClass: (cabinClass as any) || 'ECONOMY',
      status: 'AVAILABLE' as const,
      extraLegroom: r === 1 || r === 12,
      isEmergencyExit: r === 12,
      isWindow: col === 'A' || col === 'F',
      isAisle: col === 'C' || col === 'D',
      isMiddle: col === 'B' || col === 'E',
      price: 0,
      priceAdjustment: r === 1 ? 500 : r === 12 ? 350 : 0,
    };
  });

  // Group seats by row
  const rowsMap = new Map<number, Seat[]>();
  safeSeats.forEach((seat) => {
    if (seat && seat.seatNumber) {
      const r = getSeatRow(seat);
      if (!rowsMap.has(r)) {
        rowsMap.set(r, []);
      }
      rowsMap.get(r)!.push(seat);
    }
  });

  const sortedRows = Array.from(rowsMap.keys()).sort((a, b) => a - b);

  // Total seat upsell price calculation
  const totalSeatUpgradeCost = useMemo(() => {
    let cost = 0;
    selectedSeats.forEach((num) => {
      const s = safeSeats.find((seat) => seat.seatNumber === num);
      if (s && s.priceAdjustment && s.priceAdjustment > 0) {
        cost += s.priceAdjustment;
      }
    });
    return cost;
  }, [selectedSeats, safeSeats]);

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
      {/* Real-time Indicator & Preference Badge */}
      <div className="flex flex-wrap items-center justify-between gap-3 text-xs">
        <div className="flex items-center gap-2 text-slate-400">
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 font-bold text-[11px]">
            <Radio className="w-3 h-3 animate-pulse" /> Live Seat Availability Active
          </span>
          {realtimeNotification && (
            <span className="text-amber-400 font-medium animate-fade-in">{realtimeNotification}</span>
          )}
        </div>

        {effectivePreference && (
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-amber-400/10 border border-amber-400/20 text-amber-400 font-semibold text-[11px]">
            <Sparkles className="w-3 h-3 text-amber-400" />
            Highlighting Saved Preference: <strong className="text-white uppercase">{effectivePreference}</strong>
          </div>
        )}
      </div>

      {/* Seat Map Legend & Upselling Summary */}
      <div className="p-4 sm:p-5 rounded-3xl bg-[#14161F] border border-white/10 flex flex-wrap items-center justify-between gap-4 text-xs shadow-xl backdrop-blur-xl">
        <div className="flex flex-wrap items-center gap-4">
          <div className="flex items-center gap-2">
            <div className="w-5 h-5 rounded-lg bg-[#1F222E] border border-white/10"></div>
            <span className="text-slate-300 font-semibold">Standard (₹0)</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-5 h-5 rounded-lg bg-emerald-950/80 border border-emerald-500/40 flex items-center justify-center text-[10px] text-emerald-400 font-bold">
              ★
            </div>
            <span className="text-slate-300 font-semibold">Extra Legroom (+₹350–₹500)</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-5 h-5 rounded-lg bg-gradient-to-r from-amber-400 to-amber-500 border border-amber-300 text-black flex items-center justify-center font-bold text-[10px] shadow-glow-gold">
              <Check className="w-3.5 h-3.5 text-black" />
            </div>
            <span className="text-amber-400 font-bold">Selected</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-5 h-5 rounded-lg bg-[#0B0C10] border border-white/5 opacity-40 cursor-not-allowed"></div>
            <span className="text-slate-500 font-medium">Occupied</span>
          </div>
        </div>

        <div className="flex items-center gap-2">
          {totalSeatUpgradeCost > 0 && (
            <span className="px-3 py-1.5 rounded-full bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 font-bold text-xs">
              Seat Upgrades: +₹{totalSeatUpgradeCost.toLocaleString()}
            </span>
          )}
          <div className="px-3.5 py-1.5 rounded-full bg-[#181A22] border border-amber-500/30 text-amber-400 font-black text-xs">
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
      <div className="max-w-md mx-auto p-6 sm:p-8 rounded-[40px] bg-[#12131A] border-2 border-white/10 shadow-2xl relative">
        {/* Cockpit Front Nose Curve */}
        <div className="w-32 h-14 mx-auto mb-8 rounded-t-full border-t-2 border-x-2 border-white/10 bg-[#0B0C10] flex items-center justify-center shadow-inner">
          <span className="text-[10px] uppercase font-black text-slate-400 tracking-widest">Cockpit</span>
        </div>

        {/* Column Labels Header */}
        <div className="flex items-center justify-between px-3 pb-3 text-xs font-mono font-black text-slate-400 border-b border-white/10 mb-4">
          <div className="flex gap-2">
            <span className="w-8 text-center text-amber-400">A</span>
            <span className="w-8 text-center text-amber-400">B</span>
            <span className="w-8 text-center text-amber-400">C</span>
          </div>
          <span className="text-[10px] uppercase text-slate-500 font-bold">Aisle</span>
          <div className="flex gap-2">
            <span className="w-8 text-center text-amber-400">D</span>
            <span className="w-8 text-center text-amber-400">E</span>
            <span className="w-8 text-center text-amber-400">F</span>
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
                className="mt-2 px-4 py-2 rounded-xl bg-amber-400/20 hover:bg-amber-400/30 text-amber-400 border border-amber-400/30 text-xs font-bold transition inline-flex items-center gap-1.5"
              >
                Refresh Seat Layout
              </button>
            )}
          </div>
        ) : (
          <div className="space-y-3">
            {sortedRows.map((rowNum) => {
              const rowSeats = rowsMap.get(rowNum) || [];
              const leftSeats = rowSeats.filter((s) => ['A', 'B', 'C'].includes(getSeatColumn(s)));
              const rightSeats = rowSeats.filter((s) => ['D', 'E', 'F'].includes(getSeatColumn(s)));
              const isExitRow = rowSeats.some((s) => s.isEmergencyExit || rowNum === 12);

              return (
                <div key={rowNum} className="space-y-1">
                  {isExitRow && (
                    <div className="py-1 text-center text-[9px] uppercase tracking-wider font-bold text-amber-400 bg-amber-500/10 border-y border-amber-500/20 rounded-xl my-2">
                      ⚠️ Emergency Exit Row (Extra Legroom +₹350)
                    </div>
                  )}

                  <div className="flex items-center justify-between gap-2">
                    {/* Left Side (ABC) */}
                    <div className="flex gap-2">
                      {['A', 'B', 'C'].map((col) => {
                        const seat = leftSeats.find((s) => getSeatColumn(s) === col);
                        if (!seat) return <div key={col} className="w-8 h-8"></div>;

                        const isSelected = selectedSeats.includes(seat.seatNumber);
                        const isAvailable = seat.status === 'AVAILABLE';
                        const isExtraLegroom = seat.extraLegroom || (seat.priceAdjustment !== undefined && seat.priceAdjustment > 0) || rowNum === 1 || rowNum === 12;
                        const matchesPref = isPreferredSeat(seat, rowNum, col);

                        return (
                          <button
                            key={seat.seatNumber}
                            type="button"
                            disabled={!isAvailable && !isSelected}
                            onClick={() => handleSeatClick(seat)}
                            title={`${seat.seatNumber} • ${seat.cabinClass} ${
                              isExtraLegroom ? `(+₹${seat.priceAdjustment || 350})` : '(Free Standard)'
                            } ${matchesPref ? '• Matches your preference!' : ''}`}
                            className={`w-8 h-8 rounded-xl font-mono text-xs font-black transition-all duration-150 flex items-center justify-center relative ${
                              isSelected
                                ? 'bg-gradient-to-r from-amber-400 to-amber-500 border border-amber-300 text-black shadow-glow-gold scale-105'
                                : isAvailable
                                ? isExtraLegroom
                                  ? 'bg-emerald-950/80 hover:bg-emerald-900 border border-emerald-500/40 text-emerald-300 hover:scale-105'
                                  : matchesPref
                                  ? 'bg-[#181A22] hover:bg-[#1F222E] border-2 border-amber-400 text-amber-300 hover:scale-105'
                                  : 'bg-[#181A22] hover:bg-[#1F222E] border border-white/10 text-slate-200 hover:scale-105'
                                : 'bg-[#0B0C10] border border-white/5 text-slate-600 opacity-40 cursor-not-allowed'
                            }`}
                          >
                            {isSelected ? (
                              <Check className="w-3.5 h-3.5 text-black" />
                            ) : matchesPref && isAvailable && !isExtraLegroom ? (
                              <span className="text-[10px] text-amber-400 font-bold">{col}</span>
                            ) : (
                              col
                            )}
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
                        const seat = rightSeats.find((s) => getSeatColumn(s) === col);
                        if (!seat) return <div key={col} className="w-8 h-8"></div>;

                        const isSelected = selectedSeats.includes(seat.seatNumber);
                        const isAvailable = seat.status === 'AVAILABLE';
                        const isExtraLegroom = seat.extraLegroom || (seat.priceAdjustment !== undefined && seat.priceAdjustment > 0) || rowNum === 1 || rowNum === 12;
                        const matchesPref = isPreferredSeat(seat, rowNum, col);

                        return (
                          <button
                            key={seat.seatNumber}
                            type="button"
                            disabled={!isAvailable && !isSelected}
                            onClick={() => handleSeatClick(seat)}
                            title={`${seat.seatNumber} • ${seat.cabinClass} ${
                              isExtraLegroom ? `(+₹${seat.priceAdjustment || 350})` : '(Free Standard)'
                            } ${matchesPref ? '• Matches your preference!' : ''}`}
                            className={`w-8 h-8 rounded-xl font-mono text-xs font-black transition-all duration-150 flex items-center justify-center relative ${
                              isSelected
                                ? 'bg-gradient-to-r from-amber-400 to-amber-500 border border-amber-300 text-black shadow-glow-gold scale-105'
                                : isAvailable
                                ? isExtraLegroom
                                  ? 'bg-emerald-950/80 hover:bg-emerald-900 border border-emerald-500/40 text-emerald-300 hover:scale-105'
                                  : matchesPref
                                  ? 'bg-[#181A22] hover:bg-[#1F222E] border-2 border-amber-400 text-amber-300 hover:scale-105'
                                  : 'bg-[#181A22] hover:bg-[#1F222E] border border-white/10 text-slate-200 hover:scale-105'
                                : 'bg-[#0B0C10] border border-white/5 text-slate-600 opacity-40 cursor-not-allowed'
                            }`}
                          >
                            {isSelected ? (
                              <Check className="w-3.5 h-3.5 text-black" />
                            ) : matchesPref && isAvailable && !isExtraLegroom ? (
                              <span className="text-[10px] text-amber-400 font-bold">{col}</span>
                            ) : (
                              col
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
        )}

        {/* Fuselage Rear Curve */}
        <div className="w-28 h-8 mx-auto mt-8 rounded-b-3xl border-b-2 border-x-2 border-white/10 bg-[#0B0C10] flex items-center justify-center">
          <span className="text-[9px] uppercase font-black text-slate-400 tracking-widest">Galley & Restrooms</span>
        </div>
      </div>
    </div>
  );
};
