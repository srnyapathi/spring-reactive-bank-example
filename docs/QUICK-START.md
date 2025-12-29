# Domain Module UML Diagrams - Quick Start

## 🎯 Overview

This directory contains **comprehensive UML diagrams** for the **Domain Module** of the Spring Reactive Bank Example application.

**Total Diagrams**: 7 PlantUML files + Complete Documentation

---

## 🚀 Quick Access

### 📖 Documentation Files

1. **[DIAGRAMS-INDEX.md](./DIAGRAMS-INDEX.md)** ⭐ START HERE
   - Index of all diagrams with descriptions
   - Selection guide based on what you want to learn
   - Learning path recommendations

2. **[README-UML-DIAGRAMS.md](./README-UML-DIAGRAMS.md)**
   - Complete guide on viewing diagrams
   - Tool setup instructions
   - Architecture patterns explanation

3. **[domain-uml-diagrams.md](./domain-uml-diagrams.md)**
   - All diagrams embedded in Markdown format
   - Can be viewed directly on GitHub
   - Includes sequence diagrams and notes

---

## 📊 Diagram Files (.puml)

### Class Diagrams

| # | File | Description | Complexity |
|---|------|-------------|------------|
| 1 | [domain-class-diagram.puml](./domain-class-diagram.puml) | **Complete class diagram** - All domain components | High ⭐⭐⭐ |
| 2 | [domain-model-diagram.puml](./domain-model-diagram.puml) | **Domain entities** - Core business objects | Low ⭐ |
| 3 | [domain-handler-hierarchy.puml](./domain-handler-hierarchy.puml) | **Transaction handlers** - Strategy + Template Method | Medium ⭐⭐ |
| 4 | [domain-exception-hierarchy.puml](./domain-exception-hierarchy.puml) | **Exception classes** - Error handling | Low ⭐ |

### Package Diagrams

| # | File | Description | Complexity |
|---|------|-------------|------------|
| 5 | [domain-package-diagram.puml](./domain-package-diagram.puml) | **Package structure** - Module organization | Low ⭐ |

### Sequence Diagrams

| # | File | Description | Complexity |
|---|------|-------------|------------|
| 6 | [domain-transaction-sequence.puml](./domain-transaction-sequence.puml) | **Transaction flow** - Complete processing | High ⭐⭐⭐ |
| 7 | [domain-account-sequence.puml](./domain-account-sequence.puml) | **Account creation** - Simple flow | Medium ⭐⭐ |

---

## 👁️ How to View

### Option 1: Online (Easiest - No Setup Required)
1. Open [PlantUML Online Server](http://www.plantuml.com/plantuml/uml/)
2. Copy content from any `.puml` file
3. Paste into editor
4. View rendered diagram

### Option 2: VS Code
1. Install **PlantUML** extension
2. Open `.puml` file
3. Press `Alt+D`

### Option 3: IntelliJ IDEA
1. Install **PlantUML integration** plugin
2. Open `.puml` file
3. Auto-preview

### Option 4: Export to Images
```powershell
# Navigate to docs directory
cd D:\Projects\spring-boot-web-application\project\spring-reactive-bank-example\docs

# Generate all diagrams as PNG (requires Java + PlantUML)
java -jar plantuml.jar *.puml

# Or as SVG
java -jar plantuml.jar -tsvg *.puml
```

---

## 🎓 Recommended Learning Path

### 1. Start with High-Level Overview
📄 **[domain-package-diagram.puml](./domain-package-diagram.puml)**
- Understand module organization
- See architectural layers
- Identify package dependencies

### 2. Learn Domain Entities
📄 **[domain-model-diagram.puml](./domain-model-diagram.puml)**
- Core business objects (Account, Transaction, etc.)
- Relationships between entities
- Value objects and enums

### 3. Follow a Simple Flow
📄 **[domain-account-sequence.puml](./domain-account-sequence.puml)**
- See how account creation works
- Understand reactive flow
- Learn error handling

### 4. Understand Design Patterns
📄 **[domain-handler-hierarchy.puml](./domain-handler-hierarchy.puml)**
- Strategy Pattern (different transaction types)
- Template Method Pattern (common algorithm)
- Factory Pattern (handler creation)

### 5. Deep Dive into Complex Flow
📄 **[domain-transaction-sequence.puml](./domain-transaction-sequence.puml)**
- Complete transaction processing
- Multiple validation steps
- Error scenarios

### 6. Study Complete Architecture
📄 **[domain-class-diagram.puml](./domain-class-diagram.puml)**
- All classes and relationships
- Complete module structure
- Comprehensive view

---

## 🏗️ Architecture Highlights

### Hexagonal Architecture (Ports & Adapters)
```
┌──────────────────┐
│  Domain Logic    │  ← Pure business logic
│  (this module)   │
└────────┬─────────┘
         │ defines
┌────────▼─────────┐
│  Adapter Ports   │  ← Interfaces (AccountDatabaseAdapter, etc.)
│  (interfaces)    │
└────────┬─────────┘
         │ implemented by
┌────────▼─────────┐
│  Persistence     │  ← Database implementation
│  (another module)│
└──────────────────┘
```

**See**: `domain-package-diagram.puml`, `domain-class-diagram.puml`

### Strategy Pattern for Transactions
```
TransactionHandler (Abstract)
    ├── CashPurchaseTransactionHandler
    ├── InstallmentPurchaseTransactionHandler  
    ├── WithdrawalTransactionHandler
    └── PaymentTransactionHandler
```

**See**: `domain-handler-hierarchy.puml`

---

## 📦 Module Contents

### Packages
- `model` - Domain entities (Account, Transaction, etc.)
- `service` - Service interfaces
- `service.impl` - Service implementations
- `service.handlers` - Transaction handlers
- `service.factory` - Factory classes
- `adapters` - Port interfaces (Hexagonal Architecture)
- `exception` - Domain exceptions

### Key Classes
- **Account** - Aggregate root for bank accounts
- **Transaction** - Transaction entity
- **TransactionHandler** - Abstract base for handlers
- **TransactionFactory** - Creates appropriate handlers
- **AccountService** - Account operations
- **TransactionService** - Transaction operations

---

## 🎯 Use Cases Documented

### 1. Create Account
- **Sequence**: `domain-account-sequence.puml`
- **Flow**: Client → Controller → Service → Adapter → Database
- **Error**: Duplicate account handling

### 2. Process Transaction
- **Sequence**: `domain-transaction-sequence.puml`
- **Flow**: Complete validation → Handler selection → Persistence
- **Errors**: Invalid amount, invalid account, invalid operation type

### 3. Transaction Handler Selection
- **Class**: `domain-handler-hierarchy.puml`
- **Pattern**: Factory resolves handler based on operation type
- **Strategy**: Each handler implements specific logic

---

## 🔍 Design Patterns Reference

| Pattern | Files | Purpose |
|---------|-------|---------|
| **Hexagonal Architecture** | package, class | Decouple business from infrastructure |
| **Strategy** | handler-hierarchy | Different algorithms for transactions |
| **Template Method** | handler-hierarchy | Common algorithm skeleton |
| **Factory** | handler-hierarchy | Centralized handler creation |
| **Value Object** | model | Immutable domain primitives |
| **Aggregate Root** | model | Consistency boundaries |

---

## 📈 Diagram Statistics

- **Total Files**: 10 (7 .puml + 3 .md)
- **Class Diagrams**: 4
- **Sequence Diagrams**: 2
- **Package Diagrams**: 1
- **Classes Documented**: 20+
- **Interfaces Documented**: 8
- **Design Patterns Shown**: 6

---

## 🎨 Color Coding

All diagrams use consistent colors:
- **Yellow (#FFF8DC)**: Model/Entities
- **Green (#E6F3E6)**: Services
- **Blue (#E6F0FF)**: Implementations
- **Red (#FFE6E6)**: Handlers
- **Cyan (#E6FFFF)**: Adapters
- **Pink (#FFE6F0)**: Exceptions
- **Purple (#F0E6FF)**: Factories

---

## 💡 Tips

1. **Start Simple**: Begin with package and model diagrams
2. **Use Filters**: PlantUML supports hiding elements you don't need
3. **Print Large**: Class diagrams are detailed - use large format
4. **Follow Flows**: Use sequence diagrams to understand processes
5. **Reference Code**: Keep diagrams alongside code for validation

---

## 📞 Resources

- **PlantUML**: https://plantuml.com/
- **Hexagonal Architecture**: https://alistair.cockburn.us/hexagonal-architecture/
- **Project Reactor**: https://projectreactor.io/
- **Spring WebFlux**: https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html

---

## ✅ Checklist for Understanding Domain Module

- [ ] Reviewed package structure diagram
- [ ] Understand domain entities and their relationships
- [ ] Followed account creation sequence
- [ ] Understand transaction handler hierarchy
- [ ] Followed transaction processing sequence
- [ ] Reviewed exception hierarchy
- [ ] Studied complete class diagram
- [ ] Understand Hexagonal Architecture pattern
- [ ] Understand Strategy + Template Method patterns
- [ ] Can trace code from diagram to implementation

---

**Generated**: December 26, 2025  
**Module**: Domain Module  
**Project**: Spring Reactive Bank Example

---

## 🎉 You're Ready!

Start with **[DIAGRAMS-INDEX.md](./DIAGRAMS-INDEX.md)** for the complete guide.

