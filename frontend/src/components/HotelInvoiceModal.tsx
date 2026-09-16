import React, { useRef } from 'react';
import {
  X,
  Printer,
  Download,
  Building2,
  CheckCircle2,
  Calendar,
  User,
  Phone,
  Mail,
  MapPin,
  ShieldCheck,
  QrCode,
  FileText
} from 'lucide-react';
import { HotelBooking } from '../types/hotel';
import { RealQRCode } from './RealQRCode';

interface HotelInvoiceModalProps {
  booking: HotelBooking;
  onClose: () => void;
}

/**
 * Converts a number to words in standard Indian numbering system (Lakhs, Crores, Thousands).
 */
function numberToIndianWords(num: number): string {
  if (!num || num === 0) return 'Zero Indian Rupees Only';
  const integerPart = Math.floor(Math.abs(num));

  const units = ['', 'One', 'Two', 'Three', 'Four', 'Five', 'Six', 'Seven', 'Eight', 'Nine'];
  const teens = ['Ten', 'Eleven', 'Twelve', 'Thirteen', 'Fourteen', 'Fifteen', 'Sixteen', 'Seventeen', 'Eighteen', 'Nineteen'];
  const tens = ['', '', 'Twenty', 'Thirty', 'Forty', 'Fifty', 'Sixty', 'Seventy', 'Eighty', 'Ninety'];

  function convertTwoDigits(n: number): string {
    if (n === 0) return '';
    if (n < 10) return units[n];
    if (n >= 10 && n < 20) return teens[n - 10];
    const ten = Math.floor(n / 10);
    const unit = n % 10;
    return `${tens[ten]}${unit !== 0 ? ' ' + units[unit] : ''}`;
  }

  function convertThreeDigits(n: number): string {
    const hundred = Math.floor(n / 100);
    const remainder = n % 100;
    let res = '';
    if (hundred > 0) {
      res += `${units[hundred]} Hundred`;
      if (remainder > 0) res += ' and ';
    }
    if (remainder > 0) {
      res += convertTwoDigits(remainder);
    }
    return res;
  }

  let words = '';
  const crore = Math.floor(integerPart / 10000000);
  let remainder = integerPart % 10000000;

  const lakh = Math.floor(remainder / 100000);
  remainder = remainder % 100000;

  const thousand = Math.floor(remainder / 1000);
  remainder = remainder % 1000;

  if (crore > 0) {
    words += `${convertTwoDigits(crore)} Crore `;
  }
  if (lakh > 0) {
    words += `${convertTwoDigits(lakh)} Lakh `;
  }
  if (thousand > 0) {
    words += `${convertTwoDigits(thousand)} Thousand `;
  }
  if (remainder > 0) {
    words += convertThreeDigits(remainder);
  }

  return `${words.trim()} Indian Rupees Only`;
}

export const HotelInvoiceModal: React.FC<HotelInvoiceModalProps> = ({ booking, onClose }) => {
  const printAreaRef = useRef<HTMLDivElement>(null);

  const handlePrint = () => {
    window.print();
  };

  const invoiceNumber = `INV-2026-${booking.bookingReference.replace(/[^A-Z0-9]/gi, '').toUpperCase()}`;
  const invoiceDate = booking.createdAt
    ? new Date(booking.createdAt).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })
    : new Date().toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });

  // Calculate GST: 6% CGST + 6% SGST
  const totalTax = booking.taxAmount || Math.round(booking.baseAmount * 0.12);
  const cgstAmount = Math.round(totalTax / 2);
  const sgstAmount = totalTax - cgstAmount;

  const verificationUrl = `https://smart-travel-sage.vercel.app/verify/hotel/${booking.bookingReference}`;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-6 bg-black/85 backdrop-blur-md overflow-y-auto animate-fade-in">
      <div className="relative w-full max-w-3xl bg-[#14161F] border border-white/20 rounded-3xl shadow-2xl overflow-hidden my-6 text-slate-100 flex flex-col max-h-[92vh]">
        
        {/* Top Action Bar (hidden when printed) */}
        <div className="p-4 sm:px-6 bg-[#181A24] border-b border-white/10 flex items-center justify-between print:hidden">
          <div className="flex items-center gap-2">
            <FileText className="w-5 h-5 text-amber-400" />
            <h3 className="text-sm font-bold text-white">Official GST Tax Invoice & Bill</h3>
          </div>
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={handlePrint}
              className="px-4 py-1.5 rounded-xl bg-gradient-to-r from-amber-400 to-amber-500 text-black font-extrabold text-xs shadow-glow-gold flex items-center gap-1.5 hover:scale-105 transition"
            >
              <Printer className="w-4 h-4" />
              <span>Print / Save as PDF</span>
            </button>
            <button
              type="button"
              onClick={onClose}
              className="p-2 rounded-xl bg-white/5 hover:bg-white/10 text-slate-400 hover:text-white transition"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Printable Invoice Container */}
        <div
          ref={printAreaRef}
          id="printable-hotel-invoice"
          className="p-6 sm:p-10 space-y-6 overflow-y-auto bg-white text-slate-900 rounded-b-3xl font-sans print:p-0 print:m-0 print:text-black"
        >
          {/* Header & Logo */}
          <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4 border-b border-slate-200 pb-6">
            <div>
              <div className="flex items-center gap-2">
                <div className="w-8 h-8 rounded-lg bg-amber-500 text-black flex items-center justify-center font-black text-sm">
                  ST
                </div>
                <span className="text-xl font-black tracking-tight text-slate-950">SmartTravel</span>
                <span className="text-xs px-2 py-0.5 rounded-full bg-slate-100 text-slate-600 font-semibold border border-slate-200">
                  Tax Invoice
                </span>
              </div>
              <p className="text-[11px] text-slate-500 mt-2 leading-relaxed">
                SmartTravel Technologies Private Limited<br />
                CIN: U63040DL2024PTC123456 | GSTIN: <strong>07AABCS1429B1Z8</strong><br />
                Registered Office: Janpath, Connaught Place, New Delhi 110001, India<br />
                Support: bookings@smarttravel.com | Toll-Free: 1800-102-8747
              </p>
            </div>

            <div className="text-left sm:text-right space-y-1">
              <div className="text-xs font-bold text-slate-500 uppercase tracking-wider">Invoice No.</div>
              <div className="text-sm font-black font-mono text-slate-900">{invoiceNumber}</div>
              <div className="text-xs text-slate-500">
                Invoice Date: <strong className="text-slate-800">{invoiceDate}</strong>
              </div>
              <div className="text-xs text-slate-500">
                Booking Reference (PNR): <strong className="text-amber-600 font-mono font-bold">{booking.bookingReference}</strong>
              </div>
              <div className="inline-block px-2.5 py-0.5 mt-1 rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200 text-[11px] font-bold">
                ✓ PAYMENT STATUS: PAID IN FULL
              </div>
            </div>
          </div>

          {/* Bill To & Hotel Stay Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-6 text-xs border-b border-slate-200 pb-6">
            {/* Guest / Billed To */}
            <div className="p-4 rounded-2xl bg-slate-50 border border-slate-200/80 space-y-1.5">
              <div className="font-bold text-slate-500 uppercase tracking-wider text-[10px] flex items-center gap-1">
                <User className="w-3.5 h-3.5 text-slate-400" />
                <span>Billed To (Primary Guest)</span>
              </div>
              <div className="text-sm font-black text-slate-900">{booking.primaryGuestName}</div>
              <div className="text-slate-600 flex items-center gap-1.5">
                <Mail className="w-3 h-3 text-slate-400" />
                <span>{booking.primaryGuestEmail}</span>
              </div>
              {booking.primaryGuestPhone && (
                <div className="text-slate-600 flex items-center gap-1.5">
                  <Phone className="w-3 h-3 text-slate-400" />
                  <span>{booking.primaryGuestPhone}</span>
                </div>
              )}
              <div className="text-[11px] text-slate-500 pt-1">
                Place of Supply: <strong>{booking.hotelCity || 'Delhi'}, India</strong> (State Code: 07)
              </div>
            </div>

            {/* Hotel Property & Stay Details */}
            <div className="p-4 rounded-2xl bg-slate-50 border border-slate-200/80 space-y-1.5">
              <div className="font-bold text-slate-500 uppercase tracking-wider text-[10px] flex items-center gap-1">
                <Building2 className="w-3.5 h-3.5 text-slate-400" />
                <span>Accommodation & Property</span>
              </div>
              <div className="text-sm font-black text-slate-900">{booking.hotelName}</div>
              <div className="text-slate-600 flex items-center gap-1">
                <MapPin className="w-3 h-3 text-slate-400 flex-shrink-0" />
                <span>{booking.hotelAddress ? `${booking.hotelAddress}, ` : ''}{booking.hotelCity}</span>
              </div>
              <div className="text-slate-700 font-semibold pt-1">
                Room: <span className="text-amber-700 font-bold">{booking.roomTypeName}</span> ({booking.roomCategory})
              </div>
              <div className="text-slate-600 flex items-center justify-between text-[11px] pt-0.5">
                <span>Check-in: <strong>{booking.checkInDate} (14:00)</strong></span>
                <span>Check-out: <strong>{booking.checkOutDate} (11:00)</strong></span>
              </div>
              <div className="text-[11px] text-slate-500">
                Duration: <strong>{booking.nights} Night(s)</strong> | <strong>{booking.roomCount || 1} Room(s)</strong> | <strong>{booking.guestCount} Guest(s)</strong>
              </div>
            </div>
          </div>

          {/* Itemized Tax Breakdown Table */}
          <div>
            <div className="text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">Itemized Charges</div>
            <table className="w-full text-left text-xs border-collapse">
              <thead>
                <tr className="border-b-2 border-slate-300 text-slate-600 bg-slate-100 font-bold">
                  <th className="py-2.5 px-3">Service Description</th>
                  <th className="py-2.5 px-3 text-center">SAC Code</th>
                  <th className="py-2.5 px-3 text-center">Duration / Units</th>
                  <th className="py-2.5 px-3 text-right">Unit Rate (₹)</th>
                  <th className="py-2.5 px-3 text-right">Taxable Amount (₹)</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200 text-slate-800">
                <tr>
                  <td className="py-3 px-3">
                    <div className="font-bold text-slate-900">{booking.roomTypeName} Accommodation</div>
                    <div className="text-[11px] text-slate-500">{booking.hotelName} ({booking.checkInDate} to {booking.checkOutDate})</div>
                  </td>
                  <td className="py-3 px-3 text-center font-mono">996311</td>
                  <td className="py-3 px-3 text-center">{booking.nights} Night(s) × {booking.roomCount || 1} Room</td>
                  <td className="py-3 px-3 text-right">₹{booking.nightlyRate?.toLocaleString()}</td>
                  <td className="py-3 px-3 text-right font-semibold">₹{booking.baseAmount?.toLocaleString()}</td>
                </tr>
              </tbody>
            </table>
          </div>

          {/* Financial Summary & Taxes */}
          <div className="grid grid-cols-1 sm:grid-cols-12 gap-4 items-start pt-2">
            {/* Payment Authentication & QR Stamp */}
            <div className="sm:col-span-6 p-4 rounded-2xl bg-slate-50 border border-slate-200/80 flex items-center gap-4">
              <div className="flex-shrink-0 bg-white p-1.5 rounded-xl border border-slate-200 shadow-sm">
                <RealQRCode value={verificationUrl} size={88} />
              </div>
              <div className="space-y-1 text-xs">
                <div className="font-bold text-slate-800 flex items-center gap-1">
                  <ShieldCheck className="w-4 h-4 text-emerald-600" />
                  <span>Digitally Verified & Stamped</span>
                </div>
                <div className="text-[11px] text-slate-600">
                  Transaction: <strong className="font-mono text-slate-900">{booking.paymentId || 'TXN-UPI-984271'}</strong>
                </div>
                <div className="text-[11px] text-slate-600">
                  Mode: <strong>UPI / Bank Transfer (Instant Settle)</strong>
                </div>
                <div className="text-[10px] text-slate-400">
                  Scan QR code with any device to verify booking validity with hotel front-desk system.
                </div>
              </div>
            </div>

            {/* Calculations List */}
            <div className="sm:col-span-6 space-y-2 text-xs">
              <div className="flex justify-between text-slate-600 py-1">
                <span>Subtotal (Base Tariff)</span>
                <span className="font-semibold text-slate-900">₹{booking.baseAmount?.toLocaleString()}</span>
              </div>
              <div className="flex justify-between text-slate-600 py-1">
                <span>Central GST (CGST @ 6%)</span>
                <span className="font-semibold text-slate-900">₹{cgstAmount.toLocaleString()}</span>
              </div>
              <div className="flex justify-between text-slate-600 py-1">
                <span>State GST (SGST @ 6%)</span>
                <span className="font-semibold text-slate-900">₹{sgstAmount.toLocaleString()}</span>
              </div>
              {booking.discountAmount && booking.discountAmount > 0 ? (
                <div className="flex justify-between text-emerald-600 py-1 font-semibold">
                  <span>Promotional Coupon Discount</span>
                  <span>-₹{booking.discountAmount.toLocaleString()}</span>
                </div>
              ) : null}
              <div className="flex justify-between text-slate-600 py-1">
                <span>Convenience & Booking Service Fee</span>
                <span className="font-bold text-emerald-600">FREE (Waived)</span>
              </div>

              {/* Total Invoice Value */}
              <div className="pt-2 border-t-2 border-slate-900 flex justify-between items-baseline">
                <div>
                  <div className="text-sm font-black text-slate-950">Total Amount Paid</div>
                  <div className="text-[10px] text-slate-500 font-medium">(Inclusive of all applicable GST)</div>
                </div>
                <div className="text-xl font-black text-amber-600">
                  ₹{booking.totalAmount?.toLocaleString()}
                </div>
              </div>
            </div>
          </div>

          {/* Amount in Words */}
          <div className="p-3 rounded-xl bg-amber-50/70 border border-amber-200/60 text-xs text-amber-950">
            <span className="font-bold text-amber-900 uppercase tracking-wider text-[10px] mr-2">Amount in Words:</span>
            <strong className="font-semibold">{numberToIndianWords(booking.totalAmount)}</strong>
          </div>

          {/* Footer Terms & Signatory */}
          <div className="border-t border-slate-200 pt-4 flex flex-col sm:flex-row items-center justify-between text-[11px] text-slate-500 gap-4">
            <div className="space-y-0.5">
              <p>• This is a computer-generated tax invoice and requires no physical signature.</p>
              <p>• Hotel check-in requires a valid government photo ID (Aadhaar, Passport, or Voter ID).</p>
              <p>• Cancellation & refund terms governed by SmartTravel booking agreement.</p>
            </div>
            <div className="text-center sm:text-right border-t sm:border-t-0 sm:border-l border-slate-200 pt-2 sm:pt-0 sm:pl-4">
              <div className="font-bold text-slate-800">SmartTravel Technologies Pvt. Ltd.</div>
              <div className="text-[10px] text-emerald-600 font-semibold mt-0.5">Authorized Signatory & Seal</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
