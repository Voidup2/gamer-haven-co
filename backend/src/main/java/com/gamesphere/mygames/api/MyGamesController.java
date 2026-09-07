package com.gamesphere.mygames.api;

import com.gamesphere.mygames.service.MyGamesService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me/games")
public class MyGamesController {

    private final MyGamesService service;

    public MyGamesController(MyGamesService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Page<MyGameResponse>> mine(
            @RequestParam(defaultValue = "ALL") String source,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (page < 0) throw new IllegalArgumentException("page must be >= 0");
        if (size < 1 || size > 100) throw new IllegalArgumentException("size must be between 1 and 100");
        return ResponseEntity.ok(service.mine(source, PageRequest.of(page, size, Sort.by("title").ascending())));
    }

    @GetMapping("/summary")
    public ResponseEntity<MyGamesSummaryResponse> summary() {
        return ResponseEntity.ok(service.summary());
    }
}
