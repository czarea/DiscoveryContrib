package com.nepxion.discovery.contrib.plugin.rabbitmq.processor;


import com.nepxion.discovery.contrib.plugin.processor.ContribProcessor;
import com.nepxion.discovery.contrib.plugin.rabbitmq.RabbitManager;
import com.nepxion.discovery.contrib.plugin.rabbitmq.manage.SpringBoot1GrayRabbitManager;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhouzx
 */
public class RabbitContribProcessor implements ContribProcessor {

    private static final Logger logger = LoggerFactory.getLogger(RabbitContribProcessor.class);

    private final String KEY = "RabbitMQ";

    private String destination;
    private RabbitManager grayRabbitManager;

    public RabbitContribProcessor(RabbitManager grayRabbitManager) {
        this.grayRabbitManager = grayRabbitManager;
    }

    @Override
    public void process(String key, Map<String, String> value) {
        if (!StringUtils.equals(key, KEY)) {
            return;
        }
        String queue = value.get("value");
        String listener = value.get("listener");
        String status = value.get("status");
        if ("stop".equals(status)) {
            grayRabbitManager.stopListener(queue);
        } else if ("start".equals(status)) {
            grayRabbitManager.startListener(queue, listener);
        }
        logger.info("gray key:{} -> value:{}", key, value);
    }

    public String getDestination() {
        return destination;
    }
}
