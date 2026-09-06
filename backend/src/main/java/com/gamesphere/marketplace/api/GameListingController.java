package com.gamesphere.marketplace.api;

import com.gamesphere.marketplace.domain.GameListing.Condition;
import com.gamesphere.marketplace.domain.GameListing.Status;
import com.gamesphere.marketplace.service.GameListingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class GameListingController {

    private final GameListingService listingService;

    public GameListingController(GameListingService listingService) {
        this.listingService = listingService;
    }

    @GetMapping("/listings")
    public Page<GameListingResponse> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String gameId,
            @RequestParam(required = false) Condition condition,
            @RequestParam(required = false) @DecimalMin("0.00") BigDecimal minPrice,
            @RequestParam(required = false) @DecimalMin("0.00") BigDecimal maxPrice,
            @RequestParam(required = false) String platform,
            @RequestParam(required = false) Boolean boxIncluded,
            @RequestParam(required = false) Boolean manualIncluded,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("minPrice cannot be greater than maxPrice");
        }
        return listingService.findActive(search, gameId, condition, minPrice, maxPrice, platform,
                boxIncluded, manualIncluded,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @GetMapping("/games/{gameId}/listings")
    public Page<GameListingResponse> forGame(@PathVariable String gameId,
                                              @RequestParam(defaultValue = "0") @Min(0) int page,
                                              @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return listingService.findByGame(gameId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @GetMapping("/listings/{id}")
    public GameListingResponse get(@PathVariable UUID id) {
        return listingService.findById(id);
    }

    @GetMapping("/users/me/listings")
    @PreAuthorize("isAuthenticated()")
    public Page<GameListingResponse> mine(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return listingService.findMine(PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @PostMapping("/games/{gameId}/listings")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public GameListingResponse create(@PathVariable String gameId, @Valid @RequestBody GameListingRequest request) {
        return listingService.create(gameId, request);
    }

    @PutMapping("/listings/{id}")
    @PreAuthorize("isAuthenticated()")
    public GameListingResponse update(@PathVariable UUID id, @Valid @RequestBody GameListingRequest request) {
        return listingService.update(id, request);
    }

    @PutMapping("/listings/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public GameListingResponse status(@PathVariable UUID id, @RequestParam Status status) {
        return listingService.updateStatus(id, status);
    }

    @DeleteMapping("/listings/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    public void delete(@PathVariable UUID id) {
        listingService.delete(id);
    }
}
