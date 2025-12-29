package in.srnyapathi.bank.controller;

import in.srnyapathi.bank.domain.service.AccountService;
import in.srnyapathi.bank.exception.InvalidRequestException;
import in.srnyapathi.bank.mapper.AccountRequestResponseMapper;
import in.srnyapathi.bank.model.AccountResponse;
import in.srnyapathi.bank.model.AccountCreationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/accounts")
@Slf4j
@RequiredArgsConstructor

public class AccountController {

    private final AccountService accountService;
    private final AccountRequestResponseMapper mapper;

    @PostMapping
    public Mono<ResponseEntity<AccountResponse>> createAccount(@Valid @RequestBody AccountCreationRequest accountRequest) {
        log.info("Creating account for document number: {}", accountRequest.getDocumentNumber());
        return accountService.createAccount(mapper.requestToAccount(accountRequest))
                .map(mapper::accountToResponse)
                .map(response ->
                        ResponseEntity.status(HttpStatus.CREATED)
                                .body(response)
                );
    }

        @GetMapping("/{id}")
        public Mono<ResponseEntity<AccountResponse>> getAccount (@PathVariable("id") Long accountId){
            log.info("Fetching account with id: {}", accountId);
            return accountService.getAccount(Mono.just(accountId))
                    .map(mapper::accountToResponse)
                    .map(response ->
                            ResponseEntity.status(HttpStatus.OK)
                                    .body(response)
                    );
        }

    }
