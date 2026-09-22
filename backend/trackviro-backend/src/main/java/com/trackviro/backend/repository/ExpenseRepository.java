package com.trackviro.backend.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.trackviro.backend.model.Expense;
import com.trackviro.backend.model.User;

/**
 * Ported from com.example.demo.repository.ExpenseRepository.
 *
 * Every derived method and every @Query is preserved EXACTLY as written
 * in the old project — including the known defects (missing YEAR()
 * filter on getMonthlyTotal, missing isActive filters on a few
 * aggregates). Per Step 2 instructions #17/#18, those are business-logic
 * bugs, not Boot 4 / Hibernate 7 compatibility problems, so they are
 * left untouched here and will be addressed in a later step, not
 * silently fixed now.
 *
 * Compatibility check performed for Hibernate 7 (see explanation below
 * the class): nothing in this file required a change.
 */
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByEmployeeAndIsActiveTrue(User user);

    List<Expense> findByEmployeeDepartmentIdAndStatusAndIsActiveTrue(
            Long deptId, String status);

    List<Expense> findByStatusAndIsActiveTrue(String status);

    boolean existsByEmployeeAndExpenseDateAndAmountAndCategoryId(
            User employee,
            LocalDate expenseDate,
            Double amount,
            Long categoryId
    );

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.employee.id=?1 AND MONTH(e.expenseDate)=MONTH(CURRENT_DATE)")
    Double getMonthlyTotal(Long empId);

    @Query("SELECT COUNT(e) FROM Expense e WHERE e.employee.id=?1 AND e.status=?2")
    Long countByStatus(Long empId, String status);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.status='FINANCE_APPROVED'")
    Double totalFinanceApproved();

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.status='REIMBURSED'")
    Double totalReimbursed();

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.employee.id=?1 AND e.status='FINANCE_APPROVED'")
    Double getApprovedAmount(Long empId);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.employee.id=?1 AND e.status='REIMBURSED'")
    Double getReimbursedAmount(Long empId);

    @Query("SELECT COUNT(e) FROM Expense e WHERE e.employee.department.id=?1 AND e.status=?2 AND e.isActive=true")
    Long countDeptByStatus(Long deptId, String status);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE MONTH(e.expenseDate)=MONTH(CURRENT_DATE) AND e.status='FINANCE_APPROVED'")
    Double monthlyFinanceApproved();

    @Query("SELECT e.category.name, SUM(e.amount) FROM Expense e WHERE e.status='FINANCE_APPROVED' GROUP BY e.category.name")
    List<Object[]> categorySummary();

    @Query("SELECT e.category.name, SUM(e.amount) FROM Expense e WHERE e.employee.id=?1 AND e.isActive=true GROUP BY e.category.name")
    List<Object[]> getEmpCategorySummary(Long empId);

    // Employee: monthly summary (month number + total)
    @Query("SELECT MONTH(e.expenseDate), SUM(e.amount) FROM Expense e WHERE e.employee.id=?1 AND e.isActive=true GROUP BY MONTH(e.expenseDate) ORDER BY MONTH(e.expenseDate)")
    List<Object[]> getEmpMonthlySummary(Long empId);

    // Department: category summary
    @Query("SELECT e.category.name, SUM(e.amount) FROM Expense e WHERE e.employee.department.id=?1 AND e.isActive=true GROUP BY e.category.name")
    List<Object[]> getDeptCategorySummary(Long deptId);

    // Department: monthly summary
    @Query("SELECT MONTH(e.expenseDate), SUM(e.amount) FROM Expense e WHERE e.employee.department.id=?1 AND e.isActive=true GROUP BY MONTH(e.expenseDate) ORDER BY MONTH(e.expenseDate)")
    List<Object[]> getDeptMonthlySummary(Long deptId);

    // Finance: department total summary
    @Query("SELECT e.employee.department.name, SUM(e.amount) FROM Expense e WHERE e.isActive=true GROUP BY e.employee.department.name")
    List<Object[]> getDeptSummary();

    // Finance: overall monthly trend
    @Query("SELECT MONTH(e.expenseDate), SUM(e.amount) FROM Expense e WHERE e.isActive=true GROUP BY MONTH(e.expenseDate) ORDER BY MONTH(e.expenseDate)")
    List<Object[]> getMonthlyTrend();

    // Finance: total count by status (across all employees)
    @Query("SELECT COUNT(e) FROM Expense e WHERE e.status=?1 AND e.isActive=true")
    Long countAllByStatus(String status);

    // Finance: top 5 spenders
    // JPQL "LIMIT" clause — a Hibernate HQL extension since 6.2, unchanged
    // in Hibernate 7. No modification needed for Boot 4 / Hibernate 7.
    @Query("SELECT e.employee.name, SUM(e.amount) FROM Expense e WHERE e.isActive=true GROUP BY e.employee.name ORDER BY SUM(e.amount) DESC LIMIT 5")
    List<Object[]> getTopSpenders();

    @Query("SELECT e FROM Expense e WHERE e.employee.department.id = ?1 AND e.isActive = true ORDER BY e.createdAt DESC")
    List<Expense> findAllByDepartmentId(Long deptId);

}
