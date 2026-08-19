import React from 'react';

type StatusVariant = 'success' | 'warning' | 'danger' | 'info' | 'neutral' | 'purple';

const variantClasses: Record<StatusVariant, string> = {
  success: 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20',
  warning: 'bg-amber-500/10 text-amber-400 border border-amber-500/20',
  danger:  'bg-rose-500/10 text-rose-400 border border-rose-500/20',
  info:    'bg-sky-500/10 text-sky-400 border border-sky-500/20',
  neutral: 'bg-slate-500/10 text-slate-400 border border-slate-500/20',
  purple:  'bg-violet-500/10 text-violet-400 border border-violet-500/20',
};

function getFlightStatusVariant(status: string): StatusVariant {
  switch (status) {
    case 'SCHEDULED': return 'info';
    case 'BOARDING': return 'purple';
    case 'DEPARTED':
    case 'IN_AIR': return 'success';
    case 'LANDED':
    case 'ARRIVED': return 'success';
    case 'DELAYED': return 'warning';
    case 'CANCELLED': return 'danger';
    case 'DIVERTED': return 'warning';
    default: return 'neutral';
  }
}

function getBookingStatusVariant(status: string): StatusVariant {
  switch (status) {
    case 'CONFIRMED': return 'success';
    case 'PENDING': return 'warning';
    case 'CANCELLED': return 'danger';
    case 'EXPIRED': return 'neutral';
    case 'CHECKED_IN': return 'purple';
    case 'COMPLETED': return 'success';
    default: return 'neutral';
  }
}

function getRefundStatusVariant(status: string): StatusVariant {
  switch (status) {
    case 'COMPLETED': return 'success';
    case 'PROCESSING': return 'info';
    case 'PENDING': return 'warning';
    case 'FAILED': return 'danger';
    case 'CANCELLED': return 'neutral';
    default: return 'neutral';
  }
}

function getDisruptionStatusVariant(status: string): StatusVariant {
  switch (status) {
    case 'ACTIVE': return 'danger';
    case 'RESOLVED': return 'success';
    default: return 'neutral';
  }
}

function getPaymentStatusVariant(status: string): StatusVariant {
  switch (status) {
    case 'VERIFIED': return 'success';
    case 'PENDING': return 'warning';
    case 'FAILED': return 'danger';
    case 'REFUNDED': return 'purple';
    default: return 'neutral';
  }
}

interface StatusBadgeProps {
  status: string;
  type?: 'flight' | 'booking' | 'refund' | 'disruption' | 'payment' | 'ticket';
  size?: 'sm' | 'xs';
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({ status, type = 'booking', size = 'sm' }) => {
  let variant: StatusVariant = 'neutral';
  switch (type) {
    case 'flight':    variant = getFlightStatusVariant(status); break;
    case 'booking':   variant = getBookingStatusVariant(status); break;
    case 'refund':    variant = getRefundStatusVariant(status); break;
    case 'disruption':variant = getDisruptionStatusVariant(status); break;
    case 'payment':   variant = getPaymentStatusVariant(status); break;
    case 'ticket':    variant = status === 'ISSUED' ? 'success' : 'neutral'; break;
  }

  const sizeClass = size === 'xs' ? 'text-[10px] px-1.5 py-0.5' : 'text-xs px-2 py-0.5';

  return (
    <span className={`inline-flex items-center rounded-full font-medium tracking-wide ${sizeClass} ${variantClasses[variant]}`}>
      {status.replace(/_/g, ' ')}
    </span>
  );
};
