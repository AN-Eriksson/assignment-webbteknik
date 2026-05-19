package me.andreaseriksson.ufodashboard.config;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.caffeine.CaffeineCacheManager;

/**
 * Cache configuration for the application using Caffeine as an in-memory cache provider.
 *
 * This configuration exposes a CacheManager bean that provides named caches used by
 * the application: "sightings", "locations" and "shapes". Each cache is configured
 * with a short time-to-live and a maximum size to avoid unbounded memory growth.
 *
 * Notes:
 * - expireAfterWrite(5 minutes) is used to keep cached data reasonably fresh while
 *   still reducing load on the upstream API.
 * - maximumSize(5000) limits memory footprint; adjust if your workload requires larger
 *   caches.
 */
@Configuration
public class CacheConfig {

    /**
     * Create and configure the CacheManager backed by Caffeine.
     *
     * The returned CacheManager defines three named caches: sightings, locations and shapes.
     * Cached entries expire 5 minutes after write and the cache is sized to a maximum of 5000
     * entries per cache to avoid excessive memory usage.
     *
     * @return configured CacheManager instance
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("sightings", "locations", "shapes");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(5000));
        return cacheManager;
    }
}


