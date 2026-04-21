package com.authservice.app.security;

import com.auditlog.api.AuditSink;
import io.github.jho951.ratelimiter.core.RateLimitDecision;
import io.github.jho951.ratelimiter.core.RateLimitKey;
import io.github.jho951.ratelimiter.core.RateLimitPlan;
import io.github.jho951.ratelimiter.spi.RateLimiter;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@Profile({"prod", "production", "live"})
public class AuthPlatformOperationalConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AuthPlatformOperationalConfiguration.class);

    @Bean
    public AuditSink authGovernanceAuditSink() {
        return event -> log.info(
            "governanceAudit eventId={} action={} resourceType={} resourceId={} result={} reason={} requestId={} traceId={}",
            event.getEventId(),
            event.getAction(),
            event.getResourceType(),
            event.getResourceId(),
            event.getResult(),
            event.getReason(),
            event.getRequestId(),
            event.getTraceId()
        );
    }

    @Bean
    public RateLimiter platformSecurityRateLimiter(
        StringRedisTemplate redisTemplate,
        @Value("${PLATFORM_SECURITY_RATE_LIMIT_REDIS_PREFIX:platform-security:rate-limit:auth-service:}")
        String keyPrefix
    ) {
        return new RedisFixedWindowRateLimiter(redisTemplate, keyPrefix, Clock.systemUTC());
    }

    private static final class RedisFixedWindowRateLimiter implements RateLimiter {
        private final StringRedisTemplate redisTemplate;
        private final String keyPrefix;
        private final Clock clock;

        private RedisFixedWindowRateLimiter(
            StringRedisTemplate redisTemplate,
            String keyPrefix,
            Clock clock
        ) {
            this.redisTemplate = redisTemplate;
            this.keyPrefix = keyPrefix == null ? "" : keyPrefix;
            this.clock = clock;
        }

        @Override
        public RateLimitDecision tryAcquire(RateLimitKey key, long permits, RateLimitPlan plan) {
            long windowSeconds = Math.max(1L, (long) Math.ceil(plan.getCapacity() / plan.getRefillTokensPerSecond()));
            long nowSeconds = clock.instant().getEpochSecond();
            long windowIndex = nowSeconds / windowSeconds;
            long windowEndSeconds = (windowIndex + 1L) * windowSeconds;
            String redisKey = keyPrefix + key.asString() + ":" + windowIndex;

            Long current = redisTemplate.opsForValue().increment(redisKey, permits);
            if (current == null) {
                return RateLimitDecision.deny(0L, windowSeconds * 1000L);
            }
            if (current == permits) {
                redisTemplate.expire(redisKey, java.time.Duration.ofSeconds(windowSeconds + 1L));
            }

            long remaining = Math.max(0L, plan.getCapacity() - current);
            if (current <= plan.getCapacity()) {
                return RateLimitDecision.allow(remaining);
            }

            long retryAfterMillis = Math.max(0L, (windowEndSeconds - nowSeconds) * 1000L);
            return RateLimitDecision.deny(remaining, retryAfterMillis);
        }
    }
}
