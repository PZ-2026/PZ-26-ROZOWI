package pl.edu.ur.blokur.config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

class WebConfigTest {

    @Test
    void shouldRegisterUploadsResourceHandler() {
        // Given
        WebConfig webConfig = new WebConfig();
        ReflectionTestUtils.setField(webConfig, "uploadDir", "uploads");

        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        ResourceHandlerRegistration registration = mock(ResourceHandlerRegistration.class);

        when(registry.addResourceHandler("/uploads/**")).thenReturn(registration);

        // When
        webConfig.addResourceHandlers(registry);

        // Then
        verify(registry).addResourceHandler("/uploads/**");
    }
}
