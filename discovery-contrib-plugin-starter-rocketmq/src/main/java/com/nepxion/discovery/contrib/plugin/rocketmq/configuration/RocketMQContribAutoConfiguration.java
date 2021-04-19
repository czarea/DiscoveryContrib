package com.nepxion.discovery.contrib.plugin.rocketmq.configuration;

/**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 *
 * @author Haojun Ren
 * @version 1.0
 */

import com.nepxion.discovery.contrib.plugin.rocketmq.processor.RocketMQContribProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RocketMQContribAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RocketMQContribProcessor rocketMQContribProcessor() {
        return new RocketMQContribProcessor();
    }
}
