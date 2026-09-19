import React, { useState, useEffect, useMemo } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { AlertCircle, Check, Sparkles, Radio } from 'lucide-react';
import { Seat, SeatMapUpdateEvent } from '../types/api';
import { useSeatMapWebSocket } from '../hooks/useSeatMapWebSocket';
import { useAuth } from '../context/AuthContext';

interface SeatMapProps {
  flightId: string;
  cabinClass?: string;
  seats: Seat[];
  loading?: boolean;
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
  loading = false,
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

  // Active cabin filter state: 'ALL' or specific cabin like 'ECONOMY', 'BUSINESS', 'PREMIUM_ECONOMY'
  const [activeCabinFilter, setActiveCabinFilter] = useState<'ALL' | string>('ALL');

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
    if (pref === 'EXTRA_LEGROOM' && (rowNum === 1 || rowNum === 7 || rowNum === 12 || (seat.priceAdjustment && seat.priceAdjustment > 0))) return true;
    if (pref === 'MIDDLE' && (col === 'B' || col === 'E')) return true;
    return false;
  };

  // Extract raw seats
  const rawSeats: Seat[] = Array.isArray(localSeats)
    ? localSeats
    : (localSeats as any)?.seats && Array.isArray((localSeats as any).seats)
    ? (localSeats as any).seats
    : [];

  // Reliable fallback matching aircraft cabin structure
  const safeSeats: Seat[] = useMemo(() => {
    if (rawSeats.length > 0) return rawSeats;
    // When loading, don't generate fake seats to avoid layout jumping
    if (loading) return [];

    // Fallback narrowbody layout if data not available
    const fallback: Seat[] = [];
    // Rows 1-3: Business Class (2x2 layout: A, C | D, F)
    for (let r = 1; r <= 3; r++) {
      for (const col of ['A', 'C', 'D', 'F']) {
        fallback.push({
          seatNumber: `${r}${col}`,
          rowNumber: r,
          column: col,
          cabinClass: 'BUSINESS' as any,
          status: 'AVAILABLE' as const,
          extraLegroom: true,
          price: 1000,
          priceAdjustment: 1000,
        });
      }
    }
    // Rows 4-6: Premium Economy (3x3 layout)
    for (let r = 4; r <= 6; r++) {
      for (const col of ['A', 'B', 'C', 'D', 'E', 'F']) {
        fallback.push({
          seatNumber: `${r}${col}`,
          rowNumber: r,
          column: col,
          cabinClass: 'PREMIUM_ECONOMY' as any,
          status: 'AVAILABLE' as const,
          price: 600,
          priceAdjustment: 600,
        });
      }
    }
    // Rows 7-20: Economy (3x3 layout)
    for (let r = 7; r <= 20; r++) {
      for (const col of ['A', 'B', 'C', 'D', 'E', 'F']) {
        const isExit = r === 12;
        const isBulkhead = r === 7;
        fallback.push({
          seatNumber: `${r}${col}`,
          rowNumber: r,
          column: col,
          cabinClass: 'ECONOMY' as any,
          status: 'AVAILABLE' as const,
          extraLegroom: isBulkhead || isExit,
          isEmergencyExit: isExit,
          price: isBulkhead || isExit ? 350 : 0,
          priceAdjustment: isBulkhead || isExit ? 350 : 0,
        });
      }
    }
    return fallback;
  }, [rawSeats, loading]);

  // Group seats by row
  const rowsMap = useMemo(() => {
    const map = new Map<number, Seat[]>();
    safeSeats.forEach((seat) => {
      if (seat && seat.seatNumber) {
        const r = getSeatRow(seat);
        if (!map.has(r)) {
          map.set(r, []);
        }
        map.get(r)!.push(seat);
      }
    });
    return map;
  }, [safeSeats]);

  const sortedRows = useMemo(() => {
    return Array.from(rowsMap.keys()).sort((a, b) => a - b);
  }, [rowsMap]);

  // Distinct cabins present in the aircraft
  const distinctCabins = useMemo(() => {
    const set = new Set<string>();
    safeSeats.forEach((s) => {
      if (s.cabinClass) set.add(s.cabinClass);
    });
    const order = ['BUSINESS', 'PREMIUM_ECONOMY', 'ECONOMY', 'FIRST'];
    return Array.from(set).sort((a, b) => order.indexOf(a) - order.indexOf(b));
  }, [safeSeats]);

  // Visible rows filtered by active tab
  const visibleRows = useMemo(() => {
    if (activeCabinFilter === 'ALL') return sortedRows;
    return sortedRows.filter((r) => {
      const rSeats = rowsMap.get(r) || [];
      return rSeats.some((s) => s.cabinClass === activeCabinFilter);
    });
  }, [sortedRows, rowsMap, activeCabinFilter]);

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
    if (cabinClass && seat.cabinClass && seat.cabinClass !== cabinClass) {
      setConflictError(`Seat ${seat.seatNumber} is reserved for ${seat.cabinClass.replace('_', ' ')}. Please select an available seat in your chosen ${cabinClass.replace('_', ' ')} cabin.`);
      return;
    }

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

  // Reusable seat button renderer
  const renderSeatButton = (seat: Seat, is2x2: boolean) => {
    const isWrongCabin = Boolean(cabinClass && seat.cabinClass && seat.cabinClass !== cabinClass);
    const isSelected = selectedSeats.includes(seat.seatNumber);
    const isAvailable = seat.status === 'AVAILABLE' && !isWrongCabin;
    const rowNum = getSeatRow(seat);
    const col = getSeatColumn(seat);
    const isExtraLegroom = seat.extraLegroom || (seat.priceAdjustment !== undefined && seat.priceAdjustment > 0) || rowNum === 1 || rowNum === 7 || rowNum === 12;
    const matchesPref = isPreferredSeat(seat, rowNum, col);
    const sizeClass = is2x2 ? 'w-[52px] h-8' : 'w-8 h-8';

    return (
      <motion.button
        key={seat.seatNumber}
        type="button"
        disabled={!isAvailable && !isSelected}
        onClick={() => handleSeatClick(seat)}
        whileHover={{ scale: isAvailable || isSelected ? 1.08 : 1 }}
        whileTap={{ scale: isAvailable || isSelected ? 0.92 : 1 }}
        animate={isSelected ? { scale: [1, 1.12, 1.05] } : { scale: 1 }}
        transition={{ duration: 0.2 }}
        title={
          isWrongCabin
            ? `${seat.seatNumber} • Reserved for ${seat.cabinClass?.replace('_', ' ')} travelers`
            : `${seat.seatNumber} • ${seat.cabinClass?.replace('_', ' ')} ${
                isExtraLegroom ? `(+₹${seat.priceAdjustment || 350})` : '(Free Standard)'
              } ${matchesPref ? '• Matches your preference!' : ''}`
        }
        className={`${sizeClass} rounded-xl font-mono text-xs font-black transition-colors duration-150 flex items-center justify-center relative ${
          isSelected
            ? 'bg-gradient-to-r from-amber-400 to-amber-500 border border-amber-300 text-black shadow-glow-gold'
            : isWrongCabin
            ? 'bg-[#0B0C10] border border-white/5 text-slate-700 opacity-25 cursor-not-allowed'
            : isAvailable
            ? isExtraLegroom
              ? 'bg-emerald-950/80 hover:bg-emerald-900 border border-emerald-500/40 text-emerald-300'
              : matchesPref
              ? 'bg-[#181A22] hover:bg-[#1F222E] border-2 border-amber-400 text-amber-300'
              : 'bg-[#181A22] hover:bg-[#1F222E] border border-white/10 text-slate-200'
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
      </motion.button>
    );
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

      {/* Cabin Class Quick Filter Bar */}
      {distinctCabins.length > 1 && (
        <div className="flex flex-wrap items-center justify-center gap-2 p-2 rounded-2xl bg-[#14161F] border border-white/10 max-w-md mx-auto">
          <button
            type="button"
            onClick={() => setActiveCabinFilter('ALL')}
            className={`px-3 py-1.5 rounded-xl text-xs font-bold transition ${
              activeCabinFilter === 'ALL'
                ? 'bg-amber-400 text-black shadow-glow-gold'
                : 'text-slate-400 hover:text-white hover:bg-white/5'
            }`}
          >
            All Cabins
          </button>
          {distinctCabins.map((c) => {
            const isUserCabin = cabinClass === c;
            const label =
              c === 'BUSINESS'
                ? '👑 Business'
                : c === 'PREMIUM_ECONOMY'
                ? '✨ Prem. Economy'
                : '💺 Economy';
            return (
              <button
                key={c}
                type="button"
                onClick={() => setActiveCabinFilter(c)}
                className={`px-3 py-1.5 rounded-xl text-xs font-bold transition flex items-center gap-1.5 ${
                  activeCabinFilter === c
                    ? 'bg-amber-400 text-black shadow-glow-gold'
                    : isUserCabin
                    ? 'bg-emerald-500/15 text-emerald-300 border border-emerald-500/30'
                    : 'text-slate-400 hover:text-white hover:bg-white/5'
                }`}
              >
                <span>{label}</span>
                {isUserCabin && (
                  <span className="text-[9px] uppercase px-1.5 py-0.5 rounded-full bg-emerald-500/30 text-emerald-200 font-black">
                    Your Cabin
                  </span>
                )}
              </button>
            );
          })}
        </div>
      )}

      <AnimatePresence>
        {conflictError && (
          <motion.div
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
            className="p-3.5 rounded-2xl bg-rose-500/15 border border-rose-500/30 text-rose-400 text-xs font-semibold flex items-center gap-2"
          >
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span>{conflictError}</span>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Airplane Fuselage Layout */}
      <div className="max-w-md mx-auto p-6 sm:p-8 rounded-[40px] bg-[#12131A] border-2 border-white/10 shadow-2xl relative">
        {/* Cockpit Front Nose Curve */}
        <div className="w-32 h-14 mx-auto mb-6 rounded-t-full border-t-2 border-x-2 border-white/10 bg-[#0B0C10] flex items-center justify-center shadow-inner">
          <span className="text-[10px] uppercase font-black text-slate-400 tracking-widest">Cockpit</span>
        </div>

        {/* Column Labels Header */}
        <div className="flex items-center justify-between px-3 pb-3 text-xs font-mono font-black text-slate-400 border-b border-white/10 mb-4">
          <div className="flex gap-2 w-[112px] justify-around">
            {activeCabinFilter === 'BUSINESS' ? (
              <>
                <span className="w-12 text-center text-amber-400">A</span>
                <span className="w-12 text-center text-amber-400">C</span>
              </>
            ) : (
              <>
                <span className="w-8 text-center text-amber-400">A</span>
                <span className="w-8 text-center text-amber-400">B</span>
                <span className="w-8 text-center text-amber-400">C</span>
              </>
            )}
          </div>
          <span className="w-8 text-center text-[10px] uppercase text-slate-500 font-bold">Aisle</span>
          <div className="flex gap-2 w-[112px] justify-around">
            {activeCabinFilter === 'BUSINESS' ? (
              <>
                <span className="w-12 text-center text-amber-400">D</span>
                <span className="w-12 text-center text-amber-400">F</span>
              </>
            ) : (
              <>
                <span className="w-8 text-center text-amber-400">D</span>
                <span className="w-8 text-center text-amber-400">E</span>
                <span className="w-8 text-center text-amber-400">F</span>
              </>
            )}
          </div>
        </div>

        {/* Rows Container */}
        {loading || visibleRows.length === 0 ? (
          <div className="space-y-3 py-2 animate-pulse">
            {[1, 2, 3, 4, 5, 6].map((i) => (
              <div key={i} className="flex items-center justify-between px-2 py-2 rounded-xl bg-white/[0.02]">
                <div className="flex gap-2 w-[112px] justify-around">
                  <div className="w-8 h-8 rounded-lg bg-white/5 border border-white/5" />
                  <div className="w-8 h-8 rounded-lg bg-white/5 border border-white/5" />
                  <div className="w-8 h-8 rounded-lg bg-white/5 border border-white/5" />
                </div>
                <span className="w-8 text-center text-xs font-mono text-slate-600 font-bold">{i}</span>
                <div className="flex gap-2 w-[112px] justify-around">
                  <div className="w-8 h-8 rounded-lg bg-white/5 border border-white/5" />
                  <div className="w-8 h-8 rounded-lg bg-white/5 border border-white/5" />
                  <div className="w-8 h-8 rounded-lg bg-white/5 border border-white/5" />
                </div>
              </div>
            ))}
            <p className="text-center text-[11px] font-medium text-slate-500 pt-2">
              Syncing live seat map with airline reservation system...
            </p>
          </div>
        ) : (
          <div className="space-y-3">
            {visibleRows.map((rowNum, index) => {
              const rowSeats = rowsMap.get(rowNum) || [];
              const rowCabin = rowSeats[0]?.cabinClass || 'ECONOMY';
              const prevRow = index > 0 ? visibleRows[index - 1] : null;
              const prevCabin = prevRow ? (rowsMap.get(prevRow)?.[0]?.cabinClass || 'ECONOMY') : null;
              const isCabinStart = index === 0 || rowCabin !== prevCabin;

              const leftSeats = rowSeats.filter((s) => ['A', 'B', 'C'].includes(getSeatColumn(s)));
              const rightSeats = rowSeats.filter((s) => ['D', 'E', 'F'].includes(getSeatColumn(s)));

              // 2x2 layout detection: Business rows or rows without middle seats B and E
              const is2x2 =
                rowCabin === 'BUSINESS' ||
                rowCabin === 'FIRST' ||
                (leftSeats.length <= 2 &&
                  rightSeats.length <= 2 &&
                  !rowSeats.some((s) => ['B', 'E'].includes(getSeatColumn(s))));

              const isExitRow = rowSeats.some((s) => s.isEmergencyExit || rowNum === 12);
              const isBulkheadRow = rowNum === 7 && rowCabin === 'ECONOMY';

              return (
                <div key={rowNum} className="space-y-1">
                  {/* Cabin Section Divider Banner */}
                  {isCabinStart && (
                    <div className="pt-2 pb-1">
                      {rowCabin === 'BUSINESS' ? (
                        <div className="py-2 px-3 rounded-2xl bg-gradient-to-r from-amber-500/15 via-amber-500/5 to-transparent border border-amber-500/30 flex items-center justify-between">
                          <div className="flex items-center gap-2">
                            <span className="text-xs font-extrabold text-amber-300 flex items-center gap-1.5">
                              <span>👑</span> Business Class
                            </span>
                            <span className="text-[10px] text-amber-400/70 font-mono font-medium">(2×2 Luxury Recliners)</span>
                          </div>
                          {cabinClass && cabinClass !== 'BUSINESS' ? (
                            <span className="text-[9px] font-bold text-slate-400 bg-white/5 px-2 py-0.5 rounded-full border border-white/10">
                              Reserved for Business
                            </span>
                          ) : (
                            <span className="text-[9px] font-bold text-amber-400 bg-amber-500/20 px-2 py-0.5 rounded-full border border-amber-500/30">
                              Your Cabin
                            </span>
                          )}
                        </div>
                      ) : rowCabin === 'PREMIUM_ECONOMY' ? (
                        <div className="py-2 px-3 rounded-2xl bg-gradient-to-r from-purple-500/15 via-purple-500/5 to-transparent border border-purple-500/30 flex items-center justify-between">
                          <div className="flex items-center gap-2">
                            <span className="text-xs font-extrabold text-purple-300 flex items-center gap-1.5">
                              <span>✨</span> Premium Economy
                            </span>
                            <span className="text-[10px] text-purple-400/70 font-mono font-medium">(Extra Legroom)</span>
                          </div>
                          {cabinClass && cabinClass !== 'PREMIUM_ECONOMY' ? (
                            <span className="text-[9px] font-bold text-slate-400 bg-white/5 px-2 py-0.5 rounded-full border border-white/10">
                              Reserved for Prem. Economy
                            </span>
                          ) : (
                            <span className="text-[9px] font-bold text-purple-400 bg-purple-500/20 px-2 py-0.5 rounded-full border border-purple-500/30">
                              Your Cabin
                            </span>
                          )}
                        </div>
                      ) : (
                        <div className="py-2 px-3 rounded-2xl bg-gradient-to-r from-emerald-500/15 via-emerald-500/5 to-transparent border border-emerald-500/30 flex items-center justify-between">
                          <div className="flex items-center gap-2">
                            <span className="text-xs font-extrabold text-emerald-300 flex items-center gap-1.5">
                              <span>💺</span> Economy Class
                            </span>
                            <span className="text-[10px] text-emerald-400/70 font-mono font-medium">(Standard 3×3 Seating)</span>
                          </div>
                          {cabinClass === 'ECONOMY' ? (
                            <span className="text-[9px] font-bold text-emerald-300 bg-emerald-500/20 px-2.5 py-0.5 rounded-full border border-emerald-500/30">
                              Your Active Cabin
                            </span>
                          ) : (
                            <span className="text-[9px] font-bold text-slate-400 bg-white/5 px-2 py-0.5 rounded-full border border-white/10">
                              Economy
                            </span>
                          )}
                        </div>
                      )}
                    </div>
                  )}

                  {/* Special Row Banners */}
                  {isBulkheadRow && (
                    <div className="py-1 text-center text-[9px] uppercase tracking-wider font-bold text-emerald-400 bg-emerald-500/10 border-y border-emerald-500/20 rounded-xl my-2">
                      ⭐ Front Row Bulkhead — Extra Legroom (+₹350)
                    </div>
                  )}
                  {isExitRow && !isBulkheadRow && (
                    <div className="py-1 text-center text-[9px] uppercase tracking-wider font-bold text-amber-400 bg-amber-500/10 border-y border-amber-500/20 rounded-xl my-2">
                      ⚠️ Emergency Exit Row — Extra Legroom (+₹350)
                    </div>
                  )}

                  <div className="flex items-center justify-between gap-2">
                    {/* Left Side (ABC or AC in 2x2) */}
                    <div className="flex gap-2 w-[112px] justify-between">
                      {is2x2
                        ? leftSeats
                            .sort((a, b) => getSeatColumn(a).localeCompare(getSeatColumn(b)))
                            .map((seat) => renderSeatButton(seat, true))
                        : ['A', 'B', 'C'].map((col) => {
                            const seat = leftSeats.find((s) => getSeatColumn(s) === col);
                            if (!seat) return <div key={col} className="w-8 h-8" />;
                            return renderSeatButton(seat, false);
                          })}
                    </div>

                    {/* Aisle & Row Number */}
                    <span className="w-8 text-center text-xs font-mono font-black text-slate-400">
                      {rowNum}
                    </span>

                    {/* Right Side (DEF or DF in 2x2) */}
                    <div className="flex gap-2 w-[112px] justify-between">
                      {is2x2
                        ? rightSeats
                            .sort((a, b) => getSeatColumn(a).localeCompare(getSeatColumn(b)))
                            .map((seat) => renderSeatButton(seat, true))
                        : ['D', 'E', 'F'].map((col) => {
                            const seat = rightSeats.find((s) => getSeatColumn(s) === col);
                            if (!seat) return <div key={col} className="w-8 h-8" />;
                            return renderSeatButton(seat, false);
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
