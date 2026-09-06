package com.gamesphere.marketplace.api;

import com.gamesphere.marketplace.service.MarketplaceTransactionService;
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
@PreAuthorize("isAuthenticated()")
public class MarketplaceTransactionController {
    private final MarketplaceTransactionService transactionService;

    public MarketplaceTransactionController(MarketplaceTransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/listings/{listingId}/purchase")
    @ResponseStatus(HttpStatus.CREATED)
    public MarketplaceTransactionResponse initiate(@PathVariable UUID listingId) {
        return transactionService.initiate(listingId);
    }

    @GetMapping("/transactions/{id}")
    public MarketplaceTransactionResponse get(@PathVariable UUID id) {
        return transactionService.findById(id);
    }

    @GetMapping("/users/me/purchases")
    public Page<MarketplaceTransactionResponse> purchases(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return transactionService.findMineAsBuyer(pageable(page, size));
    }

    @GetMapping("/users/me/sales")
    public Page<MarketplaceTransactionResponse> sales(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return transactionService.findMineAsSeller(pageable(page, size));
    }

    @PutMapping("/transactions/{id}/complete")
    public MarketplaceTransactionResponse complete(@PathVariable UUID id) {
        return transactionService.complete(id);
    }

    @PutMapping("/transactions/{id}/cancel")
    public MarketplaceTransactionResponse cancel(@PathVariable UUID id) {
        return transactionService.cancel(id);
    }

    @GetMapping("/moderation/transactions")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<MarketplaceTransactionResponse> all(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return transactionService.findAll(pageable(page, size));
    }

    private PageRequest pageable(int page, int size) {
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}