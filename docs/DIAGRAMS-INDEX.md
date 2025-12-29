# Domain Module - UML Diagrams Index

## 📊 Available Diagrams

This directory contains comprehensive UML diagrams documenting the Domain Module architecture, design patterns, and interactions.

---

## 📁 Files Overview

### 📖 Documentation
| File | Description |
|------|-------------|
| [README-UML-DIAGRAMS.md](./README-UML-DIAGRAMS.md) | Complete guide on viewing and understanding diagrams |
| [domain-uml-diagrams.md](./domain-uml-diagrams.md) | All diagrams embedded in Markdown format |
| [DIAGRAMS-INDEX.md](./DIAGRAMS-INDEX.md) | This file - Quick reference index |

---

## 🎨 Diagram Files

### 1️⃣ Complete Class Diagram
**File**: [domain-class-diagram.puml](./domain-class-diagram.puml)

**Content**: Complete overview of all domain module classes
- All model classes
- Service interfaces and implementations
- Adapter interfaces
- Transaction handlers
- Factory classes
- Exception classes
- All relationships and dependencies

**Best for**: Getting a complete overview of the module structure

**View online**: [Open in PlantUML Server](http://www.plantuml.com/plantuml/uml/)

---

### 2️⃣ Domain Model Diagram
**File**: [domain-model-diagram.puml](./domain-model-diagram.puml)

**Content**: Core domain entities and their relationships
- Account (Aggregate Root)
- Transaction (Entity)
- AccountNumber (Value Object)
- OperationType (Entity)
- TransactionType (Enum)
- SupportedTransactionEnum (Enum)

**Best for**: Understanding domain entities and their relationships

**Key Patterns**: DDD (Domain-Driven Design), Value Objects, Aggregates

---

### 3️⃣ Transaction Handler Hierarchy
**File**: [domain-handler-hierarchy.puml](./domain-handler-hierarchy.puml)

**Content**: Transaction handler class hierarchy
- TransactionHandler (Abstract base class)
- CashPurchaseTransactionHandler
- InstallmentPurchaseTransactionHandler
- WithdrawalTransactionHandler
- PaymentTransactionHandler
- TransactionFactory

**Best for**: Understanding transaction processing strategies

**Key Patterns**: Strategy Pattern, Template Method Pattern, Factory Pattern

---

### 4️⃣ Package Structure Diagram
**File**: [domain-package-diagram.puml](./domain-package-diagram.puml)

**Content**: High-level package organization
- Package dependencies
- Module boundaries
- Architectural layers

**Best for**: Understanding module organization and dependencies

**Key Patterns**: Hexagonal Architecture (Ports & Adapters)

---

### 5️⃣ Exception Hierarchy Diagram
**File**: [domain-exception-hierarchy.puml](./domain-exception-hierarchy.puml)

**Content**: Exception class hierarchy
- All domain exceptions
- Exception categories (Validation, DataIntegrity, NotFound)
- Usage scenarios
- HTTP status mappings

**Best for**: Understanding error handling and exception types

---

### 6️⃣ Transaction Sequence Diagram
**File**: [domain-transaction-sequence.puml](./domain-transaction-sequence.puml)

**Content**: Complete transaction processing flow
- Request handling
- Operation type resolution
- Handler selection
- Validation
- Transaction persistence
- Error scenarios

**Best for**: Understanding how transactions are processed step-by-step

**Key Flows**: 
- Happy path (successful transaction)
- Error paths (validation failures, invalid operation types)

---

### 7️⃣ Account Creation Sequence Diagram
**File**: [domain-account-sequence.puml](./domain-account-sequence.puml)

**Content**: Account creation flow
- Account request handling
- Validation
- Persistence
- Duplicate detection
- Success/error responses

**Best for**: Understanding account creation process

**Key Flows**:
- Successful account creation
- Duplicate account handling

---

## 🎯 Diagram Selection Guide

### I want to understand...

#### **Domain Entities**
→ Start with: `domain-model-diagram.puml`
- See all entities, value objects, and enums
- Understand relationships between entities

#### **Transaction Processing**
→ Start with: `domain-transaction-sequence.puml`
- Follow the complete transaction flow
- See all validation steps
- Understand error handling

Then view: `domain-handler-hierarchy.puml`
- See different transaction types
- Understand handler selection

#### **Architecture & Design Patterns**
→ Start with: `domain-package-diagram.puml`
- See high-level structure
- Understand architectural layers

Then view: `domain-handler-hierarchy.puml`
- See Strategy + Template Method patterns
- Understand Factory pattern usage

#### **Error Handling**
→ Start with: `domain-exception-hierarchy.puml`
- See all exception types
- Understand when each is thrown
- See HTTP status mappings

#### **Complete Overview**
→ Start with: `domain-class-diagram.puml`
- See everything in one diagram
- Understand all relationships

---

## 🔧 How to View Diagrams

### Online (No Installation)
1. Open [PlantUML Online Server](http://www.plantuml.com/plantuml/uml/)
2. Copy content from any `.puml` file
3. Paste and view

### VS Code
1. Install **PlantUML** extension by jebbs
2. Open `.puml` file
3. Press `Alt+D` to preview

### IntelliJ IDEA
1. Install **PlantUML integration** plugin
2. Open `.puml` file
3. Diagram renders in preview pane

### Command Line
```powershell
# Generate PNG
java -jar plantuml.jar domain-class-diagram.puml

# Generate SVG
java -jar plantuml.jar -tsvg domain-class-diagram.puml

# Generate all
java -jar plantuml.jar *.puml
```

---

## 📋 Quick Reference Table

| Diagram | Type | Complexity | Focus Area |
|---------|------|------------|------------|
| domain-class-diagram.puml | Class | High | Complete structure |
| domain-model-diagram.puml | Class | Low | Domain entities |
| domain-handler-hierarchy.puml | Class | Medium | Transaction handlers |
| domain-package-diagram.puml | Package | Low | Module organization |
| domain-exception-hierarchy.puml | Class | Low | Error handling |
| domain-transaction-sequence.puml | Sequence | High | Transaction flow |
| domain-account-sequence.puml | Sequence | Medium | Account creation |

**Complexity Legend:**
- **Low**: Quick to understand, focused scope
- **Medium**: Moderate detail, specific subsystem
- **High**: Comprehensive, requires careful study

---

## 🎓 Learning Path

### Beginner
1. `domain-package-diagram.puml` - Understand overall structure
2. `domain-model-diagram.puml` - Learn domain entities
3. `domain-account-sequence.puml` - See simple flow

### Intermediate
4. `domain-handler-hierarchy.puml` - Understand design patterns
5. `domain-exception-hierarchy.puml` - Learn error handling
6. `domain-transaction-sequence.puml` - Follow complex flow

### Advanced
7. `domain-class-diagram.puml` - See complete architecture
8. Study relationships between all components
9. Trace code paths using sequence diagrams

---

## 🏗️ Design Patterns Documented

| Pattern | Diagram(s) | Purpose |
|---------|-----------|---------|
| **Hexagonal Architecture** | package, class | Decouple business logic from infrastructure |
| **Strategy Pattern** | handler-hierarchy | Different algorithms for transaction types |
| **Template Method** | handler-hierarchy | Define algorithm skeleton |
| **Factory Pattern** | handler-hierarchy | Centralize object creation |
| **Value Object** | model | Immutable domain primitives |
| **Aggregate Root** | model | Define consistency boundaries |

---

## 📊 Statistics

- **Total Diagrams**: 7
- **Class Diagrams**: 4
- **Sequence Diagrams**: 2
- **Package Diagrams**: 1
- **Total Classes Documented**: 20+
- **Design Patterns**: 6

---

## 🔄 Update History

- **2025-12-26**: Initial creation with complete domain module coverage

---

## 📞 Support

For questions or issues with diagrams:
1. Review [README-UML-DIAGRAMS.md](./README-UML-DIAGRAMS.md)
2. Check [PlantUML Documentation](https://plantuml.com/)
3. Contact development team

---

**Module**: Domain Module  
**Project**: Spring Reactive Bank Example  
**Generated**: December 26, 2025

