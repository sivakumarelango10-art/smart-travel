import React, { useState, useEffect, useCallback } from 'react';
import {
  RotateCcw, Filter, RefreshCw, ChevronLeft, ChevronRight, AlertTriangle,
  CheckCircle, Search
} from 'lucide-react';
import { adminRefundService } from '../../services/adminRefundService';
import { StatusBadge } from '../../components/admin/StatusBadge';
import { ConfirmModal } from '../../components/admin/ConfirmModal';
import { useAdminToast } from '../../components/admin/AdminToast';
import { AdminRefund, RefundStatus, RefundEligibilityResponse } from '../../types/admin';

const REFUND_STATUSES: RefundStatus[] = ['PENDING','PROCESSING','COMPLETED','FAILED','CANCELLED'];

export const AdminRefundsPage: React.FC = () => {
  const { showToast } = useAdminToast();
  const [refunds, setRefunds] = useState<AdminRefund[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [statusFilter, setStatusFilter] = useState<RefundStatus | ''>('');

  // Eligibility check
  const [paymentIdInput, setPaymentIdInput] = useState('');
  const [eligibility, setEligibility] = useState<RefundEligibilityResponse | null>(null);
  const [checkingEligibility, setCheckingEligibility] = useState(false);

  // Process refund confirm
  const [processTarget, setProcessTarget] = useState<AdminRefund | null>(null);
  const [processing, setProcessing] = useState(false);

  const fetchRefunds = useCallback(async (p = 0) => {
    setLoading(true);
    setError(null);
    try {
      const res = await adminRefundService.getAllRefunds(p, 20, statusFilter || undefined);
      setRefunds(res.data?.content ?? []);
      setTotalPages(res.data?.totalPages ?? 0);
      setTotalElements(res.data?.totalElements ?? 0);
    } catch (e: unknown) {
      const err = e as { message?: string };
      setError(err?.message ?? 'Failed to load refunds');
    } finally {
      setLoading(false);
    }
  }, [statusFilter]);

  useEffect(() => { setPage(0); fetchRefunds(0); }, [fetchRefunds]);

  const handleCheckEligibility = async () => {
    if (!paymentIdInput.trim()) return;
    setCheckingEligibility(true);
    setEligibility(null);
    try {
      const res = await adminRefundService.checkEligibility(paymentIdInput.trim());
      setEligibility(res.data);
    } catch (e: unknown) {
      const err = e as { message?: string };
      showToast('error', 'Eligibility check failed', err?.message);
    } finally {
      setCheckingEligibility(false);
    }
  };

  const handleProcessRefund = async () => {
    if (!processTarget) return;
    setProcessing(true);
    try {
      await adminRefundService.processRefund(processTarget.paymentId, {
        reason: processTarget.reason,
        description: `Admin processed refund for booking ${processTarget.bookingReference}`,
      });
      showToast('success', 'Refund processed', `Refund ${processTarget.refundNumber} has been processed.`);
      setProcessTarget(null);
      fetchRefunds(page);
    } catch (e: unknown) {
      const err = e as { message?: string };
      showToast('error', 'Refund processing failed', err?.message);
    } finally {
      setProcessing(false);
    }
  };

  return (
    <div className="space-y-6 max-w-[1400px]">
      <div className="flex items-center justify-between flex-wrap gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight flex items-center gap-2">
            <RotateCcw className="w-6 h-6 text-violet-400" /> Refund Management
          </h1>
          <p className="text-sm text-slate-400 mt-0.5">{totalElements.toLocaleString()} total refunds</p>
        </div>
      </div>

      {/* Eligibility Check */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5">
        <h2 className="text-sm font-semibold text-slate-300 uppercase tracking-wide mb-4 flex items-center gap-2">
          <Search className="w-4 h-4 text-sky-400" /> Refund Eligibility Checker
        </h2>
        <div className="flex gap-3">
          <input
            type="text"
            value={paymentIdInput}
            onChange={e => { setPaymentIdInput(e.target.value); setEligibility(null); }}
            placeholder="Enter Payment ID..."
            className="flex-1 max-w-sm px-4 py-2 bg-slate-800 border border-slate-700 rounded-xl text-sm text-white placeholder-slate-500 focus:outline-none focus:border-sky-500 focus:ring-1 focus:ring-sky-500/30"
          />
          <button
            onClick={handleCheckEligibility}
            disabled={!paymentIdInput.trim() || checkingEligibility}
            className="flex items-center gap-2 px-4 py-2 text-sm font-medium text-white bg-sky-700 hover:bg-sky-600 rounded-xl transition disabled:opacity-50"
          >
            {checkingEligibility && <span className="w-3.5 h-3.5 border-2 border-white/30 border-t-white rounded-full animate-spin" />}
            Check Eligibility
          </button>
        </div>
        {eligibility && (
          <div className={`mt-3 p-4 rounded-xl border ${eligibility.eligible ? 'bg-emerald-500/10 border-emerald-500/20' : 'bg-rose-500/10 border-rose-500/20'}`}>
            <div className="flex items-center gap-2 mb-1">
              {eligibility.eligible
                ? <CheckCircle className="w-4 h-4 text-emerald-400" />
                : <AlertTriangle className="w-4 h-4 text-rose-400" />
              }
              <span className={`text-sm font-semibold ${eligibility.eligible ? 'text-emerald-400' : 'text-rose-400'}`}>
                {eligibility.eligible ? 'Eligible for Refund' : 'Not Eligible'}
              </span>
            </div>
            {eligibility.refundableAmount && (
              <p className="text-sm text-white">Refundable: <span className="font-bold">₹{Number(eligibility.refundableAmount).toLocaleString('en-IN')}</span></p>
            )}
            {eligibility.message && <p className="text-xs text-slate-400 mt-1">{eligibility.message}</p>}
            {eligibility.eligible && (
              <button
                onClick={() => {
                  setProcessTarget({
                    id: '', refundNumber: 'NEW',
                    paymentId: paymentIdInput.trim(),
                    razorpayPaymentId: undefined,
                    bookingId: '', bookingReference: '',
                    userId: '',
                    amount: eligibility.refundableAmount ?? 0,
                    amountPaise: 0,
                    currency: 'INR',
                    reason: eligibility.reason,
                    status: 'PENDING',
                    requestedAt: new Date().toISOString(),
                  });
                }}
                className="mt-2 px-3 py-1.5 text-xs text-white bg-emerald-700 hover:bg-emerald-600 rounded-lg transition"
              >
                Process Refund Now
              </button>
            )}
          </div>
        )}
      </div>

      {/* Filters */}
      <div className="flex flex-wrap gap-3">
        <div className="relative">
          <Filter className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
          <select
            value={statusFilter}
            onChange={e => setStatusFilter(e.target.value as RefundStatus | '')}
            className="pl-9 pr-8 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-white focus:outline-none focus:border-sky-500 appearance-none cursor-pointer"
          >
            <option value="">All Statuses</option>
            {REFUND_STATUSES.map(s => <option key={s} value={s}>{s}</option>)}
          </select>
        </div>
        <button onClick={() => fetchRefunds(page)} className="p-2 bg-slate-800 border border-slate-700 rounded-xl text-slate-400 hover:text-white hover:bg-slate-700 transition">
          <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
        </button>
      </div>

      {error && (
        <div className="p-4 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm flex items-center gap-2">
          <AlertTriangle className="w-4 h-4 flex-shrink-0" />{error}
        </div>
      )}

      <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-slate-950/60 border-b border-slate-800">
                {['Refund Number','Booking Ref','Amount','Reason','Status','Requested','Actions'].map(h => (
                  <th key={h} className="text-left px-4 py-3.5 text-xs font-semibold text-slate-400 uppercase tracking-wider first:px-5">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {loading ? (
                Array.from({ length: 6 }).map((_, i) => (
                  <tr key={i} className="animate-pulse">
                    {Array.from({ length: 7 }).map((_, j) => (
                      <td key={j} className="px-4 py-4 first:px-5"><div className="h-4 bg-slate-800 rounded" /></td>
                    ))}
                  </tr>
                ))
              ) : refunds.length === 0 ? (
                <tr><td colSpan={7} className="px-5 py-12 text-center">
                  <RotateCcw className="w-10 h-10 text-slate-700 mx-auto mb-3" />
                  <p className="text-slate-500 text-sm">No refunds found</p>
                </td></tr>
              ) : (
                refunds.map(r => (
                  <tr key={r.id} className="hover:bg-slate-800/30 transition">
                    <td className="px-5 py-4"><span className="font-mono font-bold text-violet-400">{r.refundNumber}</span></td>
                    <td className="px-4 py-4"><span className="font-mono text-white">{r.bookingReference}</span></td>
                    <td className="px-4 py-4">
                      <p className="font-semibold text-white">₹{Number(r.amount).toLocaleString('en-IN')}</p>
                      <p className="text-[11px] text-slate-500">{r.currency}</p>
                    </td>
                    <td className="px-4 py-4 text-slate-400 text-xs">{r.reason?.replace(/_/g,' ')}</td>
                    <td className="px-4 py-4"><StatusBadge status={r.status} type="refund" /></td>
                    <td className="px-4 py-4 text-slate-400 text-xs">{new Date(r.requestedAt).toLocaleDateString('en-IN', { dateStyle: 'medium' })}</td>
                    <td className="px-4 py-4">
                      {r.status === 'PENDING' && (
                        <button
                          onClick={() => setProcessTarget(r)}
                          className="px-3 py-1.5 text-xs text-emerald-400 border border-emerald-500/30 hover:bg-emerald-500/10 rounded-lg transition"
                        >
                          Process
                        </button>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
        {totalPages > 1 && (
          <div className="flex items-center justify-between px-5 py-4 border-t border-slate-800">
            <p className="text-xs text-slate-500">Page {page + 1} of {totalPages}</p>
            <div className="flex items-center gap-2">
              <button onClick={() => { setPage(p => p - 1); fetchRefunds(page - 1); }} disabled={page === 0 || loading} className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition disabled:opacity-40"><ChevronLeft className="w-4 h-4" /></button>
              <button onClick={() => { setPage(p => p + 1); fetchRefunds(page + 1); }} disabled={page >= totalPages - 1 || loading} className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition disabled:opacity-40"><ChevronRight className="w-4 h-4" /></button>
            </div>
          </div>
        )}
      </div>

      <ConfirmModal
        isOpen={!!processTarget}
        title="Process Refund"
        description={`Process refund of ₹${Number(processTarget?.amount ?? 0).toLocaleString('en-IN')} for booking ${processTarget?.bookingReference}? This will trigger the payment gateway refund.`}
        confirmLabel="Process Refund"
        isLoading={processing}
        onConfirm={handleProcessRefund}
        onCancel={() => setProcessTarget(null)}
      />
    </div>
  );
};
