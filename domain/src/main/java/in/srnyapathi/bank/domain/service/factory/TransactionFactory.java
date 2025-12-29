package in.srnyapathi.bank.domain.service.factory;

import in.srnyapathi.bank.domain.exception.InvalidOperationTypeException;
import in.srnyapathi.bank.domain.model.SupportedTransactionEnum;
import in.srnyapathi.bank.domain.service.impl.TransactionHandler;
import in.srnyapathi.bank.domain.service.handlers.CashPurchaseTransactionHandler;
import in.srnyapathi.bank.domain.service.handlers.InstallmentPurchaseTransactionHandler;
import in.srnyapathi.bank.domain.service.handlers.PaymentTransactionHandler;
import in.srnyapathi.bank.domain.service.handlers.WithdrawalTransactionHandler;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


/**
 * Factory for creating and resolving transaction handlers based on operation type.
 * <p>
 * This factory uses Spring dependency injection to automatically wire all
 * {@link TransactionHandler} implementations and employs pattern matching
 * to efficiently resolve the appropriate handler for each operation type.
 * It serves as the central point for transaction handler lookup and validation.
 * </p>
 * <p>
 * The factory supports reactive programming patterns by returning handlers
 * wrapped in {@link Mono} for seamless integration with reactive streams
 * and proper error propagation in reactive pipelines.
 * </p>
 * <p>
 * Supported transaction types include:
 * <ul>
 *     <li>{@link SupportedTransactionEnum#CASH_PURCHASE_TRANSACTION}</li>
 *     <li>{@link SupportedTransactionEnum#INSTALLMENT_PURCHASE}</li>
 *     <li>{@link SupportedTransactionEnum#WITHDRAWAL}</li>
 *     <li>{@link SupportedTransactionEnum#PAYMENT}</li>
 * </ul>
 * </p>
 *
 * @author srnyapathi
 * @version 3.0.0
 * @since 1.0.0
 * @see TransactionHandler
 * @see SupportedTransactionEnum
 * @see InvalidOperationTypeException
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionFactory {

    /**
     * Handler for cash purchase transactions that directly debit the account.
     */
    private final CashPurchaseTransactionHandler cashPurchaseTransactionHandler;

    /**
     * Handler for installment purchase transactions that are paid over time.
     */
    private final InstallmentPurchaseTransactionHandler installmentPurchaseTransactionHandler;

    /**
     * Handler for payment transactions that credit the account.
     */
    private final PaymentTransactionHandler paymentTransactionHandler;

    /**
     * Handler for withdrawal transactions that debit the account.
     */
    private final WithdrawalTransactionHandler withdrawalTransactionHandler;




    /**
     * Retrieves the appropriate transaction handler for the specified operation type (reactive).
     * <p>
     * This method returns a {@link Mono} for seamless integration with reactive streams
     * and proper error propagation in reactive pipelines. The handler name is normalized
     * (trimmed and converted to uppercase) before lookup to ensure case-insensitive matching.
     * </p>
     * <p>
     * The method uses {@code Mono.defer()} to ensure lazy evaluation and proper error
     * handling within the reactive context.
     * </p>
     * <p>
     * Example usage:
     * <pre>
     * factory.getHandlerReactive("CASH_PURCHASE_TRANSACTION")
     *     .flatMap(handler -> handler.execute(transaction))
     *     .subscribe();
     * </pre>
     * </p>
     *
     * @param transactionHandler the operation type/handler name (case-insensitive),
     *                          must match one of {@link SupportedTransactionEnum} values
     *                          (e.g., "CASH_PURCHASE_TRANSACTION", "WITHDRAWAL", "PAYMENT")
     * @return a {@link Mono} emitting the resolved {@link TransactionHandler},
     *         or an error signal with {@link InvalidOperationTypeException} if the handler
     *         name is blank or not supported
     * @see SupportedTransactionEnum
     * @see TransactionHandler
     */
    public Mono<TransactionHandler> getHandlerReactive(String transactionHandler) {
        return Mono.defer(() -> {
            if (StringUtils.isBlank(transactionHandler)) {
                log.warn("Attempted to get handler with blank transaction handler");
                return Mono.error(new InvalidOperationTypeException("transactionHandler cannot be blank"));
            }

            String normalizedHandler = transactionHandler.trim().toUpperCase();
            log.debug("Resolving transaction handler for: {}", normalizedHandler);

            try {
                TransactionHandler handler = resolveHandler(normalizedHandler);
                log.debug("Successfully resolved handler: {} for type: {}",
                        handler.getClass().getSimpleName(), normalizedHandler);
                return Mono.just(handler);
            } catch (InvalidOperationTypeException e) {
                log.error("Failed to resolve handler for type: {}", normalizedHandler, e);
                return Mono.error(e);
            }
        });
    }

    /**
     * Resolves the appropriate transaction handler based on the normalized transaction type.
     * <p>
     * This internal method uses pattern matching via a switch expression for efficient
     * and type-safe handler resolution. It attempts to match the normalized handler name
     * against the {@link SupportedTransactionEnum} values and returns the corresponding
     * handler implementation.
     * </p>
     * <p>
     * If the handler name does not match any supported transaction type, a detailed
     * error message is generated listing all supported types to aid in debugging.
     * </p>
     *
     * @param normalizedHandler the normalized (trimmed and uppercase) handler name,
     *                         must match a {@link SupportedTransactionEnum} constant name
     * @return the {@link TransactionHandler} implementation for the specified operation type
     * @throws InvalidOperationTypeException if the normalized handler name does not correspond
     *                                      to any value in {@link SupportedTransactionEnum},
     *                                      wrapping the underlying {@link IllegalArgumentException}
     *                                      with a descriptive message
     */
    private TransactionHandler resolveHandler(String normalizedHandler) throws InvalidOperationTypeException {
        try {
            return switch (SupportedTransactionEnum.valueOf(normalizedHandler)) {
                case CASH_PURCHASE_TRANSACTION -> cashPurchaseTransactionHandler;
                case INSTALLMENT_PURCHASE -> installmentPurchaseTransactionHandler;
                case WITHDRAWAL -> withdrawalTransactionHandler;
                case PAYMENT -> paymentTransactionHandler;
            };
        } catch (IllegalArgumentException e) {
            log.error("Invalid transaction handler type: {}", normalizedHandler);
            String supportedTypes = String.join(", ", getSupportedHandlerNames());
            throw new InvalidOperationTypeException(
                    "Invalid transaction handler: " + normalizedHandler +
                    ". Supported types: " + supportedTypes, e);
        }
    }

    /**
     * Retrieves the names of all supported transaction handler types.
     * <p>
     * This utility method extracts the enum constant names from {@link SupportedTransactionEnum}
     * and returns them as a string array. It is primarily used for generating informative
     * error messages when an invalid handler type is requested, helping developers and users
     * understand which transaction types are supported by the system.
     * </p>
     *
     * @return a string array containing the names of all supported transaction handler types
     *         (e.g., ["CASH_PURCHASE_TRANSACTION", "INSTALLMENT_PURCHASE", "WITHDRAWAL", "PAYMENT"])
     */
    private String[] getSupportedHandlerNames() {
        return java.util.Arrays.stream(SupportedTransactionEnum.values())
                .map(Enum::name)
                .toArray(String[]::new);
    }

}

