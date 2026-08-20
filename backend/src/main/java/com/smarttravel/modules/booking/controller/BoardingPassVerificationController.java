package com.smarttravel.modules.booking.controller;

import com.smarttravel.common.response.ApiResponse;
import com.smarttravel.modules.booking.dto.BoardingPassVerificationResponse;
import com.smarttravel.modules.booking.model.BoardingPass;
import com.smarttravel.modules.booking.model.Booking;
import com.smarttravel.modules.booking.model.BookingStatus;
import com.smarttravel.modules.booking.repository.BoardingPassRepository;
import com.smarttravel.modules.booking.repository.BookingRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Controller for validating and verifying Boarding Pass QR/Barcodes at airport gates and security points.
 */
@RestController
@RequestMapping({"/api/v1/boarding-passes", "/v1/boarding-passes"})
@Tag(name = "Boarding Pass Verification", description = "Scanner validation endpoint for 2D QR and Barcode128 tokens")
public class BoardingPassVerificationController {

    private static final Logger log = LoggerFactory.getLogger(BoardingPassVerificationController.class);

    private final BoardingPassRepository boardingPassRepository;
    private final BookingRepository bookingRepository;

    public BoardingPassVerificationController(BoardingPassRepository boardingPassRepository,
                                            BookingRepository bookingRepository) {
        this.boardingPassRepository = boardingPassRepository;
        this.bookingRepository = bookingRepository;
    }

    @GetMapping("/verify")
    @Operation(
            summary = "Verify Boarding Pass QR Code / Barcode",
            description = "Validates an airport gate scanner payload or token string, verifying flight status and passenger clearance."
    )
    public ResponseEntity<ApiResponse<BoardingPassVerificationResponse>> verifyBoardingPass(
            @Parameter(description = "Barcode / QR Token String", example = "BP-ST8K4P2Q-10F")
            @RequestParam("token") String rawToken) {

        log.info("Verifying boarding pass barcode payload: {}", rawToken);

        if (rawToken == null || rawToken.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.<BoardingPassVerificationResponse>builder()
                    .success(false)
                    .message("Token is required for verification")
                    .build());
        }

        // Parse token if structured: STBP|BP-NUM|PNR|FLIGHT|SEAT|PAX
        String cleanToken = rawToken.trim();
        String passNumber = cleanToken;
        String bookingRef = null;

        if (cleanToken.startsWith("STBP|")) {
            String[] parts = cleanToken.split("\\|");
            if (parts.length >= 2) {
                passNumber = parts[1];
            }
            if (parts.length >= 3) {
                bookingRef = parts[2];
            }
        }

        // Locate boarding pass in database
        Optional<BoardingPass> passOpt = boardingPassRepository.findByBoardingPassNumber(passNumber);

        if (passOpt.isEmpty() && bookingRef != null) {
            passOpt = boardingPassRepository.findByBookingReference(bookingRef).stream().findFirst();
        }

        if (passOpt.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("Boarding pass validation result",
                    BoardingPassVerificationResponse.builder()
                            .valid(false)
                            .message("Invalid or unrecognized boarding pass token")
                            .status("REJECTED")
                            .build()));
        }

        BoardingPass bp = passOpt.get();

        // Validate booking status
        boolean bookingConfirmed = true;
        String invalidStatus = "CANCELLED";
        if (bp.getBookingId() != null) {
            Optional<Booking> bookingOpt = bookingRepository.findById(bp.getBookingId());
            if (bookingOpt.isPresent()) {
                Booking b = bookingOpt.get();
                if (b.getStatus() == BookingStatus.CANCELLED || b.getStatus() == BookingStatus.EXPIRED) {
                    bookingConfirmed = false;
                    invalidStatus = b.getStatus().name();
                }
            }
        }

        if (!bookingConfirmed) {
            return ResponseEntity.ok(ApiResponse.success("Boarding pass validation result",
                    BoardingPassVerificationResponse.builder()
                            .valid(false)
                            .message("Booking is cancelled or invalid for boarding")
                            .boardingPassNumber(bp.getBoardingPassNumber())
                            .bookingReference(bp.getBookingReference())
                            .passengerName(bp.getPassengerName())
                            .flightNumber(bp.getFlightNumber())
                            .status(invalidStatus)
                            .build()));
        }

        BoardingPassVerificationResponse response = BoardingPassVerificationResponse.builder()
                .valid(true)
                .message("Boarding pass is authentic and clear for gate entry")
                .boardingPassNumber(bp.getBoardingPassNumber())
                .bookingReference(bp.getBookingReference())
                .passengerName(bp.getPassengerName())
                .flightNumber(bp.getFlightNumber())
                .airline(bp.getAirline())
                .seatNumber(bp.getSeatNumber())
                .cabinClass(bp.getCabinClass())
                .departureAirport(bp.getDepartureAirport())
                .arrivalAirport(bp.getArrivalAirport())
                .departureTime(bp.getDepartureTime())
                .boardingTime(bp.getBoardingTime())
                .gate(bp.getGate())
                .terminal(bp.getTerminal())
                .boardingGroup(bp.getBoardingGroup())
                .status("VERIFIED")
                .build();

        return ResponseEntity.ok(ApiResponse.success("Boarding pass verified successfully", response));
    }
}
