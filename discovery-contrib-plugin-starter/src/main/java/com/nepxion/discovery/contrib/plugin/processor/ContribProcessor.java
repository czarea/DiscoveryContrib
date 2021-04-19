package com.nepxion.discovery.contrib.plugin.processor;

import java.util.Map;

/**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 *
 * @author Haojun Ren
 * @version 1.0
 */

public interface ContribProcessor {

    void process(String key, Map<String,String> value);
}
