import React from 'react';
import { Armchair } from 'lucide-react';
import { Passenger } from '../types/api';

interface PassengerFormProps {
  passengers: Passenger[];
  selectedSeats: string[];
  onChange: (index: number, updated: Passenger) => void;
  errors: Record<string, string>;
}

export const PassengerForm: React.FC<PassengerFormProps> = ({
  passengers,
  selectedSeats,
  onChange,
  errors,
}) => {
  const handleFieldChange = (index: number, field: keyof Passenger, value: any) => {
    const updated = {
      ...passengers[index],
      [field]: value,
    };
    onChange(index, updated);
  };

  return (
    <div className="space-y-6">
      {passengers.map((pax, index) => {
        const assignedSeat = selectedSeats[index] || 'Auto-assigned';

        return (
          <div
            key={index}
            className="p-6 rounded-2xl bg-white border border-slate-200 shadow-sm space-y-5"
          >
            <div className="flex items-center justify-between pb-4 border-b border-slate-100">
              <div className="flex items-center gap-3">
                <div className="w-8 h-8 rounded-xl bg-secondary/10 text-secondary font-bold text-xs flex items-center justify-center">
                  {index + 1}
                </div>
                <div>
                  <h3 className="font-bold text-primary text-base">
                    Passenger {index + 1} ({index === 0 ? 'Lead Traveler' : 'Companion'})
                  </h3>
                  <p className="text-[11px] text-slate-500">Name must match government photo ID</p>
                </div>
              </div>

              <div className="flex items-center gap-1.5 px-3 py-1 rounded-full bg-slate-100 text-xs font-mono font-bold text-slate-700 border border-slate-200">
                <Armchair className="w-3.5 h-3.5 text-secondary" />
                <span>Seat: {assignedSeat}</span>
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-12 gap-4">
              {/* Title */}
              <div className="sm:col-span-2">
                <label className="block text-xs font-bold text-slate-700 mb-1">Title *</label>
                <select
                  value={pax.title || 'Mr'}
                  onChange={(e) => handleFieldChange(index, 'title', e.target.value)}
                  className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3 py-2 text-xs font-semibold text-primary focus:outline-none focus:border-secondary transition cursor-pointer"
                >
                  <option value="Mr">Mr</option>
                  <option value="Ms">Ms</option>
                  <option value="Mrs">Mrs</option>
                  <option value="Dr">Dr</option>
                </select>
              </div>

              {/* First Name */}
              <div className="sm:col-span-5">
                <label className="block text-xs font-bold text-slate-700 mb-1">
                  First / Given Name *
                </label>
                <input
                  type="text"
                  placeholder="e.g. Rahul"
                  value={pax.firstName || ''}
                  onChange={(e) => handleFieldChange(index, 'firstName', e.target.value)}
                  className={`w-full bg-slate-50 border rounded-xl px-3 py-2 text-xs font-semibold text-primary focus:outline-none transition ${
                    errors[`firstName_${index}`]
                      ? 'border-rose-400 focus:border-rose-500'
                      : 'border-slate-200 focus:border-secondary'
                  }`}
                />
                {errors[`firstName_${index}`] && (
                  <p className="text-[10px] text-rose-500 mt-1 font-semibold">
                    {errors[`firstName_${index}`]}
                  </p>
                )}
              </div>

              {/* Last Name */}
              <div className="sm:col-span-5">
                <label className="block text-xs font-bold text-slate-700 mb-1">
                  Last / Surname *
                </label>
                <input
                  type="text"
                  placeholder="e.g. Sharma"
                  value={pax.lastName || ''}
                  onChange={(e) => handleFieldChange(index, 'lastName', e.target.value)}
                  className={`w-full bg-slate-50 border rounded-xl px-3 py-2 text-xs font-semibold text-primary focus:outline-none transition ${
                    errors[`lastName_${index}`]
                      ? 'border-rose-400 focus:border-rose-500'
                      : 'border-slate-200 focus:border-secondary'
                  }`}
                />
                {errors[`lastName_${index}`] && (
                  <p className="text-[10px] text-rose-500 mt-1 font-semibold">
                    {errors[`lastName_${index}`]}
                  </p>
                )}
              </div>

              {/* Date of Birth */}
              <div className="sm:col-span-4">
                <label className="block text-xs font-bold text-slate-700 mb-1">Date of Birth *</label>
                <input
                  type="date"
                  value={pax.dateOfBirth || '1995-01-01'}
                  onChange={(e) => handleFieldChange(index, 'dateOfBirth', e.target.value)}
                  className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3 py-2 text-xs font-semibold text-primary focus:outline-none focus:border-secondary transition cursor-pointer"
                />
              </div>

              {/* Gender */}
              <div className="sm:col-span-4">
                <label className="block text-xs font-bold text-slate-700 mb-1">Gender *</label>
                <select
                  value={pax.gender || 'MALE'}
                  onChange={(e) => handleFieldChange(index, 'gender', e.target.value)}
                  className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3 py-2 text-xs font-semibold text-primary focus:outline-none focus:border-secondary transition cursor-pointer"
                >
                  <option value="MALE">Male</option>
                  <option value="FEMALE">Female</option>
                  <option value="OTHER">Other</option>
                </select>
              </div>

              {/* Nationality */}
              <div className="sm:col-span-4">
                <label className="block text-xs font-bold text-slate-700 mb-1">Nationality</label>
                <input
                  type="text"
                  placeholder="Indian"
                  value={pax.nationality || 'Indian'}
                  onChange={(e) => handleFieldChange(index, 'nationality', e.target.value)}
                  className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3 py-2 text-xs font-semibold text-primary focus:outline-none focus:border-secondary transition"
                />
              </div>
            </div>
          </div>
        );
      })}
    </div>
  );
};
