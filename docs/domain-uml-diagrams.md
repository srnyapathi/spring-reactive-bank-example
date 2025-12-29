# Domain Module UML Diagrams

This document contains comprehensive UML diagrams for the Domain module of the Spring Reactive Bank Example application.

## Table of Contents
1. [Domain Model Class Diagram](#1-domain-model-class-diagram)
2. [Service Layer Class Diagram](#2-service-layer-class-diagram)
3. [Adapter Layer Class Diagram](#3-adapter-layer-class-diagram)
4. [Exception Hierarchy Diagram](#4-exception-hierarchy-diagram)
5. [Transaction Handler Hierarchy Diagram](#5-transaction-handler-hierarchy-diagram)
6. [Complete Domain Module Package Diagram](#6-complete-domain-module-package-diagram)
7. [Sequence Diagrams](#7-sequence-diagrams)

---

## 1. Domain Model Class Diagram

This diagram shows all the domain model classes and their relationships.

```plantuml
@startuml Domain Model Classes

skinparam classAttributeIconSize 0
skinparam class {
    BackgroundColor<<enum>> LightYellow
    BackgroundColor<<valueObject>> LightBlue
}

package "in.srnyapathi.bank.domain.model" {
    
    class Account {
        - String documentNumber
        - Long accountNumber
        - LocalDateTime updatedAt
        - LocalDateTime createdAt
        - boolean active
        + getters/setters()
    }
    
    class Transaction {
        - Long transactionId
        - AccountNumber account
        - OperationType operationType
        - BigDecimal amount
        - LocalDateTime eventDate
        - LocalDateTime createdAt
        - LocalDateTime updatedAt
        - boolean active
        + getters/setters()
    }
    
    class AccountNumber <<valueObject>> {
        - Long id
        + getAccountNumber(): Long
    }
    
    class OperationType {
        - Long operationTypeId
        - String description
        - String handler
        - TransactionType transactionType
        + getters/setters()
    }
    
    enum TransactionType <<enum>> {
        CREDIT("CR")
        DEBIT("DR")
        --
        - String name
        + getName(): String
    }
    
    enum SupportedTransactionEnum <<enum>> {
        CASH_PURCHASE_TRANSACTION
        INSTALLMENT_PURCHASE
        WITHDRAWAL
        PAYMENT
    }
}

' Relationships
Transaction "1" --> "1" AccountNumber : has
Transaction "1" --> "1" OperationType : uses
OperationType "1" --> "1" TransactionType : has
Account "1" -- "1" AccountNumber : identified by

note right of TransactionType
    Determines if transaction
    increases (CREDIT) or
    decreases (DEBIT) balance
end note

note right of SupportedTransactionEnum
    Defines all transaction
    types supported by
    the banking system
end note

@enduml
```

---

## 2. Service Layer Class Diagram

This diagram shows the service interfaces and their implementations.

```plantuml
@startuml Service Layer

skinparam classAttributeIconSize 0
skinparam interface {
    BackgroundColor LightGreen
}

package "in.srnyapathi.bank.domain.service" {
    
    interface AccountService <<interface>> {
        + createAccount(Account): Mono<Account>
        + getAccount(Mono<Long>): Mono<Account>
    }
    
    interface TransactionService <<interface>> {
        + performTransaction(Long, Long, BigDecimal): Mono<Transaction>
    }
    
    interface OperationTypeService <<interface>> {
        + getOperationType(): Mono<Map<String, OperationType>>
    }
}

package "in.srnyapathi.bank.domain.service.impl" {
    
    class AccountServiceImpl {
        - AccountDatabaseAdapter accountDatabaseAdapter
        + createAccount(Account): Mono<Account>
        + getAccount(Mono<Long>): Mono<Account>
    }
    
    class TransactionServiceImpl {
        - TransactionFactory transactionFactory
        - OperationTypeDatabaseAdapter operationTypeDatabaseAdapter
        + performTransaction(Long, Long, BigDecimal): Mono<Transaction>
        - getOperationTypesMap(Map): Map<Long, OperationType>
    }
    
    class OperationTypeServiceImpl {
        - OperationTypeDatabaseAdapter operationTypeDatabase
        + getOperationType(): Mono<Map<String, OperationType>>
    }
    
    abstract class TransactionHandler {
        # OperationTypeService operationTypeService
        # isNegativeAmount(BigDecimal): boolean
        # getAmount(BigDecimal, TransactionType): BigDecimal
        # getOperationType(): Mono<OperationType>
        # validate(Long, BigDecimal): Mono<Void>
        + performTransaction(Long, BigDecimal): Mono<Transaction>
        {abstract} # getHandler(): String
        {abstract} + execute(Long, BigDecimal): Mono<Transaction>
    }
}

package "in.srnyapathi.bank.domain.service.factory" {
    
    class TransactionFactory {
        - CashPurchaseTransactionHandler cashPurchaseTransactionHandler
        - InstallmentPurchaseTransactionHandler installmentPurchaseTransactionHandler
        - PaymentTransactionHandler paymentTransactionHandler
        - WithdrawalTransactionHandler withdrawalTransactionHandler
        + getHandlerReactive(String): Mono<TransactionHandler>
        - resolveHandler(String): TransactionHandler
        - getSupportedHandlerNames(): String[]
    }
}

' Relationships
AccountService <|.. AccountServiceImpl : implements
TransactionService <|.. TransactionServiceImpl : implements
OperationTypeService <|.. OperationTypeServiceImpl : implements

TransactionServiceImpl --> TransactionFactory : uses
TransactionFactory --> TransactionHandler : creates

note right of TransactionFactory
    Factory pattern to create
    appropriate transaction handlers
    based on operation type
end note

note right of TransactionHandler
    Template Method pattern
    for transaction processing
end note

@enduml
```

---

## 3. Adapter Layer Class Diagram

This diagram shows the adapter interfaces that define ports for external dependencies.

```plantuml
@startuml Adapter Layer (Hexagonal Architecture Ports)

skinparam interface {
    BackgroundColor LightCyan
}

package "in.srnyapathi.bank.domain.adapters" {
    
    interface AccountDatabaseAdapter <<port>> {
        + createAccount(Account): Mono<Account>
        + getAccountByAccountNumber(Long): Mono<Account>
    }
    
    interface TransactionDatabaseAdapter <<port>> {
        + saveTransaction(Transaction): Mono<Transaction>
        + getTransactionById(Long): Mono<Transaction>
    }
    
    interface OperationTypeDatabaseAdapter <<port>> {
        + getOperationTypes(): Mono<Map<String, OperationType>>
        + updateOperationTypes(OperationType): Mono<Map<String, OperationType>>
        + addOperationType(OperationType): Mono<Map<String, OperationType>>
        + deleteOperationType(Long): Mono<Map<String, OperationType>>
    }
}

package "in.srnyapathi.bank.domain.model" {
    class Account
    class Transaction
    class OperationType
}

' Relationships
AccountDatabaseAdapter ..> Account : uses
TransactionDatabaseAdapter ..> Transaction : uses
OperationTypeDatabaseAdapter ..> OperationType : uses

note top of AccountDatabaseAdapter
    Hexagonal Architecture Port
    
    These interfaces define the
    contracts for external adapters
    (database implementations)
    
    Implementations are in the
    persistence module
end note

note as N1
    **Hexagonal Architecture Pattern**
    
    Domain module defines ports (interfaces)
    Persistence module provides adapters (implementations)
    
    This ensures domain logic is independent
    of infrastructure concerns
end note

@enduml
```

---

## 4. Exception Hierarchy Diagram

This diagram shows all custom exceptions in the domain module.

```plantuml
@startuml Exception Hierarchy

skinparam classAttributeIconSize 0

package "java.lang" {
    class Exception {
        + Exception(String)
        + Exception(String, Throwable)
    }
}

package "in.srnyapathi.bank.domain.exception" {
    
    class InvalidTransactionObjectException {
        + InvalidTransactionObjectException(String)
    }
    
    class InvalidOperationTypeException {
        + InvalidOperationTypeException(String)
        + InvalidOperationTypeException(String, Throwable)
    }
    
    class InvalidAmountException {
        + InvalidAmountException()
        + InvalidAmountException(String)
    }
    
    class InvalidAccountException {
        + InvalidAccountException()
        + InvalidAccountException(String)
    }
    
    class DuplicateAccountException {
        + DuplicateAccountException(String)
    }
    
    class AccountDoesNotExist {
        + AccountDoesNotExist(String)
    }
}

' Inheritance
Exception <|-- InvalidTransactionObjectException
Exception <|-- InvalidOperationTypeException
Exception <|-- InvalidAmountException
Exception <|-- InvalidAccountException
Exception <|-- DuplicateAccountException
Exception <|-- AccountDoesNotExist

note right of DuplicateAccountException
    @ResponseStatus(code = HttpStatus.CONFLICT)
    Annotated for HTTP 409 response
end note

note as ExceptionNote
    **Domain Exception Hierarchy**
    
    All domain exceptions extend Exception
    for checked exception handling in
    reactive streams (Mono.error())
end note

@enduml
```

---

## 5. Transaction Handler Hierarchy Diagram

This diagram shows the transaction handler hierarchy and the Strategy pattern implementation.

```plantuml
@startuml Transaction Handler Hierarchy

skinparam classAttributeIconSize 0

package "in.srnyapathi.bank.domain.service.impl" {
    
    abstract class TransactionHandler {
        # OperationTypeService operationTypeService
        --
        # {abstract} getHandler(): String
        # getOperationType(): Mono<OperationType>
        # isNegativeAmount(BigDecimal): boolean
        # getAmount(BigDecimal, TransactionType): BigDecimal
        # validate(Long, BigDecimal): Mono<Void>
        + performTransaction(Long, BigDecimal): Mono<Transaction>
        + {abstract} execute(Long, BigDecimal): Mono<Transaction>
    }
}

package "in.srnyapathi.bank.domain.service.handlers" {
    
    class CashPurchaseTransactionHandler {
        - TransactionDatabaseAdapter transactionDatabaseAdapter
        # getHandler(): String
        # validate(Long, BigDecimal): Mono<Void>
        + execute(Long, BigDecimal): Mono<Transaction>
    }
    
    class InstallmentPurchaseTransactionHandler {
        - TransactionDatabaseAdapter transactionDatabaseAdapter
        # getHandler(): String
        # validate(Long, BigDecimal): Mono<Void>
        + execute(Long, BigDecimal): Mono<Transaction>
    }
    
    class WithdrawalTransactionHandler {
        - TransactionDatabaseAdapter transactionDatabaseAdapter
        # getHandler(): String
        # validate(Long, BigDecimal): Mono<Void>
        + execute(Long, BigDecimal): Mono<Transaction>
    }
    
    class PaymentTransactionHandler {
        - TransactionDatabaseAdapter transactionDatabaseAdapter
        # getHandler(): String
        # validate(Long, BigDecimal): Mono<Void>
        + execute(Long, BigDecimal): Mono<Transaction>
    }
}

package "in.srnyapathi.bank.domain.service.factory" {
    class TransactionFactory {
        - CashPurchaseTransactionHandler cashPurchaseTransactionHandler
        - InstallmentPurchaseTransactionHandler installmentPurchaseTransactionHandler
        - PaymentTransactionHandler paymentTransactionHandler
        - WithdrawalTransactionHandler withdrawalTransactionHandler
        + getHandlerReactive(String): Mono<TransactionHandler>
        - resolveHandler(String): TransactionHandler
    }
}

' Inheritance
TransactionHandler <|-- CashPurchaseTransactionHandler
TransactionHandler <|-- InstallmentPurchaseTransactionHandler
TransactionHandler <|-- WithdrawalTransactionHandler
TransactionHandler <|-- PaymentTransactionHandler

' Dependencies
TransactionFactory ..> CashPurchaseTransactionHandler : creates
TransactionFactory ..> InstallmentPurchaseTransactionHandler : creates
TransactionFactory ..> WithdrawalTransactionHandler : creates
TransactionFactory ..> PaymentTransactionHandler : creates

note right of TransactionHandler
    **Template Method Pattern**
    
    performTransaction() is template method
    execute() is abstract step
    validate() can be overridden
end note

note bottom of TransactionFactory
    **Factory + Strategy Pattern**
    
    Factory creates appropriate handler
    based on SupportedTransactionEnum
    
    Each handler implements different
    business rules for its transaction type
end note

note left of CashPurchaseTransactionHandler
    Handler: "CASH_PURCHASE_TRANSACTION"
    Type: DEBIT
end note

note left of InstallmentPurchaseTransactionHandler
    Handler: "INSTALLMENT_PURCHASE"
    Type: DEBIT
end note

note left of WithdrawalTransactionHandler
    Handler: "WITHDRAWAL"
    Type: DEBIT
end note

note left of PaymentTransactionHandler
    Handler: "PAYMENT"
    Type: CREDIT
end note

@enduml
```

---

## 6. Complete Domain Module Package Diagram

This diagram shows the overall structure of the domain module with all packages and their relationships.

```plantuml
@startuml Complete Domain Module Structure

skinparam packageStyle rectangle
skinparam package {
    BackgroundColor<<model>> LightYellow
    BackgroundColor<<service>> LightGreen
    BackgroundColor<<adapter>> LightCyan
    BackgroundColor<<exception>> Pink
    BackgroundColor<<factory>> LightBlue
}

package "Domain Module" {
    
    package "model <<model>>" {
        class Account
        class Transaction
        class AccountNumber
        class OperationType
        enum TransactionType
        enum SupportedTransactionEnum
    }
    
    package "service <<service>>" {
        interface AccountService
        interface TransactionService
        interface OperationTypeService
    }
    
    package "service.impl" {
        class AccountServiceImpl
        class TransactionServiceImpl
        class OperationTypeServiceImpl
        abstract class TransactionHandler
    }
    
    package "service.handlers" {
        class CashPurchaseTransactionHandler
        class InstallmentPurchaseTransactionHandler
        class WithdrawalTransactionHandler
        class PaymentTransactionHandler
    }
    
    package "service.factory <<factory>>" {
        class TransactionFactory
    }
    
    package "adapters <<adapter>>" {
        interface AccountDatabaseAdapter
        interface TransactionDatabaseAdapter
        interface OperationTypeDatabaseAdapter
    }
    
    package "exception <<exception>>" {
        class InvalidTransactionObjectException
        class InvalidOperationTypeException
        class InvalidAmountException
        class InvalidAccountException
        class DuplicateAccountException
        class AccountDoesNotExist
    }
    
    package "config" {
        class DomainModuleConfiguration
    }
}

' Package dependencies
service --> model : uses
service.impl --> model : uses
service.impl --> service : implements
service.impl --> adapters : uses
service.impl --> exception : throws
service.handlers --> service.impl : extends
service.handlers --> adapters : uses
service.handlers --> model : uses
service.handlers --> exception : throws
service.factory --> service.handlers : creates
service.factory --> service.impl : returns

note right of model
    **Core Domain Entities**
    - Account
    - Transaction
    - OperationType
    - Enumerations
end note

note right of adapters
    **Hexagonal Architecture Ports**
    
    Interfaces for external dependencies
    Implemented in persistence module
end note

note right of service
    **Domain Services**
    
    Business logic and orchestration
    Reactive operations (Mono/Flux)
end note

note right of exception
    **Domain Exceptions**
    
    Business rule violations
    Validation errors
end note

@enduml
```

---

## 7. Sequence Diagrams

### 7.1 Create Account Sequence Diagram

```plantuml
@startuml Create Account Flow

actor Client
participant "AccountController" as Controller
participant "AccountService" as Service
participant "AccountServiceImpl" as ServiceImpl
participant "AccountDatabaseAdapter" as Adapter
database "Database" as DB

Client -> Controller: POST /accounts
activate Controller

Controller -> Service: createAccount(account)
activate Service

Service -> ServiceImpl: createAccount(account)
activate ServiceImpl

ServiceImpl -> ServiceImpl: Mono.just(account)

ServiceImpl -> Adapter: createAccount(account)
activate Adapter

Adapter -> DB: save(account)
activate DB
DB --> Adapter: savedAccount
deactivate DB

Adapter --> ServiceImpl: Mono<Account>
deactivate Adapter

alt Success
    ServiceImpl -> ServiceImpl: log success
    ServiceImpl --> Service: Mono<Account>
    deactivate ServiceImpl
    Service --> Controller: Mono<Account>
    deactivate Service
    Controller --> Client: 201 Created
else Duplicate Document Number
    ServiceImpl -> ServiceImpl: catch DuplicateKeyException
    ServiceImpl -> ServiceImpl: throw DuplicateAccountException
    ServiceImpl --> Controller: error
    deactivate ServiceImpl
    deactivate Service
    Controller --> Client: 409 Conflict
end

deactivate Controller

@enduml
```

### 7.2 Perform Transaction Sequence Diagram

```plantuml
@startuml Perform Transaction Flow

actor Client
participant "TransactionController" as Controller
participant "TransactionService" as Service
participant "TransactionServiceImpl" as ServiceImpl
participant "OperationTypeDatabaseAdapter" as OpAdapter
participant "TransactionFactory" as Factory
participant "TransactionHandler" as Handler
participant "TransactionDatabaseAdapter" as TxAdapter
database "Database" as DB

Client -> Controller: POST /transactions
activate Controller

Controller -> Service: performTransaction(accountId, operationTypeId, amount)
activate Service

Service -> ServiceImpl: performTransaction(accountId, operationTypeId, amount)
activate ServiceImpl

ServiceImpl -> OpAdapter: getOperationTypes()
activate OpAdapter
OpAdapter -> DB: fetch operation types
activate DB
DB --> OpAdapter: Map<String, OperationType>
deactivate DB
OpAdapter --> ServiceImpl: Mono<Map>
deactivate OpAdapter

ServiceImpl -> ServiceImpl: getOperationTypesMap()
ServiceImpl -> ServiceImpl: find operationType by ID

alt Invalid Operation Type
    ServiceImpl -> ServiceImpl: operationType is empty
    ServiceImpl --> Controller: Mono.error(InvalidOperationTypeException)
    Controller --> Client: 400 Bad Request
else Valid Operation Type
    ServiceImpl -> Factory: getHandlerReactive(handler)
    activate Factory
    
    Factory -> Factory: resolveHandler(normalizedHandler)
    Factory -> Factory: switch on SupportedTransactionEnum
    Factory --> ServiceImpl: Mono<TransactionHandler>
    deactivate Factory
    
    ServiceImpl -> Handler: performTransaction(account, amount)
    activate Handler
    
    Handler -> Handler: validate(account, amount)
    
    alt Validation Fails
        Handler --> Controller: Mono.error(InvalidAmountException/InvalidAccountException)
        Controller --> Client: 400 Bad Request
    else Validation Succeeds
        Handler -> Handler: execute(account, amount)
        
        Handler -> Handler: getOperationType()
        Handler -> Handler: build Transaction object
        
        Handler -> TxAdapter: saveTransaction(transaction)
        activate TxAdapter
        TxAdapter -> DB: save(transaction)
        activate DB
        DB --> TxAdapter: savedTransaction
        deactivate DB
        TxAdapter --> Handler: Mono<Transaction>
        deactivate TxAdapter
        
        Handler --> ServiceImpl: Mono<Transaction>
        deactivate Handler
        
        ServiceImpl -> ServiceImpl: log success
        ServiceImpl --> Service: Mono<Transaction>
        deactivate ServiceImpl
        
        Service --> Controller: Mono<Transaction>
        deactivate Service
        
        Controller --> Client: 200 OK
    end
end

deactivate Controller

@enduml
```

### 7.3 Get Account Sequence Diagram

```plantuml
@startuml Get Account Flow

actor Client
participant "AccountController" as Controller
participant "AccountService" as Service
participant "AccountServiceImpl" as ServiceImpl
participant "AccountDatabaseAdapter" as Adapter
database "Database" as DB

Client -> Controller: GET /accounts/{accountNumber}
activate Controller

Controller -> Service: getAccount(Mono.just(accountNumber))
activate Service

Service -> ServiceImpl: getAccount(Mono<Long>)
activate ServiceImpl

ServiceImpl -> Adapter: getAccountByAccountNumber(accountNumber)
activate Adapter

Adapter -> DB: findByAccountNumber(accountNumber)
activate DB

alt Account Found
    DB --> Adapter: Account
    deactivate DB
    Adapter --> ServiceImpl: Mono<Account>
    deactivate Adapter
    ServiceImpl -> ServiceImpl: log success
    ServiceImpl --> Service: Mono<Account>
    deactivate ServiceImpl
    Service --> Controller: Mono<Account>
    deactivate Service
    Controller --> Client: 200 OK with Account
else Account Not Found
    DB --> Adapter: empty
    Adapter --> ServiceImpl: Mono.empty()
    deactivate Adapter
    ServiceImpl --> Service: Mono.empty()
    deactivate ServiceImpl
    Service --> Controller: Mono.empty()
    deactivate Service
    Controller --> Client: 404 Not Found
end

deactivate Controller

@enduml
```

---

## Design Patterns Used in Domain Module

### 1. **Hexagonal Architecture (Ports and Adapters)**
- **Ports**: `AccountDatabaseAdapter`, `TransactionDatabaseAdapter`, `OperationTypeDatabaseAdapter`
- **Core Domain**: Service implementations
- **Adapters**: Implemented in persistence module

### 2. **Strategy Pattern**
- **Context**: `TransactionServiceImpl`
- **Strategy Interface**: `TransactionHandler`
- **Concrete Strategies**: `CashPurchaseTransactionHandler`, `InstallmentPurchaseTransactionHandler`, `WithdrawalTransactionHandler`, `PaymentTransactionHandler`

### 3. **Factory Pattern**
- **Factory**: `TransactionFactory`
- **Products**: Different `TransactionHandler` implementations
- **Creation Logic**: Based on `SupportedTransactionEnum`

### 4. **Template Method Pattern**
- **Abstract Class**: `TransactionHandler`
- **Template Method**: `performTransaction()`
- **Abstract Methods**: `execute()`, `getHandler()`
- **Hook Methods**: `validate()` (can be overridden)

### 5. **Value Object Pattern**
- **Value Objects**: `AccountNumber`
- **Characteristics**: Immutable, compared by value

### 6. **Repository Pattern** (through adapters)
- **Interfaces**: Database adapter interfaces
- **Encapsulation**: Database access logic

---

## Module Responsibilities

### Model Package
- Define core domain entities
- Represent business concepts
- Maintain invariants

### Service Package
- Define business operations
- Orchestrate domain logic
- Provide reactive APIs

### Adapter Package
- Define ports for external systems
- Decouple domain from infrastructure
- Enable testability

### Exception Package
- Express business rule violations
- Provide meaningful error messages
- Support error handling in reactive streams

### Factory Package
- Create complex objects
- Encapsulate creation logic
- Support extensibility

---

## Reactive Programming with Project Reactor

All service methods return:
- `Mono<T>` - Single value or empty
- `Flux<T>` - Multiple values (not used in current implementation)

Benefits:
- Non-blocking I/O
- Backpressure support
- Composable operations
- Error handling in streams

---

## Key Architectural Principles

1. **Separation of Concerns**: Each package has a clear responsibility
2. **Dependency Inversion**: Domain depends on abstractions (adapters), not implementations
3. **Open/Closed Principle**: New transaction types can be added without modifying existing code
4. **Single Responsibility**: Each handler focuses on one transaction type
5. **Interface Segregation**: Small, focused interfaces

---

Generated: December 26, 2025
Module: Domain Module
Project: Spring Reactive Bank Example

