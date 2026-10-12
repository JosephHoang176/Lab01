package com.example.ops_service;

import com.example.Entity.Reports;
import com.example.Repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class OpsServiceApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ReportRepository reportRepository;

	@Autowired
	private Environment environment;

	@BeforeEach
	void seedReport() {
		reportRepository.deleteAll();
		Reports report = new Reports();
		report.setReportDate(LocalDate.of(2026, 10, 9));
		report.setCurrency("VND");
		report.setOrdersCreatedCount(10);
		report.setOrdersPaidCount(8);
		report.setOrdersFulfilledCount(6);
		report.setOrdersCancelledCount(1);
		report.setPendingPaymentCountEod(2);
		report.setOrdersCreatedAmount(new BigDecimal("1500000.00"));
		report.setPaymentsReceivedAmount(new BigDecimal("1200000.00"));
		reportRepository.save(report);
	}

	@Test
	void contextLoads() {
	}

	@Test
	void dailyReportsReturnsSeededReportAndCorrelationId() throws Exception {
		mockMvc.perform(get("/reports/daily")
						.header("X-Correlation-ID", "test-correlation-id"))
				.andExpect(status().isOk())
				.andExpect(header().string("X-Correlation-ID", "test-correlation-id"))
				.andExpect(jsonPath("$[0].reportDate").value("2026-10-09"))
				.andExpect(jsonPath("$[0].currency").value("VND"))
				.andExpect(jsonPath("$[0].ordersCreatedCount").value(10));
	}

	@Test
	void missingReportReturnsCompatibleErrorResponse() throws Exception {
		mockMvc.perform(get("/reports/999999")
						.header("X-Correlation-ID", "error-correlation-id"))
				.andExpect(status().isNotFound())
				.andExpect(header().string("X-Correlation-ID", "error-correlation-id"))
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.path").value("/reports/999999"))
				.andExpect(jsonPath("$.correlationId").value("error-correlation-id"));
	}

	@Test
	void testDatabaseIsIsolatedFromCoreDatabase() {
		String url = environment.getRequiredProperty("spring.datasource.url");
		org.assertj.core.api.Assertions.assertThat(url)
				.contains("ops-service-test")
				.doesNotContain("CoreServiceDb");
	}
}
