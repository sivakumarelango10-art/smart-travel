import React, { useState, useEffect, useCallback, useRef } from 'react';
import {
  Ticket, Search, RefreshCw, Download, RotateCcw,
  ChevronLeft, ChevronRight, AlertTriangle
} from 'lucide-react';
import { adminTicketService } from '../../services/adminTicketService';
import { StatusBadge } from '../../components/admin/StatusBadge';
import { useAdminToast } from '../../components/admin/AdminToast';
import { AdminTicket } from '../../types/admin';

export const AdminTicketsPage: React.FC = () => {
  const { showToast } = useAdminToast();
  const [tickets, setTickets] = useState<AdminTicket[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [search, setSearch] = useState('');
  const [retryingId, setRetryingId] = useState<string | null>(null);
  const [downloadingId, setDownloadingId] = useState<string | null>(null);
  const searchTimeout = useRef<ReturnType<typeof setTimeout> | null>(null);

  const fetchTickets = useCallback(async (p = 0) => {
    setLoading(true);
    setError(null);
    try {
      if (search.trim().length >= 3) {
        try {
          const res = await adminTicketService.getTicketByNumber(search.trim());
          if (res.data) { setTickets([res.data]); setTotalPages(1); setTotalElements(1); }
        } catch {
          setTickets([]); setTotalPages(0); setTotalElements(0);
        }
      } else {
        const res = await adminTicketService.getAllTickets(p, 20);
        setTickets(res.data?.content ?? []);
        setTotalPages(res.data?.totalPages ?? 0);
        setTotalElements(res.data?.totalElements ?? 0);
      }
    } catch (e: unknown) {
      const err = e as { message?: string };
      setError(err?.message ?? 'Failed to load tickets');
    } finally {
      setLoading(false);
    }
  }, [search]);

  useEffect(() => {
    setPage(0);
    if (searchTimeout.current) clearTimeout(searchTimeout.current);
    searchTimeout.current = setTimeout(() => fetchTickets(0), 350);
    return () => { if (searchTimeout.current) clearTimeout(searchTimeout.current); };
  }, [fetchTickets]);

  const handleDownload = async (ticket: AdminTicket) => {
    setDownloadingId(ticket.id);
    try {
      const blob = await adminTicketService.downloadTicketPdf(ticket.id);
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url; a.download = `ticket-${ticket.ticketNumber}.pdf`; a.click();
      URL.revokeObjectURL(url);
      showToast('success', 'Downloaded', `Ticket ${ticket.ticketNumber} PDF downloaded.`);
    } catch (e: unknown) {
      const err = e as { message?: string };
      showToast('error', 'Download failed', err?.message);
    } finally {
      setDownloadingId(null);
    }
  };

  const handleRetryIssue = async (bookingId: string) => {
    setRetryingId(bookingId);
    try {
      const res = await adminTicketService.retryIssueTicket(bookingId);
      showToast('success', 'Ticket issued', `Ticket ${res.data?.ticketNumber} issued.`);
      fetchTickets(page);
    } catch (e: unknown) {
      const err = e as { message?: string };
      showToast('error', 'Retry failed', err?.message);
    } finally {
      setRetryingId(null);
    }
  };

  return (
    <div className="space-y-6 max-w-[1400px]">
      <div className="flex items-center justify-between flex-wrap gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight flex items-center gap-2">
            <Ticket className="w-6 h-6 text-teal-400" /> Ticket Management
          </h1>
          <p className="text-sm text-slate-400 mt-0.5">{totalElements.toLocaleString()} total tickets</p>
        </div>
      </div>

      <div className="flex flex-wrap gap-3">
        <div className="relative flex-1 min-w-[220px] max-w-sm">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
          <input
            type="text"
            placeholder="Search by ticket number..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            className="w-full pl-9 pr-4 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-white placeholder-slate-500 focus:outline-none focus:border-sky-500 focus:ring-1 focus:ring-sky-500/30 transition"
          />
        </div>
        <button onClick={() => fetchTickets(page)} className="p-2 bg-slate-800 border border-slate-700 rounded-xl text-slate-400 hover:text-white hover:bg-slate-700 transition">
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
                {['Ticket Number','Booking Ref','Passenger','Flight','Status','Issued At','Actions'].map(h => (
                  <th key={h} className="text-left px-4 py-3.5 text-xs font-semibold text-slate-400 uppercase tracking-wider first:px-5">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {loading ? (
                Array.from({ length: 8 }).map((_, i) => (
                  <tr key={i} className="animate-pulse">
                    {Array.from({ length: 7 }).map((_, j) => (
                      <td key={j} className="px-4 py-4 first:px-5"><div className="h-4 bg-slate-800 rounded" /></td>
                    ))}
                  </tr>
                ))
              ) : tickets.length === 0 ? (
                <tr><td colSpan={7} className="px-5 py-12 text-center">
                  <Ticket className="w-10 h-10 text-slate-700 mx-auto mb-3" />
                  <p className="text-slate-500 text-sm">No tickets found</p>
                </td></tr>
              ) : (
                tickets.map(t => (
                  <tr key={t.id} className="hover:bg-slate-800/30 transition">
                    <td className="px-5 py-4"><span className="font-mono font-bold text-teal-400">{t.ticketNumber}</span></td>
                    <td className="px-4 py-4"><span className="font-mono text-white">{t.bookingReference}</span></td>
                    <td className="px-4 py-4 text-slate-300">{t.passengerName ?? '—'}</td>
                    <td className="px-4 py-4"><span className="font-mono text-slate-300">{t.flightNumber}</span></td>
                    <td className="px-4 py-4"><StatusBadge status={t.status} type="ticket" /></td>
                    <td className="px-4 py-4 text-slate-400 text-xs">{new Date(t.issuedAt).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' })}</td>
                    <td className="px-4 py-4">
                      <div className="flex items-center gap-2">
                        <button
                          onClick={() => handleDownload(t)}
                          disabled={downloadingId === t.id}
                          className="p-1.5 text-slate-400 hover:text-sky-400 hover:bg-sky-500/10 rounded-lg transition disabled:opacity-50"
                          title="Download PDF"
                        >
                          {downloadingId === t.id ? <span className="w-4 h-4 border-2 border-sky-400/30 border-t-sky-400 rounded-full animate-spin inline-block" /> : <Download className="w-4 h-4" />}
                        </button>
                        {t.status !== 'ISSUED' && (
                          <button
                            onClick={() => handleRetryIssue(t.bookingId)}
                            disabled={retryingId === t.bookingId}
                            className="p-1.5 text-slate-400 hover:text-teal-400 hover:bg-teal-500/10 rounded-lg transition disabled:opacity-50"
                            title="Retry issuance"
                          >
                            {retryingId === t.bookingId ? <span className="w-4 h-4 border-2 border-teal-400/30 border-t-teal-400 rounded-full animate-spin inline-block" /> : <RotateCcw className="w-4 h-4" />}
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
        {totalPages > 1 && !search && (
          <div className="flex items-center justify-between px-5 py-4 border-t border-slate-800">
            <p className="text-xs text-slate-500">Page {page + 1} of {totalPages}</p>
            <div className="flex items-center gap-2">
              <button onClick={() => { setPage(p => p - 1); fetchTickets(page - 1); }} disabled={page === 0 || loading} className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition disabled:opacity-40"><ChevronLeft className="w-4 h-4" /></button>
              <button onClick={() => { setPage(p => p + 1); fetchTickets(page + 1); }} disabled={page >= totalPages - 1 || loading} className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition disabled:opacity-40"><ChevronRight className="w-4 h-4" /></button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
