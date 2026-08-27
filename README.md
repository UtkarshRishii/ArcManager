# ArcManager ⚡

> **Personal Client Payment & Finance Manager for Android**  
> Built with **Kotlin**, **Jetpack Compose (Material 3)**, **Clean Architecture (MVVM)**, **Hilt DI**, and **Supabase (PostgreSQL + Auth + Storage)**.

---

## 💎 Design Aesthetic

ArcManager is engineered with a **Linear × Revolut** inspired dark fintech aesthetic:
- **Palette**: Deep near-black surfaces (`#0A0A0F`), elevated card containers (`#1E1E2A`), electric violet accents (`#8B5CF6`), and restrained status badges (Success Green, Warning Amber, Danger Red).
- **Typography**: Google Fonts **Outfit** for financial figures & display headings + **Inter** for crisp data tables and forms.
- **Financial Precision**: Every money value is computed using `java.math.BigDecimal` (never float/double) with Indian numbering (`₹1,24,500`) and international formatting support.
- **Zero Hallucinated Totals**: Financial totals (`totalReceived`, `pendingAmount`, `overdue`) are always calculated dynamically from actual payment records.

---

## 🏛️ Architecture

```
app/src/main/java/com/arcmanager/
├── core/
│   ├── di/                 # Hilt dependency injection modules (AppModule, RepositoryModule)
│   ├── security/           # Keystore-backed AES-GCM encryption for bank account data
│   └── util/               # Result wrapper, Constants, DateUtils, CurrencyUtils, ValidationUtils
├── data/
│   ├── mapper/             # Bidirectional DTO <-> Domain model mappers
│   ├── remote/dto/         # Kotlinx Serialization DTOs with @SerialName
│   └── repository/         # Supabase Postgrest & Auth repository implementations
├── domain/
│   ├── model/              # Immutable domain entities (Client, Project, Payment, PaymentSchedule, etc.)
│   ├── repository/         # Domain repository interfaces
│   └── usecase/            # Business logic (Dashboard overview, balance calculations, schedule builder)
└── presentation/
    ├── components/         # Reusable UI components (FinancialCard, StatusBadge, SearchBar, FilterChips, etc.)
    ├── navigation/         # NavHost, sealed Screen routes, Animated BottomNavBar
    ├── screens/
    │   ├── auth/           # Splash, Login, Register, Forgot Password
    │   ├── clients/        # Clients list, Add Client, Client Detail with Tabs
    │   ├── dashboard/      # Main financial overview, live cards, upcoming dues, recent transactions
    │   ├── more/           # Hub for accounts, retainers, analytics, profile, sign-out
    │   ├── payments/       # Transaction ledger, Add Payment (quick action), Payment Details
    │   └── projects/       # Create Project, Project Detail, Payment Schedule Builder
    ├── theme/              # Color, Type (Outfit & Inter), Theme, Shape, Dimens
    └── viewmodel/          # Hilt ViewModels with immutable StateFlow UI states
```

---

## 🚀 Getting Started

### 1. Requirements
- **Android Studio** (Koala / Ladybug or newer recommended)
- **JDK 17 or 21**
- **Android SDK Platform 35** (Min SDK 26)

### 2. Supabase Setup
1. Create a project at [supabase.com](https://supabase.com).
2. Go to **SQL Editor** in your Supabase dashboard and run the migrations in order:
   - `supabase/migrations/001_initial_schema.sql` (Tables, foreign keys, indexes)
   - `supabase/migrations/002_rls_policies.sql` (Row Level Security policies)
   - `supabase/migrations/003_functions.sql` (Auto updated_at & profile trigger)
3. Open `app/build.gradle.kts` and add your Supabase credentials:
   ```kotlin
   buildConfigField("String", "SUPABASE_URL", "\"https://your-project.supabase.co\"")
   buildConfigField("String", "SUPABASE_ANON_KEY", "\"your-anon-key\"")
   ```

### 3. Build & Run
1. Open the `arc-manager` directory in **Android Studio**.
2. Let Gradle sync dependencies.
3. Select an Android Emulator (API 26+) or physical device and click **Run (Shift + F10)**.

---

## 🔒 Security & Privacy
- **Bank Data Encryption**: Sensitive account numbers and IFSC codes are encrypted using AES-GCM backed by Android Keystore.
- **Account Number Masking**: The UI only ever displays the last 4 digits (e.g. `•••• 4821`).
- **Multi-Tenant RLS**: Supabase Row Level Security restricts every query to `auth.uid() = user_id`.
- **Bookkeeping Model**: ArcManager does NOT attempt to claim knowledge of live bank balances; it is a private receivables tracking ledger.
