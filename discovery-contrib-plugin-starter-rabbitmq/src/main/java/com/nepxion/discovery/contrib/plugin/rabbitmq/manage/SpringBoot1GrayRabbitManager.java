package com.nepxion.discovery.contrib.plugin.rabbitmq.manage;

import com.nepxion.discovery.contrib.plugin.rabbitmq.RabbitManager;
import com.sun.istack.internal.NotNull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author zhouzx
 */
public class SpringBoot1GrayRabbitManager implements ApplicationContextAware, RabbitManager {

    private static final Logger log = LoggerFactory.getLogger(SpringBoot1GrayRabbitManager.class);

    private static final Map<String, SimpleMessageListenerContainer> CONTAINER_MAP = new ConcurrentHashMap<>(8);

    private final RabbitTemplate rabbitTemplate;
    private final RabbitAdmin rabbitAdmin;
    private ApplicationContext applicationContext;

    /**
     * 默认死信交换机名称
     */
    private static final String DLX_EXCHANGE = "gray.exchange.dlx";

    /**
     * 默认死信队列名称
     */
    private static final String DLX_QUEUE = "gray.queue.dlx";

    /**
     * 默认死信队列名称
     */
    private static final String DLX_ROUTING = "gray.routing.dlx";

    public SpringBoot1GrayRabbitManager(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitAdmin = new RabbitAdmin(rabbitTemplate.getConnectionFactory());
    }

    /**
     * 动态添加队列及绑定关系
     *
     * @param queueName 队列名
     * @param exchange 交换机名
     * @param routingKey 路由名
     * @param needDlx 需要死信队列
     */
    private void addQueueAndExchange(String queueName, String exchange, String routingKey, boolean needDlx) {
        Queue queue = new Queue(queueName);
        TopicExchange topicExchange = new TopicExchange(exchange);
        if (needDlx) {
            Map<String, Object> arguments = new HashMap<>(2);
            arguments.put("x-dead-letter-exchange", DLX_EXCHANGE);
            arguments.put("x-dead-letter-routing-key", DLX_ROUTING);
            queue = new Queue(queueName, true, false, false, arguments);
            QueueInformation queueInfo = rabbitAdmin.getQueueInfo(DLX_QUEUE);
            if (queueInfo == null) {
                Queue dlxQueue = new Queue(DLX_QUEUE);
                DirectExchange dlxDirectExchange = new DirectExchange(DLX_EXCHANGE);
                rabbitAdmin.declareQueue(dlxQueue);
                rabbitAdmin.declareExchange(dlxDirectExchange);
                rabbitAdmin.declareBinding(BindingBuilder.bind(dlxQueue).to(dlxDirectExchange).with(DLX_ROUTING));
                log.info("创建死信队[{}]列成功", DLX_QUEUE);
            }
        }
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareExchange(topicExchange);
        rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(topicExchange).with(routingKey));
    }


    /**
     * 动态添加队列监听
     *
     * @param queueName 队列名
     */
    @Override
    public void startListener(String queueName, String msgListener) {
        startListener(queueName, 1, true, msgListener);
    }

    /**
     * 动态添加队列监听及修改消费者线程池大小
     *
     * @param queueName 队列名
     * @param consumerNum 消费者线程数量
     * @param ack 需要死信队列
     */
    @Override
    public void startListener(String queueName, int consumerNum, boolean ack, String msgListener) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(rabbitTemplate.getConnectionFactory());
        SimpleMessageListenerContainer getContainer = CONTAINER_MAP.putIfAbsent(queueName, container);
        if (getContainer != null) {
            log.info("动态修改mq监听成功,队列:{},线程数:{}", queueName, consumerNum);
            container = getContainer;
        } else {
            container.setQueueNames(queueName);
            log.info("动态添加mq监听成功,队列:{},线程数:{}", queueName, consumerNum);
        }
        container.setPrefetchCount(consumerNum);
        if (ack) {
            container.setAcknowledgeMode(AcknowledgeMode.AUTO);
        } else {
            container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        }
        container.setConcurrentConsumers(consumerNum);
        org.springframework.amqp.rabbit.core.ChannelAwareMessageListener listener = null;
        try {
            listener = applicationContext.getBean(msgListener, org.springframework.amqp.rabbit.core.ChannelAwareMessageListener.class);
        } catch (BeansException e) {
            log.error("", e);
        }
        if (listener != null) {
            try {
                container.setMessageListener(listener);
            } catch (final NoSuchMethodError e) {
                // Compatibility with new spring-rabbit versions
                try {
                    final Method m = AbstractMessageListenerContainer.class.getDeclaredMethod("setMessageListener", Object.class);
                    m.invoke(container, listener);
                } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e2) {
                    // Restore previous exception
                    throw e;
                }
            }
            container.setDefaultRequeueRejected(false);
            container.start();
        }
    }

    /**
     * 动态停止监听并删除队列
     *
     * @param queueName 队列名
     */
    @Override
    public void stopListener(String queueName) {
        SimpleMessageListenerContainer container = CONTAINER_MAP.get(queueName);
        if (container != null) {
            container.stop();
            container.destroy();
            CONTAINER_MAP.remove(queueName);
        }
        log.info("停止监听mq队列{}", queueName);
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
