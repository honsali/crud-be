package app.core;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.boot.jackson.autoconfigure.JacksonProperties;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.annotation.JsonFormat;

@Configuration(proxyBeanMethods = false)
class JsonConfiguration {

    @Bean
    JsonMapperBuilderCustomizer localDateJsonFormat(JacksonProperties jacksonProperties) {
        String pattern = jacksonProperties.getDateFormat();
        if (pattern == null || pattern.isBlank()) {
            throw new IllegalStateException("spring.jackson.date-format must be configured.");
        }

        try {
            DateTimeFormatter.ofPattern(pattern);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("spring.jackson.date-format must be a valid date pattern.", exception);
        }

        JsonFormat.Value format = JsonFormat.Value.forPattern(pattern);
        return builder -> builder.withConfigOverride(LocalDate.class, override -> override.setFormat(format));
    }
}
