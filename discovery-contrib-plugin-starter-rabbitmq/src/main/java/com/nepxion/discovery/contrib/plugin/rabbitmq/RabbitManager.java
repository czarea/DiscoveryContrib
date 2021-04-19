package com.nepxion.discovery.contrib.plugin.rabbitmq;

/**
 * @author zhouzx
 */
public interface RabbitManager {

    void startListener(String queueName, String msgListener);

    void startListener(String queueName, int consumerNum, boolean needDlx, String msgListener);

    void stopListener(String queueName);
}
