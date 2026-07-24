package com.stockpilot.api.stock.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Purpose: Redis and Spring Cache configuration.
 * Responsibilities: Registers CacheManager with dynamic TTLs, Jackson serializers, logging decorators, and error fallbacks.
 * Dependencies: RedisConnectionFactory, application.yml.
 * Flow: Configures RedisCacheManager with customized TTL scopes, wrapping caches in LoggingCache decorator.
 */
@Configuration
@EnableCaching
public class RedisCacheConfig implements CachingConfigurer {
    private static final Logger log = LoggerFactory.getLogger(RedisCacheConfig.class);

    private final int quoteTtl;
    private final int historyTtl;
    private final int fundamentalsTtl;

    public RedisCacheConfig(
            @Value("${stockpilot.cache.ttl.quoteSeconds}") int quoteTtl,
            @Value("${stockpilot.cache.ttl.historySeconds}") int historyTtl,
            @Value("${stockpilot.cache.ttl.fundamentalsSeconds}") int fundamentalsTtl) {
        this.quoteTtl = quoteTtl;
        this.historyTtl = historyTtl;
        this.fundamentalsTtl = fundamentalsTtl;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Configure default typing to EVERYTHING to ensure type metadata is written for collections and generics
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();
        objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(60))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("stock:quote", defaultConfig.entryTtl(Duration.ofSeconds(quoteTtl)));
        cacheConfigurations.put("stock:history", defaultConfig.entryTtl(Duration.ofSeconds(historyTtl)));
        cacheConfigurations.put("stock:fundamentals", defaultConfig.entryTtl(Duration.ofSeconds(fundamentalsTtl)));

        // Market Data Module Caches with Intelligent TTLs
        cacheConfigurations.put("market:quote", defaultConfig.entryTtl(Duration.ofSeconds(30)));       // 30 seconds
        cacheConfigurations.put("market:search", defaultConfig.entryTtl(Duration.ofSeconds(300)));     // 5 minutes
        cacheConfigurations.put("market:history", defaultConfig.entryTtl(Duration.ofSeconds(600)));    // 10 minutes
        cacheConfigurations.put("market:profile", defaultConfig.entryTtl(Duration.ofSeconds(3600)));   // 1 hour
        cacheConfigurations.put("market:gainers", defaultConfig.entryTtl(Duration.ofSeconds(300)));    // 5 minutes
        cacheConfigurations.put("market:losers", defaultConfig.entryTtl(Duration.ofSeconds(300)));     // 5 minutes
        cacheConfigurations.put("market:most-active", defaultConfig.entryTtl(Duration.ofSeconds(300)));// 5 minutes
        cacheConfigurations.put("market:indices", defaultConfig.entryTtl(Duration.ofSeconds(300)));    // 5 minutes
        cacheConfigurations.put("market:trending", defaultConfig.entryTtl(Duration.ofSeconds(300)));   // 5 minutes

        // Portfolio Intelligence Module Caches with Intelligent TTLs
        cacheConfigurations.put("portfolio:summary", defaultConfig.entryTtl(Duration.ofSeconds(60)));    // 60 seconds
        cacheConfigurations.put("portfolio:analytics", defaultConfig.entryTtl(Duration.ofSeconds(300)));// 5 minutes
        cacheConfigurations.put("portfolio:history", defaultConfig.entryTtl(Duration.ofSeconds(600)));  // 10 minutes

        return new RedisCacheManager(
                RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory),
                defaultConfig,
                cacheConfigurations
        ) {
            @Override
            public Cache getCache(String name) {
                Cache cache = super.getCache(name);
                return cache != null ? new LoggingCache(cache) : null;
            }
        };
    }

    @Override
    @Bean
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.error("Redis Cache GET failure on cache '{}' for key '{}': {}. Bypassing cache to live service.", 
                        cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.error("Redis Cache PUT failure on cache '{}' for key '{}': {}. Bypassing cache to live service.", 
                        cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.error("Redis Cache EVICT failure on cache '{}' for key '{}': {}. Bypassing cache to live service.", 
                        cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.error("Redis Cache CLEAR failure on cache '{}': {}. Bypassing cache to live service.", 
                        cache.getName(), exception.getMessage());
            }
        };
    }

    /**
     * Decorator class for logging Spring Cache hits, misses, stores, and latency values.
     */
    private static class LoggingCache implements Cache {
        private static final Logger cacheLog = LoggerFactory.getLogger(LoggingCache.class);
        private final Cache delegate;

        public LoggingCache(Cache delegate) {
            this.delegate = delegate;
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public Object getNativeCache() {
            return delegate.getNativeCache();
        }

        @Override
        public ValueWrapper get(Object key) {
            long start = System.currentTimeMillis();
            ValueWrapper wrapper = delegate.get(key);
            long latency = System.currentTimeMillis() - start;
            if (wrapper != null) {
                cacheLog.info("Cache HIT - Cache: {}, Key: {}, Latency: {}ms", getName(), key, latency);
            } else {
                cacheLog.info("Cache MISS - Cache: {}, Key: {}, Latency: {}ms", getName(), key, latency);
            }
            return wrapper;
        }

        @Override
        public <T> T get(Object key, Class<T> type) {
            long start = System.currentTimeMillis();
            T value = delegate.get(key, type);
            long latency = System.currentTimeMillis() - start;
            if (value != null) {
                cacheLog.info("Cache HIT - Cache: {}, Key: {}, Latency: {}ms", getName(), key, latency);
            } else {
                cacheLog.info("Cache MISS - Cache: {}, Key: {}, Latency: {}ms", getName(), key, latency);
            }
            return value;
        }

        @Override
        public <T> T get(Object key, Callable<T> valueLoader) {
            long start = System.currentTimeMillis();
            T value = delegate.get(key, valueLoader);
            long latency = System.currentTimeMillis() - start;
            cacheLog.info("Cache GET/Loader - Cache: {}, Key: {}, Latency: {}ms", getName(), key, latency);
            return value;
        }

        @Override
        public void put(Object key, Object value) {
            long start = System.currentTimeMillis();
            delegate.put(key, value);
            long latency = System.currentTimeMillis() - start;
            cacheLog.info("Cache STORE - Cache: {}, Key: {}, Latency: {}ms", getName(), key, latency);
        }

        @Override
        public void evict(Object key) {
            delegate.evict(key);
            cacheLog.info("Cache EVICT - Cache: {}, Key: {}", getName(), key);
        }

        @Override
        public void clear() {
            delegate.clear();
            cacheLog.info("Cache CLEAR - Cache: {}", getName());
        }
    }
}
