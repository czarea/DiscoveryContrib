package com.nepxion.discovery.contrib.plugin.rabbitmq.configuration;


import com.nepxion.discovery.contrib.plugin.rabbitmq.RabbitManager;
import com.nepxion.discovery.contrib.plugin.rabbitmq.manage.SpringBoot2GrayRabbitManager;
import com.nepxion.discovery.contrib.plugin.rabbitmq.processor.RabbitContribProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
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
    @ConditionalOnClass(ChannelAwareMessageListener.class)
    public SpringBoot2GrayRabbitManager springBoot2GrayRabbitManager(RabbitTemplate rabbitTemplate) {
        return new SpringBoot2GrayRabbitManager(rabbitTemplate);
    }

}
