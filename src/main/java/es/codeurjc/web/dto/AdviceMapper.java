package es.codeurjc.web.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import es.codeurjc.web.model.Advice;

@Mapper(componentModel = "spring")
public interface AdviceMapper {

    @Mapping(source = "seller.id", target = "sellerId")
    @Mapping(source = "seller.name", target = "sellerName")
    AdviceDTO toDTO(Advice advice);

    // FIX: Added attachmentName to the ignored list in reverse conversion
    @Mapping(target = "seller", ignore = true)
    @Mapping(target = "imageBytes", ignore = true)
    @Mapping(target = "attachmentName", ignore = true)
    @Mapping(target = "attachmentPath", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    Advice toDomain(AdviceDTO adviceDTO);
}