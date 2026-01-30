package com.boojet.boot_api.repositories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.boojet.boot_api.domain.CategoryEnum;
import com.boojet.boot_api.domain.Transaction;
import com.boojet.boot_api.repositories.projections.CategoryTotalView;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("""
              select t from Transaction t
              where t.account.id = :accountId
                and (:from is null or t.date >= :from)
                and (:to   is null or t.date <= :to)
                and (:category is null or t.category = :category)
                and (:income   is null or t.income   = :income)
              order by t.date desc
            """)
    List<Transaction> findForAccount(@Param("accountId") Long accountId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("category") CategoryEnum category,
            @Param("income") Boolean income);

    @Query("""
            select t
            from Transaction t
            where (:accountId is null or t.account.id = :accountId)
              and (:category  is null or t.category     = :category)
              and t.date >= :fromDate
              and t.date <= :toDate
            order by t.date desc
            """)
    Page<Transaction> search(@Param("accountId") Long accountId,
            @Param("category") CategoryEnum category,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable);

    /**
     * Returns a ranked list of unique transaction descriptions that start with the given query string.
     *
     * <p><b>How it works</b></p>
     * <ul>
     *   <li><b>Projection:</b> selects only {@code t.description} (not full {@link Transaction} rows) for efficiency.</li>
     *   <li><b>Validation filters:</b> excludes rows where {@code description} is {@code null} or blank ({@code ''}).</li>
     *   <li><b>Prefix matching (case-insensitive):</b>
     *     uses {@code lower(t.description) LIKE lower(concat(:q, '%'))} so the query {@code "sta"} becomes
     *     {@code "sta%"} (matches anything that starts with {@code "sta"}).</li>
     *   <li><b>Uniqueness:</b> {@code group by t.description} collapses repeated descriptions so the same merchant
     *     is returned once (e.g., "Starbucks" is not returned 50 times).</li>
     *   <li><b>Ranking:</b> orders results by most common descriptions first ({@code count(t) DESC}), then breaks ties
     *     by the most recently used ({@code max(t.date) DESC}).</li>
     *   <li><b>Limiting:</b> {@link Pageable} controls how many suggestions are returned (acts like a SQL {@code LIMIT}).</li>
     * </ul>
     *
     * <p><b>Example</b>: {@code q="sta"} may match {@code "Starbucks"} and {@code "Staples"}, but not {@code "Instacart"}.</p>
     *
     * @param q the user-entered query string used for prefix matching
     * @param pageable paging/limit information to cap the number of suggestions returned
     * @return a list of matching descriptions, ordered by frequency and recency
     */
    @Query("""
            select t.description
            from Transaction t
            where t.description is not null
              and t.description <> ''
              and lower(t.description) like lower(concat(:q, '%'))
            group by t.description
            order by count(t) desc, max(t.date) desc
            """)
    List<String> suggestPrefix(@Param("q") String q, Pageable pageable);


    @Query("""
            select t.description
            from Transaction t
            where t.description is not null
              and t.description <> ''
              and lower(t.description) like lower(concat('%', :q, '%'))
            group by t.description
            order by count(t) desc, max(t.date) desc
            """)
    List<String> suggestContains(@Param("q") String q, Pageable pageable);

    /**
     * Returns the most recently used Transaction for the given description (case-insensitive).
     *
     * <p>Spring Data will sort matching transactions by {@code date DESC} and then {@code id DESC}
     * (as a tiebreaker), and return only the first result (equivalent to {@code LIMIT 1}).</p>
     *
     * <p>Note: This method does not filter out null/blank descriptions in the database.
     * Input validation (trim, min length, non-blank) should be handled in the service layer.</p>
     *
     * @param description the transaction description to match (case-insensitive)
     * @return an Optional containing the most recent matching Transaction, or empty if none exist
     */
    Optional<Transaction> findTopByDescriptionIgnoreCaseOrderByDateDescIdDesc(String description);


    @Query("""
                select coalesce(
                  sum(case when t.income = true then t.amount else -t.amount end), 0
                )
                from Transaction t
            """)
    BigDecimal sumNetAll();

    @Query("""
                select coalesce(
                  sum(case when t.income = true then t.amount else -t.amount end), 0
                )
                from Transaction t
                where t.date >= :start and t.date <= :end
            """)
    BigDecimal sumNetBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("""
                select coalesce(
                    sum(case when t.income = true then t.amount else -t.amount end), 0
                )
                from Transaction t
                where t.category = :category
            """)
    BigDecimal sumNetByCategory(@Param("category") CategoryEnum category);

    @Query("""
            select coalesce (
                sum(case when t.income = true then t.amount else -t.amount end), 0
            )
            from Transaction t
            where t.account.id = :accountId
            """)
    BigDecimal sumNetForAccount(@Param("accountId") Long accountId);

    @Query("""
              select coalesce(sum(t.amount), 0)
              from Transaction t
              where t.income = true
                and t.date >= :start and t.date <= :end
            """)
    BigDecimal sumIncomeBetween(@Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("""
              select coalesce(sum(t.amount), 0)
              from Transaction t
              where t.income = false
                and t.date >= :start and t.date <= :end
            """)
    BigDecimal sumExpensesBetween(@Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("""
            select t.category as category,
                    coalesce(sum(case when t.income = true then t.amount else -t.amount end), 0) as total
              from Transaction t
              where t.date >= :start and t.date <= :end
              group by t.category
            """)
    List<CategoryTotalView> sumNetByCategoryBetween(@Param("start") LocalDate start,
            @Param("end") LocalDate end);

}
