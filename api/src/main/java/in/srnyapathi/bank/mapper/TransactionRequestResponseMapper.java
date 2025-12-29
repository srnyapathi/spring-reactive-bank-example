package in.srnyapathi.bank.mapper;


import in.srnyapathi.bank.domain.model.Transaction;
import in.srnyapathi.bank.model.TransactionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface TransactionRequestResponseMapper {

    @Mapping(target = "operationTypeId", source = "operationType.operationTypeId")
    @Mapping(target = "accountId", source = "account.accountNumber")
    @Mapping(target = "eventDate", source = "eventDate")
    TransactionResponse transactionToResponse(Transaction transaction);

}
