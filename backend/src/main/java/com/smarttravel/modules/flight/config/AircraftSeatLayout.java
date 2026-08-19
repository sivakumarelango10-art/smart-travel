package com.smarttravel.modules.flight.config;

import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.Seat;
import com.smarttravel.modules.flight.model.SeatStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Configuration and generator for physical aircraft seat layouts.
 */
@Component
public class AircraftSeatLayout {

    public static class CabinRowSpec {
        private final CabinClass cabinClass;
        private final int startRow;
        private final int endRow;
        private final List<String> columns;
        private final BigDecimal extraLegroomPrice;

        public CabinRowSpec(CabinClass cabinClass, int startRow, int endRow, List<String> columns, BigDecimal extraLegroomPrice) {
            this.cabinClass = cabinClass;
            this.startRow = startRow;
            this.endRow = endRow;
            this.columns = columns;
            this.extraLegroomPrice = extraLegroomPrice;
        }

        public CabinClass getCabinClass() { return cabinClass; }
        public int getStartRow() { return startRow; }
        public int getEndRow() { return endRow; }
        public List<String> getColumns() { return columns; }
        public BigDecimal getExtraLegroomPrice() { return extraLegroomPrice; }
    }

    public List<Seat> generateSeatsForFlight(String flightId, String flightNumber, String aircraftModel, Set<CabinClass> supportedCabins, int targetSeatCount) {
        List<CabinRowSpec> specs = resolveSpecsForAircraft(aircraftModel);
        List<Seat> seats = new ArrayList<>();
        Instant now = Instant.now();

        for (CabinRowSpec spec : specs) {
            // If flight specifically restricts cabins, only generate for supported cabins
            if (supportedCabins != null && !supportedCabins.isEmpty() && !supportedCabins.contains(spec.getCabinClass())) {
                continue;
            }

            for (int row = spec.getStartRow(); row <= spec.getEndRow(); row++) {
                for (String col : spec.getColumns()) {
                    if (targetSeatCount > 0 && seats.size() >= targetSeatCount) {
                        break;
                    }
                    String seatNumber = row + col;
                    BigDecimal priceAdj = BigDecimal.ZERO;
                    // Extra legroom for front rows or exit rows
                    if (row == spec.getStartRow() || row == 12 || row == 14) {
                        priceAdj = spec.getExtraLegroomPrice();
                    }

                    seats.add(Seat.builder()
                            .flightId(flightId)
                            .flightNumber(flightNumber)
                            .seatNumber(seatNumber)
                            .rowNumber(row)
                            .column(col)
                            .cabinClass(spec.getCabinClass())
                            .status(SeatStatus.AVAILABLE)
                            .priceAdjustment(priceAdj)
                            .createdAt(now)
                            .updatedAt(now)
                            .build());
                }
                if (targetSeatCount > 0 && seats.size() >= targetSeatCount) {
                    break;
                }
            }
        }

        // If target seat count was specified and we haven't reached it, fill with standard economy rows
        if (targetSeatCount > 0 && seats.size() < targetSeatCount) {
            int lastRow = seats.isEmpty() ? 1 : seats.get(seats.size() - 1).getRowNumber() + 1;
            List<String> stdCols = List.of("A", "B", "C", "D", "E", "F");
            while (seats.size() < targetSeatCount) {
                for (String col : stdCols) {
                    if (seats.size() >= targetSeatCount) break;
                    seats.add(Seat.builder()
                            .flightId(flightId)
                            .flightNumber(flightNumber)
                            .seatNumber(lastRow + col)
                            .rowNumber(lastRow)
                            .column(col)
                            .cabinClass(CabinClass.ECONOMY)
                            .status(SeatStatus.AVAILABLE)
                            .priceAdjustment(BigDecimal.ZERO)
                            .createdAt(now)
                            .updatedAt(now)
                            .build());
                }
                lastRow++;
            }
        }

        return seats;
    }

    private List<CabinRowSpec> resolveSpecsForAircraft(String aircraftModel) {
        String model = aircraftModel != null ? aircraftModel.toUpperCase() : "";

        if (model.contains("777") || model.contains("350") || model.contains("787") || model.contains("WIDEBODY")) {
            return List.of(
                    new CabinRowSpec(CabinClass.FIRST, 1, 2, List.of("A", "D", "G", "K"), new BigDecimal("2500.00")),
                    new CabinRowSpec(CabinClass.BUSINESS, 3, 6, List.of("A", "C", "D", "G", "H", "K"), new BigDecimal("1500.00")),
                    new CabinRowSpec(CabinClass.PREMIUM_ECONOMY, 7, 10, List.of("A", "B", "C", "D", "E", "F", "G", "H"), new BigDecimal("800.00")),
                    new CabinRowSpec(CabinClass.ECONOMY, 11, 40, List.of("A", "B", "C", "D", "E", "F", "G", "H", "J"), new BigDecimal("400.00"))
            );
        } else if (model.contains("737") || model.contains("BOEING")) {
            return List.of(
                    new CabinRowSpec(CabinClass.BUSINESS, 1, 3, List.of("A", "C", "D", "F"), new BigDecimal("1000.00")),
                    new CabinRowSpec(CabinClass.PREMIUM_ECONOMY, 4, 6, List.of("A", "B", "C", "D", "E", "F"), new BigDecimal("600.00")),
                    new CabinRowSpec(CabinClass.ECONOMY, 7, 30, List.of("A", "B", "C", "D", "E", "F"), new BigDecimal("350.00"))
            );
        } else {
            // Default Narrowbody (A320, etc.)
            return List.of(
                    new CabinRowSpec(CabinClass.BUSINESS, 1, 2, List.of("A", "C", "D", "F"), new BigDecimal("1000.00")),
                    new CabinRowSpec(CabinClass.ECONOMY, 3, 30, List.of("A", "B", "C", "D", "E", "F"), new BigDecimal("350.00"))
            );
        }
    }
}
