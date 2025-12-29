package in.srnyapathi.bank.mapper;

import in.srnyapathi.bank.domain.model.Account;
import in.srnyapathi.bank.model.AccountCreationRequest;
import in.srnyapathi.bank.model.AccountResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountRequestResponseMapper {


    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "accountNumber", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Account requestToAccount(AccountCreationRequest request);

    @Mapping(target = "accountId", source = "account.accountNumber")
    AccountResponse accountToResponse(Account account);
}
