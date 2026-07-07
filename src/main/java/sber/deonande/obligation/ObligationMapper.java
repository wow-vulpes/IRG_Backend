package sber.deonande.obligation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ObligationMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Obligation toEntity(ObligationCreateRequest request);

    ObligationResponse toResponse(Obligation obligation);

    List<ObligationResponse> toResponseList(List<Obligation> obligations);
}
