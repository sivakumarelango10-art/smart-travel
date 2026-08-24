import React from 'react';
import { ShieldCheck, Receipt, Clock, Lock } from 'lucide-react';
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
  expiresAt,
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
    <div className="rounded-2xl bg-white border border-slate-200 p-5 shadow-sm space-y-4 sticky top-20">
      <div className="flex items-center justify-between pb-3 border-b border-slate-100">
        <div className="flex items-center gap-2">
          <div className="w-7 h-7 rounded-lg bg-secondary/10 text-secondary flex items-center justify-center">
            <Receipt className="w-4 h-4" />
          </div>
          <div>
            <h3 className="font-bold text-primary text-sm">Fare Summary</h3>
          </div>
        </div>
        <span className="text-[10px] uppercase font-bold px-2 py-0.5 rounded-full bg-slate-100 text-slate-700 border border-slate-200">
          {cabinClass.replace('_', ' ')}
        </span>
      </div>

      {/* Breakdown Rows */}
      <div className="space-y-2 text-xs">
        <div className="flex items-center justify-between text-slate-700">
          <span>
            Base Fare ({passengerCount} {passengerCount === 1 ? 'Traveler' : 'Travelers'})
          </span>
          <span className="font-bold text-primary font-mono">₹{totalBase.toLocaleString('en-IN')}</span>
        </div>

        <div className="flex items-center justify-between text-slate-500">
          <span>Taxes & GST (18%)</span>
          <span className="font-mono">₹{totalTax.toLocaleString('en-IN')}</span>
        </div>

        <div className="flex items-center justify-between text-slate-500">
          <span>Airport & User Fees</span>
          <span className="font-mono">₹{totalFee.toLocaleString('en-IN')}</span>
        </div>

        {selectedSeats.length > 0 && (
          <div className="flex items-center justify-between text-slate-700 pt-2 border-t border-slate-100">
            <span className="font-medium">Selected Seats</span>
            <span className="text-secondary font-mono font-bold bg-secondary/10 px-2 py-0.5 rounded border border-secondary/20">
              {selectedSeats.join(', ')}
            </span>
          </div>
        )}
      </div>

      {/* Total Due */}
      <div className="pt-3 border-t border-slate-200 flex items-center justify-between">
        <div>
          <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400 block">Total Fare Payable</span>
          <span className="text-2xl font-black text-primary tracking-tight">
            ₹{totalAmount.toLocaleString('en-IN')}
          </span>
        </div>

        {appliedFreeze && (
          <span className="text-[10px] font-bold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-full border border-emerald-200 flex items-center gap-1">
            <Lock className="w-3 h-3 text-emerald-600" />
            Locked Fare
          </span>
        )}
      </div>

      {/* Security Guarantee */}
      <div className="p-2.5 rounded-xl bg-slate-50 border border-slate-200 text-[11px] text-slate-500 flex items-center gap-2">
        <ShieldCheck className="w-4 h-4 text-emerald-600 shrink-0" />
        <span>Price includes all taxes. 100% Secure Checkout.</span>
      </div>
    </div>
  );
};
