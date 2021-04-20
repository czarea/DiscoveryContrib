package com.nepxion.discovery.contrib.example.listener;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;

/**
 * @author zhouzx
 */
public class ConsumerHandler implements ChannelAwareMessageListener {

    private static final Logger log = LoggerFactory.getLogger(ConsumerHandler.class);

    /**
     * 是否需要回应
     */
    private final Boolean needAck;

    public ConsumerHandler(Boolean needAck) {
        this.needAck = needAck;
    }

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        int flag = (int) message.getMessageProperties().getHeaders().getOrDefault("retryCount", 0);
        flag++;
        if (flag > 1) {
            log.info("此消息第{}次执行", flag);
        }
        message.getMessageProperties().setHeader("retryCount", flag);
        String data = new String(message.getBody());
        log.info("[{}]收到mq消息: {}", message.getMessageProperties().getConsumerQueue(), data);
        if (getNeedAck()) {
            long deliveryTag = message.getMessageProperties().getDeliveryTag();
            try {
                handleMessage(data);
                channel.basicAck(deliveryTag, false);
            } catch (Exception e) {
                channel.basicNack(deliveryTag, false, true);
            }
        } else {
            handleMessage(data);
        }
    }


    /**
     * 处理消息
     *
     * @param data 消息体
     */
    public void handleMessage(String data) {

    }

    public Boolean getNeedAck() {
        return needAck;
    }
}
