package com.lain.config.cache.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Locale;
import java.util.TimeZone;

import static cn.hutool.core.date.DatePattern.NORM_DATETIME_PATTERN;

/**
 * 此时定义的序列化操作表示可以序列化所有类的对象，当然，这个对象所在的类一定要实现序列化接口
 *
 *
 * @date 2019-08-06 10:42
 */
public class RedisObjectSerializer extends Jackson2JsonRedisSerializer<Object> {
    public RedisObjectSerializer() {
        super(buildObjectMapper(), Object.class);
    }

    private static ObjectMapper buildObjectMapper() {
        ObjectMapper objectMapper = new JacksonObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.WRAPPER_ARRAY);
        return objectMapper;
    }

    public static class JacksonObjectMapper extends ObjectMapper {
        private static final long serialVersionUID = 1L;

        public JacksonObjectMapper() {
            super();
            // 参考BaseConfig
            super.setLocale(Locale.CHINA)
                    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                    .setTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()))
                    .setDateFormat(new SimpleDateFormat(NORM_DATETIME_PATTERN, Locale.CHINA))
                    .configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true)
                    .configure(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER.mappedFeature(), true)
                    .findAndRegisterModules()
                    .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
                    .getDeserializationConfig().withoutFeatures(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            super.registerModule(new OSJacksonModule());
            super.findAndRegisterModules();
        }
    }
}
