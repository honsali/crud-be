package app.core.cors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsFilterConfiguration {

    private final Logger log = LoggerFactory.getLogger(CorsFilterConfiguration.class);

    private final CorsConfiguration corsConfiguration;

    public CorsFilterConfiguration(CorsConfiguration corsConfiguration) {
        this.corsConfiguration = corsConfiguration;
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        if (!CollectionUtils.isEmpty(corsConfiguration.getAllowedOrigins()) || !CollectionUtils.isEmpty(corsConfiguration.getAllowedOriginPatterns())) {
            log.debug("Registering CORS filter");
            source.registerCorsConfiguration("/api/**", corsConfiguration);
        }
        return new CorsFilter(source);
    }
}
