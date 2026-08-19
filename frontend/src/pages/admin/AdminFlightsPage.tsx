import React, { useState, useEffect, useCallback, useRef } from 'react';
import { Link } from 'react-router-dom';
import {
  Plane, Search, Filter, Plus, Eye, Edit, Trash2, RefreshCw,
  ChevronLeft, ChevronRight, AlertTriangle
} from 'lucide-react';
import { adminFlightService } from '../../services/adminFlightService';
import { StatusBadge } from '../../components/admin/StatusBadge';
import { ConfirmModal } from '../../components/admin/ConfirmModal';
import { useAdminToast } from '../../components/admin/AdminToast';
import { Flight, FlightStatus } from '../../types/flight';

const FLIGHT_STATUSES: FlightStatus[] = ['SCHEDULED','BOARDING','DEPARTED','IN_AIR','LANDED','ARRIVED','DELAYED','CANCELLED','DIVERTED'];

export const AdminFlightsPage: React.FC = () => {
  const { showToast } = useAdminToast();
  const [flights, setFlights] = useState<Flight[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  // Filters
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<FlightStatus | ''>('');
  const [airlineFilter, setAirlineFilter] = useState('');
  const searchTimeout = useRef<ReturnType<typeof setTimeout> | null>(null);

  // Delete confirm
  const [deleteTarget, setDeleteTarget] = useState<Flight | null>(null);
  const [deleting, setDeleting] = useState(false);

  const fetchFlights = useCallback(async (p = 0) => {
    setLoading(true);
    setError(null);
    try {
      const params: Record<string, unknown> = { page: p, size: 20, sort: 'departureTime,asc' };
      if (statusFilter) params.status = statusFilter;
      if (airlineFilter.trim()) params.airline = airlineFilter.trim();
      if (search.trim()) params.origin = search.trim(); // best-effort text search on origin
      const res = await adminFlightService.searchFlights(params);
      setFlights(res.data?.content ?? []);
      setTotalPages(res.data?.totalPages ?? 0);
      setTotalElements(res.data?.totalElements ?? 0);
    } catch (e: unknown) {
      const err = e as { message?: string };
      setError(err?.message ?? 'Failed to load flights');
    } finally {
      setLoading(false);
    }
  }, [statusFilter, airlineFilter, search]);

  useEffect(() => {
    setPage(0);
    if (searchTimeout.current) clearTimeout(searchTimeout.current);
    searchTimeout.current = setTimeout(() => fetchFlights(0), 350);
    return () => { if (searchTimeout.current) clearTimeout(searchTimeout.current); };
  }, [fetchFlights]);

  const handleDelete = async () => {
    if (!deleteTarget) return;
    setDeleting(true);
    try {
      await adminFlightService.deleteFlight(deleteTarget.id);
      showToast('success', 'Flight deactivated', `Flight ${deleteTarget.flightNumber} has been deactivated.`);
      setDeleteTarget(null);
      fetchFlights(page);
    } catch (e: unknown) {
      const err = e as { message?: string };
      showToast('error', 'Failed to deactivate', err?.message ?? 'An error occurred');
    } finally {
      setDeleting(false);
    }
  };

  const handlePageChange = (newPage: number) => {
    setPage(newPage);
    fetchFlights(newPage);
  };

  return (
    <div className="space-y-6 max-w-[1400px]">
      {/* Header */}
      <div className="flex items-center justify-between flex-wrap gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight flex items-center gap-2">
            <Plane className="w-6 h-6 text-sky-400" /> Flight Management
          </h1>
          <p className="text-sm text-slate-400 mt-0.5">{totalElements.toLocaleString()} total flights</p>
        </div>
        <Link
          to="/admin/flights/new"
          className="flex items-center gap-2 px-4 py-2 text-sm font-medium text-white bg-gradient-to-r from-sky-600 to-indigo-600 hover:from-sky-500 hover:to-indigo-500 rounded-xl shadow-lg shadow-sky-500/20 transition"
        >
          <Plus className="w-4 h-4" /> Create Flight
        </Link>
      </div>

      {/* Filters */}
      <div className="flex flex-wrap gap-3">
        <div className="relative flex-1 min-w-[200px] max-w-xs">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
          <input
            type="text"
            placeholder="Search by origin..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            className="w-full pl-9 pr-4 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-white placeholder-slate-500 focus:outline-none focus:border-sky-500 focus:ring-1 focus:ring-sky-500/30 transition"
          />
        </div>
        <div className="relative">
          <Filter className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
          <select
            value={statusFilter}
            onChange={e => setStatusFilter(e.target.value as FlightStatus | '')}
            className="pl-9 pr-8 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-white focus:outline-none focus:border-sky-500 appearance-none cursor-pointer"
          >
            <option value="">All Statuses</option>
            {FLIGHT_STATUSES.map(s => <option key={s} value={s}>{s}</option>)}
          </select>
        </div>
        <input
          type="text"
          placeholder="Filter by airline..."
          value={airlineFilter}
          onChange={e => setAirlineFilter(e.target.value)}
          className="px-4 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-white placeholder-slate-500 focus:outline-none focus:border-sky-500 focus:ring-1 focus:ring-sky-500/30 transition min-w-[160px]"
        />
        <button
          onClick={() => fetchFlights(page)}
          className="p-2 bg-slate-800 border border-slate-700 rounded-xl text-slate-400 hover:text-white hover:bg-slate-700 transition"
          title="Refresh"
        >
          <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
        </button>
      </div>

      {error && (
        <div className="p-4 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm flex items-center gap-2">
          <AlertTriangle className="w-4 h-4 flex-shrink-0" />
          {error}
        </div>
      )}

      {/* Table */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-slate-950/60 border-b border-slate-800">
                <th className="text-left px-5 py-3.5 text-xs font-semibold text-slate-400 uppercase tracking-wider">Flight</th>
                <th className="text-left px-4 py-3.5 text-xs font-semibold text-slate-400 uppercase tracking-wider">Route</th>
                <th className="text-left px-4 py-3.5 text-xs font-semibold text-slate-400 uppercase tracking-wider">Departure</th>
                <th className="text-left px-4 py-3.5 text-xs font-semibold text-slate-400 uppercase tracking-wider">Status</th>
                <th className="text-right px-4 py-3.5 text-xs font-semibold text-slate-400 uppercase tracking-wider">Seats</th>
                <th className="text-center px-4 py-3.5 text-xs font-semibold text-slate-400 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {loading ? (
                Array.from({ length: 8 }).map((_, i) => (
                  <tr key={i} className="animate-pulse">
                    <td className="px-5 py-4"><div className="h-4 w-24 bg-slate-800 rounded" /></td>
                    <td className="px-4 py-4"><div className="h-4 w-28 bg-slate-800 rounded" /></td>
                    <td className="px-4 py-4"><div className="h-4 w-32 bg-slate-800 rounded" /></td>
                    <td className="px-4 py-4"><div className="h-5 w-20 bg-slate-800 rounded-full" /></td>
                    <td className="px-4 py-4"><div className="h-4 w-16 bg-slate-800 rounded ml-auto" /></td>
                    <td className="px-4 py-4"><div className="h-4 w-20 bg-slate-800 rounded mx-auto" /></td>
                  </tr>
                ))
              ) : flights.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-5 py-12 text-center">
                    <Plane className="w-10 h-10 text-slate-700 mx-auto mb-3" />
                    <p className="text-slate-500 text-sm">No flights found</p>
                  </td>
                </tr>
              ) : (
                flights.map(flight => (
                  <tr key={flight.id} className="hover:bg-slate-800/30 transition">
                    <td className="px-5 py-4">
                      <div>
                        <p className="font-mono font-bold text-white text-sm">{flight.flightNumber}</p>
                        <p className="text-[11px] text-slate-400">{flight.airline}</p>
                      </div>
                    </td>
                    <td className="px-4 py-4">
                      <p className="text-sm text-white font-medium">
                        {flight.departureAirport?.code} → {flight.arrivalAirport?.code}
                      </p>
                      <p className="text-[11px] text-slate-400">
                        {flight.departureAirport?.city} → {flight.arrivalAirport?.city}
                      </p>
                    </td>
                    <td className="px-4 py-4">
                      <p className="text-sm text-white">
                        {new Date(flight.departureTime).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })}
                      </p>
                      <p className="text-[11px] text-slate-400">
                        {new Date(flight.departureTime).toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit', hour12: false })} UTC
                      </p>
                    </td>
                    <td className="px-4 py-4">
                      <StatusBadge status={flight.status} type="flight" />
                      {!flight.active && (
                        <span className="ml-1 text-[10px] text-slate-500">(inactive)</span>
                      )}
                    </td>
                    <td className="px-4 py-4 text-right">
                      <p className="text-sm font-medium text-white">{flight.availableSeats}/{flight.totalSeats}</p>
                      <p className="text-[11px] text-slate-500">avail/total</p>
                    </td>
                    <td className="px-4 py-4">
                      <div className="flex items-center justify-center gap-2">
                        <Link
                          to={`/admin/flights/${flight.id}`}
                          className="p-1.5 text-slate-400 hover:text-sky-400 hover:bg-sky-500/10 rounded-lg transition"
                          title="View details"
                        >
                          <Eye className="w-4 h-4" />
                        </Link>
                        <Link
                          to={`/admin/flights/${flight.id}/edit`}
                          className="p-1.5 text-slate-400 hover:text-indigo-400 hover:bg-indigo-500/10 rounded-lg transition"
                          title="Edit flight"
                        >
                          <Edit className="w-4 h-4" />
                        </Link>
                        <button
                          onClick={() => setDeleteTarget(flight)}
                          className="p-1.5 text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 rounded-lg transition"
                          title="Deactivate flight"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="flex items-center justify-between px-5 py-4 border-t border-slate-800">
            <p className="text-xs text-slate-500">
              Page {page + 1} of {totalPages} ({totalElements.toLocaleString()} total)
            </p>
            <div className="flex items-center gap-2">
              <button
                onClick={() => handlePageChange(page - 1)}
                disabled={page === 0 || loading}
                className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition disabled:opacity-40 disabled:cursor-not-allowed"
              >
                <ChevronLeft className="w-4 h-4" />
              </button>
              <button
                onClick={() => handlePageChange(page + 1)}
                disabled={page >= totalPages - 1 || loading}
                className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition disabled:opacity-40 disabled:cursor-not-allowed"
              >
                <ChevronRight className="w-4 h-4" />
              </button>
            </div>
          </div>
        )}
      </div>

      <ConfirmModal
        isOpen={!!deleteTarget}
        title="Deactivate Flight"
        description={`Are you sure you want to deactivate flight ${deleteTarget?.flightNumber}? This will remove it from public search but NOT cancel existing bookings.`}
        impactNote="Existing confirmed bookings will not be affected. The flight will be hidden from customers."
        confirmLabel="Deactivate"
        isDestructive
        isLoading={deleting}
        onConfirm={handleDelete}
        onCancel={() => setDeleteTarget(null)}
      />
    </div>
  );
};
