package vn.soopishop.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Loads DATABASE_URL (and optional FLYWAY_DATABASE_URL for Neon direct endpoint)
 * from system environment or .env and translates them into Spring DataSource /
 * Flyway JDBC properties without hardcoding credentials.
 *
 * Supports both local PostgreSQL Docker URIs and Neon Cloud PostgreSQL URIs
 * (preserving SSL query parameters and normalizing libpq's channel_binding to
 * pgJDBC's camelCase channelBinding).
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    record ParsedJdbcUrl(String jdbcUrl, String username, String password) {}

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        Map<String, Object> props = new HashMap<>();

        String databaseUrl = resolveValue(environment, dotenv, "DATABASE_URL");
        if (databaseUrl != null && !databaseUrl.isBlank()) {
            ParsedJdbcUrl parsed = parseDatabaseUrl(databaseUrl);
            props.put("spring.datasource.url", parsed.jdbcUrl());
            if (parsed.username() != null) {
                props.put("spring.datasource.username", parsed.username());
            }
            if (parsed.password() != null) {
                props.put("spring.datasource.password", parsed.password());
            }
        }

        String flywayUrl = resolveValue(environment, dotenv, "FLYWAY_DATABASE_URL");
        if (flywayUrl != null && !flywayUrl.isBlank()) {
            ParsedJdbcUrl parsedFlyway = parseDatabaseUrl(flywayUrl);
            props.put("spring.flyway.url", parsedFlyway.jdbcUrl());
            if (parsedFlyway.username() != null) {
                props.put("spring.flyway.user", parsedFlyway.username());
            }
            if (parsedFlyway.password() != null) {
                props.put("spring.flyway.password", parsedFlyway.password());
            }
        }

        copyDotenvIfAbsent(environment, dotenv, props, "DB_POOL_MIN_IDLE");
        copyDotenvIfAbsent(environment, dotenv, props, "DB_POOL_MAX_SIZE");

        if (!props.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource("dotenvDatabaseUrl", props));
        }
    }

    private static void copyDotenvIfAbsent(
            ConfigurableEnvironment environment,
            Dotenv dotenv,
            Map<String, Object> props,
            String key
    ) {
        String existing = environment.getProperty(key);
        if (existing != null && !existing.isBlank()) {
            return;
        }
        String fromDotenv = dotenv.get(key);
        if (fromDotenv != null && !fromDotenv.isBlank()) {
            props.put(key, fromDotenv.trim());
        }
    }

    private static String resolveValue(ConfigurableEnvironment environment, Dotenv dotenv, String key) {
        String value = environment.getProperty(key);
        if (value == null || value.isBlank()) {
            value = dotenv.get(key);
        }
        return value;
    }

    static ParsedJdbcUrl parseDatabaseUrl(String rawUrl) {
        String normalized = rawUrl.trim().replaceFirst("^jdbc:", "");
        URI uri = URI.create(normalized);

        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new IllegalStateException("DATABASE_URL must include a valid host");
        }

        String username = null;
        String password = null;
        String rawUserInfo = uri.getRawUserInfo();
        if (rawUserInfo != null && !rawUserInfo.isBlank()) {
            if (!rawUserInfo.contains(":")) {
                throw new IllegalStateException("DATABASE_URL must include username and password");
            }
            String[] parts = rawUserInfo.split(":", 2);
            username = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            password = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
        } else if (!rawUrl.trim().startsWith("jdbc:")) {
            throw new IllegalStateException("DATABASE_URL must include username and password");
        }

        int port = uri.getPort() > 0 ? uri.getPort() : 5432;
        String path = uri.getRawPath() != null ? uri.getRawPath() : "";
        String query = normalizeJdbcQuery(uri.getRawQuery());

        String jdbcUrl = query.isEmpty()
                ? String.format("jdbc:postgresql://%s:%d%s", uri.getHost(), port, path)
                : String.format("jdbc:postgresql://%s:%d%s?%s", uri.getHost(), port, path, query);

        return new ParsedJdbcUrl(jdbcUrl, username, password);
    }

    static String normalizeJdbcQuery(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return "";
        }
        return Arrays.stream(rawQuery.split("&"))
                .filter(param -> !param.isBlank())
                .map(param -> {
                    int eq = param.indexOf('=');
                    if (eq <= 0) {
                        return param;
                    }
                    String key = param.substring(0, eq);
                    String val = param.substring(eq + 1);
                    if ("channel_binding".equals(key)) {
                        return "channelBinding=" + val;
                    }
                    return param;
                })
                .collect(Collectors.joining("&"));
    }
}
