package app.core.cache;

import java.time.Duration;
import javax.cache.Cache;
import javax.cache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
class CacheConfiguration {

    static void createCache(CacheManager cm, String cacheName, javax.cache.configuration.Configuration<Object, Object> configuration) {
        Cache<Object, Object> cache = cm.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        } else {
            cm.createCache(cacheName, configuration);
        }
    }

    private final javax.cache.configuration.Configuration<Object, Object> configuration;

    public CacheConfiguration(CacheProperties properties) {
        this.configuration = Eh107Configuration.fromEhcacheCacheConfiguration(CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class, Object.class, ResourcePoolsBuilder.heap(properties.getMaxEntries()))
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(properties.getTimeToLiveSeconds()))).build());
    }

    @Bean
    public JCacheManagerCustomizer cacheManagerCustomizer() {
        return cm -> {
            createCache(cm, app.core.user.UserRepository.USERS_BY_LOGIN_CACHE);
            createCache(cm, app.core.user.UserRepository.USERS_BY_EMAIL_CACHE);
            createCache(cm, app.core.user.User.class.getName());
            createCache(cm, app.core.user.Authority.class.getName());
            createCache(cm, app.core.user.User.class.getName() + ".authorities");
        };
    }

    public void createCache(CacheManager cm, String cacheName) {
        createCache(cm, cacheName, configuration);
    }
}