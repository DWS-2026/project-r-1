package es.codeurjc.web.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import es.codeurjc.web.model.Transaccion;

@Mapper(componentModel = "spring")
public interface TransaccionMapper {

    @Mapping(source = "buyer.id", target = "buyerId")
    @Mapping(source = "buyer.nombre", target = "buyerName")
    @Mapping(source = "consejo.id", target = "consejoId")
    @Mapping(source = "consejo.title", target = "consejoTitle")
    TransaccionDTO toDTO(Transaccion transaccion);
}