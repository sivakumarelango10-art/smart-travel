package com.smarttravel.modules.flight.service;

import com.smarttravel.common.response.PageResponse;
import com.smarttravel.modules.flight.dto.FlightCreateRequest;
import com.smarttravel.modules.flight.dto.FlightResponse;
import com.smarttravel.modules.flight.dto.FlightSearchCriteria;
import com.smarttravel.modules.flight.dto.FlightStatusUpdateRequest;
import com.smarttravel.modules.flight.dto.FlightUpdateRequest;

public interface FlightService {

    FlightResponse createFlight(FlightCreateRequest request);

    FlightResponse updateFlight(String id, FlightUpdateRequest request);

    void deleteFlight(String id);

    FlightResponse updateFlightStatus(String id, FlightStatusUpdateRequest request);

    FlightResponse updateFlightInventory(String id, com.smarttravel.modules.flight.dto.FlightInventoryUpdateRequest request);

    FlightResponse getFlightById(String id);

    FlightResponse getFlightByFlightNumber(String flightNumber);

    PageResponse<FlightResponse> searchFlights(FlightSearchCriteria criteria);
}
