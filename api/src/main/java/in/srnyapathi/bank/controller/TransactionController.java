package in.srnyapathi.bank.controller;

import in.srnyapathi.bank.domain.service.TransactionService;
import in.srnyapathi.bank.mapper.TransactionRequestResponseMapper;
import in.srnyapathi.bank.model.TransactionCreationRequest;
import in.srnyapathi.bank.model.TransactionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/transactions")
@Slf4j
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;
    private final TransactionRequestResponseMapper mapper;

    @PostMapping
    public Mono<ResponseEntity<TransactionResponse>> createTransaction(@Validated @RequestBody TransactionCreationRequest transaction) {
        log.info("Creating transaction : {}", transaction);
        return transactionService.performTransaction(transaction.getAccountId(),
                        transaction.getOperationTypeId(),
                        transaction.getAmount())
                .map(mapper::transactionToResponse)
                .map(response ->
                        ResponseEntity.status(HttpStatus.CREATED)
                                .body(response));
    }

}


