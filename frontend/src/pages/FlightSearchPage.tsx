import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import {
  Plane,
  SlidersHorizontal,
  AlertCircle,
  RefreshCw
} from 'lucide-react';
import { Flight, CabinClass } from '../types/api';
import { flightService } from '../services/flightService';
import { FlightSearchWidget } from '../components/FlightSearchWidget';
import { FlightCard } from '../components/FlightCard';
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
  const [error, setError] = useState<string | null>(null);

  // Filter States
  const [selectedAirlines, setSelectedAirlines] = useState<string[]>([]);
  const [priceLimit, setPriceLimit] = useState<number>(50000);
  const [maxPrice, setMaxPrice] = useState<number>(50000);
  const [nonStopOnly, setNonStopOnly] = useState<boolean>(false);
  const [timeWindow, setTimeWindow] = useState<string>('ALL');
  const [sortBy, setSortBy] = useState<string>('CHEAPEST');
  const [showMobileFilters, setShowMobileFilters] = useState<boolean>(false);

  const fetchFlights = async () => {
    try {
      setLoading(true);
      setError(null);

      const res = await flightService.searchFlights({
        origin,
        destination,
        departureDate,
        cabinClass,
        passengers,
        page: 0,
        size: 50,
      });

      if (res.success && res.data?.content) {
        setFlights(res.data.content);
        // Calculate max price from results
        const highestPrice = Math.max(
          ...res.data.content.map((f) => {
            const inv = f.cabinInventories?.find((c) => c.cabinClass === cabinClass);
            return inv ? inv.totalPrice : f.basePrice;
          }),
          10000
        );
        setMaxPrice(highestPrice);
        setPriceLimit(highestPrice);
      } else {
        setFlights([]);
      }
    } catch (err: any) {
      setError(err?.message || 'Failed to fetch flight schedules. Please try again.');
      setFlights([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchFlights();
  }, [origin, destination, departureDate, cabinClass, passengers]);

  // Extract unique airlines
  const availableAirlines = Array.from(new Set(flights.map((f) => f.airline)));

  // Client-side filtering & sorting
  const filteredFlights = flights
    .filter((flight) => {
      // Airline filter
      if (selectedAirlines.length > 0 && !selectedAirlines.includes(flight.airline)) {
        return false;
      }

      // Non-stop filter
      if (nonStopOnly && flight.stops > 0) {
        return false;
      }

      // Price filter
      const inv = flight.cabinInventories?.find((c) => c.cabinClass === cabinClass);
      const price = inv ? inv.totalPrice : flight.basePrice;
      if (price > priceLimit) {
        return false;
      }

      // Time Window filter
      if (timeWindow !== 'ALL') {
        const depHour = new Date(flight.departureTime).getUTCHours();
        if (timeWindow === 'MORNING' && (depHour < 6 || depHour >= 12)) return false;
        if (timeWindow === 'AFTERNOON' && (depHour < 12 || depHour >= 18)) return false;
        if (timeWindow === 'EVENING' && (depHour < 18 || depHour >= 24)) return false;
        if (timeWindow === 'NIGHT' && (depHour < 0 || depHour >= 6)) return false;
      }

      return true;
    })
    .sort((a, b) => {
      const getPrice = (f: Flight) => {
        const inv = f.cabinInventories?.find((c) => c.cabinClass === cabinClass);
        return inv ? inv.totalPrice : f.basePrice;
      };

      if (sortBy === 'CHEAPEST') return getPrice(a) - getPrice(b);
      if (sortBy === 'FASTEST') return a.durationMinutes - b.durationMinutes;
      if (sortBy === 'EARLIEST_DEPARTURE')
        return new Date(a.departureTime).getTime() - new Date(b.departureTime).getTime();
      if (sortBy === 'LATEST_DEPARTURE')
        return new Date(b.departureTime).getTime() - new Date(a.departureTime).getTime();
      return 0;
    });

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

  return (
    <div className="space-y-8 py-4">
      {/* Top Search Modify Bar */}
      <section>
        <FlightSearchWidget
          compact
          initialOrigin={origin}
          initialDestination={destination}
          initialDate={departureDate}
          initialCabin={cabinClass}
          initialPassengers={passengers}
          onSearch={(params) => {
            setSearchParams({
              origin: params.origin,
              destination: params.destination,
              departureDate: params.departureDate,
              cabinClass: params.cabinClass,
              passengers: params.passengers.toString(),
            });
          }}
        />
      </section>

      {/* Main Results Layout */}
      <section className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        {/* Left Filter Sidebar (Desktop) */}
        <aside className="hidden lg:block lg:col-span-4">
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
        <main className="lg:col-span-8 space-y-4">
          {/* Header Controls Bar */}
          <div className="p-4 rounded-2xl bg-slate-900/90 border border-slate-800 flex flex-wrap items-center justify-between gap-4">
            <div>
              <div className="flex items-center gap-2">
                <h2 className="font-bold text-white text-base sm:text-lg">
                  {origin} → {destination}
                </h2>
                <span className="text-xs text-slate-400 font-mono">
                  ({filteredFlights.length} {filteredFlights.length === 1 ? 'flight' : 'flights'})
                </span>
              </div>
              <p className="text-xs text-slate-400 mt-0.5">
                {new Date(departureDate).toLocaleDateString('en-US', {
                  weekday: 'short',
                  month: 'short',
                  day: 'numeric',
                  year: 'numeric',
                })}{' '}
                • {passengers} Traveler(s) • {cabinClass.replace('_', ' ')}
              </p>
            </div>

            {/* Mobile Filter Toggle */}
            <button
              type="button"
              onClick={() => setShowMobileFilters(!showMobileFilters)}
              className="lg:hidden px-3.5 py-2 rounded-xl bg-slate-800 hover:bg-slate-750 text-slate-200 text-xs font-semibold flex items-center gap-2 border border-slate-700"
            >
              <SlidersHorizontal className="w-4 h-4 text-sky-400" />
              <span>Filters ({selectedAirlines.length + (nonStopOnly ? 1 : 0)})</span>
            </button>
          </div>

          {/* Mobile Filter Drawer */}
          {showMobileFilters && (
            <div className="lg:hidden">
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
            <div className="space-y-4 py-8">
              {[1, 2, 3].map((i) => (
                <div
                  key={i}
                  className="rounded-2xl bg-slate-900 border border-slate-800 p-6 space-y-4 animate-pulse"
                >
                  <div className="flex justify-between items-center">
                    <div className="w-32 h-6 bg-slate-800 rounded"></div>
                    <div className="w-24 h-6 bg-slate-800 rounded"></div>
                  </div>
                  <div className="h-10 bg-slate-800/60 rounded"></div>
                </div>
              ))}
            </div>
          ) : error ? (
            <div className="p-8 rounded-2xl bg-slate-900 border border-slate-800 text-center space-y-4">
              <div className="w-12 h-12 rounded-full bg-rose-500/10 text-rose-400 border border-rose-500/20 flex items-center justify-center mx-auto">
                <AlertCircle className="w-6 h-6" />
              </div>
              <h3 className="font-bold text-white text-base">Search Error</h3>
              <p className="text-xs text-slate-400 max-w-sm mx-auto">{error}</p>
              <button
                onClick={fetchFlights}
                className="px-4 py-2 rounded-xl bg-slate-800 text-slate-200 hover:bg-slate-700 text-xs font-semibold inline-flex items-center gap-1.5 transition"
              >
                <RefreshCw className="w-3.5 h-3.5" />
                Retry Search
              </button>
            </div>
          ) : filteredFlights.length === 0 ? (
            <div className="p-12 rounded-2xl bg-slate-900 border border-slate-800 text-center space-y-4">
              <div className="w-16 h-16 rounded-2xl bg-sky-500/10 text-sky-400 border border-sky-500/20 flex items-center justify-center mx-auto">
                <Plane className="w-8 h-8 transform -rotate-45" />
              </div>
              <h3 className="font-bold text-white text-lg">No Flights Found</h3>
              <p className="text-xs text-slate-400 max-w-md mx-auto">
                No flights match your search criteria from {origin} to {destination} on{' '}
                {departureDate}. Try adjusting your filters, selecting a different date, or choosing nearby airports.
              </p>
              <button
                onClick={handleResetFilters}
                className="px-4 py-2 rounded-xl bg-slate-800 text-slate-200 hover:bg-slate-700 text-xs font-semibold transition"
              >
                Reset Filters
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
