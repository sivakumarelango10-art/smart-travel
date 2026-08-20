import { CabinClass } from './flight';

export interface DynamicPriceBreakdown {
  flightId: string;
  cabinClass: CabinClass;
  passengerCount: number;
  baseFare: number;
  demandAdjustment: number;
  demandAdjustmentPercent: number;
  demandReason?: string;
  seasonalAdjustment: number;
  seasonalAdjustmentPercent: number;
  seasonalReason?: string;
  holidayAdjustment: number;
  holidayAdjustmentPercent: number;
  holidayReason?: string;
  totalDynamicAdjustment: number;
  taxes: number;
  fees: number;
  totalPerPassenger: number;
  grandTotal: number;
  currency: string;
  occupancyRatio: number;
}

export type PriceFreezeStatus = 'ACTIVE' | 'USED' | 'EXPIRED' | 'CANCELLED';

export interface PriceFreeze {
  id: string;
  userId: string;
  flightId: string;
  flightNumber: string;
  cabinClass: CabinClass;
  passengerCount: number;
  lockedPricePerPassenger: number;
  lockedTotalPrice: number;
  currency: string;
  status: PriceFreezeStatus;
  bookingId?: string;
  createdAt: string;
  expiresAt: string;
  updatedAt?: string;
  basePriceAtFreeze?: number;
  demandAdjustmentPercentAtFreeze?: number;
  holidayAdjustmentPercentAtFreeze?: number;
  seasonalAdjustmentPercentAtFreeze?: number;
}

export interface FlightPriceHistory {
  id: string;
  flightId: string;
  flightNumber: string;
  cabinClass: CabinClass;
  basePrice: number;
  demandAdjustmentPercent: number;
  seasonalAdjustmentPercent: number;
  holidayAdjustmentPercent: number;
  dynamicAdjustmentAmount: number;
  taxAmount: number;
  feeAmount: number;
  finalPrice: number;
  occupancyRatio: number;
  reason?: string;
  capturedAt: string;
}
