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

    @Test
    void testPayShiftLeapYear() {
        UUID id = UUID.randomUUID();
        Obligation obligation = new Obligation();
        obligation.setId(id);
        obligation.setStatus(Status.ACTIVE);
        obligation.setRecurrence(Recurrence.MONTHLY);
        obligation.setNextPaymentDate(LocalDate.of(2024, 1, 31));

        when(obligationRepository.findById(id)).thenReturn(Optional.of(obligation));
        when(obligationRepository.save(any())).thenReturn(obligation);

        obligationService.pay(id);

        assertEquals(LocalDate.of(2024, 2, 29), obligation.getNextPaymentDate());
    }

    @Test
    void testPayNonActiveThrowsException() {
        UUID id = UUID.randomUUID();
        Obligation obligation = new Obligation();
        obligation.setId(id);
        obligation.setStatus(Status.CANCELLED);

        when(obligationRepository.findById(id)).thenReturn(Optional.of(obligation));

        BusinessException exception = assertThrows(BusinessException.class, () -> obligationService.pay(id));
        assertTrue(exception.getMessage().contains("active"));
    }
}
