package sber.deonande.obligation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sber.deonande.common.BusinessException;
import sber.deonande.payment.PaymentMapper;
import sber.deonande.payment.PaymentRepository;
import sber.deonande.sse.SseService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ObligationServiceTest {

    @Mock
    private ObligationRepository obligationRepository;
    @Mock
    private ObligationMapper obligationMapper;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private SseService sseService;

    @InjectMocks
    private ObligationService obligationService;

    // Lazy expiry
    @Test
    void testLazyExpiryRecurrenceNull() {
        UUID id = UUID.randomUUID();
        Obligation obligation = new Obligation();
        obligation.setId(id);
        obligation.setStatus(Status.ACTIVE);
        obligation.setRecurrence(null);
        obligation.setNextPaymentDate(LocalDate.now().minusDays(1));

        when(obligationRepository.findById(id)).thenReturn(Optional.of(obligation));

        BusinessException exception = assertThrows(BusinessException.class, () -> obligationService.pay(id));
        assertTrue(exception.getMessage().contains("expired"));
        assertEquals(Status.EXPIRED, obligation.getStatus());
        verify(obligationRepository).save(obligation);
    }

    // Lazy expiry с учётом исключения для рекуррентных обязательств
    @Test
    void testLazyExpiryRecurrenceNotNull() {
        UUID id = UUID.randomUUID();
        Obligation obligation = new Obligation();
        obligation.setId(id);
        obligation.setStatus(Status.ACTIVE);
        obligation.setRecurrence(Recurrence.MONTHLY);
        obligation.setNextPaymentDate(LocalDate.now().minusDays(1));

        when(obligationRepository.findById(id)).thenReturn(Optional.of(obligation));
        when(obligationRepository.save(any())).thenReturn(obligation);

        obligationService.pay(id);

        assertEquals(Status.ACTIVE, obligation.getStatus());
        assertEquals(LocalDate.now().minusDays(1).plusMonths(1), obligation.getNextPaymentDate());
    }

    // /pay для каждого значения recurrence (включая null)
    @Test
    void testPayRecurrenceNull() {
        UUID id = UUID.randomUUID();
        Obligation obligation = new Obligation();
        obligation.setId(id);
        obligation.setStatus(Status.ACTIVE);
        obligation.setRecurrence(null);

        when(obligationRepository.findById(id)).thenReturn(Optional.of(obligation));
        when(obligationRepository.save(any())).thenReturn(obligation);

        obligationService.pay(id);

        assertEquals(Status.CANCELLED, obligation.getStatus());
    }

    @Test
    void testPayMonthly() {
        UUID id = UUID.randomUUID();
        Obligation obligation = new Obligation();
        obligation.setId(id);
        obligation.setStatus(Status.ACTIVE);
        obligation.setRecurrence(Recurrence.MONTHLY);
        obligation.setNextPaymentDate(LocalDate.of(2026, 1, 31));

        when(obligationRepository.findById(id)).thenReturn(Optional.of(obligation));
        when(obligationRepository.save(any())).thenReturn(obligation);

        obligationService.pay(id);

        assertEquals(LocalDate.of(2026, 2, 28), obligation.getNextPaymentDate());
    }

    @Test
    void testPayRecurrenceQuarterly() {
        UUID idQ = UUID.randomUUID();
        Obligation obQ = new Obligation();
        obQ.setId(idQ);
        obQ.setStatus(Status.ACTIVE);
        obQ.setRecurrence(Recurrence.QUARTERLY);
        obQ.setNextPaymentDate(LocalDate.of(2024, 1, 15));

        when(obligationRepository.findById(idQ)).thenReturn(Optional.of(obQ));
        when(obligationRepository.save(any())).thenReturn(obQ);

        obligationService.pay(idQ);
        assertEquals(LocalDate.of(2024, 4, 15), obQ.getNextPaymentDate());
    }

    @Test
    void testPayRecurrenceYearly() {
        UUID idY = UUID.randomUUID();
        Obligation obY = new Obligation();
        obY.setId(idY);
        obY.setStatus(Status.ACTIVE);
        obY.setRecurrence(Recurrence.YEARLY);
        obY.setNextPaymentDate(LocalDate.of(2024, 1, 15));

        when(obligationRepository.findById(idY)).thenReturn(Optional.of(obY));
        when(obligationRepository.save(any())).thenReturn(obY);

        obligationService.pay(idY);
        assertEquals(LocalDate.of(2025, 1, 15), obY.getNextPaymentDate());
    }

    // Граничный случай: оплата 31-го числа с recurrence = monthly
    @Test
    void testPayBoundaryCaseWithDates() {
        UUID id = UUID.randomUUID();
        Obligation obligation = new Obligation();
        obligation.setId(id);
        obligation.setStatus(Status.ACTIVE);
        obligation.setRecurrence(Recurrence.MONTHLY);
        obligation.setNextPaymentDate(LocalDate.of(2024, 1, 31));

        when(obligationRepository.findById(id)).thenReturn(Optional.of(obligation));
        when(obligationRepository.save(any())).thenReturn(obligation);

        obligationService.pay(id);

        // високосный год, 29 дней
        assertEquals(LocalDate.of(2024, 2, 29), obligation.getNextPaymentDate());
    }

    // Попытка оплатить или отменить не-active обязательство
    @Test
    void testPayNonActive() {
        UUID id = UUID.randomUUID();
        Obligation obligation = new Obligation();
        obligation.setId(id);
        obligation.setStatus(Status.CANCELLED);

        when(obligationRepository.findById(id)).thenReturn(Optional.of(obligation));

        BusinessException exception = assertThrows(BusinessException.class, () -> obligationService.pay(id));
        assertTrue(exception.getMessage().contains("active"));
    }

    @Test
    void testCancelNonActive() {
        UUID id = UUID.randomUUID();
        Obligation obligation = new Obligation();
        obligation.setId(id);
        obligation.setStatus(Status.CANCELLED);

        when(obligationRepository.findById(id)).thenReturn(Optional.of(obligation));

        BusinessException exception = assertThrows(BusinessException.class, () -> obligationService.cancel(id));
        assertTrue(exception.getMessage().contains("active"));
    }

    // Создание дубля и наличие warning в ответе
    @Test
    void testCreateDuplicateWarning() {
        ObligationCreateRequest request = new ObligationCreateRequest();
        request.setTitle("Netflix");

        Obligation entity = new Obligation();
        entity.setStatus(Status.ACTIVE);
        when(obligationMapper.toEntity(request)).thenReturn(entity);

        Obligation existing = new Obligation();
        existing.setStatus(Status.ACTIVE);
        when(obligationRepository.findByTitleIgnoreCaseAndStatus("Netflix", Status.ACTIVE))
                .thenReturn(Optional.of(existing));

        when(obligationRepository.save(any())).thenReturn(entity);
        when(obligationMapper.toResponse(any())).thenReturn(new ObligationResponse());

        ObligationCreateResponse response = obligationService.create(request);

        assertNotNull(response.getWarning());
        assertEquals("Активное обязательство с таким названием уже существует", response.getWarning());
    }
}
