package com.smarttravel.modules.flight.provider;

import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.repository.FlightRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

/**
 * High-performance Internal Mock Flight Data Provider.
 * Serves real-time flight telemetry, status transitions, and route progression directly
 * from the MongoDB flight collection and internal simulation engine.
 */
@Component
@Primary
public class MockFlightStatusProviderImpl implements FlightStatusProvider {

    private final FlightRepository flightRepository;

    public MockFlightStatusProviderImpl(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    @Override
    public String getProviderName() {
        return "SMARTTRAVEL_INTERNAL_SIMULATION";
    }

    @Override
    public boolean isLiveProvider() {
        return false;
    }

    @Override
    public Optional<FlightStatusSnapshot> fetchLatestStatus(String flightNumber, Instant scheduledDeparture) {
        if (flightNumber == null || flightNumber.trim().isEmpty()) {
            return Optional.empty();
        }

        String normalizedFlightNumber = flightNumber.toUpperCase().trim();
        return flightRepository.findByFlightNumber(normalizedFlightNumber).map(this::toRichSnapshot);
    }

    private FlightStatusSnapshot toRichSnapshot(Flight f) {
        String num = f.getFlightNumber();
        FlightStatus status = f.getStatus() != null ? f.getStatus() : FlightStatus.SCHEDULED;

        String term = (f.getDepartureAirport() != null && f.getDepartureAirport().getTerminal() != null)
                ? f.getDepartureAirport().getTerminal()
                : "T3";
        String gate = "Gate " + ((Math.abs(num.hashCode()) % 15) + 1);
        String belt = "Belt " + ((Math.abs(num.hashCode()) % 8) + 1);

        String origCode = f.getDepartureAirport() != null ? f.getDepartureAirport().getCode() : "DEL";
        String origCity = f.getDepartureAirport() != null ? f.getDepartureAirport().getCity() : "New Delhi";
        String origName = f.getDepartureAirport() != null ? f.getDepartureAirport().getName() : "Indira Gandhi International Airport";

        String destCode = f.getArrivalAirport() != null ? f.getArrivalAirport().getCode() : "BOM";
        String destCity = f.getArrivalAirport() != null ? f.getArrivalAirport().getCity() : "Mumbai";
        String destName = f.getArrivalAirport() != null ? f.getArrivalAirport().getName() : "Chhatrapati Shivaji Maharaj International Airport";

        double[] origCoords = getAirportCoords(origCode);
        double[] destCoords = getAirportCoords(destCode);

        double progress = status == FlightStatus.ARRIVED ? 100.0 : (status == FlightStatus.DEPARTED ? 58.0 : 0.0);
        int alt = (status == FlightStatus.DEPARTED) ? 36000 : 0;
        int spd = (status == FlightStatus.DEPARTED) ? 840 : 0;

        double curLat = origCoords[0] + (destCoords[0] - origCoords[0]) * (progress / 100.0);
        double curLng = origCoords[1] + (destCoords[1] - origCoords[1]) * (progress / 100.0);

        return new FlightStatusSnapshot(
                num,
                status,
                f.getDelayMinutes(),
                f.getDelayReason(),
                f.getRevisedDepartureTime() != null ? f.getRevisedDepartureTime() : f.getDepartureTime(),
                f.getEstimatedArrival() != null ? f.getEstimatedArrival() : f.getArrivalTime(),
                gate,
                term,
                "MOCK_INTERNAL_SIMULATION",
                f.getAirline() != null ? f.getAirline() : resolveAirlineName(num),
                f.getAirlineCode() != null ? f.getAirlineCode() : resolveAirlineCode(num),
                origCode,
                origCity,
                origName,
                destCode,
                destCity,
                destName,
                f.getDepartureTime(),
                f.getArrivalTime(),
                f.getAircraftModel() != null ? f.getAircraftModel() : "Airbus A321neo",
                alt,
                spd,
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

    private String resolveAirlineName(String flightNum) {
        String num = flightNum.toUpperCase();
        if (num.startsWith("AI")) return "Air India";
        if (num.startsWith("6E")) return "IndiGo";
        if (num.startsWith("UK")) return "Vistara";
        if (num.startsWith("SG")) return "SpiceJet";
        if (num.startsWith("QP")) return "Akasa Air";
        if (num.startsWith("IX")) return "Air India Express";
        if (num.startsWith("EK")) return "Emirates";
        if (num.startsWith("QR")) return "Qatar Airways";
        if (num.startsWith("SQ")) return "Singapore Airlines";
        if (num.startsWith("BA")) return "British Airways";
        if (num.startsWith("LH")) return "Lufthansa";
        if (num.startsWith("EY")) return "Etihad Airways";
        return "Air India";
    }

    private String resolveAirlineCode(String flightNum) {
        String num = flightNum.toUpperCase();
        if (num.startsWith("AI")) return "AI";
        if (num.startsWith("6E")) return "6E";
        if (num.startsWith("UK")) return "UK";
        if (num.startsWith("SG")) return "SG";
        if (num.startsWith("QP")) return "QP";
        if (num.startsWith("IX")) return "IX";
        if (num.startsWith("EK")) return "EK";
        if (num.startsWith("QR")) return "QR";
        if (num.startsWith("SQ")) return "SQ";
        if (num.startsWith("BA")) return "BA";
        if (num.startsWith("LH")) return "LH";
        if (num.startsWith("EY")) return "EY";
        return "AI";
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
            case "DOH" -> new double[]{25.2731, 51.6081};
            case "AUH" -> new double[]{24.4283, 54.6511};
            default -> new double[]{28.5562, 77.1000};
        };
    }
}
