import React from 'react';
import { AlertTriangle, Clock, Ban, CheckCircle2, ShieldAlert } from 'lucide-react';
import { OverviewAnalytics, FlightAnalytics } from '../../types/analytics';
import { Link } from 'react-router-dom';

interface AlertsPanelProps {
  overview?: OverviewAnalytics | null;
  flights?: FlightAnalytics | null;
  loading?: boolean;
}

interface AlertItem {
  id: string;
  type: 'danger' | 'warning' | 'info';
  title: string;
  description: string;
  linkText?: string;
  linkTo?: string;
}

export const AlertsPanel: React.FC<AlertsPanelProps> = ({
  overview,
  flights,
  loading = false,
}) => {
  if (loading) {
    return (
      <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-5 animate-pulse">
        <div className="h-5 bg-slate-200 dark:bg-slate-700 rounded w-40 mb-4" />
        <div className="space-y-3">
          <div className="h-16 bg-slate-100 dark:bg-slate-700/40 rounded-lg" />
          <div className="h-16 bg-slate-100 dark:bg-slate-700/40 rounded-lg" />
        </div>
      </div>
    );
  }

  // Generate live actionable alerts from real backend analytics data
  const alerts: AlertItem[] = [];

  if (flights) {
    if (flights.delayedFlights > 0) {
      alerts.push({
        id: 'delayed-flights',
        type: 'danger',
        title: `${flights.delayedFlights} Delayed Flight${flights.delayedFlights > 1 ? 's' : ''}`,
        description: 'Flights currently running behind schedule requiring passenger disruption notifications.',
        linkText: 'Manage Flights',
        linkTo: '/admin/disruptions',
      });
    }

    if (flights.cancelledFlights > 0) {
      alerts.push({
        id: 'cancelled-flights',
        type: 'danger',
        title: `${flights.cancelledFlights} Cancelled Flight${flights.cancelledFlights > 1 ? 's' : ''}`,
        description: 'Cancelled flights with auto-refund eligibility processing in progress.',
        linkText: 'View Refunds',
        linkTo: '/admin/refunds',
      });
    }

    if (flights.flightsWithLowInventory > 0) {
      alerts.push({
        id: 'low-inventory',
        type: 'warning',
        title: `${flights.flightsWithLowInventory} Low Inventory Flight${flights.flightsWithLowInventory > 1 ? 's' : ''}`,
        description: 'Less than 10% seats available. Consider reviewing pricing or cabin capacity.',
        linkText: 'View Flights',
        linkTo: '/admin/flights',
      });
    }

    if (flights.boardingFlights > 0) {
      alerts.push({
        id: 'boarding-flights',
        type: 'info',
        title: `${flights.boardingFlights} Flight${flights.boardingFlights > 1 ? 's' : ''} Currently Boarding`,
        description: 'Boarding gate active. Monitor gate check-ins and passenger manifests.',
        linkText: 'Check-Ins',
        linkTo: '/admin/check-ins',
      });
    }
  }

  if (overview) {
    if (overview.pendingBookings > 0) {
      alerts.push({
        id: 'pending-bookings',
        type: 'warning',
        title: `${overview.pendingBookings} Pending Booking${overview.pendingBookings > 1 ? 's' : ''}`,
        description: 'Bookings awaiting payment confirmation before seat hold expiration.',
        linkText: 'View Bookings',
        linkTo: '/admin/bookings',
      });
    }

    if (overview.failedPayments > 0) {
      alerts.push({
        id: 'failed-payments',
        type: 'warning',
        title: `${overview.failedPayments} Failed Payment${overview.failedPayments > 1 ? 's' : ''}`,
        description: 'Payment gateway failures or drops recorded across platform lifetime.',
        linkText: 'View System',
        linkTo: '/admin/system',
      });
    }
  }

  const getAlertIcon = (type: AlertItem['type']) => {
    switch (type) {
      case 'danger':
        return <Ban className="w-5 h-5 text-rose-500 shrink-0" />;
      case 'warning':
        return <AlertTriangle className="w-5 h-5 text-amber-500 shrink-0" />;
      case 'info':
        return <Clock className="w-5 h-5 text-sky-500 shrink-0" />;
    }
  };

  const getAlertStyles = (type: AlertItem['type']) => {
    switch (type) {
      case 'danger':
        return 'bg-rose-50/70 dark:bg-rose-950/20 border-rose-200 dark:border-rose-900/40 text-rose-900 dark:text-rose-200';
      case 'warning':
        return 'bg-amber-50/70 dark:bg-amber-950/20 border-amber-200 dark:border-amber-900/40 text-amber-900 dark:text-amber-200';
      case 'info':
        return 'bg-sky-50/70 dark:bg-sky-950/20 border-sky-200 dark:border-sky-900/40 text-sky-900 dark:text-sky-200';
    }
  };

  return (
    <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-5 shadow-sm">
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-2">
          <ShieldAlert className="w-5 h-5 text-primary-500" />
          <h2 className="text-base font-semibold text-slate-900 dark:text-white">
            Operational Alerts & Actions
          </h2>
        </div>
        <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-slate-100 dark:bg-slate-700 text-slate-700 dark:text-slate-300">
          {alerts.length} Active
        </span>
      </div>

      {alerts.length === 0 ? (
        <div className="flex items-center gap-3 p-4 bg-emerald-50 dark:bg-emerald-950/30 border border-emerald-200 dark:border-emerald-900/40 rounded-lg text-emerald-800 dark:text-emerald-300">
          <CheckCircle2 className="w-5 h-5 text-emerald-500 shrink-0" />
          <div>
            <p className="text-sm font-semibold">All Systems Normal</p>
            <p className="text-xs text-emerald-700 dark:text-emerald-400 mt-0.5">
              No operational disruptions, delayed flights, or expired seat bottlenecks detected.
            </p>
          </div>
        </div>
      ) : (
        <div className="space-y-3">
          {alerts.map((alert) => (
            <div
              key={alert.id}
              className={`flex items-start justify-between gap-3 p-3.5 rounded-lg border text-sm transition-all ${getAlertStyles(
                alert.type
              )}`}
            >
              <div className="flex items-start gap-3">
                {getAlertIcon(alert.type)}
                <div>
                  <h3 className="font-semibold text-sm">{alert.title}</h3>
                  <p className="text-xs opacity-90 mt-0.5">{alert.description}</p>
                </div>
              </div>
              {alert.linkTo && (
                <Link
                  to={alert.linkTo}
                  className="shrink-0 text-xs font-semibold underline underline-offset-2 hover:opacity-80 transition-opacity"
                >
                  {alert.linkText || 'View'}
                </Link>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
