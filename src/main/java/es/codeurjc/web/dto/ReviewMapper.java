package es.codeurjc.web.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import es.codeurjc.web.model.Review;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(source = "author.id", target = "authorId")
    @Mapping(source = "author.name", target = "authorName")
    @Mapping(source = "advice.id", target = "adviceId")
    @Mapping(source = "advice.title", target = "adviceTitle")
    ReviewDTO toDTO(Review review);
}