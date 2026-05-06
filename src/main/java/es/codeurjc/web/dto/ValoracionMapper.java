package es.codeurjc.web.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import es.codeurjc.web.model.Valoracion;

@Mapper(componentModel = "spring")
public interface ValoracionMapper {

    @Mapping(source = "author.id", target = "authorId")
    @Mapping(source = "author.nombre", target = "authorName")
    @Mapping(source = "consejo.id", target = "consejoId")
    @Mapping(source = "consejo.title", target = "consejoTitle")
    ValoracionDTO toDTO(Valoracion valoracion);
}