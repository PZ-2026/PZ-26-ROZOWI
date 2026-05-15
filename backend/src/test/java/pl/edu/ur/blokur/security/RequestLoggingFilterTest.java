package pl.edu.ur.blokur.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.FilterChain;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@DisplayName("RequestLoggingFilter — strukturyzowane logowanie żądań HTTP")
class RequestLoggingFilterTest {

    private RequestLoggingFilter filter;
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        filter = new RequestLoggingFilter();

        Logger logger = (Logger) LoggerFactory.getLogger(RequestLoggingFilter.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        Logger logger = (Logger) LoggerFactory.getLogger(RequestLoggingFilter.class);
        logger.detachAppender(listAppender);
        MDC.clear();
    }

    private List<String> capturedArgs() {
        assertThat(listAppender.list).hasSize(1);
        return Arrays.stream(listAppender.list.get(0).getArgumentArray())
                .map(Object::toString)
                .toList();
    }

    @Nested
    @DisplayName("przekazywanie żądania dalej")
    class FilterChaining {

        @Test
        @DisplayName("wywołuje następny filtr w łańcuchu")
        void delegatesToFilterChain() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/tickets");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            filter.doFilterInternal(request, response, chain);

            verify(chain).doFilter(request, response);
        }

        @Test
        @DisplayName("loguje dokładnie jedno zdarzenie na żądanie")
        void logsExactlyOneEventPerRequest() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            filter.doFilterInternal(request, response, chain);

            assertThat(listAppender.list).hasSize(1);
        }
    }

    @Nested
    @DisplayName("czyszczenie MDC")
    class MdcCleanup {

        @Test
        @DisplayName("usuwa userId z MDC po zakończeniu żądania")
        void removesMdcUserIdAfterRequest() throws Exception {
            MDC.put("userId", "jan@example.com");
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/buildings");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            filter.doFilterInternal(request, response, chain);

            assertThat(MDC.get("userId")).isNull();
        }

        @Test
        @DisplayName("MDC jest puste po żądaniu bez tokenu JWT")
        void mdcRemainsEmptyForUnauthenticatedRequest() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/categories");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            filter.doFilterInternal(request, response, chain);

            assertThat(MDC.get("userId")).isNull();
        }
    }

    @Nested
    @DisplayName("logowanie pól żądania jako StructuredArguments")
    class RequestLogging {

        @Test
        @DisplayName("loguje metodę HTTP i ścieżkę żądania")
        void logsMethodAndPath() throws Exception {
            MockHttpServletRequest request =
                    new MockHttpServletRequest("DELETE", "/api/tickets/42");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            filter.doFilterInternal(request, response, chain);

            List<String> args = capturedArgs();
            assertThat(args).anyMatch(s -> s.contains("DELETE"));
            assertThat(args).anyMatch(s -> s.contains("/api/tickets/42"));
        }

        @Test
        @DisplayName("loguje userId z MDC gdy użytkownik jest zalogowany")
        void logsUserIdFromMdc() throws Exception {
            MDC.put("userId", "manager@blokur.pl");
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/buildings");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            filter.doFilterInternal(request, response, chain);

            assertThat(capturedArgs()).anyMatch(s -> s.contains("manager@blokur.pl"));
        }

        @Test
        @DisplayName("loguje 'anonymous' gdy brak userId w MDC")
        void logsAnonymousWhenNoMdcUserId() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            filter.doFilterInternal(request, response, chain);

            assertThat(capturedArgs()).anyMatch(s -> s.contains("anonymous"));
        }

        @Test
        @DisplayName("loguje kod statusu odpowiedzi HTTP")
        void logsResponseStatus() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/tickets");
            MockHttpServletResponse response = new MockHttpServletResponse();
            response.setStatus(403);
            FilterChain chain = mock(FilterChain.class);

            filter.doFilterInternal(request, response, chain);

            assertThat(capturedArgs()).anyMatch(s -> s.contains("403"));
        }

        @Test
        @DisplayName("loguje pole durationMs jako nieujemną liczbę całkowitą")
        void logsDurationMs() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/tickets");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            filter.doFilterInternal(request, response, chain);

            assertThat(capturedArgs()).anyMatch(s -> s.contains("durationMs"));
        }
    }
}
