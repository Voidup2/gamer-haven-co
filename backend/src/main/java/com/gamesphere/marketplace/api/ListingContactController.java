package com.gamesphere.marketplace.api;

import com.gamesphere.marketplace.service.ListingContactService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/listings")
public class ListingContactController {

    private final ListingContactService contactService;

    public ListingContactController(ListingContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping("/{id}/contact")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    public void contact(@PathVariable UUID id, @Valid @RequestBody ListingContactRequest request) {
        contactService.contact(id, request);
    }
}
