package com.authservice.app.common.redis.config;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

/**
 *
 */
@Configuration
@RequiredArgsConstructor
public class RedisConfig {

	@Value("${REDIS_HOST:localhost}")
	private String redisHost;

	@Value("${REDIS_PORT:6379}")
	private int redisPort;

	@Value("${REDIS_PASSWORD:}")
	private String redisPassword;

	/**
	 *
	 * @return new LettuceConnectionFactory(config)
	 */
	@Bean
	public LettuceConnectionFactory redisConnectionFactory() {
		RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
		config.setHostName(redisHost);
		config.setPort(redisPort);
		if (!redisPassword.isBlank()) {
			config.setPassword(RedisPassword.of(redisPassword));
		}
		return new LettuceConnectionFactory(config);
	}

	/**
	 * 	Redis 데이터 저장/조회
 	 */
	@Bean
	public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(redisConnectionFactory);

		// key 직렬화
		template.setKeySerializer(new StringRedisSerializer());
		template.setHashKeySerializer(new StringRedisSerializer());

		// value 직렬화
		template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
		template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

		template.afterPropertiesSet();
		return template;
	}
}
