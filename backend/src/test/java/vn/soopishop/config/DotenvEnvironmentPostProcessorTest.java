package vn.soopishop.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DotenvEnvironmentPostProcessorTest {

    @Test
    void parsesLocalDockerDatabaseUrl() {
        var parsed = DotenvEnvironmentPostProcessor.parseDatabaseUrl(
                "postgresql://soopi_user:soopi_dev_password_change_me@postgres:5432/soopi_shop"
        );

        assertEquals("jdbc:postgresql://postgres:5432/soopi_shop", parsed.jdbcUrl());
        assertEquals("soopi_user", parsed.username());
        assertEquals("soopi_dev_password_change_me", parsed.password());
    }

    @Test
    void parsesNeonCloudUrlWithSslAndNormalizesChannelBinding() {
        var parsed = DotenvEnvironmentPostProcessor.parseDatabaseUrl(
                "postgresql://neondb_owner:npg_Abc%40123%3Axyz@ep-cool-darkness-123456-pooler.ap-southeast-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require"
        );

        assertEquals(
                "jdbc:postgresql://ep-cool-darkness-123456-pooler.ap-southeast-1.aws.neon.tech:5432/neondb?sslmode=require&channelBinding=require",
                parsed.jdbcUrl()
        );
        assertEquals("neondb_owner", parsed.username());
        assertEquals("npg_Abc@123:xyz", parsed.password());
    }

    @Test
    void populatesBothDatasourceAndFlywayDirectUrl() {
        MockEnvironment env = new MockEnvironment()
                .withProperty("DATABASE_URL", "postgresql://owner:secret@ep-demo-pooler.ap-southeast-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require")
                .withProperty("FLYWAY_DATABASE_URL", "postgresql://owner:secret@ep-demo.ap-southeast-1.aws.neon.tech/neondb?sslmode=require");

        new DotenvEnvironmentPostProcessor().postProcessEnvironment(env, null);

        assertEquals(
                "jdbc:postgresql://ep-demo-pooler.ap-southeast-1.aws.neon.tech:5432/neondb?sslmode=require&channelBinding=require",
                env.getProperty("spring.datasource.url")
        );
        assertEquals("owner", env.getProperty("spring.datasource.username"));
        assertEquals("secret", env.getProperty("spring.datasource.password"));

        assertEquals(
                "jdbc:postgresql://ep-demo.ap-southeast-1.aws.neon.tech:5432/neondb?sslmode=require",
                env.getProperty("spring.flyway.url")
        );
        assertEquals("owner", env.getProperty("spring.flyway.user"));
        assertEquals("secret", env.getProperty("spring.flyway.password"));
    }

    @Test
    void rejectsNonJdbcUrlWithoutCredentials() {
        assertThrows(IllegalStateException.class, () ->
                DotenvEnvironmentPostProcessor.parseDatabaseUrl("postgresql://localhost:5432/soopi_shop")
        );
    }
}
