package com.github.nameserver.cache;

import com.github.nameserver.event.AppOnLineEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NameServerCache {

    private static final Map<String, Set<String>> cache = new ConcurrentHashMap<>(256);

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public Set<String> getServerList(String app) {
        Set<String> serverDomains = cache.get(app);
        if (null == serverDomains) {
            serverDomains = Collections.emptySet();
        }
        return serverDomains;
    }

    public void serverOnLine(String app, String serverDomain) {
        synchronized (this) {
            Set<String> serverDomainSet = cache.get(app);
            if (null != serverDomainSet) {
                serverDomainSet.add(serverDomain);
            } else {
                serverDomainSet = new HashSet<>();
                serverDomainSet.add(serverDomain);
                cache.put(app, serverDomainSet);
            }
        }
        eventPublisher.publishEvent(new AppOnLineEvent(serverDomain));
    }

    public synchronized void serverOffLine(String app, String serverDomain) {
        Set<String> serverDomainSet = cache.get(app);
        if (null == serverDomainSet || serverDomainSet.isEmpty()) {
            cache.remove(app);
            return;
        }
        serverDomainSet.remove(serverDomain);
        cache.put(app, serverDomainSet);
    }

    public Set<String> appList() {
        return cache.keySet();
    }
}
