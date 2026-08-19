import React, { useEffect, useState } from 'react';
import { Bell, CheckCheck, AlertCircle, Clock, Sparkles, ChevronLeft, ChevronRight } from 'lucide-react';
import { notificationService } from '../../services/notificationService';
import { Notification } from '../../types/notification';

export const AdminNotificationsPage: React.FC = () => {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [markingId, setMarkingId] = useState<string | null>(null);
  const [markingAll, setMarkingAll] = useState(false);

  const fetchNotifications = async (p = 0) => {
    setLoading(true); setError(null);
    try {
      const res = await notificationService.getNotifications(p, 20);
      setNotifications(res.data?.content ?? []);
      setTotalPages(res.data?.totalPages ?? 0);
      setTotalElements(res.data?.totalElements ?? 0);
    } catch (e: unknown) {
      const err = e as { message?: string };
      setError(err?.message ?? 'Failed to load notifications');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchNotifications(0); }, []);

  const handleMarkRead = async (id: string) => {
    setMarkingId(id);
    try {
      await notificationService.markAsRead(id);
      setNotifications(prev => prev.map(n => n.id === id ? { ...n, isRead: true } : n));
    } finally {
      setMarkingId(null);
    }
  };

  const handleMarkAllRead = async () => {
    setMarkingAll(true);
    try {
      await notificationService.markAllAsRead();
      setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
    } finally {
      setMarkingAll(false);
    }
  };

  const unreadCount = notifications.filter(n => !n.isRead).length;

  const notifIcon = (n: Notification) => {
    if (n.type.includes('CANCEL') || n.priority === 'URGENT') return <AlertCircle className="w-4 h-4 text-rose-400" />;
    if (n.type.includes('DELAY') || n.type.includes('RESCHEDULE')) return <Clock className="w-4 h-4 text-amber-400" />;
    return <Sparkles className="w-4 h-4 text-sky-400" />;
  };

  return (
    <div className="space-y-6 max-w-3xl">
      <div className="flex items-center justify-between flex-wrap gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight flex items-center gap-2">
            <Bell className="w-6 h-6 text-sky-400" /> Notifications
          </h1>
          <p className="text-sm text-slate-400 mt-0.5">{totalElements} total · {unreadCount} unread</p>
        </div>
        {unreadCount > 0 && (
          <button
            onClick={handleMarkAllRead}
            disabled={markingAll}
            className="flex items-center gap-2 px-4 py-2 text-sm font-medium text-sky-400 border border-sky-500/30 hover:bg-sky-500/10 rounded-xl transition disabled:opacity-50"
          >
            {markingAll ? <span className="w-3.5 h-3.5 border-2 border-sky-400/30 border-t-sky-400 rounded-full animate-spin" /> : <CheckCheck className="w-4 h-4" />}
            Mark All Read
          </button>
        )}
      </div>

      {error && (
        <div className="p-4 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm">{error}</div>
      )}

      <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden">
        <div className="divide-y divide-slate-800/60">
          {loading ? (
            Array.from({ length: 8 }).map((_, i) => (
              <div key={i} className="flex items-start gap-4 px-5 py-4 animate-pulse">
                <div className="w-8 h-8 bg-slate-800 rounded-xl flex-shrink-0" />
                <div className="flex-1 space-y-2">
                  <div className="h-4 bg-slate-800 rounded w-48" />
                  <div className="h-3 bg-slate-800 rounded w-full" />
                  <div className="h-3 bg-slate-800 rounded w-24" />
                </div>
              </div>
            ))
          ) : notifications.length === 0 ? (
            <div className="py-16 text-center">
              <Bell className="w-10 h-10 text-slate-700 mx-auto mb-3" />
              <p className="text-slate-500 text-sm">No notifications</p>
            </div>
          ) : (
            notifications.map(n => (
              <div
                key={n.id}
                className={`flex items-start gap-4 px-5 py-4 transition cursor-pointer ${n.isRead ? 'hover:bg-slate-800/30' : 'bg-sky-950/20 hover:bg-sky-950/30 border-l-2 border-sky-500'}`}
                onClick={() => !n.isRead && handleMarkRead(n.id)}
              >
                <div className={`w-8 h-8 rounded-xl flex items-center justify-center flex-shrink-0 ${n.isRead ? 'bg-slate-800' : 'bg-sky-500/10 border border-sky-500/20'}`}>
                  {notifIcon(n)}
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-start justify-between gap-2">
                    <p className={`text-sm font-semibold ${n.isRead ? 'text-slate-300' : 'text-white'}`}>{n.title}</p>
                    {!n.isRead && markingId === n.id && (
                      <span className="w-3.5 h-3.5 border-2 border-sky-400/30 border-t-sky-400 rounded-full animate-spin flex-shrink-0 mt-0.5" />
                    )}
                  </div>
                  <p className="text-xs text-slate-400 mt-0.5 line-clamp-2">{n.message}</p>
                  <div className="flex items-center gap-3 mt-1">
                    <p className="text-[11px] text-slate-500">{new Date(n.createdAt).toLocaleString('en-IN', { dateStyle: 'short', timeStyle: 'short' })}</p>
                    <span className={`text-[10px] px-1.5 py-0.5 rounded-full font-medium ${n.priority === 'URGENT' ? 'bg-rose-500/10 text-rose-400' : n.priority === 'HIGH' ? 'bg-amber-500/10 text-amber-400' : 'bg-slate-800 text-slate-400'}`}>
                      {n.priority}
                    </span>
                    <span className="text-[10px] text-slate-600">{n.type.replace(/_/g,' ')}</span>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>

        {totalPages > 1 && (
          <div className="flex items-center justify-between px-5 py-4 border-t border-slate-800">
            <p className="text-xs text-slate-500">Page {page + 1} of {totalPages}</p>
            <div className="flex items-center gap-2">
              <button onClick={() => { setPage(p => p - 1); fetchNotifications(page - 1); }} disabled={page === 0 || loading} className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition disabled:opacity-40"><ChevronLeft className="w-4 h-4" /></button>
              <button onClick={() => { setPage(p => p + 1); fetchNotifications(page + 1); }} disabled={page >= totalPages - 1 || loading} className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition disabled:opacity-40"><ChevronRight className="w-4 h-4" /></button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
