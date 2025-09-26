package com.boojet.boot_api;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import com.boojet.boot_api.UI.ConsoleUI;

@SpringBootApplication
public class BootApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(BootApiApplication.class, args);
	}

	// Optional: run your existing Console UI when you use the 'cli' profile
	@Bean @Profile("cli")
	CommandLineRunner runCli() {
		return args -> {
		var manager = new TransactionManager(SaveMode.MANUAL);
		var ui = new ConsoleUI(manager); // matches your package com.boojet.UI
		ui.run();
		System.exit(0); // exit after CLI finishes so Boot doesn't keep the server alive
		};
	}
}
