package sber.deonande.obligation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sber.deonande.common.BusinessException;
import sber.deonande.payment.Payment;
import sber.deonande.payment.PaymentMapper;
import sber.deonande.payment.PaymentRepository;
import sber.deonande.payment.PaymentResultResponse;
import sber.deonande.sse.SseService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ObligationService {

    private final ObligationRepository obligationRepository;
    private final ObligationMapper obligationMapper;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final SseService sseService;

    @Transactional
    public ObligationCreateResponse create(ObligationCreateRequest request) {
        Obligation entity = obligationMapper.toEntity(request);

        if (request.getNextPaymentDate() != null && request.getNextPaymentDate().isBefore(LocalDate.now())) {
            entity.setStatus(Status.EXPIRED);
        } else {
            entity.setStatus(Status.ACTIVE);
        }

        String warning = null;
        Optional<Obligation> existingActive = obligationRepository.findByTitleIgnoreCaseAndStatus(request.getTitle(),
                Status.ACTIVE);
        if (existingActive.isPresent()) {
            Obligation existing = existingActive.get();
            checkAndApplyExpiry(existing);

            if (existing.getStatus() == Status.ACTIVE) {
                warning = "Активное обязательство с таким названием уже существует";
            }
        }

        Obligation saved = obligationRepository.saveAndFlush(entity);
        return new ObligationCreateResponse(obligationMapper.toResponse(saved), warning);
    }

    @Transactional
    public List<ObligationResponse> getAll(Category category, Status status) {
        obligationRepository.applyLazyExpiry(LocalDate.now());
        List<Obligation> obligations = obligationRepository.findFiltered(category, status);
        return obligationMapper.toResponseList(obligations);
    }

    @Transactional
    public UpcomingResponse getUpcoming(int days) {
        obligationRepository.applyLazyExpiry(LocalDate.now());
        LocalDate today = LocalDate.now();
        LocalDate endWindow = today.plusDays(days);

        List<Obligation> obligations = obligationRepository.findActiveUpcoming(today, endWindow);

        Map<String, BigDecimal> totals = obligations.stream()
                .collect(Collectors.groupingBy(Obligation::getCurrency,
                        Collectors.reducing(BigDecimal.ZERO, Obligation::getAmount, BigDecimal::add)));

        List<RenewalAlert> alerts = obligations.stream()
                .filter(o -> o.getCategory() == Category.SUBSCRIPTION && o.getRecurrence() != null)
                .map(o -> RenewalAlert.builder()
                        .id(o.getId())
                        .title(o.getTitle())
                        .nextPaymentDate(o.getNextPaymentDate())
                        .amount(o.getAmount())
                        .currency(o.getCurrency())
                        .build())
                .toList();

        return UpcomingResponse.builder()
                .obligations(obligationMapper.toResponseList(obligations))
                .totals(totals)
                .renewalAlerts(alerts)
                .build();
    }

    @Transactional
    public PaymentResultResponse pay(UUID id) {
        Obligation obligation = obligationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Обязательство не найдено"));

        checkAndApplyExpiry(obligation);

        if (obligation.getStatus() != Status.ACTIVE) {
            throw new BusinessException("Оплатить можно только обязательство со статусом active. Текущий статус: "
                    + obligation.getStatus().name().toLowerCase());
        }

        Payment payment = Payment.builder()
                .obligation(obligation)
                .amount(obligation.getAmount())
                .currency(obligation.getCurrency())
                .build();
        payment = paymentRepository.save(payment);

        if (obligation.getRecurrence() == null) {
            obligation.setStatus(Status.CANCELLED);
        } else {
            LocalDate next = obligation.getNextPaymentDate();
            if (next != null) {
                switch (obligation.getRecurrence()) {
                    case MONTHLY -> next = next.plusMonths(1);
                    case QUARTERLY -> next = next.plusMonths(3);
                    case YEARLY -> next = next.plusYears(1);
                }
                obligation.setNextPaymentDate(next);
            }
        }
        obligation = obligationRepository.save(obligation);

        return new PaymentResultResponse(obligationMapper.toResponse(obligation), paymentMapper.toResponse(payment));
    }

    @Transactional
    public void cancel(UUID id) {
        Obligation obligation = obligationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Обязательство не найдено"));

        checkAndApplyExpiry(obligation);

        if (obligation.getStatus() != Status.ACTIVE) {
            throw new BusinessException("Отменить можно только обязательство со статусом active. Текущий статус: "
                    + obligation.getStatus().name().toLowerCase());
        }

        obligation.setStatus(Status.CANCELLED);
        obligationRepository.save(obligation);
    }

    @Transactional
    public void delete(UUID id) {
        obligationRepository.deleteById(id);
        sseService.broadcast(Map.of("type", "obligation_deleted", "id", id.toString()));
    }

    private void checkAndApplyExpiry(Obligation obligation) {
        if (obligation.getStatus() == Status.ACTIVE &&
                obligation.getRecurrence() == null &&
                obligation.getNextPaymentDate() != null &&
                obligation.getNextPaymentDate().isBefore(LocalDate.now())) {

            obligation.setStatus(Status.EXPIRED);
            obligationRepository.save(obligation);
        }
    }
}
