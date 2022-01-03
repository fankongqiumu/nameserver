package com.github.nameserver;


import com.dtflys.forest.callback.OnError;
import com.dtflys.forest.callback.OnSuccess;
import com.dtflys.forest.exceptions.ForestRuntimeException;
import com.dtflys.forest.http.ForestRequest;
import com.dtflys.forest.http.ForestResponse;
import com.github.nameserver.forest.ForestFactory;
import com.github.nameserver.forest.NameServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class NameServerClient {
    private static final Logger logger = LoggerFactory.getLogger(NameServerClient.class);

    private final String nameServerDomain;
    private final NameServerService nameServerService;

    private static final Object lock = new Object();

    private NameServerClient(final String nameServerDomain, boolean withCheck) {
        if (null == nameServerDomain || nameServerDomain.isEmpty()){
            throw new IllegalArgumentException("nameServerDomain can not be null or empty...");
        }
        this.nameServerService = ForestFactory.createInstance(NameServerService.class);
        this.nameServerDomain = nameServerDomain;
        if (withCheck){
            nameServerService.health(nameServerDomain);
        }
    }

    public static NameServerClient getInstance(final String nameServerDomain){
        return CustomerNameServerClientBuilder.build(nameServerDomain, false);
    }

    /**
     * @param nameServerDomain
     * @param withCheck 为true时，会检测指定的nameServerDomain是否可用，
     *                  否则和getInstance(final String nameServerDomain) 行为一致
     * @return
     */
    public static NameServerClient getInstance(final String nameServerDomain, boolean withCheck){
        return CustomerNameServerClientBuilder.build(nameServerDomain, withCheck);
    }


    public void register(final String appName,
                         final String ip, final int port) throws Exception{
        CountDownLatch countDownLatch = new CountDownLatch(1);
        final String serverDomain = ip + ":" + port;
        final ForestRuntimeException[] forestRuntimeException = {null};
        nameServerService.register(nameServerDomain, appName, serverDomain, new OnSuccess() {
            @Override
            public void onSuccess(Object data, ForestRequest req, ForestResponse res) {
                logger.info("register this server [{}] serverDomain [{}] to nameserver [{}] success...", appName, serverDomain, nameServerDomain);
                countDownLatch.countDown();
            }
        }, new OnError() {
            @Override
            public void onError(ForestRuntimeException ex, ForestRequest req, ForestResponse res) {
                logger.info("register this server [{}] serverDomain [{}] to nameserver [{}] error...", appName, serverDomain, nameServerDomain);
                countDownLatch.countDown();
                forestRuntimeException[0] = ex;
            }
        });
        boolean await = countDownLatch.await(3L, TimeUnit.SECONDS);
        if (!await) {
            throw new RuntimeException("register this node [" + serverDomain + "] to nameServer [" + nameServerDomain + "] failed");
        }
        if (null != forestRuntimeException[0]){
            throw new RuntimeException("register this node [" + serverDomain + "] to nameServer [" + nameServerDomain + "] failed", forestRuntimeException[0]);
        }
    }

    public Set<String> getAppNodeList(final String appName){
        try {
            return nameServerService.serverList(nameServerDomain, appName);
        } catch (Exception exception){
            throw new RuntimeException("getAppNodeList failed...", exception);
        }
    }

    private static class CustomerNameServerClientBuilder {
        private static volatile NameServerClient INSTANCE;
        private static NameServerClient build(String nameServerDomain, boolean withCheck){
            if (null != INSTANCE){
                return INSTANCE;
            }
            synchronized (lock){
                if (null == INSTANCE){
                    INSTANCE = new NameServerClient(nameServerDomain, withCheck);
                }
            }
            return INSTANCE;
        }
    }
}
