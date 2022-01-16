package com.github.nameserver;


import com.dtflys.forest.callback.OnError;
import com.dtflys.forest.callback.OnSuccess;
import com.dtflys.forest.exceptions.ForestRuntimeException;
import com.dtflys.forest.http.ForestRequest;
import com.dtflys.forest.http.ForestResponse;
import com.github.nameserver.constant.Constants;
import com.github.nameserver.forest.ForestFactory;
import com.github.nameserver.forest.NameServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class NameServerClient {
    private static final Logger logger = LoggerFactory.getLogger(NameServerClient.class);

    private final List<String> nameServerDomains;
    private final NameServerService nameServerService;

    private static final Random randomNumberGenerator = new Random();

    private static final Object lock = new Object();

    private NameServerClient(List<String> nameServerDomains, boolean withCheck) {
        if (null == nameServerDomains || nameServerDomains.isEmpty()) {
            throw new IllegalArgumentException("nameServerDomain can not be null or empty...");
        }
        nameServerDomains = new ArrayList<>(new HashSet<>(nameServerDomains));
        this.nameServerService = ForestFactory.createInstance(NameServerService.class);
        if (withCheck) {
            allCheck(nameServerDomains);
        }
        Collections.shuffle(nameServerDomains, randomNumberGenerator);
        this.nameServerDomains = nameServerDomains;
    }

    private void allCheck(List<String> nameServerDomains){
        // 检测每一个nameServer是否可用,有一个不可用则启动失败
        int size = nameServerDomains.size();
        List<ForestRuntimeException> forestRuntimeExceptions = new ArrayList<>(size);
        CountDownLatch countDownLatch = new CountDownLatch(size);
        for (String nameServerDomain : nameServerDomains) {
            nameServerService.health(nameServerDomain, new OnSuccess() {
                @Override
                public void onSuccess(Object data, ForestRequest req, ForestResponse res) {
                    countDownLatch.countDown();
                }
            }, new OnError() {
                @Override
                public void onError(ForestRuntimeException ex, ForestRequest req, ForestResponse res) {
                    logger.info("check this nameserver [{}] error...", nameServerDomain);
                    countDownLatch.countDown();
                    forestRuntimeExceptions.add(ex);
                }
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
        if (!forestRuntimeExceptions.isEmpty()){
            throw new RuntimeException("Check NameServerClient error...", forestRuntimeExceptions.get(Constants.NumberConstants.INTEGER_ZERO));
        }
    }

    /**
     * 只创建client，不检测nameServer可用性;
     * 如果想在创建时,检测nameServer是否可用,请使用{@link #getInstance(List, boolean)}
     * @param nameServerDomains
     * @return
     */
    public static NameServerClient getInstance(List<String> nameServerDomains) {
        return CustomerNameServerClientBuilder.build(nameServerDomains, false);
    }

    /**
     * @param nameServerDomains
     * @param withCheck         为true时，会检测指定的nameServerDomain是否可用，
     *                          否则和 {@link #getInstance(List)}行为一致
     * @return
     */
    public static NameServerClient getInstance(final List<String> nameServerDomains, boolean withCheck) {
        return CustomerNameServerClientBuilder.build(nameServerDomains, withCheck);
    }


    public void register(final String appName,
                         final String ip, final int port) throws Exception {
        final String serverDomain = ip + ":" + port;
        final ForestRuntimeException[] forestRuntimeException = {null};
        // 往所有的nameserver上注册
        for (String nameServerDomain : nameServerDomains) {
            CountDownLatch countDownLatch = new CountDownLatch(1);
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
            if (null != forestRuntimeException[0]) {
                throw new RuntimeException("register this node [" + serverDomain + "] to nameServer [" + nameServerDomain + "] failed", forestRuntimeException[0]);
            }
        }
    }

    /**
     * 如果失败，默认会重试三次；
     * 如果不想重试或想自定义重试次数，请使用{@link #getAppNodeList(String, int)}
     *
     * @param appName
     * @return
     */
    public List<String> getAppNodeList(final String appName) {
        return getAppNodeList(appName, Constants.DEFAULT_RETRY);
    }

    /**
     * 自定义重试次数的的获取
     * @param appName
     * @param retry 如果为0，失败后不会重试，会快速失败；
     *              如果该值大于NameServerDomain的数量，则最多会执行【NameServerDomain的数量】次
     * @return
     */
    public List<String> getAppNodeList(final String appName, int retry) {
        return getAppNodeList(appName, Constants.NumberConstants.INTEGER_ZERO, retry);
    }

    private List<String> getAppNodeList(final String appName, int index, int retry) {
        int domainIndex = index++;
        int size = nameServerDomains.size();
        try {
            String nameServerDomain = nameServerDomains.get(index);
            Set<String> appNodeList = nameServerService.serverList(nameServerDomain, appName);
            if (null == appNodeList || appNodeList.isEmpty()) {
                return Collections.emptyList();
            }
            ArrayList<String> list = new ArrayList<>(appNodeList);
            // 做随机处理 避免某些场景下 大量请求同时打到一台机器
            Collections.shuffle(list, randomNumberGenerator);
            return list;
        } catch (Exception exception) {
            if (domainIndex == size || retry == Constants.NumberConstants.INTEGER_ZERO){
                throw new RuntimeException("getAppNodeList failed...", exception);
            }
            retry--;
            int targetIndex;
            if (retry == Constants.NumberConstants.INTEGER_ZERO) {
                int maxIndex = size - 1;
                targetIndex = randomNumberGenerator.nextInt(maxIndex) % (maxIndex - domainIndex + 1) + domainIndex;
            } else {
                targetIndex = domainIndex;
            }
            return getAppNodeList(appName, targetIndex, retry);
        }
    }

    private static class CustomerNameServerClientBuilder {
        private static volatile NameServerClient INSTANCE;

        private static NameServerClient build(List<String> nameServerDomains, boolean withCheck) {
            if (null != INSTANCE) {
                return INSTANCE;
            }
            synchronized (lock) {
                if (null == INSTANCE) {
                    INSTANCE = new NameServerClient(nameServerDomains, withCheck);
                }
            }
            return INSTANCE;
        }
    }
}
