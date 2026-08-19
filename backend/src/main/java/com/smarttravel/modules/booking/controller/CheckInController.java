package com.smarttravel.modules.booking.controller;

import com.smarttravel.common.response.ApiResponse;
import com.smarttravel.common.security.SecurityUtils;
import com.smarttravel.common.security.UserPrincipal;
import com.smarttravel.modules.booking.dto.BoardingPassResponse;
import com.smarttravel.modules.booking.dto.CheckInRequest;
import com.smarttravel.modules.booking.dto.CheckInResponse;
import com.smarttravel.modules.booking.service.CheckInService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller for Online Check-In and Boarding Pass Generation/Download.
 */
@RestController
@RequestMapping("/api/v1/bookings/{bookingId}")
@Tag(name = "Check-In & Boarding Pass", description = "Online Passenger Check-In, Boarding Pass Retrieval, and PDF Download APIs")
@SecurityRequirement(name = "BearerAuth")
public class CheckInController {

    private final CheckInService checkInService;

    public CheckInController(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    @PostMapping("/check-in")
    @Operation(
            summary = "Perform Online Check-In",
            description = "Performs online check-in for an eligible confirmed booking within the 24-hour departure window, assigning seats and issuing boarding passes."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Check-in completed successfully", content = @Content(schema = @Schema(implementation = CheckInResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Full authentication is required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking not found or access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Booking not eligible for check-in or outside departure window")
    })
    public ResponseEntity<ApiResponse<CheckInResponse>> checkIn(
            @Parameter(description = "Booking MongoDB ID", example = "66c1e101f1a2b3c4d5e6f801")
            @PathVariable String bookingId,
            @RequestBody(required = false) CheckInRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        String userId = principal != null ? principal.getId() : SecurityUtils.getCurrentUserId().orElse("user-1");
        CheckInResponse response = checkInService.performCheckIn(bookingId, request, userId, false);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Check-in completed successfully", response));
    }

    @GetMapping("/check-in")
    @Operation(
            summary = "Get Check-In Status",
            description = "Retrieves the check-in confirmation and boarding pass summary for a booking."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Check-in retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Full authentication is required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Check-in or booking not found")
    })
    public ResponseEntity<ApiResponse<CheckInResponse>> getCheckIn(
            @Parameter(description = "Booking MongoDB ID", example = "66c1e101f1a2b3c4d5e6f801")
            @PathVariable String bookingId,
            @AuthenticationPrincipal UserPrincipal principal) {
        String userId = principal != null ? principal.getId() : SecurityUtils.getCurrentUserId().orElse("user-1");

        CheckInResponse response = checkInService.getCheckInByBookingId(bookingId, userId, false);
        return ResponseEntity.ok(ApiResponse.success("Check-in retrieved successfully", response));
    }

    @GetMapping("/boarding-pass")
    @Operation(
            summary = "Get Boarding Passes (JSON)",
            description = "Retrieves passenger boarding pass records in JSON format for the booking."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Boarding passes retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Full authentication is required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Boarding pass or booking not found")
    })
    public ResponseEntity<ApiResponse<List<BoardingPassResponse>>> getBoardingPasses(
            @Parameter(description = "Booking MongoDB ID", example = "66c1e101f1a2b3c4d5e6f801")
            @PathVariable String bookingId,
            @AuthenticationPrincipal UserPrincipal principal) {
        String userId = principal != null ? principal.getId() : SecurityUtils.getCurrentUserId().orElse("user-1");

        List<BoardingPassResponse> response = checkInService.getBoardingPasses(bookingId, userId, false);
        return ResponseEntity.ok(ApiResponse.success("Boarding passes retrieved successfully", response));
    }

    @GetMapping("/boarding-pass/pdf")
    @Operation(
            summary = "Download Boarding Pass (PDF)",
            description = "Streams high-fidelity printable vector PDF boarding pass document."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Boarding pass PDF binary stream"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Full authentication is required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Boarding pass or booking not found")
    })
    public ResponseEntity<byte[]> downloadBoardingPassPdf(
            @Parameter(description = "Booking MongoDB ID", example = "66c1e101f1a2b3c4d5e6f801")
            @PathVariable String bookingId,
            @AuthenticationPrincipal UserPrincipal principal) {
        String userId = principal != null ? principal.getId() : SecurityUtils.getCurrentUserId().orElse("user-1");

        byte[] pdfBytes = checkInService.getBoardingPassPdf(bookingId, userId, false);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "SmartTravel-BoardingPass-" + bookingId + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
