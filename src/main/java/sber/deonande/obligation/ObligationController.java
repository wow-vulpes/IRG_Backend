package sber.deonande.obligation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import sber.deonande.payment.PaymentResultResponse;
import sber.deonande.sse.SseService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/obligations")
@RequiredArgsConstructor
public class ObligationController {

    private final ObligationService service;
    private final SseService sseService;

    @GetMapping("/stream")
    public SseEmitter stream() {
        return sseService.subscribe();
    }

    @PostMapping
    public ResponseEntity<ObligationCreateResponse> create(@Valid @RequestBody ObligationCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping
    public ResponseEntity<List<ObligationResponse>> getAll(
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) Status status) {
        return ResponseEntity.ok(service.getAll(category, status));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<UpcomingResponse> getUpcoming(
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(service.getUpcoming(days));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<PaymentResultResponse> pay(@PathVariable UUID id) {
        return ResponseEntity.ok(service.pay(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        service.cancel(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
