package dev.ambll.cloudclip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CloudclipApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudclipApplication.class, args);
	}

}
