package com.boojet.boot_api;

import javax.sql.DataSource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;
import org.springframework.jdbc.core.JdbcTemplate;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.extern.java.Log;

@OpenAPIDefinition(
	info = @Info(
		title = "Boojet API",
		version = "v1",
		description = "Budgeting API built with Java and Spring Boot. It exposes a REST API to track and record transactions for multiple accounts and display statistics based on the input data. Allows the user to define plans based on their expected income. Also includes a minimal static web page for quick interaction. "
	)
)
@EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)
@Log
@SpringBootApplication
public class BootApiApplication implements CommandLineRunner {
	private final DataSource dataSource;

	public BootApiApplication(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public static void main(String[] args) {
		SpringApplication.run(BootApiApplication.class, args);
	}

	@Override
	public void run(String... args){
		log.info("DataSource: " + dataSource.toString());
		final JdbcTemplate jdbc = new JdbcTemplate(dataSource);
		jdbc.execute("SELECT 1");

			// Ensure pay_type check constraint allows ANNUAL
			try {
				jdbc.execute("ALTER TABLE income_plans DROP CONSTRAINT IF EXISTS income_plans_pay_type_check");
				jdbc.execute("ALTER TABLE income_plans ADD CONSTRAINT income_plans_pay_type_check CHECK (pay_type IN ('HOURLY','WEEKLY','BIWEEKLY','MONTHLY','ANNUAL'))");
				log.info("Verified/updated income_plans_pay_type_check constraint");
			} catch (Exception e) {
				log.warning("Could not adjust income_plans pay_type constraint: " + e.getMessage());
			}

			// Ensure a default user with id=1 exists (used by IncomePlanServiceImpl)
			try {
				jdbc.execute("CREATE TABLE IF NOT EXISTS users (id BIGINT PRIMARY KEY, username VARCHAR(255))");
				jdbc.execute("INSERT INTO users (id, username) VALUES (1, 'default') ON CONFLICT (id) DO NOTHING");
				log.info("Verified default user (id=1)");
			} catch (Exception e) {
				log.warning("Could not ensure default user: " + e.getMessage());
			}
		}
	}
