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
    <div className="rounded-3xl bg-slate-900/90 border border-slate-800 p-6 shadow-2xl space-y-5 sticky top-24 backdrop-blur-xl">
      <div className="flex items-center justify-between pb-4 border-b border-slate-800">
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 rounded-xl bg-sky-500/10 text-sky-400 border border-sky-500/20 flex items-center justify-center">
            <Receipt className="w-4 h-4" />
          </div>
          <div>
            <h3 className="font-extrabold text-white text-base">Fare Summary</h3>
            <p className="text-[10px] text-slate-400">Authoritative price breakdown</p>
          </div>
        </div>
        <span className="text-[10px] uppercase font-black px-2.5 py-1 rounded-full bg-slate-800 text-sky-300 border border-slate-700">
          {cabinClass.replace('_', ' ')}
        </span>
      </div>

      {/* Breakdown Rows */}
      <div className="space-y-3 text-xs">
        <div className="flex items-center justify-between text-slate-300">
          <span>
            Base Fare ({passengerCount} {passengerCount === 1 ? 'Traveler' : 'Travelers'})
          </span>
          <span className="font-bold text-white font-mono">₹{totalBase.toLocaleString('en-IN')}</span>
        </div>

        <div className="flex items-center justify-between text-slate-400">
          <span>Taxes & GST (18%)</span>
          <span className="font-mono">₹{totalTax.toLocaleString('en-IN')}</span>
        </div>

        <div className="flex items-center justify-between text-slate-400">
          <span>Airport & User Development Fees</span>
          <span className="font-mono">₹{totalFee.toLocaleString('en-IN')}</span>
        </div>

        {selectedSeats.length > 0 && (
          <div className="flex items-center justify-between text-slate-300 pt-2.5 border-t border-slate-800/80">
            <span className="font-medium">Selected Seats</span>
            <span className="text-sky-400 font-mono font-bold bg-sky-950/60 px-2 py-0.5 rounded border border-sky-800/40">
              {selectedSeats.join(', ')}
            </span>
          </div>
        )}
      </div>

      {/* Total Due */}
      <div className="pt-4 border-t border-slate-800 flex items-baseline justify-between">
        <div>
          <span className="text-xs text-slate-400 block font-semibold">Total Amount Due</span>
          <span className="text-[11px] text-emerald-400 font-medium">All taxes & fees included</span>
        </div>
        <div className="text-right">
          <span className="text-3xl font-black text-white tracking-tight">
            ₹{totalAmount.toLocaleString('en-IN')}
          </span>
        </div>
      </div>

      {/* Expiration or Guarantee Notice */}
      {expiresAt ? (
        <div className="p-3.5 rounded-2xl bg-amber-500/15 border border-amber-500/30 flex items-center gap-2.5 text-amber-300 text-xs font-bold shadow-md">
          <Clock className="w-4 h-4 shrink-0" />
          <span>Payment Window: 15-minute concurrency lock active</span>
        </div>
      ) : (
        <div className="p-3.5 rounded-2xl bg-sky-500/10 border border-sky-500/20 flex items-center gap-2.5 text-sky-300 text-xs font-medium">
          <ShieldCheck className="w-4 h-4 shrink-0 text-sky-400" />
          <span>Atomic seat inventory lock & real-time fare guarantee.</span>
        </div>
      )}
    </div>
  );
};

