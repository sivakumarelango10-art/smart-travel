package com.smarttravel.modules.flight.repository;

import com.smarttravel.modules.flight.dto.FlightSearchCriteria;
import com.smarttravel.modules.flight.model.Flight;
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

        // Departure Date filter (UTC day range)
        if (criteria.getDepartureDate() != null) {
            LocalDate date = criteria.getDepartureDate();
            Instant startOfDay = date.atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant endOfDay = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
            andCriteriaList.add(Criteria.where("departureTime").gte(startOfDay).lt(endOfDay));
        }

        // Airline filter
        if (criteria.getAirline() != null && !criteria.getAirline().isBlank()) {
            andCriteriaList.add(Criteria.where("airline").regex(Pattern.quote(criteria.getAirline().trim()), "i"));
        }

        // Cabin Class filter
        if (criteria.getCabinClass() != null) {
            andCriteriaList.add(Criteria.where("cabinClasses").is(criteria.getCabinClass()));
        }

        // Price range filter
        if (criteria.getMinPrice() != null && criteria.getMaxPrice() != null) {
            andCriteriaList.add(Criteria.where("basePrice").gte(criteria.getMinPrice()).lte(criteria.getMaxPrice()));
        } else if (criteria.getMinPrice() != null) {
            andCriteriaList.add(Criteria.where("basePrice").gte(criteria.getMinPrice()));
        } else if (criteria.getMaxPrice() != null) {
            andCriteriaList.add(Criteria.where("basePrice").lte(criteria.getMaxPrice()));
        }

        // Status filter
        if (criteria.getStatus() != null) {
            andCriteriaList.add(Criteria.where("status").is(criteria.getStatus()));
        }

        Query query = new Query();
        if (!andCriteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(andCriteriaList.toArray(new Criteria[0])));
        }

        // Count total matching elements
        long total = mongoTemplate.count(query, Flight.class);

        // Sorting
        String sortBy = mapSortField(criteria.getSortBy());
        Sort.Direction direction = "desc".equalsIgnoreCase(criteria.getSortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy);

        int pageNum = Math.max(0, criteria.getPage());
        int pageSize = criteria.getSize() > 0 ? criteria.getSize() : 20;
        Pageable pageable = PageRequest.of(pageNum, pageSize, sort);

        query.with(pageable);
        List<Flight> flights = mongoTemplate.find(query, Flight.class);

        return new PageImpl<>(flights, pageable, total);
    }

    private String mapSortField(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "departureTime";
        }
        return switch (sortBy.trim().toLowerCase()) {
            case "price", "baseprice" -> "basePrice";
            case "duration", "durationminutes" -> "durationMinutes";
            case "arrival", "arrivaltime" -> "arrivalTime";
            case "airline" -> "airline";
            case "createdat" -> "createdAt";
            default -> "departureTime";
        };
    }
}
