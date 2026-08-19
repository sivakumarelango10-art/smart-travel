import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Plane, Save, ArrowLeft, Plus, Trash2, AlertTriangle } from 'lucide-react';
import { adminFlightService } from '../../services/adminFlightService';
import { useAdminToast } from '../../components/admin/AdminToast';
import { CabinClass, FlightStatus } from '../../types/flight';
import { CabinInventoryDto } from '../../types/admin';

const CABIN_CLASSES: CabinClass[] = ['ECONOMY', 'PREMIUM_ECONOMY', 'BUSINESS', 'FIRST'];
const FLIGHT_STATUSES: FlightStatus[] = ['SCHEDULED', 'BOARDING', 'DEPARTED', 'IN_AIR', 'LANDED', 'ARRIVED', 'DELAYED', 'CANCELLED'];

interface FormData {
  flightNumber: string;
  airline: string;
  airlineCode: string;
  departureCode: string;
  departureName: string;
  departureCity: string;
  departureCountry: string;
  departureTerminal: string;
  arrivalCode: string;
  arrivalName: string;
  arrivalCity: string;
  arrivalCountry: string;
  arrivalTerminal: string;
  departureTime: string;
  arrivalTime: string;
  aircraftModel: string;
  basePrice: string;
  totalSeats: string;
  status: FlightStatus;
  cabinInventories: CabinInventoryDto[];
}

const emptyForm: FormData = {
  flightNumber: '', airline: '', airlineCode: '',
  departureCode: '', departureName: '', departureCity: '', departureCountry: 'India', departureTerminal: '',
  arrivalCode: '', arrivalName: '', arrivalCity: '', arrivalCountry: 'India', arrivalTerminal: '',
  departureTime: '', arrivalTime: '',
  aircraftModel: '', basePrice: '', totalSeats: '',
  status: 'SCHEDULED',
  cabinInventories: [],
};

export const AdminFlightFormPage: React.FC = () => {
  const { flightId } = useParams<{ flightId?: string }>();
  const isEdit = !!flightId && flightId !== 'new';
  const navigate = useNavigate();
  const { showToast } = useAdminToast();

  const [form, setForm] = useState<FormData>(emptyForm);
  const [loading, setLoading] = useState(false);
  const [fetchLoading, setFetchLoading] = useState(isEdit);
  const [errors, setErrors] = useState<Partial<Record<keyof FormData, string>>>({});

  useEffect(() => {
    if (!isEdit || !flightId) return;
    (async () => {
      setFetchLoading(true);
      try {
        const res = await adminFlightService.getFlightById(flightId);
        const f = res.data;
        setForm({
          flightNumber: f.flightNumber ?? '',
          airline: f.airline ?? '',
          airlineCode: f.airlineCode ?? '',
          departureCode: f.departureAirport?.code ?? '',
          departureName: f.departureAirport?.name ?? '',
          departureCity: f.departureAirport?.city ?? '',
          departureCountry: f.departureAirport?.country ?? 'India',
          departureTerminal: f.departureAirport?.terminal ?? '',
          arrivalCode: f.arrivalAirport?.code ?? '',
          arrivalName: f.arrivalAirport?.name ?? '',
          arrivalCity: f.arrivalAirport?.city ?? '',
          arrivalCountry: f.arrivalAirport?.country ?? 'India',
          arrivalTerminal: f.arrivalAirport?.terminal ?? '',
          departureTime: f.departureTime ? new Date(f.departureTime).toISOString().slice(0, 16) : '',
          arrivalTime: f.arrivalTime ? new Date(f.arrivalTime).toISOString().slice(0, 16) : '',
          aircraftModel: f.aircraftModel ?? '',
          basePrice: f.basePrice?.toString() ?? '',
          totalSeats: f.totalSeats?.toString() ?? '',
          status: f.status ?? 'SCHEDULED',
          cabinInventories: f.cabinInventories?.map(c => ({
            cabinClass: c.cabinClass,
            totalSeats: c.totalSeats,
            availableSeats: c.availableSeats,
            basePrice: c.basePrice,
            taxAmount: c.taxAmount,
            feeAmount: c.feeAmount,
          })) ?? [],
        });
      } catch {
        showToast('error', 'Failed to load flight data');
        navigate('/admin/flights');
      } finally {
        setFetchLoading(false);
      }
    })();
  }, [flightId, isEdit]);

  const set = <K extends keyof FormData>(key: K, value: FormData[K]) => {
    setForm(prev => ({ ...prev, [key]: value }));
    setErrors(prev => ({ ...prev, [key]: undefined }));
  };

  const validate = (): boolean => {
    const e: Partial<Record<keyof FormData, string>> = {};
    if (!form.flightNumber.trim()) e.flightNumber = 'Required';
    if (!form.airline.trim()) e.airline = 'Required';
    if (!form.airlineCode.trim()) e.airlineCode = 'Required';
    if (!form.departureCode.trim()) e.departureCode = 'Required';
    if (!form.arrivalCode.trim()) e.arrivalCode = 'Required';
    if (form.departureCode.trim().toUpperCase() === form.arrivalCode.trim().toUpperCase()) e.arrivalCode = 'Arrival must differ from departure';
    if (!form.departureTime) e.departureTime = 'Required';
    if (!form.arrivalTime) e.arrivalTime = 'Required';
    if (form.departureTime && form.arrivalTime && new Date(form.arrivalTime) <= new Date(form.departureTime)) e.arrivalTime = 'Arrival must be after departure';
    if (!form.aircraftModel.trim()) e.aircraftModel = 'Required';
    if (!form.basePrice || isNaN(Number(form.basePrice)) || Number(form.basePrice) <= 0) e.basePrice = 'Must be > 0';
    if (!form.totalSeats || isNaN(Number(form.totalSeats)) || Number(form.totalSeats) < 1) e.totalSeats = 'Must be >= 1';
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSubmit = async () => {
    if (!validate()) return;
    setLoading(true);
    try {
      const payload = {
        flightNumber: form.flightNumber.trim(),
        airline: form.airline.trim(),
        airlineCode: form.airlineCode.trim(),
        departureAirport: { code: form.departureCode.trim(), name: form.departureName.trim(), city: form.departureCity.trim(), country: form.departureCountry.trim(), terminal: form.departureTerminal.trim() || undefined },
        arrivalAirport: { code: form.arrivalCode.trim(), name: form.arrivalName.trim(), city: form.arrivalCity.trim(), country: form.arrivalCountry.trim(), terminal: form.arrivalTerminal.trim() || undefined },
        departureTime: new Date(form.departureTime).toISOString(),
        arrivalTime: new Date(form.arrivalTime).toISOString(),
        aircraftModel: form.aircraftModel.trim(),
        basePrice: Number(form.basePrice),
        totalSeats: Number(form.totalSeats),
        status: form.status,
        cabinInventories: form.cabinInventories.length > 0 ? form.cabinInventories : undefined,
      };

      if (isEdit && flightId) {
        await adminFlightService.updateFlight(flightId, payload);
        showToast('success', 'Flight updated', `Flight ${form.flightNumber} updated successfully.`);
      } else {
        await adminFlightService.createFlight(payload);
        showToast('success', 'Flight created', `Flight ${form.flightNumber} created successfully.`);
      }
      navigate('/admin/flights');
    } catch (e: unknown) {
      const err = e as { message?: string };
      showToast('error', 'Save failed', err?.message ?? 'Could not save flight');
    } finally {
      setLoading(false);
    }
  };

  const addCabin = () => {
    const usedClasses = form.cabinInventories.map(c => c.cabinClass);
    const next = CABIN_CLASSES.find(c => !usedClasses.includes(c));
    if (!next) return;
    setForm(prev => ({
      ...prev,
      cabinInventories: [...prev.cabinInventories, { cabinClass: next, totalSeats: 0, availableSeats: 0, basePrice: 0 }],
    }));
  };

  const updateCabin = (index: number, field: keyof CabinInventoryDto, value: string | number) => {
    setForm(prev => ({
      ...prev,
      cabinInventories: prev.cabinInventories.map((c, i) => i === index ? { ...c, [field]: value } : c),
    }));
  };

  const removeCabin = (index: number) => {
    setForm(prev => ({ ...prev, cabinInventories: prev.cabinInventories.filter((_, i) => i !== index) }));
  };

  const Field = ({ label, field, type = 'text', placeholder = '', halfWidth = false }: { label: string; field: keyof FormData; type?: string; placeholder?: string; halfWidth?: boolean }) => (
    <div className={halfWidth ? '' : ''}>
      <label className="block text-xs font-medium text-slate-400 mb-1">{label}</label>
      <input
        type={type}
        value={form[field] as string}
        onChange={e => set(field, e.target.value as FormData[typeof field])}
        placeholder={placeholder}
        className={`w-full px-3 py-2 bg-slate-800 border rounded-lg text-sm text-white placeholder-slate-500 focus:outline-none focus:ring-1 transition ${errors[field] ? 'border-rose-500 focus:border-rose-500 focus:ring-rose-500/30' : 'border-slate-700 focus:border-sky-500 focus:ring-sky-500/30'}`}
      />
      {errors[field] && <p className="text-xs text-rose-400 mt-1">{errors[field]}</p>}
    </div>
  );

  if (fetchLoading) {
    return <div className="flex items-center justify-center h-64"><div className="w-8 h-8 border-4 border-sky-500/30 border-t-sky-500 rounded-full animate-spin" /></div>;
  }

  return (
    <div className="space-y-6 max-w-3xl">
      <div className="flex items-center gap-3">
        <button onClick={() => navigate('/admin/flights')} className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-xl transition">
          <ArrowLeft className="w-5 h-5" />
        </button>
        <h1 className="text-xl font-bold text-white flex items-center gap-2">
          <Plane className="w-5 h-5 text-sky-400" />
          {isEdit ? 'Edit Flight' : 'Create Flight'}
        </h1>
      </div>

      <div className="bg-slate-900 border border-slate-800 rounded-2xl divide-y divide-slate-800">
        {/* Basic Info */}
        <div className="p-6 space-y-4">
          <h3 className="text-sm font-semibold text-slate-300 uppercase tracking-wide">Basic Information</h3>
          <div className="grid grid-cols-2 gap-4">
            <Field label="Flight Number *" field="flightNumber" placeholder="AI-101" />
            <Field label="Airline Code *" field="airlineCode" placeholder="AI" />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <Field label="Airline Name *" field="airline" placeholder="Air India" />
            <Field label="Aircraft Model *" field="aircraftModel" placeholder="Airbus A321neo" />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <Field label="Base Price (INR) *" field="basePrice" type="number" placeholder="5000" />
            <Field label="Total Seats *" field="totalSeats" type="number" placeholder="180" />
          </div>
          <div>
            <label className="block text-xs font-medium text-slate-400 mb-1">Status</label>
            <select
              value={form.status}
              onChange={e => set('status', e.target.value as FlightStatus)}
              className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-sm text-white focus:outline-none focus:border-sky-500"
            >
              {FLIGHT_STATUSES.map(s => <option key={s} value={s}>{s}</option>)}
            </select>
          </div>
        </div>

        {/* Departure */}
        <div className="p-6 space-y-4">
          <h3 className="text-sm font-semibold text-slate-300 uppercase tracking-wide">Departure Airport</h3>
          <div className="grid grid-cols-2 gap-4">
            <Field label="IATA Code *" field="departureCode" placeholder="DEL" />
            <Field label="Terminal" field="departureTerminal" placeholder="T3" />
          </div>
          <Field label="Airport Name" field="departureName" placeholder="Indira Gandhi International Airport" />
          <div className="grid grid-cols-2 gap-4">
            <Field label="City" field="departureCity" placeholder="New Delhi" />
            <Field label="Country" field="departureCountry" placeholder="India" />
          </div>
          <Field label="Departure Time *" field="departureTime" type="datetime-local" />
        </div>

        {/* Arrival */}
        <div className="p-6 space-y-4">
          <h3 className="text-sm font-semibold text-slate-300 uppercase tracking-wide">Arrival Airport</h3>
          <div className="grid grid-cols-2 gap-4">
            <Field label="IATA Code *" field="arrivalCode" placeholder="BOM" />
            <Field label="Terminal" field="arrivalTerminal" placeholder="T2" />
          </div>
          <Field label="Airport Name" field="arrivalName" placeholder="CSMI Airport" />
          <div className="grid grid-cols-2 gap-4">
            <Field label="City" field="arrivalCity" placeholder="Mumbai" />
            <Field label="Country" field="arrivalCountry" placeholder="India" />
          </div>
          <Field label="Arrival Time *" field="arrivalTime" type="datetime-local" />
          {errors.arrivalTime && <p className="text-xs text-rose-400 flex items-center gap-1"><AlertTriangle className="w-3 h-3" />{errors.arrivalTime}</p>}
        </div>

        {/* Cabin Inventories */}
        <div className="p-6 space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-semibold text-slate-300 uppercase tracking-wide">Cabin Inventories</h3>
            <button
              onClick={addCabin}
              disabled={form.cabinInventories.length >= 4}
              className="flex items-center gap-1.5 px-3 py-1.5 text-xs text-sky-400 border border-sky-500/30 hover:bg-sky-500/10 rounded-lg transition disabled:opacity-40"
            >
              <Plus className="w-3.5 h-3.5" /> Add Cabin
            </button>
          </div>
          {form.cabinInventories.length === 0 && (
            <p className="text-sm text-slate-500">No cabin inventories configured. Default ECONOMY will be used.</p>
          )}
          {form.cabinInventories.map((cabin, i) => (
            <div key={i} className="p-4 bg-slate-800/60 border border-slate-700 rounded-xl space-y-3">
              <div className="flex items-center justify-between">
                <select
                  value={cabin.cabinClass}
                  onChange={e => updateCabin(i, 'cabinClass', e.target.value as CabinClass)}
                  className="px-3 py-1.5 bg-slate-800 border border-slate-600 rounded-lg text-sm text-white focus:outline-none focus:border-sky-500"
                >
                  {CABIN_CLASSES.map(c => <option key={c} value={c}>{c.replace(/_/g, ' ')}</option>)}
                </select>
                <button onClick={() => removeCabin(i)} className="p-1.5 text-rose-400 hover:bg-rose-500/10 rounded-lg transition">
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
              <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
                {[
                  { label: 'Total Seats', field: 'totalSeats' as const },
                  { label: 'Available Seats', field: 'availableSeats' as const },
                  { label: 'Base Price (INR)', field: 'basePrice' as const },
                  { label: 'Tax Amount', field: 'taxAmount' as const },
                ].map(({ label, field }) => (
                  <div key={field}>
                    <label className="block text-[11px] text-slate-400 mb-1">{label}</label>
                    <input
                      type="number"
                      value={(cabin[field] ?? '') as number}
                      onChange={e => updateCabin(i, field, Number(e.target.value))}
                      className="w-full px-2.5 py-1.5 bg-slate-800 border border-slate-700 rounded-lg text-sm text-white focus:outline-none focus:border-sky-500"
                    />
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Submit */}
      <div className="flex justify-end gap-3">
        <button onClick={() => navigate('/admin/flights')} className="px-5 py-2.5 text-sm text-slate-400 hover:text-white bg-slate-800 hover:bg-slate-700 border border-slate-700 rounded-xl transition">
          Discard
        </button>
        <button
          onClick={handleSubmit}
          disabled={loading}
          className="flex items-center gap-2 px-5 py-2.5 text-sm font-medium text-white bg-gradient-to-r from-sky-600 to-indigo-600 hover:from-sky-500 hover:to-indigo-500 rounded-xl shadow-lg shadow-sky-500/20 transition disabled:opacity-50"
        >
          {loading && <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />}
          <Save className="w-4 h-4" />
          {isEdit ? 'Save Changes' : 'Create Flight'}
        </button>
      </div>
    </div>
  );
};
