package com.nepxion.discovery.contrib.plugin.subscriber;

/**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 * @author Haojun Ren
 * @version 1.0
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.eventbus.Subscribe;
import com.nepxion.discovery.common.entity.ParameterEntity;
import com.nepxion.discovery.common.entity.ParameterServiceEntity;
import com.nepxion.discovery.common.exception.DiscoveryException;
import com.nepxion.discovery.contrib.plugin.cache.ContribCache;
import com.nepxion.discovery.contrib.plugin.constant.ContribConstant;
import com.nepxion.discovery.contrib.plugin.matcher.ContribMatcher;
import com.nepxion.discovery.contrib.plugin.processor.ContribProcessor;
import com.nepxion.discovery.plugin.framework.adapter.PluginAdapter;
import com.nepxion.discovery.plugin.framework.event.ParameterChangedEvent;
import com.nepxion.eventbus.annotation.EventBus;

@EventBus
public class ContribSubscriber {
    private static final Logger LOG = LoggerFactory.getLogger(ContribSubscriber.class);

    @Autowired
    protected PluginAdapter pluginAdapter;

    @Autowired
    protected ContribMatcher contribMatcher;

    @Autowired
    protected ContribCache contribCache;

    @Autowired(required = false)
    protected List<ContribProcessor> contribProcessorList;

    @Subscribe
    public void onParameterChanged(ParameterChangedEvent parameterChangedEvent) {
        ParameterEntity parameterEntity = parameterChangedEvent.getParameterEntity();
        if (parameterEntity == null) {
            return;
        }

        Map<String, List<ParameterServiceEntity>> parameterServiceMap = parameterEntity.getParameterServiceMap();

        String serviceId = pluginAdapter.getServiceId();
        List<ParameterServiceEntity> parameterServiceEntityList = parameterServiceMap.get(serviceId);

        if (parameterServiceEntityList == null) {
            return;
        }

        Map<String, String> keyMap = new HashMap<String, String>();
        for (ParameterServiceEntity parameterServiceEntity : parameterServiceEntityList) {
            Map<String, String> parameterMap = parameterServiceEntity.getParameterMap();

            String tagKey = parameterMap.get(ContribConstant.TAG_KEY);
            if (StringUtils.isEmpty(tagKey)) {
                throw new DiscoveryException("Tag key can be null or empty");
            }
            String tagValue = parameterMap.get(ContribConstant.TAG_VALUE);
            if (StringUtils.isEmpty(tagValue)) {
                throw new DiscoveryException("Tag value can be null or empty");
            }
            String key = parameterMap.get(ContribConstant.KEY);
            if (StringUtils.isEmpty(key)) {
                throw new DiscoveryException("Key can be null or empty");
            }
            String value = parameterMap.get(ContribConstant.VALUE);
            if (StringUtils.isEmpty(value)) {
                throw new DiscoveryException("Value can be null or empty");
            }

            // 不允许同时从多个维度进行对指定服务的指定组件进行灰度发布
            // 例如：对于指定服务的数据库灰度发布，既从版本维度，又从区域维度去执行灰度发布，会导致逻辑混乱
            // 但允许同时从多个维度对指定服务的不同组件进行灰度发布
            // 例如：对于指定服务的数据库灰度发布，从版本维度；对于指定服务的消息队列灰度发布，从区域维度
            // 判断是否有重复的维度
            if (keyMap.containsKey(key)) {
                String existedTagKey = keyMap.get(key);
                if (!StringUtils.equals(tagKey, existedTagKey)) {
                    throw new DiscoveryException("Gray release for [" + key + "] has existed for [" + existedTagKey + "] dimension, [" + tagKey + "] dimension is duplicated");
                }
            } else {
                keyMap.put(key, tagKey);
            }
        }

        for (ParameterServiceEntity parameterServiceEntity : parameterServiceEntityList) {
            Map<String, String> parameterMap = parameterServiceEntity.getParameterMap();

            String tagKey = parameterMap.get(ContribConstant.TAG_KEY);
            String tagValue = parameterMap.get(ContribConstant.TAG_VALUE);
            String key = parameterMap.get(ContribConstant.KEY);
            String value = parameterMap.get(ContribConstant.VALUE);

            // <service service-name="discovery-guide-service-a" tag-key="version" tag-value="1.0" key="ShardingSphere" value="db1"/>
            // <service service-name="discovery-guide-service-a" tag-key="version" tag-value="1.1" key="ShardingSphere" value="db2"/>
            // <service service-name="discovery-guide-service-a" tag-key="region" tag-value="dev" key="RocketMQ" value="queue1"/>
            // <service service-name="discovery-guide-service-a" tag-key="region" tag-value="qa" key="RocketMQ" value="queue2"/>
            if (StringUtils.equals(tagKey, ContribConstant.VERSION)) {
                if (contribMatcher.match(tagValue, pluginAdapter.getVersion())) {
                    process(key, parameterMap);
                }
            } else if (StringUtils.equals(tagKey, ContribConstant.REGION)) {
                if (contribMatcher.match(tagValue, pluginAdapter.getRegion())) {
                    process(key, parameterMap);
                }
            } else if (StringUtils.equals(tagKey, ContribConstant.ENVIRONMENT)) {
                if (contribMatcher.match(tagValue, pluginAdapter.getEnvironment())) {
                    process(key, parameterMap);
                }
            } else if (StringUtils.equals(tagKey, ContribConstant.ZONE)) {
                if (contribMatcher.match(tagValue, pluginAdapter.getZone())) {
                    process(key, parameterMap);
                }
            } else if (StringUtils.equals(tagKey, ContribConstant.ADDRESS)) {
                if (contribMatcher.matchAddress(tagValue)) {
                    process(key, parameterMap);
                }
            }
        }
    }

    public void process(String key, Map<String, String> value) {
        if (CollectionUtils.isEmpty(contribProcessorList)) {
            return;
        }
        LOG.info("Gray release for {} with {}", key, value);

        for (ContribProcessor contribProcessor : contribProcessorList) {
            contribProcessor.process(key, value);
        }
    }
}
