package com.boojet.boot_api;

import javax.sql.DataSource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

import lombok.extern.java.Log;

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
		final JdbcTemplate restTemplate = new JdbcTemplate(dataSource);
		restTemplate.execute("SELECT 1");
	}
}
