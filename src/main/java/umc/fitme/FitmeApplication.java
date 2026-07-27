package umc.fitme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FitmeApplication {

	public static void main(String[] args) {
		SpringApplication.run(FitmeApplication.class, args);
	}

}
