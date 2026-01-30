package com.boojet.boot_api.testutil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.cglib.core.Local;

import com.boojet.boot_api.domain.Account;
import com.boojet.boot_api.domain.AccountType;
import com.boojet.boot_api.domain.CategoryEnum;
import com.boojet.boot_api.domain.IncomePlan;
import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.domain.PayType;
import com.boojet.boot_api.domain.Transaction;

public final class TestDataUtil{
    
    private TestDataUtil(){
        //private constructor to prevent instantiation
    }


    // -------------------------------Factory Methods-------------------------------- 
    public static Account createAccount(String name, AccountType type, Money balance){
        return Account.builder()
                .name(name)
                .type(type)
                .openingBalance(balance)
                .build();
    }

    public static Transaction createTransaction(String description, Money amount, LocalDate date, CategoryEnum category, boolean income, Account account){
        return Transaction.builder()
                .description(description)
                .amount(amount)
                .date(date)
                .category(category)
                .income(income)
                .account(account)
                .build();
    }

    public static IncomePlan createIncomePlan(String name, PayType payType, Money amount, BigDecimal hoursPerWeek, LocalDate effectiveFrom, LocalDate effectiveTo){
        return IncomePlan.builder()
                .sourceName(name)
                .payType(payType)
                .amount(amount)
                .hoursPerWeek(hoursPerWeek)
                .effectiveFrom(effectiveFrom)
                .effectiveTo(effectiveTo)
                .build();
    }
                

    public static Account createTestAccountA(){
        return createAccount("Test Account A", AccountType.CHEQUING, Money.of("1000.00"));
    }

    public static Account createTestAccountB(){
        return createAccount("Test Account B", AccountType.SAVINGS, Money.of("5000.00"));
    }

    public static Transaction createTestTransactionA(Account account){
        return createTransaction("Salary", Money.of("3000.00"), LocalDate.of(2024, 6, 1), CategoryEnum.INCOME, true, account);
    }

    public static Transaction createTestTransactionB(Account account){
        return createTransaction("Grocery Shopping", Money.of("150.75"), LocalDate.of(2024, 6, 15), CategoryEnum.FOOD, false, account);
    }

    public static Transaction createTestTransactionC(Account account){
        return createTransaction("Electricity Bill", Money.of("75.50"), LocalDate.of(2024, 6, 20), CategoryEnum.UTILITIES, false, account);
    }

    public static List<Transaction> someTransactions(Account a) {
        return List.of(
            createTestTransactionA(a),
            createTestTransactionB(a),
            createTestTransactionC(a)
        );
    }

    // -----------------------------------------------------------------------------


    // ------------------------ Builder with sensible defaults ------------------------
    public static TxBuilder aTransaction() { return new TxBuilder(); }

    public static final class TxBuilder {
        private String description = "Sample";
        private Money amount = Money.of("12.34");
        private LocalDate date = LocalDate.of(2025, 1, 15);
        private CategoryEnum category = CategoryEnum.FOOD;          
        private boolean income = false;
        private Account account = createTestAccountA();

        public TxBuilder desc(String s){ this.description = s; return this; }
        public TxBuilder amt(String v){ this.amount = Money.of(v); return this; }
        public TxBuilder amt(Money m){ this.amount = m; return this; }
        public TxBuilder on(LocalDate d){ this.date = d; return this; }
        public TxBuilder on(YearMonth ym, int day){ this.date = ym.atDay(day); return this; }
        public TxBuilder cat(CategoryEnum c){ this.category = c; return this; }
        public TxBuilder income(){ this.income = true; this.category = CategoryEnum.INCOME; return this; }
        public TxBuilder expense(){ this.income = false; return this; }
        public TxBuilder acct(Account a){ this.account = a; return this; }
        public TxBuilder acct(String name, AccountType type, String opening){
        this.account = createAccount(name, type, Money.of(opening)); return this;
        }

        public Transaction build(){
            return createTransaction(description, amount, date, category, income, account);
        }
    }
    

    public static AcctBuilder anAccount() { return new AcctBuilder(); }

    public static final class AcctBuilder {
        private String name = "Default Account";
        private AccountType type = AccountType.CHEQUING;
        private Money openingBalance = Money.zero();

        public AcctBuilder name(String n){ this.name = n; return this; }
        public AcctBuilder type(AccountType t){ this.type = t; return this; }
        public AcctBuilder opening(Money m){ this.openingBalance = m; return this; }
        public AcctBuilder opening(String v){ this.openingBalance = Money.of(v); return this; }

        public Account build(){
            return createAccount(name, type, openingBalance);
        }
    }

    public static IncomePlanBuilder anIncomePlan() { return new IncomePlanBuilder(); }

    public static final class IncomePlanBuilder {
        private String sourceName = "Default Income Plan";
        private PayType payType = PayType.MONTHLY;
        private Money amount = Money.of("1000.00");
        private BigDecimal hoursPerWeek = null;
        private LocalDate effectiveFrom = LocalDate.now();
        private LocalDate effectiveTo = null;

        public IncomePlanBuilder sourceName(String n){ this.sourceName = n; return this; }
        public IncomePlanBuilder payType(PayType pt){ this.payType = pt; return this; }
        public IncomePlanBuilder amount(Money m){ this.amount = m; return this; }
        public IncomePlanBuilder amount(String v){ this.amount = Money.of(v); return this; }
        public IncomePlanBuilder hoursPerWeek(BigDecimal h){ this.hoursPerWeek = h; return this; }
        public IncomePlanBuilder effectiveFrom(LocalDate d){ this.effectiveFrom = d; return this; }
        public IncomePlanBuilder effectiveTo(LocalDate d){ this.effectiveTo = d; return this; }

        public IncomePlan build(){
            return IncomePlan.builder()
                    .sourceName(sourceName)
                    .payType(payType)
                    .amount(amount)
                    .hoursPerWeek(hoursPerWeek)
                    .effectiveFrom(effectiveFrom)
                    .effectiveTo(effectiveTo)
                    .build();
        }
    }
}