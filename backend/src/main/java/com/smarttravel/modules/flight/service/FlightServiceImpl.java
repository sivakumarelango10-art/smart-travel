package com.smarttravel.modules.flight.service;

import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.common.exception.DuplicateResourceException;
import com.smarttravel.common.exception.ResourceNotFoundException;
import com.smarttravel.common.response.PageResponse;
import com.smarttravel.common.security.SecurityUtils;
import com.smarttravel.modules.flight.dto.CabinInventoryDto;
import com.smarttravel.modules.flight.dto.FlightCreateRequest;
import com.smarttravel.modules.flight.dto.FlightInventoryUpdateRequest;
import com.smarttravel.modules.flight.dto.FlightResponse;
import com.smarttravel.modules.flight.dto.FlightSearchCriteria;
import com.smarttravel.modules.flight.dto.FlightStatusUpdateRequest;
import com.smarttravel.modules.flight.dto.FlightUpdateRequest;
import com.smarttravel.modules.flight.mapper.FlightMapper;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.CabinInventory;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.model.FlightStatusHistory;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.flight.repository.FlightStatusHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class FlightServiceImpl implements FlightService {

    private static final Logger log = LoggerFactory.getLogger(FlightServiceImpl.class);

    private final FlightRepository flightRepository;
    private final FlightStatusHistoryRepository flightStatusHistoryRepository;
    private final FlightStateMachine flightStateMachine;
    private final FareCalculationService fareCalculationService;
    private final com.smarttravel.modules.flight.provider.FlightStatusProvider flightStatusProvider;

    @org.springframework.beans.factory.annotation.Autowired
    public FlightServiceImpl(FlightRepository flightRepository,
                             FlightStatusHistoryRepository flightStatusHistoryRepository,
                             FlightStateMachine flightStateMachine,
                             FareCalculationService fareCalculationService,
                             @org.springframework.beans.factory.annotation.Autowired(required = false) com.smarttravel.modules.flight.provider.FlightStatusProvider flightStatusProvider) {
        this.flightRepository = flightRepository;
        this.flightStatusHistoryRepository = flightStatusHistoryRepository;
        this.flightStateMachine = flightStateMachine;
        this.fareCalculationService = fareCalculationService;
        this.flightStatusProvider = flightStatusProvider;
    }

    public FlightServiceImpl(FlightRepository flightRepository,
                             FlightStatusHistoryRepository flightStatusHistoryRepository,
                             FlightStateMachine flightStateMachine,
                             FareCalculationService fareCalculationService) {
        this(flightRepository, flightStatusHistoryRepository, flightStateMachine, fareCalculationService, null);
    }

    @Override
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = {com.smarttravel.common.config.CacheConfig.CACHE_FLIGHT_SEARCH, com.smarttravel.common.config.CacheConfig.CACHE_FLIGHT_DETAILS}, allEntries = true)
    public FlightResponse createFlight(FlightCreateRequest request) {
        log.info("Creating new flight with number: {}", request.getFlightNumber());

        String flightNum = request.getFlightNumber().toUpperCase().trim();
        if (flightRepository.existsByFlightNumber(flightNum)) {
            log.warn("Flight creation rejected: Flight number '{}' already exists", flightNum);
            throw new DuplicateResourceException("Flight already exists with flight number: '" + flightNum + "'");
        }

        Flight flight = FlightMapper.toEntity(request);
        flight.setLastStatusUpdated(Instant.now());
        Flight savedFlight = flightRepository.save(flight);
        log.info("Flight created successfully with ID: {} and number: {}", savedFlight.getId(), savedFlight.getFlightNumber());

        return FlightMapper.toResponse(savedFlight);
    }

    @Override
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = {com.smarttravel.common.config.CacheConfig.CACHE_FLIGHT_SEARCH, com.smarttravel.common.config.CacheConfig.CACHE_FLIGHT_DETAILS}, allEntries = true)
    public FlightResponse updateFlight(String id, FlightUpdateRequest request) {
        log.info("Updating flight with ID: {}", id);

        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "id", id));

        FlightMapper.updateEntity(flight, request);
        Flight updatedFlight = flightRepository.save(flight);
        log.info("Flight with ID: {} updated successfully", id);

        return FlightMapper.toResponse(updatedFlight);
    }

    @Override
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = {com.smarttravel.common.config.CacheConfig.CACHE_FLIGHT_SEARCH, com.smarttravel.common.config.CacheConfig.CACHE_FLIGHT_DETAILS}, allEntries = true)
    public void deleteFlight(String id) {
        log.info("Deactivating / deleting flight with ID: {}", id);

        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "id", id));

        flight.setActive(false);
        flightRepository.save(flight);
        log.info("Flight with ID: {} deactivated successfully", id);
    }

    @Override
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = {com.smarttravel.common.config.CacheConfig.CACHE_FLIGHT_SEARCH, com.smarttravel.common.config.CacheConfig.CACHE_FLIGHT_DETAILS}, allEntries = true)
    public FlightResponse updateFlightStatus(String id, FlightStatusUpdateRequest request) {
        log.info("Admin updating flight status for ID: {} to {}", id, request.getStatus());

        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "id", id));

        FlightStatus previousStatus = flight.getStatus();
        FlightStatus newStatus = request.getStatus();

        // 1. Validate State Machine Transition
        flightStateMachine.validateTransition(previousStatus, newStatus);

        // 2. Handle Delay Parameters & Timestamps
        if (newStatus == FlightStatus.DELAYED) {
            if (request.getDelayMinutes() == null || request.getDelayMinutes() < 0) {
                throw new BadRequestException("Delay minutes must be non-negative when status is DELAYED");
            }
            if (request.getDelayReason() == null || request.getDelayReason().isBlank()) {
                throw new BadRequestException("Delay reason is required when status is DELAYED");
            }

            Instant revisedDep = request.getRevisedDepartureTime();
            if (revisedDep == null) {
                revisedDep = flight.getDepartureTime().plus(request.getDelayMinutes(), ChronoUnit.MINUTES);
            } else if (revisedDep.isBefore(flight.getDepartureTime())) {
                throw new BadRequestException("Revised departure time cannot be earlier than scheduled departure time");
            }

            Instant estArr = request.getEstimatedArrival();
            if (estArr == null) {
                estArr = flight.getArrivalTime().plus(request.getDelayMinutes(), ChronoUnit.MINUTES);
            } else if (!estArr.isAfter(revisedDep)) {
                throw new BadRequestException("Estimated arrival time must be after revised departure time");
            }

            flight.setDelayMinutes(request.getDelayMinutes());
            flight.setDelayReason(request.getDelayReason().trim());
            flight.setRevisedDepartureTime(revisedDep);
            flight.setEstimatedArrival(estArr);
        } else if (newStatus == FlightStatus.ON_TIME) {
            flight.setDelayMinutes(0);
            flight.setDelayReason(null);
            flight.setRevisedDepartureTime(flight.getDepartureTime());
            flight.setEstimatedArrival(flight.getArrivalTime());
        }

        Instant now = Instant.now();
        flight.setStatus(newStatus);
        flight.setLastStatusUpdated(now);

        Flight savedFlight = flightRepository.save(flight);

        // 3. Create Audit History Record
        String adminUser = SecurityUtils.getCurrentUsernameOrAnonymous();
        FlightStatusHistory history = FlightStatusHistory.builder()
                .flightId(savedFlight.getId())
                .flightNumber(savedFlight.getFlightNumber())
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .delayMinutes(savedFlight.getDelayMinutes())
                .delayReason(savedFlight.getDelayReason())
                .revisedDepartureTime(savedFlight.getRevisedDepartureTime())
                .estimatedArrival(savedFlight.getEstimatedArrival())
                .changedAt(now)
                .changedBy(adminUser)
                .build();
        flightStatusHistoryRepository.save(history);

        log.info("Flight status for ID: {} updated from {} to {} by {}", id, previousStatus, newStatus, adminUser);
        return FlightMapper.toResponse(savedFlight);
    }

    @Override
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = {com.smarttravel.common.config.CacheConfig.CACHE_FLIGHT_SEARCH, com.smarttravel.common.config.CacheConfig.CACHE_FLIGHT_DETAILS}, allEntries = true)
    public FlightResponse updateFlightInventory(String id, FlightInventoryUpdateRequest request) {
        log.info("Admin updating cabin inventories for flight ID: {}", id);

        if (request == null || request.getCabinInventories() == null || request.getCabinInventories().isEmpty()) {
            throw new BadRequestException("Cabin inventories payload must not be empty");
        }

        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "id", id));

        List<CabinInventory> inventories = new ArrayList<>();
        Set<CabinClass> cabinClasses = new HashSet<>();
        int totalSeatsAggregate = 0;
        int availableSeatsAggregate = 0;

        for (CabinInventoryDto dto : request.getCabinInventories()) {
            if (dto.getAvailableSeats() > dto.getTotalSeats()) {
                throw new BadRequestException("Available seats (" + dto.getAvailableSeats() +
                        ") cannot exceed total seats (" + dto.getTotalSeats() + ") for cabin " + dto.getCabinClass());
            }

            CabinInventory inventory = FlightMapper.toCabinInventory(dto);
            inventories.add(inventory);
            cabinClasses.add(dto.getCabinClass());
            totalSeatsAggregate += dto.getTotalSeats();
            availableSeatsAggregate += dto.getAvailableSeats();
        }

        flight.setCabinInventories(inventories);
        flight.setCabinClasses(cabinClasses);
        flight.setTotalSeats(totalSeatsAggregate);
        flight.setAvailableSeats(availableSeatsAggregate);

        Flight updatedFlight = flightRepository.save(flight);
        log.info("Flight ID: {} inventories updated successfully. Total capacity: {}, Available: {}",
                id, totalSeatsAggregate, availableSeatsAggregate);

        return FlightMapper.toResponse(updatedFlight);
    }

    @Override
    @org.springframework.cache.annotation.Cacheable(value = com.smarttravel.common.config.CacheConfig.CACHE_FLIGHT_DETAILS, key = "#id")
    public FlightResponse getFlightById(String id) {
        log.debug("Fetching flight by ID: {}", id);

        Flight flight = flightRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "id", id));

        return FlightMapper.toResponse(flight);
    }

    @Override
    public FlightResponse getFlightByFlightNumber(String flightNumber) {
        log.debug("Fetching flight by flight number: {}", flightNumber);

        String normalizedFlightNumber = flightNumber != null ? flightNumber.toUpperCase().trim() : "";
        Flight flight = flightRepository.findByFlightNumberAndActiveTrue(normalizedFlightNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "flightNumber", flightNumber));

        return FlightMapper.toResponse(flight);
    }

    @Override
    @org.springframework.cache.annotation.Cacheable(
            value = com.smarttravel.common.config.CacheConfig.CACHE_FLIGHT_SEARCH,
            key = "{#criteria.origin, #criteria.destination, #criteria.departureDate, #criteria.cabinClass, #criteria.passengers, #criteria.departureTimeWindow, #criteria.sortBy, #criteria.sortDirection, #criteria.page, #criteria.size}",
            unless = "#result == null || #result.content == null || #result.content.isEmpty()"
    )
    public PageResponse<FlightResponse> searchFlights(FlightSearchCriteria criteria) {
        log.debug("Searching flights with criteria: origin='{}', dest='{}', date='{}', passengers={}",
                criteria.getOrigin(), criteria.getDestination(), criteria.getDepartureDate(), criteria.getPassengers());

        Page<Flight> flightPage = flightRepository.searchFlights(criteria);

        // Dynamic On-Demand Synthesis: If 0 flights found for a valid origin & destination pair, synthesize authentic schedules
        if (flightPage.isEmpty() && criteria.getOrigin() != null && !criteria.getOrigin().isBlank()
                && criteria.getDestination() != null && !criteria.getDestination().isBlank()) {
            java.time.LocalDate date = criteria.getDepartureDate() != null ? criteria.getDepartureDate() : java.time.LocalDate.now(java.time.ZoneOffset.UTC).plusDays(1);
            List<Flight> synthesized = synthesizeOnDemandFlights(criteria.getOrigin().trim().toUpperCase(), criteria.getDestination().trim().toUpperCase(), date);
            if (!synthesized.isEmpty()) {
                List<Flight> toSave = new ArrayList<>();
                for (Flight f : synthesized) {
                    if (!flightRepository.existsByFlightNumber(f.getFlightNumber())) {
                        toSave.add(f);
                    }
                }
                if (!toSave.isEmpty()) {
                    flightRepository.saveAll(toSave);
                    log.info("Synthesized and persisted {} on-demand flights for route {} -> {} on {}", toSave.size(), criteria.getOrigin(), criteria.getDestination(), date);
                }
                flightPage = flightRepository.searchFlights(criteria);
            }
        }

        List<FlightResponse> flightResponses = flightPage.getContent().stream()
                .map(flight -> FlightMapper.toResponse(flight, criteria.getCabinClass(), criteria.getPassengers(), fareCalculationService))
                .toList();

        return PageResponse.of(flightResponses, flightPage);
    }

    @Override
    public com.smarttravel.modules.flight.provider.FlightStatusProvider.FlightStatusSnapshot getLiveFlightStatus(String flightNumber) {
        String normalizedFlightNumber = flightNumber != null ? flightNumber.toUpperCase().trim() : "";
        log.info("Fetching live operational status for flight: {}", normalizedFlightNumber);

        if (flightStatusProvider != null) {
            var snapshotOpt = flightStatusProvider.fetchLatestStatus(normalizedFlightNumber, null);
            if (snapshotOpt.isPresent()) {
                com.smarttravel.modules.flight.provider.FlightStatusProvider.FlightStatusSnapshot snap = snapshotOpt.get();
                if (snap.originCode() != null && snap.flightId() != null && !snap.flightId().isBlank()) {
                    return snap;
                }
                return enrichSnapshot(snap);
            }
        }

        // Check local MongoDB flight
        var localFlightOpt = flightRepository.findByFlightNumber(normalizedFlightNumber);
        if (localFlightOpt.isPresent()) {
            return toRichSnapshot(localFlightOpt.get(), "SIMULATED");
        }

        // Check unhyphenated or hyphenated variant
        String standardNumber = formatStandardFlightCode(normalizedFlightNumber);
        localFlightOpt = flightRepository.findByFlightNumber(standardNumber);
        if (localFlightOpt.isPresent()) {
            return toRichSnapshot(localFlightOpt.get(), "SIMULATED");
        }

        // Auto-provision flight into MongoDB so it has a permanent entity ID
        Flight autoFlight = autoProvisionFlight(standardNumber);
        if (autoFlight != null) {
            return toRichSnapshot(autoFlight, "SIMULATED");
        }

        return generateSimulatedFlightSnapshot(normalizedFlightNumber);
    }

    @Override
    public List<com.smarttravel.modules.flight.provider.FlightStatusProvider.FlightStatusSnapshot> getPopularLiveFlights() {
        List<com.smarttravel.modules.flight.provider.FlightStatusProvider.FlightStatusSnapshot> popular = new ArrayList<>();
        
        // Fetch up to 6 real active flights from DB
        List<Flight> activeFlights = flightRepository.findAll().stream()
                .filter(Flight::isActive)
                .limit(6)
                .toList();

        for (Flight f : activeFlights) {
            popular.add(toRichSnapshot(f, "SIMULATED"));
        }

        if (popular.size() < 6) {
            String[] popularCodes = {"AI-101", "6E-204", "UK-955", "BA-112", "EK-500", "SQ-402"};
            for (String code : popularCodes) {
                if (popular.stream().noneMatch(p -> p.flightNumber().equalsIgnoreCase(code))) {
                    popular.add(generateSimulatedFlightSnapshot(code));
                }
            }
        }

        return popular;
    }

    private com.smarttravel.modules.flight.provider.FlightStatusProvider.FlightStatusSnapshot toRichSnapshot(Flight f, String source) {
        String term = (f.getDepartureAirport() != null && f.getDepartureAirport().getTerminal() != null)
                ? f.getDepartureAirport().getTerminal()
                : "T3";
        String gate = "Gate " + ((Math.abs(f.getFlightNumber().hashCode()) % 15) + 1);
        String belt = "Belt " + ((Math.abs(f.getFlightNumber().hashCode()) % 8) + 1);

        String origCode = f.getDepartureAirport() != null ? f.getDepartureAirport().getCode() : "DEL";
        String origCity = f.getDepartureAirport() != null ? f.getDepartureAirport().getCity() : "New Delhi";
        String origName = f.getDepartureAirport() != null ? f.getDepartureAirport().getName() : "Indira Gandhi Int'l Airport";

        String destCode = f.getArrivalAirport() != null ? f.getArrivalAirport().getCode() : "BOM";
        String destCity = f.getArrivalAirport() != null ? f.getArrivalAirport().getCity() : "Mumbai";
        String destName = f.getArrivalAirport() != null ? f.getArrivalAirport().getName() : "Chhatrapati Shivaji Maharaj Int'l Airport";

        double[] origCoords = getAirportCoords(origCode);
        double[] destCoords = getAirportCoords(destCode);

        FlightStatus status = f.getStatus() != null ? f.getStatus() : FlightStatus.ON_TIME;
        int altitude = (status == FlightStatus.DEPARTED) ? 36000 : 0;
        int speed = (status == FlightStatus.DEPARTED) ? 840 : 0;
        double progress = (status == FlightStatus.ARRIVED) ? 100.0 : (status == FlightStatus.DEPARTED ? 58.0 : 0.0);

        double curLat = origCoords[0] + (destCoords[0] - origCoords[0]) * (progress / 100.0);
        double curLng = origCoords[1] + (destCoords[1] - origCoords[1]) * (progress / 100.0);

        return new com.smarttravel.modules.flight.provider.FlightStatusProvider.FlightStatusSnapshot(
                f.getFlightNumber(),
                status,
                f.getDelayMinutes(),
                f.getDelayReason(),
                f.getRevisedDepartureTime() != null ? f.getRevisedDepartureTime() : f.getDepartureTime(),
                f.getEstimatedArrival() != null ? f.getEstimatedArrival() : f.getArrivalTime(),
                gate,
                term,
                source,
                f.getAirline() != null ? f.getAirline() : "SmartTravel Airways",
                f.getAirlineCode() != null ? f.getAirlineCode() : "ST",
                origCode,
                origCity,
                origName,
                destCode,
                destCity,
                destName,
                f.getDepartureTime(),
                f.getArrivalTime(),
                f.getAircraftModel() != null ? f.getAircraftModel() : "Airbus A321neo",
                altitude,
                speed,
                progress,
                belt,
                origCoords[0],
                origCoords[1],
                destCoords[0],
                destCoords[1],
                curLat,
                curLng,
                f.getId()
        );
    }

    private com.smarttravel.modules.flight.provider.FlightStatusProvider.FlightStatusSnapshot enrichSnapshot(com.smarttravel.modules.flight.provider.FlightStatusProvider.FlightStatusSnapshot snap) {
        String num = snap.flightNumber() != null ? formatStandardFlightCode(snap.flightNumber()) : "AI-101";
        String airline = resolveAirlineName(num);
        String airlineCode = resolveAirlineCode(num);

        String orig = "DEL";
        String dest = "BOM";
        if (num.contains("204") || num.contains("6E")) { orig = "BLR"; dest = "DEL"; }
        else if (num.contains("500") || num.contains("EK")) { orig = "DXB"; dest = "BOM"; }
        else if (num.contains("112") || num.contains("BA")) { orig = "LHR"; dest = "DEL"; }
        else if (num.contains("402") || num.contains("SQ")) { orig = "SIN"; dest = "BOM"; }

        double[] origCoords = getAirportCoords(orig);
        double[] destCoords = getAirportCoords(dest);
        double progress = snap.status() == FlightStatus.ARRIVED ? 100.0 : (snap.status() == FlightStatus.DEPARTED ? 65.0 : 0.0);
        int alt = snap.status() == FlightStatus.DEPARTED ? 37000 : 0;
        int spd = snap.status() == FlightStatus.DEPARTED ? 860 : 0;

        String resolvedFlightId = snap.flightId();
        if (resolvedFlightId == null || resolvedFlightId.isBlank() || resolvedFlightId.startsWith("sim_") || resolvedFlightId.startsWith("radar_")) {
            Optional<Flight> existing = flightRepository.findByFlightNumber(num);
            if (existing.isPresent()) {
                resolvedFlightId = existing.get().getId();
            } else {
                Flight provisioned = autoProvisionFlight(num);
                resolvedFlightId = provisioned != null ? provisioned.getId() : "sim_" + num.replace("-", "").toLowerCase();
            }
        }

        return new com.smarttravel.modules.flight.provider.FlightStatusProvider.FlightStatusSnapshot(
                num,
                snap.status(),
                snap.delayMinutes(),
                snap.delayReason(),
                snap.revisedDepartureTime(),
                snap.revisedArrivalTime(),
                snap.gate() != null ? snap.gate() : "Gate " + ((Math.abs(num.hashCode()) % 15) + 1),
                snap.terminal() != null ? snap.terminal() : "T3",
                snap.updatedSource(),
                airline,
                airlineCode,
                orig,
                getCityName(orig),
                getAirportFullName(orig),
                dest,
                getCityName(dest),
                getAirportFullName(dest),
                snap.revisedDepartureTime() != null ? snap.revisedDepartureTime() : Instant.now().minus(1, ChronoUnit.HOURS),
                snap.revisedArrivalTime() != null ? snap.revisedArrivalTime() : Instant.now().plus(1, ChronoUnit.HOURS),
                "Boeing 787-9 Dreamliner",
                alt,
                spd,
                progress,
                "Belt " + ((Math.abs(num.hashCode()) % 8) + 1),
                origCoords[0],
                origCoords[1],
                destCoords[0],
                destCoords[1],
                origCoords[0] + (destCoords[0] - origCoords[0]) * (progress / 100.0),
                origCoords[1] + (destCoords[1] - origCoords[1]) * (progress / 100.0),
                resolvedFlightId
        );
    }

    private com.smarttravel.modules.flight.provider.FlightStatusProvider.FlightStatusSnapshot generateSimulatedFlightSnapshot(String flightNum) {
        String cleanNum = (flightNum == null || flightNum.isBlank()) ? "AI-101" : formatStandardFlightCode(flightNum.toUpperCase().trim());
        String airline = resolveAirlineName(cleanNum);
        String airlineCode = resolveAirlineCode(cleanNum);

        String orig = "DEL";
        String dest = "BOM";
        if (cleanNum.contains("6E") || cleanNum.contains("204")) { orig = "BLR"; dest = "DEL"; }
        else if (cleanNum.contains("UK") || cleanNum.contains("955")) { orig = "BOM"; dest = "GOI"; }
        else if (cleanNum.contains("EK") || cleanNum.contains("500")) { orig = "DXB"; dest = "BOM"; }
        else if (cleanNum.contains("BA") || cleanNum.contains("112")) { orig = "LHR"; dest = "DEL"; }
        else if (cleanNum.contains("SQ") || cleanNum.contains("402")) { orig = "SIN"; dest = "BOM"; }
        else if (cleanNum.contains("LH") || cleanNum.contains("760")) { orig = "FRA"; dest = "DEL"; }

        double[] origCoords = getAirportCoords(orig);
        double[] destCoords = getAirportCoords(dest);

        FlightStatus status = (Math.abs(cleanNum.hashCode()) % 5 == 0) ? FlightStatus.DELAYED :
                              (Math.abs(cleanNum.hashCode()) % 3 == 0) ? FlightStatus.BOARDING : FlightStatus.DEPARTED;

        int delayMins = status == FlightStatus.DELAYED ? 35 : 0;
        String delayReason = status == FlightStatus.DELAYED ? "Air traffic management delay" : null;

        Instant depTime = Instant.now().minus(45, ChronoUnit.MINUTES);
        Instant arrTime = Instant.now().plus(75, ChronoUnit.MINUTES);
        Instant revDep = status == FlightStatus.DELAYED ? depTime.plus(delayMins, ChronoUnit.MINUTES) : depTime;
        Instant estArr = status == FlightStatus.DELAYED ? arrTime.plus(delayMins, ChronoUnit.MINUTES) : arrTime;

        double progress = (status == FlightStatus.DEPARTED) ? 55.0 : (status == FlightStatus.BOARDING ? 5.0 : 15.0);
        int alt = (status == FlightStatus.DEPARTED) ? 36000 : 0;
        int spd = (status == FlightStatus.DEPARTED) ? 840 : 0;

        String resolvedFlightId;
        Optional<Flight> existing = flightRepository.findByFlightNumber(cleanNum);
        if (existing.isPresent()) {
            resolvedFlightId = existing.get().getId();
        } else {
            Flight provisioned = autoProvisionFlight(cleanNum);
            resolvedFlightId = provisioned != null ? provisioned.getId() : "radar_" + cleanNum.replace("-", "").toLowerCase();
        }

        return new com.smarttravel.modules.flight.provider.FlightStatusProvider.FlightStatusSnapshot(
                cleanNum,
                status,
                delayMins,
                delayReason,
                revDep,
                estArr,
                "Gate " + ((Math.abs(cleanNum.hashCode()) % 15) + 1),
                "T3",
                "SIMULATED",
                airline,
                airlineCode,
                orig,
                getCityName(orig),
                getAirportFullName(orig),
                dest,
                getCityName(dest),
                getAirportFullName(dest),
                depTime,
                arrTime,
                "Airbus A321neo",
                alt,
                spd,
                progress,
                "Belt " + ((Math.abs(cleanNum.hashCode()) % 8) + 1),
                origCoords[0],
                origCoords[1],
                destCoords[0],
                destCoords[1],
                origCoords[0] + (destCoords[0] - origCoords[0]) * (progress / 100.0),
                origCoords[1] + (destCoords[1] - origCoords[1]) * (progress / 100.0),
                resolvedFlightId
        );
    }

    private Flight autoProvisionFlight(String flightNumber) {
        try {
            String num = formatStandardFlightCode(flightNumber);
            String airline = resolveAirlineName(num);
            String airlineCode = resolveAirlineCode(num);
            String orig = "DEL";
            String dest = "BOM";
            if (num.contains("204") || num.contains("6E")) { orig = "BLR"; dest = "DEL"; }
            else if (num.contains("955") || num.contains("UK")) { orig = "BOM"; dest = "GOI"; }
            else if (num.contains("500") || num.contains("EK")) { orig = "DXB"; dest = "BOM"; }
            else if (num.contains("112") || num.contains("BA")) { orig = "LHR"; dest = "DEL"; }
            else if (num.contains("402") || num.contains("SQ")) { orig = "SIN"; dest = "BOM"; }

            Flight flight = Flight.builder()
                    .flightNumber(num)
                    .airline(airline)
                    .airlineCode(airlineCode)
                    .status(FlightStatus.ON_TIME)
                    .departureAirport(com.smarttravel.modules.flight.model.AirportInfo.builder()
                            .code(orig)
                            .city(getCityName(orig))
                            .name(getAirportFullName(orig))
                            .terminal("T3")
                            .build())
                    .arrivalAirport(com.smarttravel.modules.flight.model.AirportInfo.builder()
                            .code(dest)
                            .city(getCityName(dest))
                            .name(getAirportFullName(dest))
                            .terminal("T2")
                            .build())
                    .departureTime(Instant.now().plus(3, ChronoUnit.HOURS))
                    .arrivalTime(Instant.now().plus(5, ChronoUnit.HOURS))
                    .aircraftModel("Airbus A321neo")
                    .active(true)
                    .build();

            return flightRepository.save(flight);
        } catch (Exception ex) {
            log.warn("Auto-provisioning flight {} into MongoDB produced notice: {}", flightNumber, ex.getMessage());
            return null;
        }
    }

    private String formatStandardFlightCode(String raw) {
        if (raw == null) return "AI-101";
        String clean = raw.toUpperCase().trim();
        if (clean.startsWith("RADAR_")) clean = clean.substring(6);
        if (clean.startsWith("SIM_")) clean = clean.substring(4);
        if (clean.contains("-")) return clean;
        // Regex split letters from numbers e.g. AI101 -> AI-101
        if (clean.matches("^[A-Z0-9]{2}\\d+$")) {
            return clean.substring(0, 2) + "-" + clean.substring(2);
        }
        return clean;
    }

    private String resolveAirlineName(String flightNum) {
        String num = flightNum.toUpperCase();
        if (num.startsWith("AI") || num.startsWith("AIC")) return "Air India";
        if (num.startsWith("6E") || num.startsWith("IGO")) return "IndiGo";
        if (num.startsWith("UK") || num.startsWith("VTI")) return "Vistara";
        if (num.startsWith("SG") || num.startsWith("SEJ")) return "SpiceJet";
        if (num.startsWith("EK") || num.startsWith("UAE")) return "Emirates";
        if (num.startsWith("BA") || num.startsWith("BAW")) return "British Airways";
        if (num.startsWith("LH") || num.startsWith("DLH")) return "Lufthansa";
        if (num.startsWith("SQ") || num.startsWith("SIA")) return "Singapore Airlines";
        if (num.startsWith("QR") || num.startsWith("QTR")) return "Qatar Airways";
        return "Global Airways";
    }

    private String resolveAirlineCode(String flightNum) {
        String num = flightNum.toUpperCase();
        if (num.startsWith("AI")) return "AI";
        if (num.startsWith("6E")) return "6E";
        if (num.startsWith("UK")) return "UK";
        if (num.startsWith("SG")) return "SG";
        if (num.startsWith("EK")) return "EK";
        if (num.startsWith("BA")) return "BA";
        if (num.startsWith("LH")) return "LH";
        if (num.startsWith("SQ")) return "SQ";
        if (num.startsWith("QR")) return "QR";
        return "GA";
    }

    private double[] getAirportCoords(String code) {
        if (code == null) return new double[]{28.5562, 77.1000};
        return switch (code.toUpperCase().trim()) {
            case "BOM" -> new double[]{19.0896, 72.8656};
            case "BLR" -> new double[]{13.1986, 77.7066};
            case "MAA" -> new double[]{12.9941, 80.1709};
            case "CCU" -> new double[]{22.6547, 88.4467};
            case "HYD" -> new double[]{17.2403, 78.4294};
            case "GOI" -> new double[]{15.3808, 73.8314};
            case "DXB" -> new double[]{25.2532, 55.3657};
            case "LHR" -> new double[]{51.4700, -0.4543};
            case "SIN" -> new double[]{1.3644, 103.9915};
            case "FRA" -> new double[]{50.0379, 8.5622};
            case "JFK" -> new double[]{40.6413, -73.7781};
            default -> new double[]{28.5562, 77.1000};
        };
    }

    private String getCityName(String code) {
        if (code == null) return "New Delhi";
        return switch (code.toUpperCase().trim()) {
            case "BOM" -> "Mumbai";
            case "BLR" -> "Bengaluru";
            case "MAA" -> "Chennai";
            case "CCU" -> "Kolkata";
            case "HYD" -> "Hyderabad";
            case "GOI" -> "Goa";
            case "DXB" -> "Dubai";
            case "LHR" -> "London";
            case "SIN" -> "Singapore";
            case "FRA" -> "Frankfurt";
            case "JFK" -> "New York";
            default -> "New Delhi";
        };
    }

    private String getAirportFullName(String code) {
        if (code == null) return "Indira Gandhi International Airport";
        return switch (code.toUpperCase().trim()) {
            case "BOM" -> "Chhatrapati Shivaji Maharaj International Airport";
            case "BLR" -> "Kempegowda International Airport";
            case "MAA" -> "Chennai International Airport";
            case "CCU" -> "Netaji Subhash Chandra Bose International Airport";
            case "HYD" -> "Rajiv Gandhi International Airport";
            case "GOI" -> "Dabolim Airport";
            case "DXB" -> "Dubai International Airport";
            case "LHR" -> "London Heathrow Airport";
            case "SIN" -> "Singapore Changi Airport";
            case "FRA" -> "Frankfurt Airport";
            case "JFK" -> "John F. Kennedy International Airport";
            default -> "Indira Gandhi International Airport";
        };
    }

    private List<Flight> synthesizeOnDemandFlights(String origin, String dest, java.time.LocalDate date) {
        List<Flight> list = new ArrayList<>();
        String dateSuffix = date.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);

        boolean isIntl = isInternationalAirport(origin) || isInternationalAirport(dest);
        int baseDuration = calculateEstimatedDuration(origin, dest, isIntl);
        double baseFare = calculateEstimatedFare(origin, dest, isIntl);

        // 1. Early Morning (Air India • Boeing 787-9 Dreamliner / Airbus A320neo)
        list.add(createSynthesizedFlight("AI", "Air India", 101, origin, dest, isIntl ? "Boeing 787-9 Dreamliner" : "Airbus A320neo", date.atTime(6, 30).toInstant(java.time.ZoneOffset.UTC), baseDuration, baseFare * 0.95, dateSuffix));
        
        // 2. Mid Morning (IndiGo • Airbus A321neo / Airbus A320ceo)
        list.add(createSynthesizedFlight("6E", "IndiGo", 204, origin, dest, "Airbus A321neo", date.atTime(9, 15).toInstant(java.time.ZoneOffset.UTC), baseDuration, baseFare * 0.90, dateSuffix));
        
        // 3. Afternoon (Akasa Air / Air India Express • Boeing 737 MAX 8)
        list.add(createSynthesizedFlight(isIntl ? "EK" : "QP", isIntl ? "Emirates" : "Akasa Air", 520, origin, dest, isIntl ? "Boeing 777-300ER" : "Boeing 737 MAX 8", date.atTime(13, 45).toInstant(java.time.ZoneOffset.UTC), baseDuration, baseFare * 1.05, dateSuffix));
        
        // 4. Evening (Air India • Airbus A350-900)
        list.add(createSynthesizedFlight("AI", "Air India", 830, origin, dest, "Airbus A350-900", date.atTime(18, 20).toInstant(java.time.ZoneOffset.UTC), baseDuration, baseFare * 1.10, dateSuffix));
        
        // 5. Night (Air India Express / SpiceJet • Boeing 737-800)
        list.add(createSynthesizedFlight(isIntl ? "SQ" : "IX", isIntl ? "Singapore Airlines" : "Air India Express", 412, origin, dest, isIntl ? "Airbus A350-900" : "Boeing 737-800", date.atTime(21, 50).toInstant(java.time.ZoneOffset.UTC), baseDuration, baseFare * 0.92, dateSuffix));

        return list;
    }

    private Flight createSynthesizedFlight(String airlineCode, String airline, int baseNum, String orig, String dest,
                                           String aircraft, Instant depTime, int durationMins, double basePriceVal, String dateSuffix) {
        String flightNum = airlineCode + "-" + baseNum + "-" + dateSuffix;
        return com.smarttravel.modules.flight.seeder.FlightDataSeeder.buildFlight(
                flightNum,
                airline,
                airlineCode,
                orig,
                dest,
                aircraft,
                depTime,
                durationMins,
                basePriceVal,
                FlightStatus.SCHEDULED,
                null,
                null
        );
    }

    private boolean isInternationalAirport(String code) {
        if (code == null) return false;
        String clean = code.toUpperCase().trim();
        return Set.of("DPS", "MLE", "BKK", "HKT", "DXB", "AUH", "DOH", "SIN", "KUL", "HND", "NRT", "ICN", "LHR", "CDG", "FRA", "AMS", "ZRH", "JFK", "SFO", "YYZ", "SYD").contains(clean);
    }

    private int calculateEstimatedDuration(String orig, String dest, boolean isIntl) {
        if (!isIntl) return 130;
        String o = orig != null ? orig.toUpperCase().trim() : "";
        String d = dest != null ? dest.toUpperCase().trim() : "";
        if (o.equals("DPS") || d.equals("DPS")) return 530;
        if (o.equals("MLE") || d.equals("MLE")) return 200;
        if (o.equals("DXB") || d.equals("DXB") || o.equals("AUH") || d.equals("AUH") || o.equals("DOH") || d.equals("DOH")) return 210;
        if (o.equals("SIN") || d.equals("SIN") || o.equals("KUL") || d.equals("KUL") || o.equals("BKK") || d.equals("BKK")) return 300;
        if (o.equals("LHR") || d.equals("LHR") || o.equals("CDG") || d.equals("CDG") || o.equals("FRA") || d.equals("FRA") || o.equals("AMS") || d.equals("AMS") || o.equals("ZRH") || d.equals("ZRH")) return 550;
        if (o.equals("JFK") || d.equals("JFK") || o.equals("SFO") || d.equals("SFO") || o.equals("YYZ") || d.equals("YYZ")) return 950;
        if (o.equals("SYD") || d.equals("SYD")) return 740;
        return 240;
    }

    private double calculateEstimatedFare(String orig, String dest, boolean isIntl) {
        if (!isIntl) return 4500;
        String o = orig != null ? orig.toUpperCase().trim() : "";
        String d = dest != null ? dest.toUpperCase().trim() : "";
        if (o.equals("DPS") || d.equals("DPS")) return 16999;
        if (o.equals("MLE") || d.equals("MLE")) return 12999;
        if (o.equals("DXB") || d.equals("DXB") || o.equals("AUH") || d.equals("AUH") || o.equals("DOH") || d.equals("DOH")) return 21500;
        if (o.equals("SIN") || d.equals("SIN") || o.equals("KUL") || d.equals("KUL") || o.equals("BKK") || d.equals("BKK")) return 14500;
        if (o.equals("LHR") || d.equals("LHR") || o.equals("CDG") || d.equals("CDG") || o.equals("FRA") || d.equals("FRA") || o.equals("AMS") || d.equals("AMS") || o.equals("ZRH") || d.equals("ZRH")) return 49500;
        if (o.equals("JFK") || d.equals("JFK") || o.equals("SFO") || d.equals("SFO") || o.equals("YYZ") || d.equals("YYZ")) return 72000;
        if (o.equals("SYD") || d.equals("SYD")) return 68000;
        return 18000;
    }
}
