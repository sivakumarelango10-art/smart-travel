package com.smarttravel.modules.payment.refund.controller;

import com.smarttravel.common.response.ApiResponse;
import com.smarttravel.common.response.PageResponse;
import com.smarttravel.common.security.SecurityUtils;
import com.smarttravel.common.security.UserPrincipal;
import com.smarttravel.modules.payment.refund.dto.RefundEligibilityResponse;
import com.smarttravel.modules.payment.refund.dto.RefundProcessRequest;
import com.smarttravel.modules.payment.refund.dto.RefundResponse;
import com.smarttravel.modules.payment.refund.model.RefundReason;
import com.smarttravel.modules.payment.refund.model.RefundStatus;
import com.smarttravel.modules.payment.refund.service.RefundEligibilityService;
import com.smarttravel.modules.payment.refund.service.RefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller exposing administrative refund endpoints and refund eligibility assessments.
 */
@RestController
@RequestMapping({"/api/v1/admin/refunds", "/v1/admin/refunds", "/api/admin/refunds", "/admin/refunds"})
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Refund Operations", description = "Privileged refund processing and inspection endpoints")
@SecurityRequirement(name = "BearerAuth")
public class AdminRefundController {

    private final RefundService refundService;
    private final RefundEligibilityService refundEligibilityService;

    public AdminRefundController(RefundService refundService,
                                 RefundEligibilityService refundEligibilityService) {
        this.refundService = refundService;
        this.refundEligibilityService = refundEligibilityService;
    }

    @PostMapping("/{paymentId}/process")
    @Operation(summary = "Process Payment Refund", description = "Executes refund via payment gateway abstraction and updates refund audit records")
    public ResponseEntity<ApiResponse<RefundResponse>> processRefund(
            @PathVariable String paymentId,
            @Valid @RequestBody(required = false) RefundProcessRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        String adminUser = principal != null ? principal.getEmail() : SecurityUtils.getCurrentUsernameOrAnonymous();
        RefundResponse response = refundService.processRefund(paymentId, request, adminUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Refund processed successfully", response));
    }

    @GetMapping("/{paymentId}/eligibility")
    @Operation(summary = "Check Refund Eligibility", description = "Evaluates whether payment meets criteria for full or partial refund")
    public ResponseEntity<ApiResponse<RefundEligibilityResponse>> checkEligibility(
            @PathVariable String paymentId,
            @RequestParam(required = false, defaultValue = "FLIGHT_CANCELLED") RefundReason reason) {
        RefundEligibilityResponse response = refundEligibilityService.checkPaymentRefundEligibility(paymentId, reason);
        return ResponseEntity.ok(ApiResponse.success("Refund eligibility assessed", response));
    }

    @GetMapping
    @Operation(summary = "List All Refunds", description = "Retrieves paginated refunds with optional status filtering")
    public ResponseEntity<ApiResponse<PageResponse<RefundResponse>>> getAllRefunds(
            @Parameter(description = "Optional status filter", example = "COMPLETED")
            @RequestParam(required = false) RefundStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<RefundResponse> response = refundService.getAllRefunds(status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Refunds retrieved successfully", response));
    }
}
