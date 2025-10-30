package com.example.hirelink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = "com.example.hirelink")
@EnableJpaRepositories("com.example.hirelink.Repositories")
@EntityScan("com.example.hirelink.User")
public class HirelinkApplication {
	public static void main(String[] args) {
		SpringApplication.run(HirelinkApplication.class, args);
	}
}
