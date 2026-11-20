package vn.soopishop.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads DATABASE_URL from .env (or system environment) and translates it
 * into Spring DataSource JDBC properties without hardcoding credentials.
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        String databaseUrl = environment.getProperty("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank()) {
            databaseUrl = dotenv.get("DATABASE_URL");
        }
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return;
        }

        URI uri = URI.create(databaseUrl.replaceFirst("^jdbc:", ""));
        String userInfo = uri.getUserInfo();
        if (userInfo == null || !userInfo.contains(":")) {
            throw new IllegalStateException("DATABASE_URL must include username and password");
        }

        String[] parts = userInfo.split(":", 2);
        String username = parts[0];
        String password = parts[1];
        int port = uri.getPort() > 0 ? uri.getPort() : 5432;
        String jdbcUrl = String.format("jdbc:postgresql://%s:%d%s", uri.getHost(), port, uri.getPath());

        Map<String, Object> props = new HashMap<>();
        props.put("spring.datasource.url", jdbcUrl);
        props.put("spring.datasource.username", username);
        props.put("spring.datasource.password", password);

        String seedPassword = dotenv.get("SEED_DEFAULT_PASSWORD");
        if (seedPassword != null && !seedPassword.isBlank()) {
            props.put("SEED_DEFAULT_PASSWORD", seedPassword);
        }

        environment.getPropertySources().addFirst(new MapPropertySource("dotenvDatabaseUrl", props));
    }
}
