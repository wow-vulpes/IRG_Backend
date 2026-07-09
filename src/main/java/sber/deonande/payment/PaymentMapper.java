package sber.deonande.payment;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentMapper {
    @Mapping(target = "obligationId", source = "obligation.id")
    PaymentResponse toResponse(Payment payment);
}
