import React from 'react';
import { ShieldCheck, Receipt, Lock } from 'lucide-react';
import { Flight, CabinClass, PriceFreeze } from '../types/api';
import { useFlightPricingWebSocket } from '../hooks/useFlightPricingWebSocket';

interface FareSummaryCardProps {
  flight: Flight;
  cabinClass: CabinClass;
  passengerCount: number;
  selectedSeats?: string[];
  expiresAt?: string;
  appliedFreeze?: PriceFreeze | null;
}

export const FareSummaryCard: React.FC<FareSummaryCardProps> = ({
  flight,
  cabinClass,
  passengerCount,
  selectedSeats = [],
  appliedFreeze,
}) => {
  const { updatedPrice } = useFlightPricingWebSocket(flight.id, cabinClass);

  const cabinInv =
    flight.cabinInventories?.find((c) => c.cabinClass === cabinClass) ||
    flight.cabinInventories?.[0];

  let basePricePerPax: number;
  let taxPerPax: number;
  let feePerPax: number;
  let totalPerPax: number;

  if (appliedFreeze) {
    totalPerPax = appliedFreeze.lockedPricePerPassenger;
    basePricePerPax = appliedFreeze.basePriceAtFreeze || Math.round(totalPerPax * 0.8);
    taxPerPax = Math.round(basePricePerPax * 0.12);
    feePerPax = Math.max(0, totalPerPax - basePricePerPax - taxPerPax);
  } else {
    const rawBase = cabinInv ? cabinInv.basePrice : flight.basePrice;
    basePricePerPax = rawBase;
    taxPerPax = cabinInv ? cabinInv.taxAmount : Math.round(rawBase * 0.12);
    feePerPax = cabinInv ? cabinInv.feeAmount : 150;
    const defaultTotal = cabinInv ? cabinInv.totalPrice : basePricePerPax + taxPerPax + feePerPax;
    totalPerPax = updatedPrice != null ? updatedPrice : defaultTotal;
  }

  const totalBase = basePricePerPax * passengerCount;
  const totalTax = taxPerPax * passengerCount;
  const totalFee = feePerPax * passengerCount;
  const totalAmount = appliedFreeze ? appliedFreeze.lockedTotalPrice : totalPerPax * passengerCount;

  return (
    <div className="rounded-2xl bg-[#14161F] border border-white/10 p-5 shadow-xl space-y-4 sticky top-20">
      <div className="flex items-center justify-between pb-3 border-b border-white/10">
        <div className="flex items-center gap-2">
          <div className="w-7 h-7 rounded-lg bg-amber-400/10 text-amber-400 border border-amber-400/20 flex items-center justify-center shadow-glow-gold">
            <Receipt className="w-4 h-4" />
          </div>
          <div>
            <h3 className="font-bold text-white text-sm">Fare Summary</h3>
          </div>
        </div>
        <span className="text-[10px] uppercase font-bold px-2.5 py-0.5 rounded-full bg-amber-400/10 text-amber-400 border border-amber-400/20">
          {cabinClass.replace('_', ' ')}
        </span>
      </div>

      {/* Breakdown Rows */}
      <div className="space-y-2 text-xs">
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
          <span>Airport & User Fees</span>
          <span className="font-mono">₹{totalFee.toLocaleString('en-IN')}</span>
        </div>

        {selectedSeats.length > 0 && (
          <div className="flex items-center justify-between text-slate-300 pt-2 border-t border-white/5">
            <span className="font-medium">Selected Seats</span>
            <span className="text-amber-400 font-mono font-bold bg-[#181A22] px-2 py-0.5 rounded border border-white/10">
              {selectedSeats.join(', ')}
            </span>
          </div>
        )}
      </div>

      {/* Total Due */}
      <div className="pt-3 border-t border-white/10 flex items-center justify-between">
        <div>
          <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400 block">Total Fare Payable</span>
          <span className="text-2xl font-black text-amber-400 tracking-tight">
            ₹{totalAmount.toLocaleString('en-IN')}
          </span>
        </div>

        {appliedFreeze && (
          <span className="text-[10px] font-bold text-emerald-400 bg-emerald-500/10 px-2.5 py-0.5 rounded-full border border-emerald-500/20 flex items-center gap-1 shadow-glow-emerald">
            <Lock className="w-3 h-3 text-emerald-400" />
            Locked Fare
          </span>
        )}
      </div>

      {/* Security Guarantee */}
      <div className="p-2.5 rounded-xl bg-[#181A22] border border-white/10 text-[11px] text-slate-300 flex items-center gap-2">
        <ShieldCheck className="w-4 h-4 text-emerald-400 shrink-0" />
        <span>Price includes all taxes. 100% Secure Checkout.</span>
      </div>
    </div>
  );
};
