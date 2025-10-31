package game.Happy_Zombie_Farm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(exclude = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
@ConfigurationPropertiesScan("game.Happy_Zombie_Farm.config")
public class HappyZombieFarmApplication {

	public static void main(String[] args) {
		SpringApplication.run(HappyZombieFarmApplication.class, args);
	}

}
