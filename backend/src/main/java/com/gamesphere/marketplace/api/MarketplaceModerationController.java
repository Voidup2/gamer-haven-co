package com.gamesphere.marketplace.api;

import com.gamesphere.marketplace.domain.GameListing.Status;
import com.gamesphere.marketplace.service.GameListingService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/moderation/listings")
@PreAuthorize("hasRole('ADMIN')")
public class MarketplaceModerationController {

    private final GameListingService listingService;

    public MarketplaceModerationController(GameListingService listingService) {
        this.listingService = listingService;
    }

    @GetMapping
    public Page<GameListingResponse> list(
            @RequestParam(required = false) Status status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return listingService.findForModeration(status,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @PutMapping("/{id}/remove")
    public GameListingResponse remove(@PathVariable UUID id) {
        return listingService.moderateStatus(id, Status.REMOVED);
    }

    @PutMapping("/{id}/restore")
    public GameListingResponse restore(@PathVariable UUID id) {
        return listingService.moderateStatus(id, Status.ACTIVE);
    }

    @PutMapping("/{id}/sold")
    public GameListingResponse sold(@PathVariable UUID id) {
        return listingService.moderateStatus(id, Status.SOLD);
    }
}
