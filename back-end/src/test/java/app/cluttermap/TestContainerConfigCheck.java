package app.cluttermap;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(initializers = TestcontainersInitializer.class)
@ActiveProfiles("test")
public class TestContainerConfigCheck {

    @Value("${spring.datasource.url}")
    private String dataSourceUrl;

    @Test
    void verifyTestContainerConfig() {
        System.out.println("Current DataSource URL: " + dataSourceUrl);
        assertTrue(dataSourceUrl.contains("jdbc:postgresql"));
    }
}
