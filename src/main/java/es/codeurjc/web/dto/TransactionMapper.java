package es.codeurjc.web.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import es.codeurjc.web.model.Transaction;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(source = "buyer.id", target = "buyerId")
    @Mapping(source = "buyer.name", target = "buyerName")
    @Mapping(source = "advice.id", target = "adviceId")
    @Mapping(source = "advice.title", target = "adviceTitle")
    TransactionDTO toDTO(Transaction transaction);
}