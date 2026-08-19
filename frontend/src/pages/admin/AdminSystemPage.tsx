import React, { useEffect, useState } from 'react';
import { Activity, RefreshCw, CheckCircle, AlertTriangle, Database, Server, Clock } from 'lucide-react';
import { healthService } from '../../services/healthService';
import { HealthResponse } from '../../types/admin';

export const AdminSystemPage: React.FC = () => {
  const [health, setHealth] = useState<HealthResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [lastChecked, setLastChecked] = useState<Date | null>(null);

  const fetchHealth = async () => {
    setLoading(true); setError(null);
    try {
      const res = await healthService.getHealth();
      setHealth(res.data);
      setLastChecked(new Date());
    } catch (e: unknown) {
      const err = e as { message?: string };
      setError(err?.message ?? 'Health check failed');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchHealth(); }, []);

  const isHealthy = health?.status === 'UP';
  const isDbConnected = health?.database === 'CONNECTED';

  const InfoCard: React.FC<{ label: string; value: string; icon: React.ReactNode; ok: boolean; detail?: string }> = ({ label, value, icon, ok, detail }) => (
    <div className={`bg-slate-900 border rounded-2xl p-6 ${ok ? 'border-emerald-500/20' : 'border-rose-500/30'}`}>
      <div className="flex items-start gap-4">
        <div className={`w-12 h-12 rounded-xl flex items-center justify-center flex-shrink-0 ${ok ? 'bg-emerald-500/10 border border-emerald-500/20' : 'bg-rose-500/10 border border-rose-500/20'}`}>
          {icon}
        </div>
        <div>
          <p className="text-xs font-semibold text-slate-400 uppercase tracking-wide">{label}</p>
          <p className={`text-xl font-bold mt-1 ${ok ? 'text-emerald-400' : 'text-rose-400'}`}>{value}</p>
          {detail && <p className="text-xs text-slate-500 mt-1">{detail}</p>}
        </div>
        <div className="ml-auto">
          {ok
            ? <CheckCircle className="w-5 h-5 text-emerald-400" />
            : <AlertTriangle className="w-5 h-5 text-rose-400" />
          }
        </div>
      </div>
    </div>
  );

  return (
    <div className="space-y-8 max-w-3xl">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight flex items-center gap-2">
            <Activity className="w-6 h-6 text-emerald-400" /> System Health
          </h1>
          {lastChecked && <p className="text-sm text-slate-400 mt-0.5">Last checked: {lastChecked.toLocaleTimeString()}</p>}
        </div>
        <button
          onClick={fetchHealth}
          disabled={loading}
          className="flex items-center gap-2 px-4 py-2 text-sm font-medium text-slate-300 hover:text-white bg-slate-800 hover:bg-slate-700 border border-slate-700 rounded-xl transition disabled:opacity-50"
        >
          <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          Refresh
        </button>
      </div>

      {error && (
        <div className="p-5 bg-rose-500/10 border border-rose-500/20 rounded-2xl flex items-center gap-3">
          <AlertTriangle className="w-5 h-5 text-rose-400 flex-shrink-0" />
          <div>
            <p className="text-sm font-semibold text-rose-400">Health check failed</p>
            <p className="text-xs text-slate-400 mt-0.5">{error}</p>
          </div>
        </div>
      )}

      {/* Overall status banner */}
      {!loading && health && (
        <div className={`p-5 rounded-2xl border flex items-center gap-4 ${isHealthy ? 'bg-emerald-500/5 border-emerald-500/20' : 'bg-rose-500/5 border-rose-500/30'}`}>
          <div className={`w-4 h-4 rounded-full flex-shrink-0 ${isHealthy ? 'bg-emerald-400 shadow-lg shadow-emerald-400/40 animate-pulse' : 'bg-rose-400'}`} />
          <div>
            <p className={`text-base font-bold ${isHealthy ? 'text-emerald-400' : 'text-rose-400'}`}>
              {isHealthy ? 'All systems operational' : 'System issue detected'}
            </p>
            <p className="text-sm text-slate-400">{health.service} · {health.environment} environment</p>
          </div>
        </div>
      )}

      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {Array.from({ length: 4 }).map((_, i) => (
            <div key={i} className="bg-slate-900 border border-slate-800 rounded-2xl p-6 animate-pulse">
              <div className="flex items-start gap-4">
                <div className="w-12 h-12 bg-slate-800 rounded-xl flex-shrink-0" />
                <div className="space-y-2 flex-1">
                  <div className="h-3 bg-slate-800 rounded w-20" />
                  <div className="h-6 bg-slate-800 rounded w-32" />
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : health ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <InfoCard
            label="API Status"
            value={health.status}
            icon={<Server className="w-5 h-5 text-emerald-400" />}
            ok={isHealthy}
            detail="SmartTravel Backend API"
          />
          <InfoCard
            label="Database"
            value={health.database}
            icon={<Database className="w-5 h-5 text-teal-400" />}
            ok={isDbConnected}
            detail="MongoDB Atlas"
          />
          <InfoCard
            label="Environment"
            value={health.environment}
            icon={<Activity className="w-5 h-5 text-sky-400" />}
            ok={true}
            detail="Active deployment profile"
          />
          <InfoCard
            label="Server Time"
            value={new Date(health.timestamp).toLocaleTimeString('en-IN')}
            icon={<Clock className="w-5 h-5 text-violet-400" />}
            ok={true}
            detail={new Date(health.timestamp).toLocaleDateString('en-IN', { dateStyle: 'full' })}
          />
        </div>
      ) : null}

      {/* System Info */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6">
        <h2 className="text-sm font-semibold text-slate-400 uppercase tracking-wide mb-4">Platform Information</h2>
        <dl className="space-y-3">
          {[
            ['Service', 'SmartTravel Backend'],
            ['Frontend', 'React 18 + TypeScript + TailwindCSS'],
            ['Backend', 'Spring Boot 3.3.x + MongoDB Atlas'],
            ['Payment Gateway', 'Razorpay'],
            ['Architecture', 'REST API + WebSocket Notifications'],
          ].map(([k, v]) => (
            <div key={k} className="flex justify-between py-2 border-b border-slate-800/60 last:border-0">
              <dt className="text-xs text-slate-500 font-medium">{k}</dt>
              <dd className="text-xs text-slate-300 font-medium">{v}</dd>
            </div>
          ))}
        </dl>
      </div>
    </div>
  );
};
