package com.smarttravel.modules.analytics.service;

import com.smarttravel.modules.analytics.dto.*;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * Analytics service implementation using MongoDB aggregation pipelines.
 * No findAll() or in-memory iteration of large collections.
 * All revenue calculations use VERIFIED payments as authoritative source.
 */
@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsServiceImpl.class);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final MongoTemplate mongoTemplate;

    public AnalyticsServiceImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Overview
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public OverviewAnalyticsResponse getOverview() {
        log.debug("Computing platform overview analytics");

        // 1. Booking status counts via aggregation
        Map<String, Long> bookingCounts = aggregateStatusCounts("bookings", "status");
        long totalBookings = bookingCounts.values().stream().mapToLong(Long::longValue).sum();
        long confirmed = bookingCounts.getOrDefault("CONFIRMED", 0L);
        long pending = bookingCounts.getOrDefault("PENDING", 0L);
        long cancelled = bookingCounts.getOrDefault("CANCELLED", 0L);
        long expired = bookingCounts.getOrDefault("EXPIRED", 0L);

        // 2. Revenue from VERIFIED payments only
        BigDecimal grossRevenue = aggregateSumField("payments", "amount",
                Criteria.where("paymentStatus").is("VERIFIED"));
        BigDecimal refundedAmount = aggregateSumField("refunds", "amount",
                Criteria.where("status").is("COMPLETED"));
        BigDecimal netRevenue = grossRevenue.subtract(refundedAmount);

        // 3. Flight status counts
        Map<String, Long> flightCounts = aggregateStatusCounts("flights", "status");
        long totalFlights = mongoTemplate.getCollection("flights").estimatedDocumentCount();
        long activeFlights = mongoTemplate.getCollection("flights")
                .countDocuments(new Document("active", true));
        long scheduled = flightCounts.getOrDefault("SCHEDULED", 0L) +
                flightCounts.getOrDefault("ON_TIME", 0L);
        long delayed = flightCounts.getOrDefault("DELAYED", 0L);
        long cancelledFlights = flightCounts.getOrDefault("CANCELLED", 0L);
        long departed = flightCounts.getOrDefault("DEPARTED", 0L) +
                flightCounts.getOrDefault("ARRIVED", 0L);

        // 4. Seat counts from seats collection
        Map<String, Long> seatCounts = aggregateStatusCounts("seats", "status");
        long totalSeats = seatCounts.values().stream().mapToLong(Long::longValue).sum();
        long availableSeats = seatCounts.getOrDefault("AVAILABLE", 0L);
        long bookedSeats = seatCounts.getOrDefault("BOOKED", 0L);
        long heldSeats = seatCounts.getOrDefault("HELD", 0L);

        // 5. Tickets and check-ins
        long ticketsIssued = mongoTemplate.getCollection("tickets").estimatedDocumentCount();
        long checkIns = mongoTemplate.getCollection("check_ins").estimatedDocumentCount();

        // 6. Customer counts
        long totalCustomers = mongoTemplate.getCollection("users")
                .countDocuments(new Document("roles", "ROLE_CUSTOMER"));
        long activeCustomers = mongoTemplate.getCollection("users")
                .countDocuments(new Document("roles", "ROLE_CUSTOMER").append("active", true));

        // 7. Payment metrics
        Map<String, Long> paymentCounts = aggregateStatusCounts("payments", "paymentStatus");
        long successfulPayments = paymentCounts.getOrDefault("VERIFIED", 0L);
        long failedPayments = paymentCounts.getOrDefault("FAILED", 0L);
        long totalDecided = successfulPayments + failedPayments;
        BigDecimal successRate = totalDecided > 0
                ? BigDecimal.valueOf(successfulPayments * 100.0 / totalDecided).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return OverviewAnalyticsResponse.builder()
                .totalBookings(totalBookings)
                .confirmedBookings(confirmed)
                .pendingBookings(pending)
                .cancelledBookings(cancelled)
                .expiredBookings(expired)
                .totalGrossRevenue(grossRevenue)
                .totalRefundedAmount(refundedAmount)
                .totalNetRevenue(netRevenue)
                .totalFlights(totalFlights)
                .activeFlights(activeFlights)
                .scheduledFlights(scheduled)
                .delayedFlights(delayed)
                .cancelledFlights(cancelledFlights)
                .departedFlights(departed)
                .totalSeats(totalSeats)
                .availableSeats(availableSeats)
                .bookedSeats(bookedSeats)
                .heldSeats(heldSeats)
                .ticketsIssued(ticketsIssued)
                .checkInsCompleted(checkIns)
                .totalCustomers(totalCustomers)
                .activeCustomers(activeCustomers)
                .successfulPayments(successfulPayments)
                .failedPayments(failedPayments)
                .paymentSuccessRate(successRate)
                .generatedAt(Instant.now())
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Revenue
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public RevenueAnalyticsResponse getRevenueAnalytics(AnalyticsDateRangeRequest request) {
        DateRange range = resolveDateRange(request);
        log.debug("Revenue analytics: {} to {}", range.from, range.to);

        // Revenue in requested period (VERIFIED payments only)
        Criteria periodCriteria = Criteria.where("paymentStatus").is("VERIFIED")
                .and("createdAt").gte(range.from).lte(range.to);
        BigDecimal grossRevenue = aggregateSumField("payments", "amount", periodCriteria);
        long successCount = mongoTemplate.getCollection("payments")
                .countDocuments(new Document("paymentStatus", "VERIFIED")
                        .append("createdAt", new Document("$gte", Date.from(range.from)).append("$lte", Date.from(range.to))));

        // Refunds in period
        Criteria refundCriteria = Criteria.where("status").is("COMPLETED")
                .and("createdAt").gte(range.from).lte(range.to);
        BigDecimal refundedAmount = aggregateSumField("refunds", "amount", refundCriteria);
        BigDecimal netRevenue = grossRevenue.subtract(refundedAmount);

        BigDecimal avgOrderValue = successCount > 0
                ? grossRevenue.divide(BigDecimal.valueOf(successCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Fixed comparison periods (always UTC midnight)
        Instant todayStart = ZonedDateTime.now(ZoneOffset.UTC).toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant todayEnd = todayStart.plus(Duration.ofDays(1)).minusSeconds(1);
        Instant last7Start = todayStart.minus(Duration.ofDays(7));
        Instant last30Start = todayStart.minus(Duration.ofDays(30));
        Instant thisMonthStart = ZonedDateTime.now(ZoneOffset.UTC).with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant prevMonthStart = ZonedDateTime.now(ZoneOffset.UTC).minusMonths(1).with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant prevMonthEnd = thisMonthStart.minusSeconds(1);

        BigDecimal revenueToday = aggregateSumField("payments", "amount",
                Criteria.where("paymentStatus").is("VERIFIED").and("createdAt").gte(todayStart).lte(todayEnd));
        BigDecimal revenueLast7 = aggregateSumField("payments", "amount",
                Criteria.where("paymentStatus").is("VERIFIED").and("createdAt").gte(last7Start).lte(todayEnd));
        BigDecimal revenueLast30 = aggregateSumField("payments", "amount",
                Criteria.where("paymentStatus").is("VERIFIED").and("createdAt").gte(last30Start).lte(todayEnd));
        BigDecimal revenueThisMonth = aggregateSumField("payments", "amount",
                Criteria.where("paymentStatus").is("VERIFIED").and("createdAt").gte(thisMonthStart).lte(todayEnd));
        BigDecimal revenuePrevMonth = aggregateSumField("payments", "amount",
                Criteria.where("paymentStatus").is("VERIFIED").and("createdAt").gte(prevMonthStart).lte(prevMonthEnd));

        // Daily trend
        List<TrendDataPoint> trend = buildRevenueTrend(range);

        return RevenueAnalyticsResponse.builder()
                .grossRevenue(grossRevenue)
                .refundedAmount(refundedAmount)
                .netRevenue(netRevenue)
                .successfulPaymentCount(successCount)
                .averageOrderValue(avgOrderValue)
                .revenueToday(revenueToday)
                .revenueLast7Days(revenueLast7)
                .revenueLast30Days(revenueLast30)
                .revenueThisMonth(revenueThisMonth)
                .revenuePreviousMonth(revenuePrevMonth)
                .trend(trend)
                .period(request.getPeriod() != null ? request.getPeriod().name() : "custom")
                .from(range.from)
                .to(range.to)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bookings
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public BookingAnalyticsResponse getBookingAnalytics(AnalyticsDateRangeRequest request) {
        DateRange range = resolveDateRange(request);
        log.debug("Booking analytics: {} to {}", range.from, range.to);

        // Status counts in period via aggregation pipeline
        Criteria periodMatch = Criteria.where("createdAt").gte(range.from).lte(range.to);
        Map<String, Long> statusCounts = aggregateStatusCountsWithCriteria("bookings", "status", periodMatch);

        long total = statusCounts.values().stream().mapToLong(Long::longValue).sum();
        long confirmedCount = statusCounts.getOrDefault("CONFIRMED", 0L);
        long pendingCount = statusCounts.getOrDefault("PENDING", 0L);
        long cancelledCount = statusCounts.getOrDefault("CANCELLED", 0L);
        long expiredCount = statusCounts.getOrDefault("EXPIRED", 0L);

        BigDecimal confirmationRate = total > 0
                ? BigDecimal.valueOf(confirmedCount * 100.0 / total).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal cancellationRate = total > 0
                ? BigDecimal.valueOf(cancelledCount * 100.0 / total).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal expirationRate = total > 0
                ? BigDecimal.valueOf(expiredCount * 100.0 / total).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Avg booking value from confirmed bookings only
        BigDecimal avgValue = aggregateAvgField("bookings", "totalAmount",
                Criteria.where("status").is("CONFIRMED").and("createdAt").gte(range.from).lte(range.to));

        List<TrendDataPoint> trend = buildBookingTrend(range);

        return BookingAnalyticsResponse.builder()
                .totalBookings(total)
                .confirmedBookings(confirmedCount)
                .pendingBookings(pendingCount)
                .cancelledBookings(cancelledCount)
                .expiredBookings(expiredCount)
                .confirmationRate(confirmationRate)
                .cancellationRate(cancellationRate)
                .expirationRate(expirationRate)
                .averageBookingValue(avgValue)
                .trend(trend)
                .period(request.getPeriod() != null ? request.getPeriod().name() : "custom")
                .from(range.from)
                .to(range.to)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Flights
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public FlightAnalyticsResponse getFlightAnalytics(AnalyticsDateRangeRequest request) {
        DateRange range = resolveDateRange(request);
        log.debug("Flight analytics: {} to {}", range.from, range.to);

        // Status distribution (all flights, not date-filtered — flights are not time-scoped by creation)
        Map<String, Long> statusDist = aggregateStatusCounts("flights", "status");
        long total = mongoTemplate.getCollection("flights").estimatedDocumentCount();
        long active = mongoTemplate.getCollection("flights")
                .countDocuments(new Document("active", true));

        Instant todayStart = ZonedDateTime.now(ZoneOffset.UTC).toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant todayEnd = todayStart.plus(Duration.ofDays(1));

        long departingToday = mongoTemplate.getCollection("flights")
                .countDocuments(new Document("departureTime",
                        new Document("$gte", Date.from(todayStart)).append("$lt", Date.from(todayEnd)))
                        .append("active", true));

        // Low inventory: active flights where availableSeats < 10% of totalSeats
        // Use aggregation to compute this server-side
        long lowInventory = countLowInventoryFlights();

        // Average occupancy (server-side aggregation on flights.cabinInventories)
        BigDecimal avgOccupancy = computeAverageFlightOccupancy();

        // Top flights by revenue (join bookings → flights)
        List<FlightPerformanceDto> topByRevenue = getTopFlightsByRevenue(range, 10);
        List<FlightPerformanceDto> topByBookings = getTopFlightsByBookings(range, 10);
        List<FlightPerformanceDto> topByOccupancy = getTopFlightsByOccupancy(10);
        List<FlightPerformanceDto> leastUtilized = getLeastUtilizedFlights(10);

        return FlightAnalyticsResponse.builder()
                .totalFlights(total)
                .activeFlights(active)
                .scheduledFlights(statusDist.getOrDefault("SCHEDULED", 0L) +
                        statusDist.getOrDefault("ON_TIME", 0L))
                .boardingFlights(statusDist.getOrDefault("BOARDING", 0L))
                .delayedFlights(statusDist.getOrDefault("DELAYED", 0L))
                .cancelledFlights(statusDist.getOrDefault("CANCELLED", 0L))
                .departedFlights(statusDist.getOrDefault("DEPARTED", 0L))
                .arrivedFlights(statusDist.getOrDefault("ARRIVED", 0L))
                .divertedFlights(statusDist.getOrDefault("DIVERTED", 0L))
                .flightsDepartingToday(departingToday)
                .flightsWithLowInventory(lowInventory)
                .averageOccupancyPercentage(avgOccupancy)
                .statusDistribution(statusDist)
                .topByRevenue(topByRevenue)
                .topByBookings(topByBookings)
                .topByOccupancy(topByOccupancy)
                .leastUtilized(leastUtilized)
                .period(request.getPeriod() != null ? request.getPeriod().name() : "custom")
                .from(range.from)
                .to(range.to)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Seats
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public SeatAnalyticsResponse getSeatAnalytics() {
        log.debug("Computing seat analytics by cabin class");

        // Aggregate seats by cabinClass + status
        AggregationOperation match = match(new Criteria()); // all seats
        AggregationOperation group = group("cabinClass", "status").count().as("count");
        AggregationOperation project = project("count")
                .and("_id.cabinClass").as("cabinClass")
                .and("_id.status").as("status");

        Aggregation agg = newAggregation(match, group, project);
        AggregationResults<Document> results = mongoTemplate.aggregate(agg, "seats", Document.class);

        // Aggregate into cabin-level map: cabin -> status -> count
        Map<String, Map<String, Long>> cabinStatusMap = new LinkedHashMap<>();
        for (Document doc : results.getMappedResults()) {
            String cabin = doc.getString("cabinClass");
            String status = doc.getString("status");
            long count = ((Number) doc.get("count")).longValue();
            if (cabin == null) continue;
            cabinStatusMap.computeIfAbsent(cabin, k -> new LinkedHashMap<>())
                    .merge(status, count, Long::sum);
        }

        long totalSeats = 0, availableSeats = 0, bookedSeats = 0, heldSeats = 0;
        List<CabinUtilizationDto> cabinList = new ArrayList<>();

        for (Map.Entry<String, Map<String, Long>> entry : cabinStatusMap.entrySet()) {
            String cabin = entry.getKey();
            Map<String, Long> statusMap = entry.getValue();
            long cabinTotal = statusMap.values().stream().mapToLong(Long::longValue).sum();
            long cabinAvail = statusMap.getOrDefault("AVAILABLE", 0L);
            long cabinBooked = statusMap.getOrDefault("BOOKED", 0L);
            long cabinHeld = statusMap.getOrDefault("HELD", 0L);
            BigDecimal occ = cabinTotal > 0
                    ? BigDecimal.valueOf(cabinBooked * 100.0 / cabinTotal).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            cabinList.add(new CabinUtilizationDto(cabin, cabinTotal, cabinAvail, cabinBooked, cabinHeld, occ));
            totalSeats += cabinTotal;
            availableSeats += cabinAvail;
            bookedSeats += cabinBooked;
            heldSeats += cabinHeld;
        }

        // Sort by canonical order
        List<String> order = List.of("ECONOMY", "PREMIUM_ECONOMY", "BUSINESS", "FIRST");
        cabinList.sort(Comparator.comparingInt(c -> {
            int idx = order.indexOf(c.getCabinClass());
            return idx == -1 ? 99 : idx;
        }));

        BigDecimal overallOcc = totalSeats > 0
                ? BigDecimal.valueOf(bookedSeats * 100.0 / totalSeats).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return SeatAnalyticsResponse.builder()
                .totalSeats(totalSeats)
                .availableSeats(availableSeats)
                .bookedSeats(bookedSeats)
                .heldSeats(heldSeats)
                .overallOccupancyPercentage(overallOcc)
                .cabinUtilization(cabinList)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Payments
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public PaymentAnalyticsResponse getPaymentAnalytics(AnalyticsDateRangeRequest request) {
        DateRange range = resolveDateRange(request);
        log.debug("Payment analytics: {} to {}", range.from, range.to);

        Criteria periodMatch = Criteria.where("createdAt").gte(range.from).lte(range.to);
        Map<String, Long> statusCounts = aggregateStatusCountsWithCriteria("payments", "paymentStatus", periodMatch);

        long total = statusCounts.values().stream().mapToLong(Long::longValue).sum();
        long successful = statusCounts.getOrDefault("VERIFIED", 0L);
        long failed = statusCounts.getOrDefault("FAILED", 0L);
        long pending = statusCounts.getOrDefault("PENDING", 0L) +
                statusCounts.getOrDefault("ORDER_CREATED", 0L) +
                statusCounts.getOrDefault("CREATED", 0L);
        long cancelled = statusCounts.getOrDefault("CANCELLED", 0L);
        long expired = statusCounts.getOrDefault("EXPIRED", 0L);

        BigDecimal successfulAmount = aggregateSumField("payments", "amount",
                Criteria.where("paymentStatus").is("VERIFIED").and("createdAt").gte(range.from).lte(range.to));
        BigDecimal refundedAmount = aggregateSumField("refunds", "amount",
                Criteria.where("status").is("COMPLETED").and("createdAt").gte(range.from).lte(range.to));

        long decided = successful + failed;
        BigDecimal successRate = decided > 0
                ? BigDecimal.valueOf(successful * 100.0 / decided).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<TrendDataPoint> trend = buildPaymentTrend(range);

        return PaymentAnalyticsResponse.builder()
                .totalPayments(total)
                .successfulPayments(successful)
                .failedPayments(failed)
                .pendingPayments(pending)
                .cancelledPayments(cancelled)
                .expiredPayments(expired)
                .totalSuccessfulAmount(successfulAmount)
                .totalRefundedAmount(refundedAmount)
                .paymentSuccessRate(successRate)
                .trend(trend)
                .period(request.getPeriod() != null ? request.getPeriod().name() : "custom")
                .from(range.from)
                .to(range.to)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Customers
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public CustomerAnalyticsResponse getCustomerAnalytics(AnalyticsDateRangeRequest request) {
        DateRange range = resolveDateRange(request);
        log.debug("Customer analytics: {} to {}", range.from, range.to);

        // Total customers (ROLE_CUSTOMER users)
        long totalCustomers = mongoTemplate.getCollection("users")
                .countDocuments(new Document("roles", "ROLE_CUSTOMER"));
        long activeCustomers = mongoTemplate.getCollection("users")
                .countDocuments(new Document("roles", "ROLE_CUSTOMER").append("active", true));

        // New customers in period
        long newInPeriod = mongoTemplate.getCollection("users")
                .countDocuments(new Document("roles", "ROLE_CUSTOMER")
                        .append("createdAt", new Document("$gte", Date.from(range.from)).append("$lte", Date.from(range.to))));

        // Customers with at least one booking (via aggregation on bookings)
        long customersWithBookings = countDistinctUsersWithBookings();
        long repeatCustomers = countRepeatCustomers();

        BigDecimal avgBookings = customersWithBookings > 0
                ? BigDecimal.valueOf((double) getTotalConfirmedBookingCount() / customersWithBookings)
                        .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<TrendDataPoint> trend = buildCustomerTrend(range);

        return CustomerAnalyticsResponse.builder()
                .totalCustomers(totalCustomers)
                .activeCustomers(activeCustomers)
                .customersWithBookings(customersWithBookings)
                .repeatCustomers(repeatCustomers)
                .averageBookingsPerCustomer(avgBookings)
                .newCustomersInPeriod(newInPeriod)
                .trend(trend)
                .period(request.getPeriod() != null ? request.getPeriod().name() : "custom")
                .from(range.from)
                .to(range.to)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Trend builders (MongoDB $dateTrunc aggregation)
    // ─────────────────────────────────────────────────────────────────────────

    private List<TrendDataPoint> buildRevenueTrend(DateRange range) {
        try {
            return runDateTrendAggregation("payments",
                    List.of(
                            new Document("$match", new Document("paymentStatus", "VERIFIED")
                                    .append("createdAt", new Document("$gte", Date.from(range.from)).append("$lte", Date.from(range.to)))),
                            new Document("$group", new Document("_id",
                                    new Document("$dateToString", new Document("format", "%Y-%m-%d").append("date", "$createdAt").append("timezone", "UTC")))
                                    .append("grossRevenue", new Document("$sum", "$amount"))),
                            new Document("$sort", new Document("_id", 1))
                    ),
                    doc -> {
                        String date = doc.getString("_id");
                        BigDecimal gross = toBigDecimal(doc.get("grossRevenue"));
                        return TrendDataPoint.builder().date(date).grossRevenue(gross).build();
                    }
            );
        } catch (Exception e) {
            log.warn("Revenue trend aggregation failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<TrendDataPoint> buildBookingTrend(DateRange range) {
        try {
            return runDateTrendAggregation("bookings",
                    List.of(
                            new Document("$match", new Document("createdAt",
                                    new Document("$gte", Date.from(range.from)).append("$lte", Date.from(range.to)))),
                            new Document("$group", new Document("_id",
                                    new Document("$dateToString", new Document("format", "%Y-%m-%d").append("date", "$createdAt").append("timezone", "UTC")))
                                    .append("total", new Document("$sum", 1))
                                    .append("confirmed", new Document("$sum", new Document("$cond",
                                            List.of(new Document("$eq", List.of("$status", "CONFIRMED")), 1, 0))))
                                    .append("cancelled", new Document("$sum", new Document("$cond",
                                            List.of(new Document("$eq", List.of("$status", "CANCELLED")), 1, 0))))
                                    .append("expired", new Document("$sum", new Document("$cond",
                                            List.of(new Document("$eq", List.of("$status", "EXPIRED")), 1, 0))))
                                    .append("pending", new Document("$sum", new Document("$cond",
                                            List.of(new Document("$eq", List.of("$status", "PENDING")), 1, 0))))
                            ),
                            new Document("$sort", new Document("_id", 1))
                    ),
                    doc -> TrendDataPoint.builder()
                            .date(doc.getString("_id"))
                            .bookings(toLong(doc.get("total")))
                            .confirmed(toLong(doc.get("confirmed")))
                            .cancelled(toLong(doc.get("cancelled")))
                            .expired(toLong(doc.get("expired")))
                            .pending(toLong(doc.get("pending")))
                            .build()
            );
        } catch (Exception e) {
            log.warn("Booking trend aggregation failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<TrendDataPoint> buildPaymentTrend(DateRange range) {
        try {
            return runDateTrendAggregation("payments",
                    List.of(
                            new Document("$match", new Document("createdAt",
                                    new Document("$gte", Date.from(range.from)).append("$lte", Date.from(range.to)))),
                            new Document("$group", new Document("_id",
                                    new Document("$dateToString", new Document("format", "%Y-%m-%d").append("date", "$createdAt").append("timezone", "UTC")))
                                    .append("successful", new Document("$sum", new Document("$cond",
                                            List.of(new Document("$eq", List.of("$paymentStatus", "VERIFIED")), 1, 0))))
                                    .append("failed", new Document("$sum", new Document("$cond",
                                            List.of(new Document("$eq", List.of("$paymentStatus", "FAILED")), 1, 0))))
                            ),
                            new Document("$sort", new Document("_id", 1))
                    ),
                    doc -> TrendDataPoint.builder()
                            .date(doc.getString("_id"))
                            .successfulPayments(toLong(doc.get("successful")))
                            .failedPayments(toLong(doc.get("failed")))
                            .build()
            );
        } catch (Exception e) {
            log.warn("Payment trend aggregation failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<TrendDataPoint> buildCustomerTrend(DateRange range) {
        try {
            return runDateTrendAggregation("users",
                    List.of(
                            new Document("$match", new Document("roles", "ROLE_CUSTOMER")
                                    .append("createdAt", new Document("$gte", Date.from(range.from)).append("$lte", Date.from(range.to)))),
                            new Document("$group", new Document("_id",
                                    new Document("$dateToString", new Document("format", "%Y-%m-%d").append("date", "$createdAt").append("timezone", "UTC")))
                                    .append("newCustomers", new Document("$sum", 1))),
                            new Document("$sort", new Document("_id", 1))
                    ),
                    doc -> TrendDataPoint.builder()
                            .date(doc.getString("_id"))
                            .newCustomers(toLong(doc.get("newCustomers")))
                            .build()
            );
        } catch (Exception e) {
            log.warn("Customer trend aggregation failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Top flight helpers
    // ─────────────────────────────────────────────────────────────────────────

    private List<FlightPerformanceDto> getTopFlightsByRevenue(DateRange range, int limit) {
        try {
            List<Document> pipeline = List.of(
                    new Document("$match", new Document("status", "CONFIRMED")
                            .append("createdAt", new Document("$gte", Date.from(range.from)).append("$lte", Date.from(range.to)))),
                    new Document("$group", new Document("_id", "$flightId")
                            .append("flightNumber", new Document("$first", "$flightNumber"))
                            .append("airline", new Document("$first", "$airline"))
                            .append("origin", new Document("$first", "$departureAirport.code"))
                            .append("destination", new Document("$first", "$arrivalAirport.code"))
                            .append("departureTime", new Document("$first", "$departureTime"))
                            .append("bookingCount", new Document("$sum", 1))
                            .append("revenue", new Document("$sum", "$totalAmount"))),
                    new Document("$sort", new Document("revenue", -1)),
                    new Document("$limit", limit)
            );
            return runFlightPipelineWithOccupancy(pipeline);
        } catch (Exception e) {
            log.warn("Top flights by revenue failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<FlightPerformanceDto> getTopFlightsByBookings(DateRange range, int limit) {
        try {
            List<Document> pipeline = List.of(
                    new Document("$match", new Document("status", "CONFIRMED")
                            .append("createdAt", new Document("$gte", Date.from(range.from)).append("$lte", Date.from(range.to)))),
                    new Document("$group", new Document("_id", "$flightId")
                            .append("flightNumber", new Document("$first", "$flightNumber"))
                            .append("airline", new Document("$first", "$airline"))
                            .append("origin", new Document("$first", "$departureAirport.code"))
                            .append("destination", new Document("$first", "$arrivalAirport.code"))
                            .append("departureTime", new Document("$first", "$departureTime"))
                            .append("bookingCount", new Document("$sum", 1))
                            .append("revenue", new Document("$sum", "$totalAmount"))),
                    new Document("$sort", new Document("bookingCount", -1)),
                    new Document("$limit", limit)
            );
            return runFlightPipelineWithOccupancy(pipeline);
        } catch (Exception e) {
            log.warn("Top flights by bookings failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<FlightPerformanceDto> getTopFlightsByOccupancy(int limit) {
        try {
            // Compute occupancy from flights.cabinInventories server-side
            List<Document> pipeline = List.of(
                    new Document("$match", new Document("active", true).append("totalSeats", new Document("$gt", 0))),
                    new Document("$project", new Document("flightNumber", 1)
                            .append("airline", 1)
                            .append("departureAirport", 1)
                            .append("arrivalAirport", 1)
                            .append("departureTime", 1)
                            .append("totalSeats", 1)
                            .append("availableSeats", 1)
                            .append("bookedSeats", new Document("$subtract", List.of("$totalSeats", "$availableSeats")))
                            .append("occupancy", new Document("$multiply", List.of(
                                    new Document("$divide", List.of(
                                            new Document("$subtract", List.of("$totalSeats", "$availableSeats")),
                                            "$totalSeats"
                                    )), 100
                            )))),
                    new Document("$sort", new Document("occupancy", -1)),
                    new Document("$limit", limit)
            );
            return mongoTemplate.getCollection("flights").aggregate(pipeline).into(new ArrayList<>())
                    .stream().map(doc -> FlightPerformanceDto.builder()
                            .flightId(doc.getObjectId("_id").toHexString())
                            .flightNumber(doc.getString("flightNumber"))
                            .airline(doc.getString("airline"))
                            .origin(getNestedString(doc, "departureAirport", "code"))
                            .destination(getNestedString(doc, "arrivalAirport", "code"))
                            .departureTime(doc.getDate("departureTime") != null ? doc.getDate("departureTime").toInstant().toString() : null)
                            .totalSeats(doc.getInteger("totalSeats", 0))
                            .bookedSeats(((Number) doc.getOrDefault("bookedSeats", 0)).intValue())
                            .occupancyPercentage(toBigDecimal(doc.get("occupancy")).setScale(2, RoundingMode.HALF_UP))
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Top flights by occupancy failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<FlightPerformanceDto> getLeastUtilizedFlights(int limit) {
        try {
            List<Document> pipeline = List.of(
                    new Document("$match", new Document("active", true).append("totalSeats", new Document("$gt", 0))),
                    new Document("$project", new Document("flightNumber", 1)
                            .append("airline", 1)
                            .append("departureAirport", 1)
                            .append("arrivalAirport", 1)
                            .append("departureTime", 1)
                            .append("totalSeats", 1)
                            .append("availableSeats", 1)
                            .append("bookedSeats", new Document("$subtract", List.of("$totalSeats", "$availableSeats")))
                            .append("occupancy", new Document("$multiply", List.of(
                                    new Document("$divide", List.of(
                                            new Document("$subtract", List.of("$totalSeats", "$availableSeats")),
                                            "$totalSeats"
                                    )), 100
                            )))),
                    new Document("$sort", new Document("occupancy", 1)),
                    new Document("$limit", limit)
            );
            return mongoTemplate.getCollection("flights").aggregate(pipeline).into(new ArrayList<>())
                    .stream().map(doc -> FlightPerformanceDto.builder()
                            .flightId(doc.getObjectId("_id").toHexString())
                            .flightNumber(doc.getString("flightNumber"))
                            .airline(doc.getString("airline"))
                            .origin(getNestedString(doc, "departureAirport", "code"))
                            .destination(getNestedString(doc, "arrivalAirport", "code"))
                            .departureTime(doc.getDate("departureTime") != null ? doc.getDate("departureTime").toInstant().toString() : null)
                            .totalSeats(doc.getInteger("totalSeats", 0))
                            .bookedSeats(((Number) doc.getOrDefault("bookedSeats", 0)).intValue())
                            .occupancyPercentage(toBigDecimal(doc.get("occupancy")).setScale(2, RoundingMode.HALF_UP))
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Least utilized flights failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<FlightPerformanceDto> runFlightPipelineWithOccupancy(List<Document> pipeline) {
        return mongoTemplate.getCollection("bookings").aggregate(pipeline).into(new ArrayList<>())
                .stream().map(doc -> {
                    String flightId = doc.getString("_id");
                    return FlightPerformanceDto.builder()
                            .flightId(flightId)
                            .flightNumber(doc.getString("flightNumber"))
                            .airline(doc.getString("airline"))
                            .origin(doc.getString("origin"))
                            .destination(doc.getString("destination"))
                            .departureTime(doc.getDate("departureTime") != null ? doc.getDate("departureTime").toInstant().toString() : null)
                            .bookingCount(toLong(doc.get("bookingCount")))
                            .revenue(toBigDecimal(doc.get("revenue")))
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private Map<String, Long> aggregateStatusCounts(String collection, String field) {
        List<Document> pipeline = List.of(
                new Document("$group", new Document("_id", "$" + field).append("count", new Document("$sum", 1)))
        );
        Map<String, Long> result = new LinkedHashMap<>();
        mongoTemplate.getCollection(collection).aggregate(pipeline).into(new ArrayList<>())
                .forEach(doc -> {
                    Object id = doc.get("_id");
                    if (id != null) {
                        result.put(id.toString(), toLong(doc.get("count")));
                    }
                });
        return result;
    }

    private Map<String, Long> aggregateStatusCountsWithCriteria(String collection, String field, Criteria criteria) {
        Document matchDoc = criteria.getCriteriaObject();
        List<Document> pipeline = List.of(
                new Document("$match", matchDoc),
                new Document("$group", new Document("_id", "$" + field).append("count", new Document("$sum", 1)))
        );
        Map<String, Long> result = new LinkedHashMap<>();
        mongoTemplate.getCollection(collection).aggregate(pipeline).into(new ArrayList<>())
                .forEach(doc -> {
                    Object id = doc.get("_id");
                    if (id != null) {
                        result.put(id.toString(), toLong(doc.get("count")));
                    }
                });
        return result;
    }

    private BigDecimal aggregateSumField(String collection, String field, Criteria criteria) {
        Document matchDoc = criteria.getCriteriaObject();
        List<Document> pipeline = List.of(
                new Document("$match", matchDoc),
                new Document("$group", new Document("_id", null).append("total", new Document("$sum", "$" + field)))
        );
        List<Document> results = mongoTemplate.getCollection(collection).aggregate(pipeline).into(new ArrayList<>());
        if (results.isEmpty()) return BigDecimal.ZERO;
        return toBigDecimal(results.get(0).get("total"));
    }

    private BigDecimal aggregateAvgField(String collection, String field, Criteria criteria) {
        Document matchDoc = criteria.getCriteriaObject();
        List<Document> pipeline = List.of(
                new Document("$match", matchDoc),
                new Document("$group", new Document("_id", null).append("avg", new Document("$avg", "$" + field)))
        );
        List<Document> results = mongoTemplate.getCollection(collection).aggregate(pipeline).into(new ArrayList<>());
        if (results.isEmpty()) return BigDecimal.ZERO;
        BigDecimal avg = toBigDecimal(results.get(0).get("avg"));
        return avg.setScale(2, RoundingMode.HALF_UP);
    }

    private long countLowInventoryFlights() {
        try {
            List<Document> pipeline = List.of(
                    new Document("$match", new Document("active", true).append("totalSeats", new Document("$gt", 0))),
                    new Document("$project", new Document("isLow",
                            new Document("$lt", List.of(
                                    new Document("$divide", List.of("$availableSeats", "$totalSeats")),
                                    0.1
                            )))),
                    new Document("$match", new Document("isLow", true)),
                    new Document("$count", "count")
            );
            List<Document> results = mongoTemplate.getCollection("flights").aggregate(pipeline).into(new ArrayList<>());
            return results.isEmpty() ? 0L : toLong(results.get(0).get("count"));
        } catch (Exception e) {
            log.warn("Low inventory count failed: {}", e.getMessage());
            return 0L;
        }
    }

    private BigDecimal computeAverageFlightOccupancy() {
        try {
            List<Document> pipeline = List.of(
                    new Document("$match", new Document("active", true).append("totalSeats", new Document("$gt", 0))),
                    new Document("$project", new Document("occupancy",
                            new Document("$multiply", List.of(
                                    new Document("$divide", List.of(
                                            new Document("$subtract", List.of("$totalSeats", "$availableSeats")),
                                            "$totalSeats"
                                    )), 100
                            )))),
                    new Document("$group", new Document("_id", null)
                            .append("avgOccupancy", new Document("$avg", "$occupancy")))
            );
            List<Document> results = mongoTemplate.getCollection("flights").aggregate(pipeline).into(new ArrayList<>());
            if (results.isEmpty()) return BigDecimal.ZERO;
            return toBigDecimal(results.get(0).get("avgOccupancy")).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.warn("Average occupancy computation failed: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    private long countDistinctUsersWithBookings() {
        try {
            List<Document> pipeline = List.of(
                    new Document("$match", new Document("status", "CONFIRMED")),
                    new Document("$group", new Document("_id", "$userId")),
                    new Document("$count", "count")
            );
            List<Document> results = mongoTemplate.getCollection("bookings").aggregate(pipeline).into(new ArrayList<>());
            return results.isEmpty() ? 0L : toLong(results.get(0).get("count"));
        } catch (Exception e) {
            log.warn("Count users with bookings failed: {}", e.getMessage());
            return 0L;
        }
    }

    private long countRepeatCustomers() {
        try {
            List<Document> pipeline = List.of(
                    new Document("$match", new Document("status", "CONFIRMED")),
                    new Document("$group", new Document("_id", "$userId")
                            .append("bookingCount", new Document("$sum", 1))),
                    new Document("$match", new Document("bookingCount", new Document("$gt", 1))),
                    new Document("$count", "count")
            );
            List<Document> results = mongoTemplate.getCollection("bookings").aggregate(pipeline).into(new ArrayList<>());
            return results.isEmpty() ? 0L : toLong(results.get(0).get("count"));
        } catch (Exception e) {
            log.warn("Repeat customers count failed: {}", e.getMessage());
            return 0L;
        }
    }

    private long getTotalConfirmedBookingCount() {
        return mongoTemplate.getCollection("bookings")
                .countDocuments(new Document("status", "CONFIRMED"));
    }

    @FunctionalInterface
    private interface TrendDocMapper {
        TrendDataPoint map(Document doc);
    }

    private List<TrendDataPoint> runDateTrendAggregation(String collection,
                                                          List<Document> pipeline,
                                                          TrendDocMapper mapper) {
        return mongoTemplate.getCollection(collection).aggregate(pipeline).into(new ArrayList<>())
                .stream()
                .filter(doc -> doc.getString("_id") != null)
                .map(mapper::map)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Date range resolution (all UTC, no server locale)
    // ─────────────────────────────────────────────────────────────────────────

    public record DateRange(Instant from, Instant to) {
    }

    public DateRange resolveDateRange(AnalyticsDateRangeRequest request) {
        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
        LocalDate today = nowUtc.toLocalDate();

        if (request == null || request.getPeriod() == null) {
            return new DateRange(
                    today.minusDays(30).atStartOfDay(ZoneOffset.UTC).toInstant(),
                    today.atTime(23, 59, 59).atZone(ZoneOffset.UTC).toInstant()
            );
        }

        return switch (request.getPeriod()) {
            case today -> new DateRange(
                    today.atStartOfDay(ZoneOffset.UTC).toInstant(),
                    today.atTime(23, 59, 59).atZone(ZoneOffset.UTC).toInstant()
            );
            case yesterday -> {
                LocalDate yesterday = today.minusDays(1);
                yield new DateRange(
                        yesterday.atStartOfDay(ZoneOffset.UTC).toInstant(),
                        yesterday.atTime(23, 59, 59).atZone(ZoneOffset.UTC).toInstant()
                );
            }
            case last7days -> new DateRange(
                    today.minusDays(7).atStartOfDay(ZoneOffset.UTC).toInstant(),
                    today.atTime(23, 59, 59).atZone(ZoneOffset.UTC).toInstant()
            );
            case last30days -> new DateRange(
                    today.minusDays(30).atStartOfDay(ZoneOffset.UTC).toInstant(),
                    today.atTime(23, 59, 59).atZone(ZoneOffset.UTC).toInstant()
            );
            case thisMonth -> new DateRange(
                    today.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay(ZoneOffset.UTC).toInstant(),
                    today.atTime(23, 59, 59).atZone(ZoneOffset.UTC).toInstant()
            );
            case lastMonth -> {
                LocalDate firstOfLastMonth = today.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
                LocalDate lastOfLastMonth = firstOfLastMonth.with(TemporalAdjusters.lastDayOfMonth());
                yield new DateRange(
                        firstOfLastMonth.atStartOfDay(ZoneOffset.UTC).toInstant(),
                        lastOfLastMonth.atTime(23, 59, 59).atZone(ZoneOffset.UTC).toInstant()
                );
            }
            case custom -> {
                if (request.getFrom() == null || request.getTo() == null) {
                    throw new IllegalArgumentException("Custom date range requires both 'from' and 'to' parameters");
                }
                if (request.getFrom().isAfter(request.getTo())) {
                    throw new IllegalArgumentException("Date range 'from' must be before or equal to 'to'");
                }
                Duration span = Duration.between(request.getFrom(), request.getTo());
                if (span.toDays() > 366) {
                    throw new IllegalArgumentException("Custom date range cannot exceed 366 days");
                }
                yield new DateRange(request.getFrom(), request.getTo());
            }
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Type helpers
    // ─────────────────────────────────────────────────────────────────────────

    private BigDecimal toBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof BigDecimal bd) return bd;
        if (val instanceof Double d) return BigDecimal.valueOf(d);
        if (val instanceof Integer i) return BigDecimal.valueOf(i);
        if (val instanceof Long l) return BigDecimal.valueOf(l);
        if (val instanceof org.bson.types.Decimal128 d128) return d128.bigDecimalValue();
        try {
            return new BigDecimal(val.toString());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private long toLong(Object val) {
        if (val == null) return 0L;
        if (val instanceof Long l) return l;
        if (val instanceof Integer i) return i.longValue();
        if (val instanceof Double d) return d.longValue();
        if (val instanceof Number n) return n.longValue();
        return 0L;
    }

    private String getNestedString(Document doc, String parentKey, String childKey) {
        Object parent = doc.get(parentKey);
        if (parent instanceof Document parentDoc) {
            return parentDoc.getString(childKey);
        }
        return null;
    }
}
