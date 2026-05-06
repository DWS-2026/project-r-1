package es.codeurjc.web.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import es.codeurjc.web.model.Consejo;

@Mapper(componentModel = "spring")
public interface ConsejoMapper {

    @Mapping(source = "seller.id", target = "sellerId")
    @Mapping(source = "seller.nombre", target = "sellerName")
    ConsejoDTO toDTO(Consejo consejo);

    // Mapeo inverso necesario para recibir DTOs de entrada en el POST/PUT
    @Mapping(target = "seller", ignore = true)
    @Mapping(target = "imageBytes", ignore = true)
    @Mapping(target = "attachmentPath", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    Consejo toDomain(ConsejoDTO consejoDTO);
}