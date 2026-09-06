package com.gamesphere.marketplace.api;

import com.gamesphere.marketplace.service.SellerRatingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class SellerRatingController {
    private final SellerRatingService ratingService;

    public SellerRatingController(SellerRatingService ratingService) {
        this.ratingService = ratingService;
    }

    @GetMapping("/sellers/{sellerId}/ratings")
    public Page<SellerRatingResponse> list(@PathVariable Long sellerId,
                                            @RequestParam(defaultValue = "0") @Min(0) int page,
                                            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ratingService.findBySeller(sellerId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @GetMapping("/sellers/{sellerId}/rating-summary")
    public SellerRatingService.SellerRatingSummary summary(@PathVariable Long sellerId) {
        return ratingService.summary(sellerId);
    }

    @GetMapping("/seller-ratings/{id}")
    public SellerRatingResponse get(@PathVariable UUID id) {
        return ratingService.findById(id);
    }

    @PostMapping("/listings/{listingId}/seller-rating")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public SellerRatingResponse create(@PathVariable UUID listingId, @Valid @RequestBody SellerRatingRequest request) {
        return ratingService.create(listingId, request);
    }

    @PutMapping("/seller-ratings/{id}")
    @PreAuthorize("isAuthenticated()")
    public SellerRatingResponse update(@PathVariable UUID id, @Valid @RequestBody SellerRatingRequest request) {
        return ratingService.update(id, request);
    }

    @DeleteMapping("/seller-ratings/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    public void delete(@PathVariable UUID id) {
        ratingService.delete(id);
    }
}
