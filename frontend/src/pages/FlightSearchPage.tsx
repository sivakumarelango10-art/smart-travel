import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useSearchParams } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
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
  ArrowLeftRight
} from 'lucide-react';
import { Flight, CabinClass } from '../types/api';
import { flightService } from '../services/flightService';
import { FlightSearchWidget } from '../components/FlightSearchWidget';
import { FlightCard } from '../components/FlightCard';
import { FlightCardSkeleton } from '../components/FlightCardSkeleton';
import { FlightFilters } from '../components/FlightFilters';
import { RecommendationsSection } from '../components/RecommendationsSection';
import { staggerContainerVariants } from '../lib/motion';

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
  const [maxPrice, setMaxPrice] = useState<number>(0);
  const [priceLimit, setPriceLimit] = useState<number>(0);
  const [nonStopOnly, setNonStopOnly] = useState<boolean>(false);
  const [timeWindow, setTimeWindow] = useState<string>('ALL');
  const [sortBy, setSortBy] = useState<string>('CHEAPEST');
  const [showMobileFilters, setShowMobileFilters] = useState<boolean>(false);

  const slowTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const applyFlightData = useCallback((data: any) => {
    let flightList: Flight[] = [];
    if (Array.isArray(data)) {
      flightList = data;
    } else if (data && Array.isArray(data.content)) {
      flightList = data.content;
    }

    setFlights(flightList);
    setLastUpdated(new Date());

    if (flightList.length > 0) {
      const prices = flightList.map((f: Flight) => {
        const inv = f.cabinInventories?.find((c) => c.cabinClass === cabinClass);
        return (inv ? inv.totalPrice : f.basePrice) * passengers;
      });
      const highestPrice = Math.max(...prices, 15000);
      setMaxPrice(highestPrice);
      setPriceLimit((prev) => (prev === 0 || prev < highestPrice ? highestPrice : prev));
    }
  }, [cabinClass, passengers]);

  const fetchFlights = useCallback(async () => {
    const searchParamsObj = { origin, destination, departureDate, cabinClass, passengers };
    
    // Check if we have instant cached data to display immediately
    const cached = flightService.getCachedSearch(searchParamsObj);
    if (cached && cached.data && cached.data.data) {
      applyFlightData(cached.data.data);
      setLoading(false);
    } else {
      setLoading(true);
    }

    setError(null);
    setSlowMessage(null);

    // Warm-up timeout indicator for Render backend if request takes longer
    slowTimerRef.current = setTimeout(() => {
      setSlowMessage('Connecting to live airline reservation systems. Synchronizing real-time seat availability...');
    }, 3500);

    try {
      const res = await flightService.searchFlights(searchParamsObj);
      if (res && res.data) {
        applyFlightData(res.data);
      } else {
        setFlights([]);
      }
    } catch (err: any) {
      if (flights.length === 0) {
        setError(err.message || 'Failed to retrieve flights for this route.');
      }
    } finally {
      if (slowTimerRef.current) clearTimeout(slowTimerRef.current);
      setLoading(false);
      setSlowMessage(null);
    }
  }, [origin, destination, departureDate, cabinClass, passengers, applyFlightData, flights.length]);

  useEffect(() => {
    fetchFlights();
    return () => {
      if (slowTimerRef.current) clearTimeout(slowTimerRef.current);
    };
  }, [origin, destination, departureDate, cabinClass, passengers]);


  // Live "Updated X seconds ago" counter
  useEffect(() => {
    if (!lastUpdated) return;

    const interval = setInterval(() => {
      const diffSec = Math.floor((Date.now() - lastUpdated.getTime()) / 1000);
      if (diffSec < 60) {
        setTimeAgoText(`Updated ${diffSec}s ago`);
      } else {
        const mins = Math.floor(diffSec / 60);
        setTimeAgoText(`Updated ${mins}m ago`);
      }
    }, 5000);

    return () => clearInterval(interval);
  }, [lastUpdated]);

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
      <section className="p-4 sm:p-5 rounded-2xl bg-[#14161F] border border-white/10 shadow-xl">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-[#181A22] text-amber-400 border border-white/10 flex items-center justify-center font-bold shadow-glow-gold">
              <Plane className="w-5 h-5 text-amber-400 transform rotate-45" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h1 className="text-lg sm:text-xl font-black text-white tracking-tight">
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
                  className="p-1.5 rounded-lg bg-[#181A22] hover:bg-[#1F222E] text-amber-400 border border-white/10 transition-transform active:rotate-180 duration-300"
                >
                  <ArrowLeftRight className="w-3.5 h-3.5" />
                </button>
                <span className="text-xs font-bold text-amber-400 bg-amber-400/10 px-2.5 py-0.5 rounded-full border border-amber-400/20">
                  {cabinClass.replace('_', ' ')}
                </span>
              </div>

              <div className="flex items-center gap-3 text-xs text-slate-400 mt-0.5">
                <span className="flex items-center gap-1">
                  <Calendar className="w-3.5 h-3.5 text-amber-400" />
                  {new Date(departureDate).toLocaleDateString('en-US', {
                    weekday: 'short',
                    month: 'short',
                    day: 'numeric',
                    year: 'numeric',
                  })}
                </span>
                <span className="text-white/20">•</span>
                <span className="flex items-center gap-1">
                  <Users className="w-3.5 h-3.5 text-amber-400" />
                  {passengers} {passengers === 1 ? 'Traveler' : 'Travelers'}
                </span>
              </div>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => setShowModifySearch(!showModifySearch)}
              className="px-4 py-2 rounded-xl bg-[#181A22] hover:bg-[#1F222E] text-white text-xs font-bold flex items-center gap-1.5 border border-white/10 transition"
            >
              <Edit3 className="w-3.5 h-3.5 text-amber-400" />
              <span>{showModifySearch ? 'Hide Search Form' : 'Modify Search'}</span>
              {showModifySearch ? <ChevronUp className="w-3.5 h-3.5" /> : <ChevronDown className="w-3.5 h-3.5" />}
            </button>
          </div>
        </div>

        {/* Expandable Search Widget */}
        {showModifySearch && (
          <div className="mt-4 pt-4 border-t border-white/10 animate-slide-up">
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
          <div className="p-4 rounded-2xl bg-[#14161F] border border-white/10 shadow-xl flex flex-wrap items-center justify-between gap-4">
            <div>
              <div className="flex items-center gap-2">
                <h2 className="font-black text-white text-base">
                  Available Flights
                </h2>
                <span className="text-xs font-bold text-amber-400 bg-amber-400/10 px-2 py-0.5 rounded border border-amber-400/20">
                  {filteredFlights.length} {filteredFlights.length === 1 ? 'flight found' : 'flights found'}
                </span>
                {timeAgoText && (
                  <span className="text-[11px] text-emerald-400 font-semibold bg-emerald-500/10 border border-emerald-500/20 px-2 py-0.5 rounded-full flex items-center gap-1 shadow-glow-emerald">
                    <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse" />
                    {timeAgoText}
                  </span>
                )}
              </div>
              <p className="text-xs text-slate-400 mt-0.5">
                All fares in INR (₹) with airport taxes and mandatory fees included upfront
              </p>
            </div>

            {/* Mobile Filter Toggle */}
            <button
              type="button"
              onClick={() => setShowMobileFilters(!showMobileFilters)}
              className="xl:hidden px-4 py-2 rounded-xl bg-[#181A22] hover:bg-[#1F222E] text-white text-xs font-bold flex items-center gap-2 border border-white/10"
            >
              <SlidersHorizontal className="w-4 h-4 text-amber-400" />
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
                <div className="p-4 rounded-2xl bg-amber-400/10 border border-amber-400/20 text-amber-400 text-xs flex items-center gap-3 animate-fade-in shadow-glow-gold">
                  <div className="w-2.5 h-2.5 rounded-full bg-amber-400 animate-ping shrink-0" />
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
            <div className="p-10 rounded-2xl bg-[#14161F] border border-white/10 text-center space-y-4 shadow-xl">
              <div className="w-12 h-12 rounded-xl bg-rose-500/15 text-rose-400 border border-rose-500/30 flex items-center justify-center mx-auto">
                <AlertCircle className="w-6 h-6" />
              </div>
              <h3 className="font-bold text-white text-lg">Unable to Load Flights</h3>
              <p className="text-xs text-slate-400 max-w-sm mx-auto">{error}</p>
              <button
                onClick={fetchFlights}
                className="px-4 py-2 rounded-xl bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-300 hover:to-amber-400 text-black text-xs font-extrabold inline-flex items-center gap-2 transition shadow-glow-gold"
              >
                <RefreshCw className="w-4 h-4 text-black" />
                Retry Search
              </button>
            </div>
          ) : filteredFlights.length === 0 ? (
            <div className="p-12 rounded-2xl bg-[#14161F] border border-white/10 text-center space-y-4 shadow-xl">
              <div className="w-14 h-14 rounded-2xl bg-amber-400/10 text-amber-400 border border-amber-400/20 flex items-center justify-center mx-auto shadow-glow-gold">
                <Plane className="w-7 h-7 transform -rotate-45" />
              </div>
              <div className="space-y-1">
                <h3 className="font-black text-white text-lg">No Flights Found</h3>
                <p className="text-xs text-slate-400 max-w-md mx-auto leading-relaxed">
                  No flights match your filters between <strong className="text-white">{origin}</strong> and{' '}
                  <strong className="text-white">{destination}</strong> on {departureDate}.
                </p>
              </div>
              <button
                type="button"
                onClick={handleResetFilters}
                className="px-4 py-2 rounded-xl bg-[#181A22] hover:bg-[#1F222E] text-amber-400 border border-white/10 text-xs font-bold transition"
              >
                Reset All Filters
              </button>
            </div>
          ) : (
            <motion.div
              variants={staggerContainerVariants}
              initial="hidden"
              animate="visible"
              className="space-y-4"
            >
              {filteredFlights.map((flight) => (
                <FlightCard
                  key={flight.id}
                  flight={flight}
                  selectedCabin={cabinClass}
                  passengerCount={passengers}
                />
              ))}
            </motion.div>
          )}
        </main>
      </section>

      {/* RECOMMENDED FLIGHTS & DESTINATIONS */}
      <RecommendationsSection
        context="FLIGHT_SEARCH"
        destination={destination}
        title="Alternative Routes & Stays You May Like"
        subtitle={`Personalized flights and trending destinations connected to ${destination}`}
        limit={4}
      />
    </div>
  );
};
