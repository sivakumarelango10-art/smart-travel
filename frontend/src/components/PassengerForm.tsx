import React from 'react';
import { Armchair } from 'lucide-react';
import { Passenger } from '../types/api';

interface PassengerFormProps {
  passengers: Passenger[];
  selectedSeats: string[];
  onChange: (index: number, field: keyof Passenger, value: any) => void;
  errors: Record<string, string>;
}

export const PassengerForm: React.FC<PassengerFormProps> = ({
  passengers,
  selectedSeats,
  onChange,
  errors,
}) => {
  return (
    <div className="space-y-6">
      {passengers.map((pax, index) => {
        const assignedSeat = selectedSeats[index] || 'Auto-assigned';

        return (
          <div
            key={index}
            className="p-6 sm:p-7 rounded-3xl bg-slate-900/90 border border-slate-800 shadow-2xl space-y-5 backdrop-blur-xl"
          >
            <div className="flex items-center justify-between pb-4 border-b border-slate-800">
              <div className="flex items-center gap-3">
                <div className="w-9 h-9 rounded-xl bg-sky-500/10 text-sky-400 border border-sky-500/20 flex items-center justify-center font-black text-sm">
                  {index + 1}
                </div>
                <div>
                  <h3 className="font-extrabold text-white text-base">
                    Passenger {index + 1} ({index === 0 ? 'Lead Traveler' : 'Companion'})
                  </h3>
                  <p className="text-[11px] text-slate-400">Name must match government photo ID</p>
                </div>
              </div>

              <div className="flex items-center gap-1.5 px-3.5 py-1.5 rounded-full bg-sky-950/60 border border-sky-800/40 text-xs font-mono font-bold text-sky-400">
                <Armchair className="w-3.5 h-3.5" />
                <span>Seat: {assignedSeat}</span>
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-12 gap-4">
              {/* Title */}
              <div className="sm:col-span-2">
                <label className="block text-[11px] font-bold text-slate-300 mb-1.5">Title *</label>
                <select
                  value={pax.title || 'Mr'}
                  onChange={(e) => onChange(index, 'title', e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2.5 text-xs font-bold text-white focus:outline-none focus:border-sky-500 transition cursor-pointer"
                >
                  <option value="Mr">Mr</option>
                  <option value="Ms">Ms</option>
                  <option value="Mrs">Mrs</option>
                  <option value="Dr">Dr</option>
                </select>
              </div>

              {/* First Name */}
              <div className="sm:col-span-5">
                <label className="block text-[11px] font-bold text-slate-300 mb-1.5">
                  First / Given Name *
                </label>
                <input
                  type="text"
                  placeholder="e.g. Sarah"
                  value={pax.firstName || ''}
                  onChange={(e) => onChange(index, 'firstName', e.target.value)}
                  className={`w-full bg-slate-950 border rounded-xl px-3.5 py-2.5 text-xs font-semibold text-white focus:outline-none transition ${
                    errors[`pax_${index}_firstName`]
                      ? 'border-rose-500 focus:border-rose-400'
                      : 'border-slate-800 focus:border-sky-500'
                  }`}
                />
                {errors[`pax_${index}_firstName`] && (
                  <p className="text-[10px] text-rose-400 mt-1 font-semibold">
                    {errors[`pax_${index}_firstName`]}
                  </p>
                )}
              </div>

              {/* Last Name */}
              <div className="sm:col-span-5">
                <label className="block text-[11px] font-bold text-slate-300 mb-1.5">
                  Last / Surname *
                </label>
                <input
                  type="text"
                  placeholder="e.g. Connor"
                  value={pax.lastName || ''}
                  onChange={(e) => onChange(index, 'lastName', e.target.value)}
                  className={`w-full bg-slate-950 border rounded-xl px-3.5 py-2.5 text-xs font-semibold text-white focus:outline-none transition ${
                    errors[`pax_${index}_lastName`]
                      ? 'border-rose-500 focus:border-rose-400'
                      : 'border-slate-800 focus:border-sky-500'
                  }`}
                />
                {errors[`pax_${index}_lastName`] && (
                  <p className="text-[10px] text-rose-400 mt-1 font-semibold">
                    {errors[`pax_${index}_lastName`]}
                  </p>
                )}
              </div>

              {/* Gender */}
              <div className="sm:col-span-3">
                <label className="block text-[11px] font-bold text-slate-300 mb-1.5">Gender *</label>
                <select
                  value={pax.gender || 'FEMALE'}
                  onChange={(e) => onChange(index, 'gender', e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2.5 text-xs font-bold text-white focus:outline-none focus:border-sky-500 transition cursor-pointer"
                >
                  <option value="MALE">Male</option>
                  <option value="FEMALE">Female</option>
                  <option value="OTHER">Other</option>
                </select>
              </div>

              {/* Date of Birth */}
              <div className="sm:col-span-3">
                <label className="block text-[11px] font-bold text-slate-300 mb-1.5">Date of Birth</label>
                <input
                  type="date"
                  value={pax.dateOfBirth || ''}
                  onChange={(e) => onChange(index, 'dateOfBirth', e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2.5 text-xs font-semibold text-white focus:outline-none focus:border-sky-500 transition [color-scheme:dark] cursor-pointer"
                />
              </div>

              {/* Nationality */}
              <div className="sm:col-span-3">
                <label className="block text-[11px] font-bold text-slate-300 mb-1.5">Nationality</label>
                <input
                  type="text"
                  placeholder="e.g. Indian"
                  value={pax.nationality || 'Indian'}
                  onChange={(e) => onChange(index, 'nationality', e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2.5 text-xs font-semibold text-white focus:outline-none focus:border-sky-500 transition"
                />
              </div>

              {/* Passport / Govt ID */}
              <div className="sm:col-span-3">
                <label className="block text-[11px] font-bold text-slate-300 mb-1.5">
                  Passport / Gov ID No.
                </label>
                <input
                  type="text"
                  placeholder="e.g. P1234567"
                  value={pax.passportNumber || ''}
                  onChange={(e) => onChange(index, 'passportNumber', e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2.5 text-xs font-semibold text-white focus:outline-none focus:border-sky-500 transition"
                />
              </div>
            </div>
          </div>
        );
      })}
    </div>
  );
};

