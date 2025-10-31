package game.Happy_Zombie_Farm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@ConfigurationPropertiesScan("game.Happy_Zombie_Farm.config")
@EnableConfigurationProperties
public class HappyZombieFarmApplication {

	public static void main(String[] args) {
		SpringApplication.run(HappyZombieFarmApplication.class, args);
	}

}
