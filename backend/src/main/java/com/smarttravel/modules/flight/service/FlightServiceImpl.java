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
import java.util.Set;

@Service
public class FlightServiceImpl implements FlightService {

    private static final Logger log = LoggerFactory.getLogger(FlightServiceImpl.class);

    private final FlightRepository flightRepository;
    private final FlightStatusHistoryRepository flightStatusHistoryRepository;
    private final FlightStateMachine flightStateMachine;
    private final FareCalculationService fareCalculationService;
    private final com.smarttravel.modules.flight.provider.FlightDataProviderRegistry providerRegistry;
    private final com.smarttravel.modules.flight.provider.aviationstack.AviationstackClient aviationstackClient;
    private final com.smarttravel.modules.flight.provider.aviationstack.AviationstackDataNormalizer normalizer;
    private final com.smarttravel.modules.flight.config.AviationstackProperties aviationstackProperties;

    @org.springframework.beans.factory.annotation.Autowired
    public FlightServiceImpl(FlightRepository flightRepository,
                             FlightStatusHistoryRepository flightStatusHistoryRepository,
                             FlightStateMachine flightStateMachine,
                             FareCalculationService fareCalculationService,
                             @org.springframework.beans.factory.annotation.Autowired(required = false) com.smarttravel.modules.flight.provider.FlightDataProviderRegistry providerRegistry,
                             @org.springframework.beans.factory.annotation.Autowired(required = false) com.smarttravel.modules.flight.provider.aviationstack.AviationstackClient aviationstackClient,
                             @org.springframework.beans.factory.annotation.Autowired(required = false) com.smarttravel.modules.flight.provider.aviationstack.AviationstackDataNormalizer normalizer,
                             @org.springframework.beans.factory.annotation.Autowired(required = false) com.smarttravel.modules.flight.config.AviationstackProperties aviationstackProperties) {
        this.flightRepository = flightRepository;
        this.flightStatusHistoryRepository = flightStatusHistoryRepository;
        this.flightStateMachine = flightStateMachine;
        this.fareCalculationService = fareCalculationService;
        this.providerRegistry = providerRegistry;
        this.aviationstackClient = aviationstackClient;
        this.normalizer = normalizer;
        this.aviationstackProperties = aviationstackProperties;
    }

    public FlightServiceImpl(FlightRepository flightRepository,
                             FlightStatusHistoryRepository flightStatusHistoryRepository,
                             FlightStateMachine flightStateMachine,
                             FareCalculationService fareCalculationService) {
        this(flightRepository, flightStatusHistoryRepository, flightStateMachine, fareCalculationService, null, null, null, null);
    }

    @Override
    @Transactional
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
    public PageResponse<FlightResponse> searchFlights(FlightSearchCriteria criteria) {
        log.debug("Searching flights with criteria: origin='{}', dest='{}', date='{}', passengers={}",
                criteria.getOrigin(), criteria.getDestination(), criteria.getDepartureDate(), criteria.getPassengers());

        Page<Flight> flightPage = flightRepository.searchFlights(criteria);
        List<FlightResponse> flightResponses = flightPage.getContent().stream()
                .map(flight -> FlightMapper.toResponse(flight, criteria.getCabinClass(), criteria.getPassengers(), fareCalculationService))
                .toList();

        return PageResponse.of(flightResponses, flightPage);
    }

    @Override
    public com.smarttravel.modules.flight.provider.FlightStatusProvider.FlightStatusSnapshot getLiveFlightStatus(String flightNumber) {
        String normalizedFlightNumber = flightNumber != null ? flightNumber.toUpperCase().trim() : "";
        log.info("Fetching live operational status for flight: {}", normalizedFlightNumber);

        if (providerRegistry != null) {
            com.smarttravel.modules.flight.provider.FlightStatusProvider provider = providerRegistry.getActiveProvider();
            var snapshotOpt = provider.fetchLatestStatus(normalizedFlightNumber, null);
            if (snapshotOpt.isPresent()) {
                return snapshotOpt.get();
            }
        }

        // Fallback to local MongoDB lookup
        return flightRepository.findByFlightNumber(normalizedFlightNumber).map(f -> {
            String term = (f.getDepartureAirport() != null && f.getDepartureAirport().getTerminal() != null)
                    ? f.getDepartureAirport().getTerminal()
                    : "T3";
            String gate = "Gate " + ((Math.abs(f.getFlightNumber().hashCode()) % 15) + 1);

            return new com.smarttravel.modules.flight.provider.FlightStatusProvider.FlightStatusSnapshot(
                    f.getFlightNumber(),
                    f.getStatus(),
                    f.getDelayMinutes(),
                    f.getDelayReason(),
                    f.getRevisedDepartureTime(),
                    f.getEstimatedArrival(),
                    gate,
                    term,
                    "SMARTTRAVEL_LOCAL_DB"
            );
        }).orElseThrow(() -> new ResourceNotFoundException("Flight", "flightNumber", flightNumber));
    }
}
