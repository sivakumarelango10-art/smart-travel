import React, { useState, useEffect } from 'react';
import { TrendingUp, Flame, Calendar, Sparkles, Lock } from 'lucide-react';
import { CabinClass, DynamicPriceBreakdown } from '../types/api';
import { pricingService } from '../services/pricingService';

interface PriceBreakdownCardProps {
  flightId: string;
  cabinClass: CabinClass;
  passengerCount: number;
  onFreezeClick?: () => void;
  onPriceCalculated?: (breakdown: DynamicPriceBreakdown) => void;
}

export const PriceBreakdownCard: React.FC<PriceBreakdownCardProps> = ({
  flightId,
  cabinClass,
  passengerCount,
  onFreezeClick,
  onPriceCalculated,
}) => {
  const [breakdown, setBreakdown] = useState<DynamicPriceBreakdown | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let isMounted = true;
    setLoading(true);

    pricingService
      .getPriceBreakdown(flightId, cabinClass, passengerCount)
      .then((data) => {
        if (isMounted) {
          setBreakdown(data);
          onPriceCalculated?.(data);
        }
      })
      .catch((err) => {
        console.error('Failed to fetch dynamic price breakdown', err);
      })
      .finally(() => {
        if (isMounted) setLoading(false);
      });

    return () => {
      isMounted = false;
    };
  }, [flightId, cabinClass, passengerCount, onPriceCalculated]);

  if (loading) {
    return (
      <div className="p-5 bg-slate-900/60 border border-slate-800 rounded-2xl animate-pulse">
        <div className="h-5 bg-slate-800 rounded w-1/3 mb-4" />
        <div className="space-y-2">
          <div className="h-4 bg-slate-800/60 rounded w-full" />
          <div className="h-4 bg-slate-800/60 rounded w-4/5" />
          <div className="h-4 bg-slate-800/60 rounded w-2/3" />
        </div>
      </div>
    );
  }

  if (!breakdown) return null;

  return (
    <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-5 backdrop-blur-md">
      <div className="flex items-center justify-between pb-3 border-b border-slate-800">
        <h4 className="text-sm font-bold text-white flex items-center gap-2">
          <TrendingUp className="w-4 h-4 text-cyan-400" />
          Dynamic Fare Breakdown
        </h4>
        <span className="text-[11px] font-medium text-slate-400 bg-slate-800 px-2 py-0.5 rounded-full">
          {passengerCount} {passengerCount === 1 ? 'Passenger' : 'Passengers'}
        </span>
      </div>

      <div className="mt-4 space-y-2.5 text-xs">
        {/* Base Fare */}
        <div className="flex items-center justify-between text-slate-300">
          <span>Base Airfare ({cabinClass})</span>
          <span className="font-semibold text-white">₹{breakdown.baseFare.toLocaleString()}</span>
        </div>

        {/* Dynamic Adjustments */}
        {breakdown.demandAdjustment !== 0 && (
          <div className="flex items-center justify-between text-amber-400 bg-amber-500/5 p-2 rounded-lg border border-amber-500/10">
            <span className="flex items-center gap-1.5">
              <Flame className="w-3.5 h-3.5 text-amber-400" />
              {breakdown.demandReason || 'Demand Surcharge'} ({breakdown.demandAdjustmentPercent > 0 ? `+${breakdown.demandAdjustmentPercent}%` : `${breakdown.demandAdjustmentPercent}%`})
            </span>
            <span className="font-semibold">
              {breakdown.demandAdjustment > 0 ? '+' : ''}₹{breakdown.demandAdjustment.toLocaleString()}
            </span>
          </div>
        )}

        {breakdown.seasonalAdjustment !== 0 && (
          <div className="flex items-center justify-between text-indigo-300 bg-indigo-500/5 p-2 rounded-lg border border-indigo-500/10">
            <span className="flex items-center gap-1.5">
              <Calendar className="w-3.5 h-3.5 text-indigo-400" />
              {breakdown.seasonalReason || 'Seasonal Peak'} (+{breakdown.seasonalAdjustmentPercent}%)
            </span>
            <span className="font-semibold">+₹{breakdown.seasonalAdjustment.toLocaleString()}</span>
          </div>
        )}

        {breakdown.holidayAdjustment !== 0 && (
          <div className="flex items-center justify-between text-purple-300 bg-purple-500/5 p-2 rounded-lg border border-purple-500/10">
            <span className="flex items-center gap-1.5">
              <Sparkles className="w-3.5 h-3.5 text-purple-400" />
              {breakdown.holidayReason || 'Holiday Surcharge'} (+{breakdown.holidayAdjustmentPercent}%)
            </span>
            <span className="font-semibold">+₹{breakdown.holidayAdjustment.toLocaleString()}</span>
          </div>
        )}

        {/* Taxes & Fees */}
        <div className="flex items-center justify-between text-slate-400 pt-1">
          <span>Aviation GST (12%)</span>
          <span className="font-medium text-slate-300">₹{breakdown.taxes.toLocaleString()}</span>
        </div>

        <div className="flex items-center justify-between text-slate-400">
          <span>Airport & Convenience Fees</span>
          <span className="font-medium text-slate-300">₹{breakdown.fees.toLocaleString()}</span>
        </div>

        {/* Per-Passenger Total */}
        <div className="pt-2 border-t border-slate-800 flex items-center justify-between text-slate-300">
          <span>Fare per passenger</span>
          <span className="font-bold text-white">₹{breakdown.totalPerPassenger.toLocaleString()}</span>
        </div>

        {/* Grand Total */}
        <div className="pt-2 border-t border-slate-700/80 flex items-center justify-between text-sm">
          <span className="font-bold text-white">Total Amount Due</span>
          <div className="text-right">
            <span className="text-lg font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 to-blue-500">
              ₹{breakdown.grandTotal.toLocaleString()}
            </span>
          </div>
        </div>
      </div>

      {/* Freeze Fare CTA */}
      {onFreezeClick && (
        <div className="mt-4 pt-3 border-t border-slate-800">
          <button
            type="button"
            onClick={onFreezeClick}
            className="w-full flex items-center justify-center gap-2 py-2 px-3 bg-slate-800/80 hover:bg-slate-700/80 border border-cyan-500/30 hover:border-cyan-500/60 text-cyan-300 hover:text-cyan-200 text-xs font-semibold rounded-xl transition-all shadow-sm"
          >
            <Lock className="w-3.5 h-3.5 text-cyan-400" />
            Freeze this price for 30 minutes
          </button>
        </div>
      )}
    </div>
  );
};
