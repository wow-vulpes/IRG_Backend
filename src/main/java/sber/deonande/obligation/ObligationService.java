package sber.deonande.obligation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sber.deonande.common.BusinessException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ObligationService {

    private final ObligationRepository repository;
    private final ObligationMapper mapper;

    @Transactional
    public ObligationCreateResponse create(ObligationCreateRequest request) {
        Obligation entity = mapper.toEntity(request);

        if (request.getNextPaymentDate() != null && request.getNextPaymentDate().isBefore(LocalDate.now())) {
            entity.setStatus(Status.EXPIRED);
        } else {
            entity.setStatus(Status.ACTIVE);
        }

        String warning = null;
        Optional<Obligation> existingActive = repository.findByTitleIgnoreCaseAndStatus(request.getTitle(), Status.ACTIVE);
        if (existingActive.isPresent()) {
            warning = "Активное обязательство с таким названием уже существует";
        }

        Obligation saved = repository.save(entity);
        return new ObligationCreateResponse(mapper.toResponse(saved), warning);
    }

    @Transactional(readOnly = true)
    public List<ObligationResponse> getAll() {
        return mapper.toResponseList(repository.findAll());
    }

    @Transactional
    public void cancel(UUID id) {
        Obligation obligation = repository.findById(id)
                .orElseThrow(() -> new BusinessException("Обязательство не найдено"));

        if (obligation.getStatus() != Status.ACTIVE) {
            throw new BusinessException("Отменить можно только обязательство со статусом active");
        }

        obligation.setStatus(Status.CANCELLED);
        repository.save(obligation);
    }

    @Transactional
    public void delete(UUID id) {
        repository.deleteById(id);
    }
}
