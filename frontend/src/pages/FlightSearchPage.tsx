import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useSearchParams } from 'react-router-dom';
import {
  Plane,
  SlidersHorizontal,
  AlertCircle,
  RefreshCw,
  Edit3,
  ChevronDown,
  ChevronUp,
  Calendar,
  Users,
  ArrowLeftRight,
  Sparkles
} from 'lucide-react';
import { Flight, CabinClass } from '../types/api';
import { flightService } from '../services/flightService';
import { FlightSearchWidget } from '../components/FlightSearchWidget';
import { FlightCard } from '../components/FlightCard';
import { FlightCardSkeleton } from '../components/FlightCardSkeleton';
import { FlightFilters } from '../components/FlightFilters';

export const FlightSearchPage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();

  const origin = searchParams.get('origin') || 'DEL';
  const destination = searchParams.get('destination') || 'BOM';
  const departureDate = searchParams.get('departureDate') || new Date(Date.now() + 86400000).toISOString().split('T')[0];
  const cabinClass = (searchParams.get('cabinClass') as CabinClass) || 'ECONOMY';
  const passengers = parseInt(searchParams.get('passengers') || '1', 10);

  const [flights, setFlights] = useState<Flight[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [slowMessage, setSlowMessage] = useState<string | null>(null);
  const [lastUpdated, setLastUpdated] = useState<Date | null>(null);
  const [timeAgoText, setTimeAgoText] = useState<string>('');
  const [error, setError] = useState<string | null>(null);
  const [showModifySearch, setShowModifySearch] = useState<boolean>(false);

  // Filter States
  const [selectedAirlines, setSelectedAirlines] = useState<string[]>([]);
  const [priceLimit, setPriceLimit] = useState<number>(50000);
  const [maxPrice, setMaxPrice] = useState<number>(50000);
  const [nonStopOnly, setNonStopOnly] = useState<boolean>(false);
  const [timeWindow, setTimeWindow] = useState<string>('ALL');
  const [sortBy, setSortBy] = useState<string>('CHEAPEST');
  const [showMobileFilters, setShowMobileFilters] = useState<boolean>(false);
  const searchSeqRef = useRef<number>(0);
  const abortControllerRef = useRef<AbortController | null>(null);

  // Format "Updated X ago"
  useEffect(() => {
    if (!lastUpdated) return;

    const updateAgo = () => {
      const seconds = Math.floor((Date.now() - lastUpdated.getTime()) / 1000);
      if (seconds < 10) {
        setTimeAgoText('Updated just now');
      } else if (seconds < 60) {
        setTimeAgoText(`Updated ${seconds}s ago`);
      } else {
        const mins = Math.floor(seconds / 60);
        setTimeAgoText(`Updated ${mins}m ago`);
      }
    };

    updateAgo();
    const timer = setInterval(updateAgo, 5000);
    return () => clearInterval(timer);
  }, [lastUpdated]);

  const fetchFlights = useCallback(async () => {
    const seq = ++searchSeqRef.current;

    // Abort previous search request if running
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }
    const abortController = new AbortController();
    abortControllerRef.current = abortController;

    const searchParamsObj = {
      origin,
      destination,
      departureDate,
      cabinClass,
      passengers,
      page: 0,
      size: 50,
    };

    // Stale-While-Revalidate caching
    const cached = flightService.getCachedSearch(searchParamsObj);
    if (cached && cached.data?.data?.content) {
      setFlights(cached.data.data.content);
      setLastUpdated(new Date(cached.timestamp));
      setLoading(false);
      setError(null);
    } else {
      setLoading(true);
      setError(null);
    }

    const slowTimer = setTimeout(() => {
      if (seq === searchSeqRef.current) {
        setSlowMessage('Connecting to airline GDS inventory...');
      }
    }, 2000);

    try {
      const res = await flightService.searchFlights(searchParamsObj);
      if (seq !== searchSeqRef.current) return;

      const items = res.data.content || [];
      setFlights(items);
      setLastUpdated(new Date());

      // Compute dynamic max budget
      if (items.length > 0) {
        const highestFare = Math.max(
          ...items.map((f) => {
            const inv = f.cabinInventories?.find((c) => c.cabinClass === cabinClass);
            return inv ? inv.totalPrice : f.basePrice;
          })
        );
        const roundedMax = Math.ceil(highestFare / 1000) * 1000 + 2000;
        setMaxPrice(roundedMax);
        setPriceLimit(roundedMax);
      }
      setError(null);
    } catch (err: any) {
      if (err?.name === 'CanceledError' || err?.code === 'ERR_CANCELED') {
        return;
      }
      if (seq !== searchSeqRef.current) return;
      if (!cached) {
        setError(err.message || 'Unable to retrieve live flight schedules.');
      }
    } finally {
      clearTimeout(slowTimer);
      if (seq === searchSeqRef.current) {
        setLoading(false);
        setSlowMessage(null);
      }
    }
  }, [origin, destination, departureDate, cabinClass, passengers]);

  useEffect(() => {
    fetchFlights();
  }, [fetchFlights]);

  // Extract available airlines
  const availableAirlines = Array.from(new Set(flights.map((f) => f.airline))).filter(Boolean);

  const handleAirlineToggle = (airline: string) => {
    setSelectedAirlines((prev) =>
      prev.includes(airline) ? prev.filter((a) => a !== airline) : [...prev, airline]
    );
  };

  const handleResetFilters = () => {
    setSelectedAirlines([]);
    setPriceLimit(maxPrice);
    setNonStopOnly(false);
    setTimeWindow('ALL');
    setSortBy('CHEAPEST');
  };

  // Filter & Sort Logic
  const filteredFlights = flights
    .filter((f) => {
      const inv = f.cabinInventories?.find((c) => c.cabinClass === cabinClass);
      const price = (inv ? inv.totalPrice : f.basePrice) * passengers;

      if (price > priceLimit) return false;
      if (selectedAirlines.length > 0 && !selectedAirlines.includes(f.airline)) return false;
      if (nonStopOnly && f.stops > 0) return false;

      // Time Window filtering
      if (timeWindow !== 'ALL') {
        const depHour = new Date(f.departureTime).getHours();
        if (timeWindow === 'MORNING' && (depHour < 6 || depHour >= 12)) return false;
        if (timeWindow === 'AFTERNOON' && (depHour < 12 || depHour >= 18)) return false;
        if (timeWindow === 'EVENING' && (depHour < 18 || depHour >= 24)) return false;
        if (timeWindow === 'NIGHT' && (depHour < 0 || depHour >= 6)) return false;
      }

      return true;
    })
    .sort((a, b) => {
      const invA = a.cabinInventories?.find((c) => c.cabinClass === cabinClass);
      const priceA = invA ? invA.totalPrice : a.basePrice;

      const invB = b.cabinInventories?.find((c) => c.cabinClass === cabinClass);
      const priceB = invB ? invB.totalPrice : b.basePrice;

      if (sortBy === 'CHEAPEST') return priceA - priceB;
      if (sortBy === 'FASTEST') return a.durationMinutes - b.durationMinutes;
      if (sortBy === 'EARLIEST_DEPARTURE') {
        return new Date(a.departureTime).getTime() - new Date(b.departureTime).getTime();
      }
      if (sortBy === 'LATEST_DEPARTURE') {
        return new Date(b.departureTime).getTime() - new Date(a.departureTime).getTime();
      }
      return 0;
    });

  return (
    <div className="space-y-6 pb-16">
      {/* 1. TOP ROUTE SUMMARY HEADER */}
      <section className="p-4 sm:p-5 rounded-2xl bg-white border border-slate-200 shadow-sm">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-primary text-white flex items-center justify-center font-bold shadow-sm">
              <Plane className="w-5 h-5 text-secondary transform rotate-45" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h1 className="text-lg sm:text-xl font-black text-primary tracking-tight">
                  {origin} ➔ {destination}
                </h1>
                <button
                  type="button"
                  title="Swap Origin and Destination"
                  onClick={() => {
                    setSearchParams({
                      origin: destination,
                      destination: origin,
                      departureDate,
                      cabinClass,
                      passengers: passengers.toString(),
                    });
                  }}
                  className="p-1 rounded-lg bg-slate-100 hover:bg-slate-200 text-secondary transition-transform active:rotate-180 duration-300"
                >
                  <ArrowLeftRight className="w-3.5 h-3.5" />
                </button>
                <span className="text-xs font-bold text-secondary bg-secondary/10 px-2.5 py-0.5 rounded-full border border-secondary/20">
                  {cabinClass.replace('_', ' ')}
                </span>
              </div>

              <div className="flex items-center gap-3 text-xs text-slate-500 mt-0.5">
                <span className="flex items-center gap-1">
                  <Calendar className="w-3.5 h-3.5 text-slate-400" />
                  {new Date(departureDate).toLocaleDateString('en-US', {
                    weekday: 'short',
                    month: 'short',
                    day: 'numeric',
                    year: 'numeric',
                  })}
                </span>
                <span>•</span>
                <span className="flex items-center gap-1">
                  <Users className="w-3.5 h-3.5 text-slate-400" />
                  {passengers} {passengers === 1 ? 'Traveler' : 'Travelers'}
                </span>
              </div>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => setShowModifySearch(!showModifySearch)}
              className="px-4 py-2 rounded-xl bg-slate-100 hover:bg-slate-200 text-primary text-xs font-bold flex items-center gap-1.5 border border-slate-200 transition"
            >
              <Edit3 className="w-3.5 h-3.5 text-secondary" />
              <span>{showModifySearch ? 'Hide Search Form' : 'Modify Search'}</span>
              {showModifySearch ? <ChevronUp className="w-3.5 h-3.5" /> : <ChevronDown className="w-3.5 h-3.5" />}
            </button>
          </div>
        </div>

        {/* Expandable Search Widget */}
        {showModifySearch && (
          <div className="mt-4 pt-4 border-t border-slate-100 animate-slide-up">
            <FlightSearchWidget
              compact
              initialOrigin={origin}
              initialDestination={destination}
              initialDate={departureDate}
              initialCabin={cabinClass}
              initialPassengers={passengers}
              onSearch={(params) => {
                setShowModifySearch(false);
                setSearchParams({
                  origin: params.origin,
                  destination: params.destination,
                  departureDate: params.departureDate,
                  cabinClass: params.cabinClass,
                  passengers: params.passengers.toString(),
                });
              }}
            />
          </div>
        )}
      </section>

      {/* 2. RESULTS & FILTERS GRID */}
      <section className="grid grid-cols-1 xl:grid-cols-12 gap-6 items-start">
        {/* Left Filter Sidebar (Desktop) */}
        <aside className="hidden xl:block xl:col-span-3">
          <FlightFilters
            availableAirlines={availableAirlines}
            selectedAirlines={selectedAirlines}
            onAirlineToggle={handleAirlineToggle}
            maxPrice={maxPrice}
            priceLimit={priceLimit}
            onPriceChange={setPriceLimit}
            nonStopOnly={nonStopOnly}
            onNonStopToggle={setNonStopOnly}
            timeWindow={timeWindow}
            onTimeWindowChange={setTimeWindow}
            sortBy={sortBy}
            onSortChange={setSortBy}
            onReset={handleResetFilters}
          />
        </aside>

        {/* Right Flight List Column */}
        <main className="xl:col-span-9 space-y-4">
          {/* Header Controls Bar */}
          <div className="p-4 rounded-2xl bg-white border border-slate-200 shadow-sm flex flex-wrap items-center justify-between gap-4">
            <div>
              <div className="flex items-center gap-2">
                <h2 className="font-black text-primary text-base">
                  Available Flights
                </h2>
                <span className="text-xs font-bold text-secondary bg-secondary/10 px-2 py-0.5 rounded border border-secondary/20">
                  {filteredFlights.length} {filteredFlights.length === 1 ? 'flight found' : 'flights found'}
                </span>
                {timeAgoText && (
                  <span className="text-[11px] text-emerald-700 font-semibold bg-emerald-50 border border-emerald-200 px-2 py-0.5 rounded-full flex items-center gap-1">
                    <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
                    {timeAgoText}
                  </span>
                )}
              </div>
              <p className="text-xs text-slate-500 mt-0.5">
                All fares in INR (₹) with airport taxes and mandatory fees included upfront
              </p>
            </div>

            {/* Mobile Filter Toggle */}
            <button
              type="button"
              onClick={() => setShowMobileFilters(!showMobileFilters)}
              className="xl:hidden px-4 py-2 rounded-xl bg-slate-100 hover:bg-slate-200 text-primary text-xs font-bold flex items-center gap-2 border border-slate-200"
            >
              <SlidersHorizontal className="w-4 h-4 text-secondary" />
              <span>Filters ({selectedAirlines.length + (nonStopOnly ? 1 : 0)})</span>
            </button>
          </div>

          {/* Mobile Filter Drawer */}
          {showMobileFilters && (
            <div className="xl:hidden animate-slide-up">
              <FlightFilters
                availableAirlines={availableAirlines}
                selectedAirlines={selectedAirlines}
                onAirlineToggle={handleAirlineToggle}
                maxPrice={maxPrice}
                priceLimit={priceLimit}
                onPriceChange={setPriceLimit}
                nonStopOnly={nonStopOnly}
                onNonStopToggle={setNonStopOnly}
                timeWindow={timeWindow}
                onTimeWindowChange={setTimeWindow}
                sortBy={sortBy}
                onSortChange={setSortBy}
                onReset={handleResetFilters}
              />
            </div>
          )}

          {/* Flight Results Content */}
          {loading ? (
            <div className="space-y-4 py-2">
              {slowMessage && (
                <div className="p-4 rounded-2xl bg-secondary/10 border border-secondary/20 text-secondary text-xs flex items-center gap-3 animate-fade-in shadow-sm">
                  <div className="w-2.5 h-2.5 rounded-full bg-secondary animate-ping shrink-0" />
                  <div className="flex-1 font-semibold">
                    {slowMessage}
                  </div>
                </div>
              )}
              {[1, 2, 3, 4].map((i) => (
                <FlightCardSkeleton key={i} />
              ))}
            </div>
          ) : error ? (
            <div className="p-10 rounded-2xl bg-white border border-slate-200 text-center space-y-4 shadow-sm">
              <div className="w-12 h-12 rounded-xl bg-rose-50 text-rose-500 border border-rose-200 flex items-center justify-center mx-auto">
                <AlertCircle className="w-6 h-6" />
              </div>
              <h3 className="font-bold text-primary text-lg">Unable to Load Flights</h3>
              <p className="text-xs text-slate-500 max-w-sm mx-auto">{error}</p>
              <button
                onClick={fetchFlights}
                className="px-4 py-2 rounded-xl bg-primary text-white hover:bg-primary-hover text-xs font-bold inline-flex items-center gap-2 transition"
              >
                <RefreshCw className="w-4 h-4" />
                Retry Search
              </button>
            </div>
          ) : filteredFlights.length === 0 ? (
            <div className="p-12 rounded-2xl bg-white border border-slate-200 text-center space-y-4 shadow-sm">
              <div className="w-14 h-14 rounded-2xl bg-secondary/10 text-secondary border border-secondary/20 flex items-center justify-center mx-auto">
                <Plane className="w-7 h-7 transform -rotate-45" />
              </div>
              <div className="space-y-1">
                <h3 className="font-black text-primary text-lg">No Flights Found</h3>
                <p className="text-xs text-slate-500 max-w-md mx-auto leading-relaxed">
                  No flights match your filters between <strong className="text-primary">{origin}</strong> and{' '}
                  <strong className="text-primary">{destination}</strong> on {departureDate}.
                </p>
              </div>
              <button
                type="button"
                onClick={handleResetFilters}
                className="px-4 py-2 rounded-xl bg-primary hover:bg-primary-hover text-white text-xs font-bold transition"
              >
                Reset All Filters
              </button>
            </div>
          ) : (
            <div className="space-y-4">
              {filteredFlights.map((flight) => (
                <FlightCard
                  key={flight.id}
                  flight={flight}
                  selectedCabin={cabinClass}
                  passengerCount={passengers}
                />
              ))}
            </div>
          )}
        </main>
      </section>
    </div>
  );
};
