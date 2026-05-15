package es.codeurjc.web.dto;

import org.mapstruct.Mapper;
import es.codeurjc.web.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDTO(User user);
}