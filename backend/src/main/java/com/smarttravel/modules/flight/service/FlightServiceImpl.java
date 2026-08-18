package com.smarttravel.modules.flight.service;

import com.smarttravel.common.exception.DuplicateResourceException;
import com.smarttravel.common.exception.ResourceNotFoundException;
import com.smarttravel.common.response.PageResponse;
import com.smarttravel.modules.flight.dto.FlightCreateRequest;
import com.smarttravel.modules.flight.dto.FlightResponse;
import com.smarttravel.modules.flight.dto.FlightSearchCriteria;
import com.smarttravel.modules.flight.dto.FlightStatusUpdateRequest;
import com.smarttravel.modules.flight.dto.FlightUpdateRequest;
import com.smarttravel.modules.flight.mapper.FlightMapper;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.repository.FlightRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FlightServiceImpl implements FlightService {

    private static final Logger log = LoggerFactory.getLogger(FlightServiceImpl.class);

    private final FlightRepository flightRepository;

    public FlightServiceImpl(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
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

        flight.setStatus(request.getStatus());
        Flight savedFlight = flightRepository.save(flight);
        log.info("Flight status for ID: {} updated to {}", id, savedFlight.getStatus());

        return FlightMapper.toResponse(savedFlight);
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
        log.debug("Searching flights with criteria: origin='{}', dest='{}', date='{}'",
                criteria.getOrigin(), criteria.getDestination(), criteria.getDepartureDate());

        Page<Flight> flightPage = flightRepository.searchFlights(criteria);
        List<FlightResponse> flightResponses = flightPage.getContent().stream()
                .map(FlightMapper::toResponse)
                .toList();

        return PageResponse.of(flightResponses, flightPage);
    }
}
