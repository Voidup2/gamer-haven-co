package com.gamesphere.groups.api;

import com.gamesphere.groups.service.GroupService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/groups")
public class GroupController {
    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping
    public Page<GroupResponse> list(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return groupService.findPublic(search, PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @GetMapping("/{id}")
    public GroupResponse get(@PathVariable UUID id) {
        return groupService.findById(id);
    }

    @GetMapping("/{id}/members")
    public List<GroupMemberResponse> members(@PathVariable UUID id) {
        return groupService.members(id);
    }

    @GetMapping("/{id}/membership")
    @PreAuthorize("isAuthenticated()")
    public boolean membership(@PathVariable UUID id) {
        return groupService.isMember(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public GroupResponse create(@Valid @RequestBody GroupRequest request) {
        return groupService.create(request);
    }

    @GetMapping("/mine")
    @PreAuthorize("isAuthenticated()")
    public Page<GroupResponse> mine(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return groupService.mine(PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public GroupResponse update(@PathVariable UUID id, @Valid @RequestBody GroupRequest request) {
        return groupService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    public void delete(@PathVariable UUID id) {
        groupService.delete(id);
    }

    @PostMapping("/{id}/join")
    @PreAuthorize("isAuthenticated()")
    public GroupResponse join(@PathVariable UUID id) {
        return groupService.join(id);
    }

    @DeleteMapping("/{id}/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    public void leave(@PathVariable UUID id) {
        groupService.leave(id);
    }
}
