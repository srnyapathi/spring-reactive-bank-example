# Domain Module UML Diagrams - README

This directory contains comprehensive UML diagrams for the **Domain Module** of the Spring Reactive Bank Example application.

## 📋 Table of Contents

1. [Overview](#overview)
2. [Diagram Files](#diagram-files)
3. [How to View Diagrams](#how-to-view-diagrams)
4. [Architecture Patterns](#architecture-patterns)
5. [Module Structure](#module-structure)

---

## 🎯 Overview

The Domain Module is the core of the application, implementing business logic using:
- **Hexagonal Architecture** (Ports & Adapters)
- **Strategy Pattern** (Transaction Handlers)
- **Factory Pattern** (Handler Creation)
- **Template Method Pattern** (Handler Base Class)
- **Reactive Programming** (Project Reactor)

---

## 📁 Diagram Files

### Main Documentation
- **`domain-uml-diagrams.md`** - Complete documentation with all diagrams in Markdown format

### PlantUML Source Files

| File | Description | Type |
|------|-------------|------|
| `domain-class-diagram.puml` | Complete class diagram of all domain components | Class Diagram |
| `domain-model-diagram.puml` | Domain model entities and their relationships | Class Diagram |
| `domain-handler-hierarchy.puml` | Transaction handler hierarchy (Strategy + Template Method) | Class Diagram |
| `domain-package-diagram.puml` | Package structure and dependencies | Package Diagram |
| `domain-transaction-sequence.puml` | Transaction processing flow | Sequence Diagram |
| `domain-account-sequence.puml` | Account creation flow | Sequence Diagram |

---

## 👀 How to View Diagrams

### Option 1: Using PlantUML Online Server
1. Go to [PlantUML Online Server](http://www.plantuml.com/plantuml/uml/)
2. Copy the content of any `.puml` file
3. Paste into the editor
4. View the rendered diagram

### Option 2: Using VS Code Extension
1. Install the **PlantUML** extension by jebbs
2. Open any `.puml` file
3. Press `Alt+D` to preview

### Option 3: Using IntelliJ IDEA
1. Install the **PlantUML integration** plugin
2. Open any `.puml` file
3. The diagram renders automatically in the preview pane

### Option 4: Using PlantUML Command Line
```powershell
# Install PlantUML (requires Java)
# Download from: https://plantuml.com/download

# Generate PNG
java -jar plantuml.jar domain-class-diagram.puml

# Generate SVG
java -jar plantuml.jar -tsvg domain-class-diagram.puml

# Generate all diagrams
java -jar plantuml.jar *.puml
```

### Option 5: Using Docker
```powershell
# Pull PlantUML Docker image
docker pull plantuml/plantuml

# Generate diagram
docker run --rm -v ${PWD}:/data plantuml/plantuml -tpng /data/domain-class-diagram.puml
```

---

## 🏛️ Architecture Patterns

### 1. Hexagonal Architecture (Ports & Adapters)
```
┌─────────────────────────────────────┐
│        Domain Module (Core)         │
│                                     │
│  ┌──────────────────────────────┐  │
│  │   Business Logic (Services)  │  │
│  └──────────────────────────────┘  │
│              ↓ uses                 │
│  ┌──────────────────────────────┐  │
│  │    Ports (Adapters Interfaces)│ │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘
              ↓ implements
┌─────────────────────────────────────┐
│    Persistence Module (Adapter)     │
│  - Database Access                  │
│  - Entity Mapping                   │
└─────────────────────────────────────┘
```

**Files to see:**
- `domain-package-diagram.puml` - Shows port definitions
- `domain-class-diagram.puml` - Shows adapter interfaces

### 2. Strategy Pattern
Transaction handlers implement different strategies for processing transactions.

```
TransactionHandler (Interface/Abstract)
    ↑ implements
    ├── CashPurchaseTransactionHandler
    ├── InstallmentPurchaseTransactionHandler
    ├── WithdrawalTransactionHandler
    └── PaymentTransactionHandler
```

**Files to see:**
- `domain-handler-hierarchy.puml` - Detailed handler hierarchy
- `domain-class-diagram.puml` - Complete relationships

### 3. Factory Pattern
TransactionFactory creates appropriate handlers based on operation type.

```
TransactionFactory
    ├── resolveHandler(String)
    └── returns → TransactionHandler
```

**Files to see:**
- `domain-handler-hierarchy.puml` - Factory and handler relationships
- `domain-transaction-sequence.puml` - Factory usage in action

### 4. Template Method Pattern
TransactionHandler defines the algorithm skeleton, subclasses fill in the steps.

```
TransactionHandler (Abstract Class)
    └── performTransaction() [Template Method]
        ├── validate() [Hook - can override]
        └── execute() [Abstract - must implement]
```

**Files to see:**
- `domain-handler-hierarchy.puml` - Template method structure
- `domain-transaction-sequence.puml` - Template method execution

---

## 📦 Module Structure

```
domain/
├── src/main/java/in/srnyapathi/bank/domain/
│   ├── model/                      # Domain entities
│   │   ├── Account.java
│   │   ├── Transaction.java
│   │   ├── AccountNumber.java
│   │   ├── OperationType.java
│   │   ├── TransactionType.java
│   │   └── SupportedTransactionEnum.java
│   │
│   ├── service/                    # Service interfaces
│   │   ├── AccountService.java
│   │   ├── TransactionService.java
│   │   └── OperationTypeService.java
│   │
│   ├── service/impl/               # Service implementations
│   │   ├── AccountServiceImpl.java
│   │   ├── TransactionServiceImpl.java
│   │   ├── OperationTypeServiceImpl.java
│   │   └── TransactionHandler.java (abstract)
│   │
│   ├── service/handlers/           # Transaction handlers
│   │   ├── CashPurchaseTransactionHandler.java
│   │   ├── InstallmentPurchaseTransactionHandler.java
│   │   ├── WithdrawalTransactionHandler.java
│   │   └── PaymentTransactionHandler.java
│   │
│   ├── service/factory/            # Factory classes
│   │   └── TransactionFactory.java
│   │
│   ├── adapters/                   # Ports (interfaces)
│   │   ├── AccountDatabaseAdapter.java
│   │   ├── TransactionDatabaseAdapter.java
│   │   └── OperationTypeDatabaseAdapter.java
│   │
│   ├── exception/                  # Domain exceptions
│   │   ├── InvalidTransactionObjectException.java
│   │   ├── InvalidOperationTypeException.java
│   │   ├── InvalidAmountException.java
│   │   ├── InvalidAccountException.java
│   │   ├── DuplicateAccountException.java
│   │   └── AccountDoesNotExist.java
│   │
│   └── config/
│       └── DomainModuleConfiguration.java
```

---

## 🔍 Quick Reference

### To Understand...

| Topic | See Diagram |
|-------|-------------|
| Domain entities and relationships | `domain-model-diagram.puml` |
| Complete class structure | `domain-class-diagram.puml` |
| Transaction processing flow | `domain-transaction-sequence.puml` |
| Account creation flow | `domain-account-sequence.puml` |
| Handler hierarchy and patterns | `domain-handler-hierarchy.puml` |
| Module organization | `domain-package-diagram.puml` |
| All diagrams in one place | `domain-uml-diagrams.md` |

### Key Concepts

#### Reactive Programming
- All methods return `Mono<T>` or `Flux<T>`
- Non-blocking, asynchronous operations
- Error handling via `Mono.error()`

#### Transaction Types
- **CREDIT**: Increases balance (Payment)
- **DEBIT**: Decreases balance (Purchase, Withdrawal)

#### Supported Transactions
1. **CASH_PURCHASE_TRANSACTION** - Immediate debit
2. **INSTALLMENT_PURCHASE** - Deferred debit
3. **WITHDRAWAL** - Cash removal
4. **PAYMENT** - Credit to account

---

## 🛠️ Tools & Technologies

- **Java**: 17+
- **Spring Boot**: 3.x
- **Project Reactor**: Reactive streams
- **Lombok**: Boilerplate reduction
- **PlantUML**: Diagram generation

---

## 📚 Additional Resources

- [PlantUML Documentation](https://plantuml.com/)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Project Reactor](https://projectreactor.io/docs)
- [Spring WebFlux](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html)

---

## 📝 Notes

- All diagrams were generated based on the actual source code
- Diagrams are kept in sync with the implementation
- Color coding is consistent across all diagrams:
  - **Yellow**: Model/Entities
  - **Green**: Services
  - **Blue**: Implementations
  - **Red**: Handlers
  - **Cyan**: Adapters
  - **Pink**: Exceptions
  - **Purple**: Factories

---

**Generated**: December 26, 2025  
**Module**: Domain Module  
**Project**: Spring Reactive Bank Example  
**Author**: Documentation Team

