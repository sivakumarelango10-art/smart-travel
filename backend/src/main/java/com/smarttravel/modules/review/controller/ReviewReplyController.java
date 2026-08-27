package com.smarttravel.modules.review.controller;

import com.smarttravel.common.response.ApiResponse;
import com.smarttravel.modules.review.model.ReviewReply;
import com.smarttravel.modules.review.service.ReviewReplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/reviews/{reviewId}/replies", "/v1/reviews/{reviewId}/replies", "/api/reviews/{reviewId}/replies"})
@Tag(name = "Review Replies", description = "Threaded comments and responses to reviews")
public class ReviewReplyController {

    private final ReviewReplyService replyService;

    public ReviewReplyController(ReviewReplyService replyService) {
        this.replyService = replyService;
    }

    @Operation(summary = "Get all published replies for a review")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReviewReply>>> getReplies(@PathVariable String reviewId) {
        List<ReviewReply> replies = replyService.getRepliesForReview(reviewId);
        return ResponseEntity.ok(ApiResponse.success("Replies retrieved", replies));
    }

    @Operation(summary = "Submit a reply to a review")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReviewReply>> createReply(
            @PathVariable String reviewId,
            @Valid @RequestBody CreateReplyRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        String userName = authentication.getName();
        if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            userName = ud.getUsername();
        }
        ReviewReply reply = replyService.createReply(
                reviewId,
                userId,
                request.userName() != null ? request.userName() : userName,
                request.content()
        );
        return ResponseEntity.ok(ApiResponse.success("Reply submitted", reply));
    }

    @Operation(summary = "Edit own reply")
    @PutMapping("/{replyId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReviewReply>> updateReply(
            @PathVariable String reviewId,
            @PathVariable String replyId,
            @Valid @RequestBody UpdateReplyRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        ReviewReply updated = replyService.updateReply(replyId, userId, request.content(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Reply updated", updated));
    }

    @Operation(summary = "Delete own reply")
    @DeleteMapping("/{replyId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteReply(
            @PathVariable String reviewId,
            @PathVariable String replyId,
            Authentication authentication) {
        String userId = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        replyService.deleteReply(replyId, userId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Reply deleted"));
    }

    public record CreateReplyRequest(
            @NotBlank(message = "Reply content cannot be blank")
            @Size(min = 2, max = 1000, message = "Reply must be 2-1000 characters")
            String content,
            String userName
    ) {}

    public record UpdateReplyRequest(
            @NotBlank(message = "Reply content cannot be blank")
            @Size(min = 2, max = 1000, message = "Reply must be 2-1000 characters")
            String content
    ) {}
}
