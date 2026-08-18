package com.smarttravel.modules.flight.repository;

import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.modules.flight.dto.DepartureTimeWindow;
import com.smarttravel.modules.flight.dto.FlightSearchCriteria;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Repository
public class FlightRepositoryCustomImpl implements FlightRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public FlightRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<Flight> searchFlights(FlightSearchCriteria criteria) {
        List<Criteria> andCriteriaList = new ArrayList<>();

        // Validate origin != destination
        if (criteria.getOrigin() != null && criteria.getDestination() != null
                && !criteria.getOrigin().isBlank() && !criteria.getDestination().isBlank()
                && criteria.getOrigin().trim().equalsIgnoreCase(criteria.getDestination().trim())) {
            throw new BadRequestException("Origin and destination airport/city must not be identical");
        }

        // Validate passenger count
        if (criteria.getPassengers() != null) {
            if (criteria.getPassengers() < 1 || criteria.getPassengers() > 9) {
                throw new BadRequestException("Passenger count must be between 1 and 9");
            }
        }
        int passengers = (criteria.getPassengers() != null && criteria.getPassengers() >= 1) ? criteria.getPassengers() : 1;

        // Validate price range
        if (criteria.getMinPrice() != null && criteria.getMinPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Minimum price cannot be negative");
        }
        if (criteria.getMaxPrice() != null && criteria.getMaxPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Maximum price cannot be negative");
        }
        if (criteria.getMinPrice() != null && criteria.getMaxPrice() != null
                && criteria.getMinPrice().compareTo(criteria.getMaxPrice()) > 0) {
            throw new BadRequestException("Minimum price cannot exceed maximum price");
        }

        // Always filter by active flights
        andCriteriaList.add(Criteria.where("active").is(true));

        // Origin filter (match 3-letter IATA code or city name)
        if (criteria.getOrigin() != null && !criteria.getOrigin().isBlank()) {
            String origin = criteria.getOrigin().trim();
            Criteria codeMatch = Criteria.where("departureAirport.code").is(origin.toUpperCase());
            Criteria cityMatch = Criteria.where("departureAirport.city").regex(Pattern.quote(origin), "i");
            andCriteriaList.add(new Criteria().orOperator(codeMatch, cityMatch));
        }

        // Destination filter (match 3-letter IATA code or city name)
        if (criteria.getDestination() != null && !criteria.getDestination().isBlank()) {
            String dest = criteria.getDestination().trim();
            Criteria codeMatch = Criteria.where("arrivalAirport.code").is(dest.toUpperCase());
            Criteria cityMatch = Criteria.where("arrivalAirport.city").regex(Pattern.quote(dest), "i");
            andCriteriaList.add(new Criteria().orOperator(codeMatch, cityMatch));
        }

        // Departure Date and Time Window filter
        if (criteria.getDepartureDate() != null) {
            LocalDate date = criteria.getDepartureDate();
            LocalDate todayUtc = LocalDate.now(ZoneOffset.UTC);
            if (date.isBefore(todayUtc)) {
                throw new BadRequestException("Departure date cannot be in the past");
            }

            if (criteria.getDepartureTimeWindow() != null && criteria.getDepartureTimeWindow() != DepartureTimeWindow.ALL) {
                Instant windowStart = date.atTime(criteria.getDepartureTimeWindow().getStartTime()).atZone(ZoneOffset.UTC).toInstant();
                Instant windowEnd = date.atTime(criteria.getDepartureTimeWindow().getEndTime()).atZone(ZoneOffset.UTC).toInstant();
                andCriteriaList.add(Criteria.where("departureTime").gte(windowStart).lte(windowEnd));
            } else {
                Instant startOfDay = date.atStartOfDay(ZoneOffset.UTC).toInstant();
                Instant endOfDay = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
                andCriteriaList.add(Criteria.where("departureTime").gte(startOfDay).lt(endOfDay));
            }
        }

        // Airline filter
        if (criteria.getAirline() != null && !criteria.getAirline().isBlank()) {
            andCriteriaList.add(Criteria.where("airline").regex(Pattern.quote(criteria.getAirline().trim()), "i"));
        }

        // Cabin Class & Passenger Availability filter
        if (criteria.getCabinClass() != null) {
            Criteria cabinInventoryMatch = Criteria.where("cabinInventories").elemMatch(
                    Criteria.where("cabinClass").is(criteria.getCabinClass())
                            .and("availableSeats").gte(passengers)
            );
            Criteria legacyCabinMatch = new Criteria().andOperator(
                    new Criteria().orOperator(
                            Criteria.where("cabinInventories").exists(false),
                            Criteria.where("cabinInventories").is(java.util.Collections.emptyList()),
                            Criteria.where("cabinInventories").size(0)
                    ),
                    Criteria.where("cabinClasses").is(criteria.getCabinClass()),
                    Criteria.where("availableSeats").gte(passengers)
            );
            andCriteriaList.add(new Criteria().orOperator(cabinInventoryMatch, legacyCabinMatch));
        } else if (passengers > 1) {
            Criteria anyCabinAvail = Criteria.where("cabinInventories.availableSeats").gte(passengers);
            Criteria legacyAvail = new Criteria().andOperator(
                    new Criteria().orOperator(
                            Criteria.where("cabinInventories").exists(false),
                            Criteria.where("cabinInventories").is(java.util.Collections.emptyList()),
                            Criteria.where("cabinInventories").size(0)
                    ),
                    Criteria.where("availableSeats").gte(passengers)
            );
            andCriteriaList.add(new Criteria().orOperator(anyCabinAvail, legacyAvail));
        }

        // Price range filter
        if (criteria.getMinPrice() != null && criteria.getMaxPrice() != null) {
            andCriteriaList.add(Criteria.where("basePrice").gte(criteria.getMinPrice()).lte(criteria.getMaxPrice()));
        } else if (criteria.getMinPrice() != null) {
            andCriteriaList.add(Criteria.where("basePrice").gte(criteria.getMinPrice()));
        } else if (criteria.getMaxPrice() != null) {
            andCriteriaList.add(Criteria.where("basePrice").lte(criteria.getMaxPrice()));
        }

        // Status filter (exclude cancelled/arrived/diverted by default if not explicitly specified)
        if (criteria.getStatus() != null) {
            andCriteriaList.add(Criteria.where("status").is(criteria.getStatus()));
        } else {
            andCriteriaList.add(Criteria.where("status").nin(FlightStatus.CANCELLED, FlightStatus.ARRIVED, FlightStatus.DIVERTED));
        }

        Query query = new Query();
        if (!andCriteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(andCriteriaList.toArray(new Criteria[0])));
        }

        // Count total matching elements
        long total = mongoTemplate.count(query, Flight.class);

        // Sorting
        Sort sort = buildSort(criteria.getSortBy(), criteria.getSortDirection());

        int pageNum = Math.max(0, criteria.getPage());
        int pageSize = criteria.getSize() > 0 ? Math.min(criteria.getSize(), 100) : 20;
        Pageable pageable = PageRequest.of(pageNum, pageSize, sort);

        query.with(pageable);
        List<Flight> flights = mongoTemplate.find(query, Flight.class);

        return new PageImpl<>(flights, pageable, total);
    }

    private Sort buildSort(String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "departureTime");
        }

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;

        return switch (sortBy.trim().toUpperCase()) {
            case "CHEAPEST", "PRICE", "BASEPRICE" -> Sort.by(Sort.Direction.ASC, "basePrice");
            case "FASTEST", "DURATION", "DURATIONMINUTES" -> Sort.by(Sort.Direction.ASC, "durationMinutes");
            case "EARLIEST_DEPARTURE" -> Sort.by(Sort.Direction.ASC, "departureTime");
            case "LATEST_DEPARTURE" -> Sort.by(Sort.Direction.DESC, "departureTime");
            case "BEST" -> Sort.by(Sort.Order.asc("durationMinutes"), Sort.Order.asc("basePrice"));
            case "ARRIVAL", "ARRIVALTIME" -> Sort.by(direction, "arrivalTime");
            case "AIRLINE" -> Sort.by(direction, "airline");
            case "CREATEDAT" -> Sort.by(direction, "createdAt");
            default -> Sort.by(direction, "departureTime");
        };
    }
}
