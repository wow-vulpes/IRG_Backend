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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/obligations")
@RequiredArgsConstructor
@Tag(name = "Obligations", description = "Управление подписками и платежами")
public class ObligationController {

    private final ObligationService service;
    private final SseService sseService;

    @Operation(summary = "Подписка на события", description = "Открывает SSE-соединение для получения событий об удалении подписок в реальном времени")
    @GetMapping("/stream")
    public SseEmitter stream() {
        return sseService.subscribe();
    }

    @Operation(summary = "Создать обязательство", description = "Создает новую подписку или разовый платеж. Возвращает предупреждение, если активная подписка с таким именем уже существует.")
    @PostMapping
    public ResponseEntity<ObligationCreateResponse> create(@Valid @RequestBody ObligationCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @Operation(summary = "Список обязательств", description = "Возвращает список всех подписок с возможностью фильтрации по категории и статусу")
    @GetMapping
    public ResponseEntity<List<ObligationResponse>> getAll(
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) Status status) {
        return ResponseEntity.ok(service.getAll(category, status));
    }

    @Operation(summary = "Ближайшие платежи", description = "Суммирует все платежи за указанное количество дней (по умолчанию 7)")
    @GetMapping("/upcoming")
    public ResponseEntity<UpcomingResponse> getUpcoming(
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(service.getUpcoming(days));
    }

    @Operation(summary = "Оплатить обязательство", description = "Фиксирует оплату подписки и сдвигает дату следующего платежа (если подписка рекуррентная). Возвращает 422, если статус не ACTIVE.")
    @PostMapping("/{id}/pay")
    public ResponseEntity<PaymentResultResponse> pay(@PathVariable UUID id) {
        return ResponseEntity.ok(service.pay(id));
    }

    @Operation(summary = "Отменить обязательство", description = "Переводит подписку в статус CANCELLED. Возвращает 422, если подписка уже не ACTIVE.")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        service.cancel(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Удалить обязательство", description = "Полностью удаляет подписку из БД и отправляет SSE событие всем подключенным клиентам.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
