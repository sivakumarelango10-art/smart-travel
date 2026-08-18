package com.smarttravel.modules.flight.repository;

import com.smarttravel.modules.flight.dto.FlightSearchCriteria;
import com.smarttravel.modules.flight.model.Flight;
import org.springframework.data.domain.Page;

/**
 * Custom query interface for dynamic multi-criteria flight search.
 */
public interface FlightRepositoryCustom {

    Page<Flight> searchFlights(FlightSearchCriteria criteria);
}
