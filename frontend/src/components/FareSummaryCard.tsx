import React from 'react';
import { ShieldCheck, Receipt, Clock } from 'lucide-react';
import { Flight, CabinClass } from '../types/api';

interface FareSummaryCardProps {
  flight: Flight;
  cabinClass: CabinClass;
  passengerCount: number;
  selectedSeats?: string[];
  expiresAt?: string;
}

export const FareSummaryCard: React.FC<FareSummaryCardProps> = ({
  flight,
  cabinClass,
  passengerCount,
  selectedSeats = [],
  expiresAt,
}) => {
  const cabinInv =
    flight.cabinInventories?.find((c) => c.cabinClass === cabinClass) ||
    flight.cabinInventories?.[0];

  const basePricePerPax = cabinInv ? cabinInv.basePrice : flight.basePrice;
  const taxPerPax = cabinInv ? cabinInv.taxAmount : 540;
  const feePerPax = cabinInv ? cabinInv.feeAmount : 150;
  const totalPerPax = cabinInv ? cabinInv.totalPrice : basePricePerPax + taxPerPax + feePerPax;

  const totalBase = basePricePerPax * passengerCount;
  const totalTax = taxPerPax * passengerCount;
  const totalFee = feePerPax * passengerCount;
  const totalAmount = totalPerPax * passengerCount;

  return (
    <div className="rounded-2xl bg-slate-900/90 border border-slate-800 p-5 sm:p-6 shadow-2xl space-y-5 sticky top-20">
      <div className="flex items-center justify-between pb-3 border-b border-slate-800">
        <div className="flex items-center gap-2">
          <Receipt className="w-4 h-4 text-sky-400" />
          <h3 className="font-bold text-white text-base">Fare Summary</h3>
        </div>
        <span className="text-[10px] uppercase font-bold px-2 py-0.5 rounded bg-slate-800 text-slate-300 border border-slate-700">
          {cabinClass.replace('_', ' ')}
        </span>
      </div>

      {/* Breakdown Rows */}
      <div className="space-y-3 text-xs">
        <div className="flex items-center justify-between text-slate-300">
          <span>
            Base Fare ({passengerCount} {passengerCount === 1 ? 'Adult' : 'Adults'})
          </span>
          <span className="font-semibold text-white">₹{totalBase.toLocaleString('en-IN')}</span>
        </div>

        <div className="flex items-center justify-between text-slate-400">
          <span>Taxes & GST (18%)</span>
          <span>₹{totalTax.toLocaleString('en-IN')}</span>
        </div>

        <div className="flex items-center justify-between text-slate-400">
          <span>Airport & Regulatory Fees</span>
          <span>₹{totalFee.toLocaleString('en-IN')}</span>
        </div>

        {selectedSeats.length > 0 && (
          <div className="flex items-center justify-between text-slate-400 pt-2 border-t border-slate-800/60">
            <span>Seat Assignments</span>
            <span className="text-emerald-400 font-medium">{selectedSeats.join(', ')}</span>
          </div>
        )}
      </div>

      {/* Total Due */}
      <div className="pt-4 border-t border-slate-800 flex items-baseline justify-between">
        <div>
          <span className="text-xs text-slate-400 block">Total Authoritative Fare</span>
          <span className="text-xs text-emerald-400 font-medium">Includes all taxes & fees</span>
        </div>
        <div className="text-right">
          <span className="text-2xl font-black text-white">
            ₹{totalAmount.toLocaleString('en-IN')}
          </span>
        </div>
      </div>

      {/* Expiration or Guarantee Notice */}
      {expiresAt ? (
        <div className="p-3 rounded-xl bg-amber-500/10 border border-amber-500/20 flex items-center gap-2 text-amber-400 text-xs font-semibold">
          <Clock className="w-4 h-4 shrink-0" />
          <span>Payment Window: 15 min lock active</span>
        </div>
      ) : (
        <div className="p-3 rounded-xl bg-sky-500/10 border border-sky-500/20 flex items-center gap-2 text-sky-400 text-xs">
          <ShieldCheck className="w-4 h-4 shrink-0" />
          <span>Price guaranteed by backend atomic state machine.</span>
        </div>
      )}
    </div>
  );
};
