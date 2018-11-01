package org.jys.githook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class GithookApplication {

	public static void main(String[] args) {
		SpringApplication.run(GithookApplication.class, args);
	}
}
