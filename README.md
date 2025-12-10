# 💰 Money-Splitter

A **Kotlin Multiplatform** expense splitting application for Android and iOS, built with Jetpack Compose. Track shared expenses, calculate balances, and settle debts with friends — all with persistent SQLite storage.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.21-blue.svg)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose-1.9.3-green.svg)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![SQLDelight](https://img.shields.io/badge/SQLDelight-2.0.2-orange.svg)](https://cashapp.github.io/sqldelight/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📱 Features

### Core Functionality
- ✅ **Track Shared Expenses** - Record who paid for what and who participated
- ✅ **Automatic Balance Calculation** - Smart algorithm calculates who owes whom
- ✅ **Debt Simplification** - Minimizes transactions needed to settle all debts
- ✅ **Custom Shares** - Split equally or assign custom amounts per person
- ✅ **Group Management** - Organize friends into groups (e.g., "Roommates", "Trip Team")
- ✅ **Payment Tracking** - Mark payments as settled when friends pay back
- ✅ **SQLite Persistence** - All data saved locally with SQLDelight

### User Interface
- 🏠 **Home Dashboard** - View net balances and friend debts at a glance
- ➕ **Add Expense** - Quick expense entry with flexible splitting options
- 👥 **Groups** - Create and manage friend groups
- 💳 **Payments** - View pending and settled payments
- 👤 **Profile** - User statistics and information

---

## 🎯 Use Cases

| Scenario | How It Helps |
|----------|--------------|
| **Restaurant Bills** | Split equally or by what each person ordered |
| **Trip Expenses** | Track who paid for hotels, transport, food |
| **Shared Rent** | Split utilities by usage or equally |
| **Group Purchases** | One person buys, others reimburse |
| **Event Planning** | Track contributions vs. actual costs |

---

## 🏗 Architecture

```
┌─────────────────────────────────────────────────┐
│              UI Layer (Compose)                 │
│  Home │ Add Expense │ Groups │ Payments │ Profile│
└────────────────┬────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────┐
│           ViewModel Layer                       │
│  State Management + Business Logic              │
└────────────────┬────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────┐
│         ExpenseRepository                       │
│  • Balance Calculation                          │
│  • Debt Simplification Algorithm                │
│  • Data Persistence                             │
└────────────────┬────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────┐
│      DatabaseHelper (SQLDelight)                │
│  SQLite Database with 7 tables                  │
└─────────────────────────────────────────────────┘
```

---

## 🗄 Database Schema

| Table | Purpose |
|-------|---------|
| `users` | All users in the system |
| `friends` | Users who are friends with current user |
| `expenses` | Expense records (description, amount, date, payer) |
| `expense_participants` | Who participated in each expense and their share |
| `groups` | Named groups of friends |
| `group_members` | Group membership mapping |
| `payments` | Payment obligations and settlement status |

---

## 🧮 Expense Splitting Algorithm

### How It Works

1. **Record Expense**
   ```
   Example: $150 dinner paid by you
   Split: You ($50), Alice ($50), Bob ($50)
   ```

2. **Generate Payments**
   ```
   Automatic creation:
   - Alice owes You $50
   - Bob owes You $50
   ```

3. **Calculate Net Balances**
   ```
   You: +$100 (owed to you)
   Alice: -$50 (owes)
   Bob: -$50 (owes)
   ```

4. **Simplify Debts**
   ```
   Greedy algorithm minimizes transactions:
   - Matches largest creditor with largest debtor
   - Reduces complex chains (A→B→C→A) to direct payments
   ```

### Example Scenarios

#### Scenario 1: Equal Split
```
Bus Ticket: Rs.1000 (5 people)
Paid by: Ifaz (Rs.500), Kalana A. (Rs.500)

Result:
✅ Ifaz receives Rs.300
✅ Kalana A. receives Rs.300
❌ Others pay Rs.200 each
```

#### Scenario 2: Custom Consumption
```
Total: Rs.3200
Paid: Ifaz (Rs.1000), Kalana P. (Rs.1000), Suhas (Rs.500), Sangeeth (Rs.700)
Consumed: Ifaz (Rs.400), Kalana P. (Rs.700), Kalana A. (Rs.600), 
          Sangeeth (Rs.1000), Suhas (Rs.500)

Result:
✅ Ifaz receives Rs.600
✅ Kalana P. receives Rs.300
⚖️ Suhas settled (Rs.0)
❌ Sangeeth pays Rs.300
❌ Kalana A. pays Rs.600
```

---

## 🚀 Getting Started

### Prerequisites
- **JDK 17** or higher
- **Android Studio** Hedgehog or newer
- **Xcode** 15+ (for iOS development)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/Money-Splitter.git
   cd Money-Splitter
   ```

2. **Build the project**
   ```bash
   ./gradlew build
   ```

3. **Run on Android**
   ```bash
   ./gradlew composeApp:assembleDebug
   # Install APK from: composeApp/build/outputs/apk/debug/
   ```

4. **Run on iOS**
   ```bash
   cd iosApp
   xcodebuild -scheme iosApp -configuration Debug
   ```

---

## 🛠 Tech Stack

| Component | Technology |
|-----------|------------|
| **Language** | Kotlin 2.2.21 |
| **UI Framework** | Jetpack Compose Multiplatform 1.9.3 |
| **Database** | SQLDelight 2.0.2 (SQLite) |
| **State Management** | Kotlin StateFlow + ViewModel |
| **Date/Time** | kotlinx-datetime 0.6.0 |
| **Build System** | Gradle 8.14.3 with Kotlin DSL |
| **Platforms** | Android (API 24+) & iOS (14.0+) |

---

## 📁 Project Structure

```
Money-Splitter/
├── composeApp/
│   ├── src/
│   │   ├── commonMain/kotlin/com/rexosphere/money_splitter/
│   │   │   ├── App.kt                    # Main app navigation
│   │   │   ├── data/
│   │   │   │   ├── database/             # SQLite (SQLDelight)
│   │   │   │   │   ├── DatabaseDriverFactory.kt
│   │   │   │   │   ├── DatabaseHelper.kt
│   │   │   │   │   └── DatabaseProvider.kt
│   │   │   │   └── repository/
│   │   │   │       └── ExpenseRepository.kt
│   │   │   ├── domain/model/
│   │   │   │   └── Models.kt             # Data classes
│   │   │   ├── expense_calculator/       # Standalone calculator
│   │   │   │   ├── ExpenseSplitCalculator.kt
│   │   │   │   └── ExpenseSplitScenarios.kt
│   │   │   └── presentation/             # UI Screens
│   │   │       ├── home/
│   │   │       ├── add_expense/
│   │   │       ├── groups/
│   │   │       ├── payments/
│   │   │       └── profile/
│   │   ├── androidMain/                  # Android-specific
│   │   ├── iosMain/                      # iOS-specific
│   │   └── commonMain/sqldelight/        # SQL schema
│   └── build.gradle.kts
├── iosApp/                               # iOS Xcode project
└── gradle/
```

---

## 💻 Usage Examples

### Adding an Expense
```kotlin
val expense = Expense(
    id = UUID.random().toString(),
    description = "Dinner at restaurant",
    amount = 150.0,
    date = today,
    paidBy = currentUser,
    participants = mapOf(
        currentUser to 50.0,
        alice to 50.0,
        bob to 50.0
    )
)
repository.addExpense(expense)
```

### Using the Expense Calculator
```kotlin
val calculator = ExpenseSplitCalculator()

// Equal split
val shares = calculator.createEqualShares(participants, 1000.0)

// Calculate balances
val balances = calculator.calculateNetBalances(expense)

// Simplify debts
val debts = calculator.simplifyDebts(balances)
```

---

## 🧪 Testing

### Run Demo Scenarios
```kotlin
import com.rexosphere.money_splitter.ExpenseCalculatorDemo

// In MainActivity or test
ExpenseCalculatorDemo.runDemo()
```

This will output detailed calculations for both equal-split and custom-consumption scenarios.

---

## 🔧 Configuration

### Database Location
- **Android**: `/data/data/com.rexosphere.money_splitter/databases/money_splitter.db`
- **iOS**: App sandbox Documents directory

### Customization
Edit `gradle/libs.versions.toml` to update dependencies:
```toml
[versions]
kotlin = "2.2.21"
sqldelight = "2.0.2"
composeMultiplatform = "1.9.3"
```

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- Built with [Jetpack Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- Database powered by [SQLDelight](https://cashapp.github.io/sqldelight/)
- Inspired by [Splitwise](https://www.splitwise.com/)

---

## 📧 Contact

**Ifaz Ikram** - [@yourusername](https://github.com/yourusername)

Project Link: [https://github.com/yourusername/Money-Splitter](https://github.com/yourusername/Money-Splitter)

---

## 🗺 Roadmap

- [ ] Multi-currency support
- [ ] Export to CSV/PDF
- [ ] Cloud sync
- [ ] Receipt photo upload
- [ ] Recurring expenses
- [ ] Email notifications
- [ ] Dark mode
- [ ] Expense categories

---

**Made with ❤️ using Kotlin Multiplatform**
