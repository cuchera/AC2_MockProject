package br.com.valueprojects.mock_spring;

import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

class MockSpringApplicationTest {

    @Test
    void delegaInicializacaoAoSpringApplication() {
        String[] argumentos = {"--spring.main.web-application-type=none"};
        try (MockedStatic<SpringApplication> spring = mockStatic(SpringApplication.class)) {
            Mock_SpringApplication.main(argumentos);
            spring.verify(() -> SpringApplication.run(Mock_SpringApplication.class, argumentos));
        }
    }
}
