package es.codeurjc.web.dto;

import org.mapstruct.Mapper;
import es.codeurjc.web.model.Usuario;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {
    UsuarioDTO toDTO(Usuario usuario);
}