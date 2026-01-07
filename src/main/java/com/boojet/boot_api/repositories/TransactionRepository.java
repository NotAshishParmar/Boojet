package com.boojet.boot_api.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.boojet.boot_api.domain.Category;
import com.boojet.boot_api.domain.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

  List<Transaction> findByAccountIdOrderByDateDesc(Long accountId);

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
      @Param("category") Category category,
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
      @Param("category") Category category,
      @Param("fromDate") LocalDate fromDate,
      @Param("toDate") LocalDate toDate,
      Pageable pageable);
}
