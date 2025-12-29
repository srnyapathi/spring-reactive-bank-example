package in.srnyapathi.bank.exception;

import in.srnyapathi.bank.domain.exception.AccountDoesNotExist;
import in.srnyapathi.bank.domain.exception.DuplicateAccountException;
import in.srnyapathi.bank.domain.exception.InvalidAccountException;
import in.srnyapathi.bank.domain.exception.InvalidAmountException;
import in.srnyapathi.bank.domain.exception.InvalidOperationTypeException;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.webflux.autoconfigure.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.webflux.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@Order(-2)
public class ReactiveGlobalExceptionHandler extends AbstractErrorWebExceptionHandler {

    public static final String STATUS = "status";
    public static final String ERROR = "error";
    public static final String MESSAGE = "message";

    public ReactiveGlobalExceptionHandler(ErrorAttributes errorAttributes,
                                          WebProperties webProperties,
                                          ApplicationContext applicationContext,
                                          ServerCodecConfigurer codecConfigurer) {
        super(errorAttributes, webProperties.getResources(), applicationContext);
        this.setMessageWriters(codecConfigurer.getWriters());
    }


    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Throwable error = getError(request);

        if (error instanceof DuplicateAccountException) {
            return ServerResponse.status(HttpStatus.CONFLICT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(Map.of(
                            STATUS, HttpStatus.CONFLICT.value(),
                            ERROR, "Conflict",
                            MESSAGE, error.getMessage()
                    )));
        }

        if (error instanceof AccountDoesNotExist) {
            return ServerResponse.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(Map.of(
                            STATUS, HttpStatus.NOT_FOUND.value(),
                            ERROR, "Not Found",
                            MESSAGE, error.getMessage()
                    )));
        }

        if (error instanceof InvalidAccountException || error instanceof InvalidAmountException) {
            return ServerResponse.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(Map.of(
                            STATUS, HttpStatus.BAD_REQUEST.value(),
                            ERROR, "Bad Request",
                            MESSAGE, error.getMessage()
                    )));
        }

        if (error instanceof ServerWebInputException) {
            return ServerResponse.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(Map.of(
                            STATUS, HttpStatus.BAD_REQUEST.value(),
                            ERROR, "Bad Request",
                            MESSAGE, "Invalid request payload"
                    )));
        }

        if (error instanceof InvalidOperationTypeException) {
            return ServerResponse.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(Map.of(
                            STATUS, HttpStatus.BAD_REQUEST.value(),
                            ERROR, "Bad Request",
                            MESSAGE, error.getMessage()
                    )));
        }

        if (error instanceof InvalidRequestException) {
            return ServerResponse.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(Map.of(
                            STATUS, HttpStatus.BAD_REQUEST.value(),
                            ERROR, "Bad Request",
                            MESSAGE, error.getMessage()
                    )));
        }

        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(Map.of(
                        STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        ERROR, "Internal Server Error",
                        MESSAGE, "Something went wrong,please try after sometime"
                )));
    }
}
