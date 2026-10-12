package com.example.Repository;

import com.example.Entity.Reports;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Reports, Long> {

    Optional<Reports> findByReportDateAndCurrency(LocalDate reportDate, String currency);

    List<Reports> findByReportDateBetweenOrderByReportDateDesc(
            LocalDate fromDate,
            LocalDate toDate
    );

    List<Reports> findAllByOrderByReportDateDescGeneratedAtDesc();

    List<Reports> findByCurrencyOrderByReportDateDesc(String currency);
}
