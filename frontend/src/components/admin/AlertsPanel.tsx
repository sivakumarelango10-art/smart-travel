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
      <div className="bg-[#14161F] rounded-2xl border border-white/10 p-5 animate-pulse">
        <div className="h-5 bg-[#181A22] rounded w-40 mb-4" />
        <div className="space-y-3">
          <div className="h-16 bg-[#181A22] rounded-xl" />
          <div className="h-16 bg-[#181A22] rounded-xl" />
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
        return <Ban className="w-5 h-5 text-rose-400 shrink-0" />;
      case 'warning':
        return <AlertTriangle className="w-5 h-5 text-amber-400 shrink-0" />;
      case 'info':
        return <Clock className="w-5 h-5 text-amber-400 shrink-0" />;
    }
  };

  const getAlertStyles = (type: AlertItem['type']) => {
    switch (type) {
      case 'danger':
        return 'bg-rose-500/10 border-rose-500/30 text-rose-300';
      case 'warning':
        return 'bg-amber-500/10 border-amber-500/30 text-amber-300';
      case 'info':
        return 'bg-amber-400/10 border-amber-400/20 text-amber-300';
    }
  };

  return (
    <div className="bg-[#14161F] rounded-2xl border border-white/10 p-5 shadow-xl">
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-2">
          <ShieldAlert className="w-5 h-5 text-amber-400" />
          <h2 className="text-base font-bold text-white">
            Operational Alerts & Actions
          </h2>
        </div>
        <span className="text-xs font-bold px-2.5 py-0.5 rounded-full bg-amber-400/10 border border-amber-400/20 text-amber-400 shadow-glow-gold">
          {alerts.length} Active
        </span>
      </div>

      {alerts.length === 0 ? (
        <div className="flex items-center gap-3 p-4 bg-emerald-500/10 border border-emerald-500/30 rounded-xl text-emerald-300 shadow-glow-emerald">
          <CheckCircle2 className="w-5 h-5 text-emerald-400 shrink-0" />
          <div>
            <p className="text-sm font-bold text-white">All Systems Normal</p>
            <p className="text-xs text-slate-400 mt-0.5">
              No operational disruptions, delayed flights, or expired seat bottlenecks detected.
            </p>
          </div>
        </div>
      ) : (
        <div className="space-y-3">
          {alerts.map((alert) => (
            <div
              key={alert.id}
              className={`flex items-start justify-between gap-3 p-3.5 rounded-xl border text-sm transition-all ${getAlertStyles(
                alert.type
              )}`}
            >
              <div className="flex items-start gap-3">
                {getAlertIcon(alert.type)}
                <div>
                  <h3 className="font-bold text-sm text-white">{alert.title}</h3>
                  <p className="text-xs opacity-90 mt-0.5 text-slate-300">{alert.description}</p>
                </div>
              </div>
              {alert.linkTo && (
                <Link
                  to={alert.linkTo}
                  className="shrink-0 text-xs font-bold text-amber-400 underline underline-offset-2 hover:opacity-80 transition-opacity"
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
