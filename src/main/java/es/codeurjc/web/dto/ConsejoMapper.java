package es.codeurjc.web.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import es.codeurjc.web.model.Consejo;

@Mapper(componentModel = "spring")
public interface ConsejoMapper {

    @Mapping(source = "seller.id", target = "sellerId")
    @Mapping(source = "seller.nombre", target = "sellerName")
    ConsejoDTO toDTO(Consejo consejo);

    // FIX: Añadimos attachmentName a la lista de ignorados en la conversión inversa
    @Mapping(target = "seller", ignore = true)
    @Mapping(target = "imageBytes", ignore = true)
    @Mapping(target = "attachmentName", ignore = true)
    @Mapping(target = "attachmentPath", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    Consejo toDomain(ConsejoDTO consejoDTO);
}