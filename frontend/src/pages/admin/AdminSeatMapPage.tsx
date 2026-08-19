import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { Armchair, ArrowLeft, RefreshCw, AlertTriangle } from 'lucide-react';
import { seatService } from '../../services/seatService';
import { SeatMap } from '../../components/SeatMap';
import { CabinClass } from '../../types/flight';
import { Seat } from '../../types/api';

export const AdminSeatMapPage: React.FC = () => {
  const { flightId } = useParams<{ flightId: string }>();
  const [selectedCabin, setSelectedCabin] = useState<CabinClass>('ECONOMY');
  const [seats, setSeats] = useState<Seat[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchSeats = async () => {
    if (!flightId) return;
    setLoading(true); setError(null);
    try {
      const res = await seatService.getSeats(flightId, selectedCabin);
      setSeats(res.data ?? []);
    } catch (e: unknown) {
      const err = e as { message?: string };
      setError(err?.message ?? 'Failed to load seats');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchSeats(); }, [flightId, selectedCabin]);

  if (!flightId) return null;

  return (
    <div className="space-y-6 max-w-4xl">
      <div className="flex items-center gap-3">
        <Link to={`/admin/flights/${flightId}`} className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-xl transition">
          <ArrowLeft className="w-5 h-5" />
        </Link>
        <div className="flex-1">
          <h1 className="text-xl font-bold text-white flex items-center gap-2">
            <Armchair className="w-5 h-5 text-sky-400" /> Seat Map — Admin View
          </h1>
          <p className="text-sm text-slate-400">Read-only seat layout for flight {flightId}</p>
        </div>
        <button onClick={fetchSeats} className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-xl transition border border-slate-700">
          <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
        </button>
      </div>

      <div className="flex flex-wrap gap-2">
        {(['ECONOMY','PREMIUM_ECONOMY','BUSINESS','FIRST'] as CabinClass[]).map(c => (
          <button
            key={c}
            onClick={() => setSelectedCabin(c)}
            className={`px-3 py-1.5 text-xs font-medium rounded-lg border transition ${selectedCabin === c ? 'bg-sky-500/15 border-sky-500/30 text-sky-400' : 'bg-slate-800 border-slate-700 text-slate-400 hover:text-white'}`}
          >
            {c.replace(/_/g,' ')}
          </button>
        ))}
      </div>

      {error && (
        <div className="p-4 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm flex items-center gap-2">
          <AlertTriangle className="w-4 h-4" />{error}
        </div>
      )}

      {loading ? (
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-8 flex items-center justify-center">
          <div className="w-8 h-8 border-4 border-sky-500/30 border-t-sky-500 rounded-full animate-spin" />
        </div>
      ) : (
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-4">
          <p className="text-xs text-slate-500 mb-4 text-center">
            {seats.filter(s => s.status !== 'AVAILABLE').length} occupied · {seats.filter(s => s.status === 'AVAILABLE').length} available · {seats.length} total
          </p>
          <SeatMap
            flightId={flightId}
            cabinClass={selectedCabin}
            seats={seats}
            requiredCount={0}
            selectedSeats={[]}
            onSeatSelect={() => {}}
            onRefreshSeats={fetchSeats}
          />
        </div>
      )}
    </div>
  );
};
