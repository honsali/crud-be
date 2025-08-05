package app.core.config;

import java.time.format.DateTimeFormatter;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

@Configuration
public class DateTimeFormatConfiguration implements WebMvcConfigurer {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        return new Jackson2ObjectMapperBuilderCustomizer() {
            @Override
            public void customize(Jackson2ObjectMapperBuilder builder) {

                builder//
                        .createXmlMapper(false) //
                        .indentOutput(true) //
                        .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) //
                        .serializers(new LocalDateSerializer(DateTimeFormatter.ofPattern("dd/MM/yyyy")))//
                        .deserializers(new LocalDateDeserializer(DateTimeFormatter.ofPattern("dd/MM/yyyy"))) //
                        .modules(new JavaTimeModule(), new ParameterNamesModule(), new Jdk8Module());
            }
        };
    }
}
