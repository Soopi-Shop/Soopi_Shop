package vn.soopishop;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import vn.soopishop.seed.DatabaseSeeder;
import vn.soopishop.verify.DatabaseVerifier;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class SoopiShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(SoopiShopApplication.class, args);
    }

    @Bean
    CommandLineRunner cliRunner(DatabaseSeeder seeder, DatabaseVerifier verifier) {
        return args -> {
            List<String> argList = Arrays.asList(args);
            if (argList.contains("--seed")) {
                seeder.seed();
            }
            if (argList.contains("--verify")) {
                verifier.verify();
            }
        };
    }
}
