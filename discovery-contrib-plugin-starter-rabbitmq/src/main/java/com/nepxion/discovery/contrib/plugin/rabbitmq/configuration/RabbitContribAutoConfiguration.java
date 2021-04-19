package com.nepxion.discovery.contrib.plugin.rabbitmq.configuration;


import com.nepxion.discovery.contrib.plugin.rabbitmq.RabbitManager;
import com.nepxion.discovery.contrib.plugin.rabbitmq.manage.SpringBoot1GrayRabbitManager;
import com.nepxion.discovery.contrib.plugin.rabbitmq.processor.RabbitContribProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouzx
 */
@Configuration
public class RabbitContribAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RabbitContribProcessor rabbitMQContribProcessor(RabbitManager grayRabbitManager) {
        return new RabbitContribProcessor(grayRabbitManager);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(org.springframework.amqp.rabbit.core.ChannelAwareMessageListener.class)
    public SpringBoot1GrayRabbitManager springBoot1GrayRabbitManager(RabbitTemplate rabbitTemplate) {
        return new SpringBoot1GrayRabbitManager(rabbitTemplate);
    }

}
