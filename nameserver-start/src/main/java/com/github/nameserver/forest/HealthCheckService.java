package com.github.nameserver.forest;

import com.dtflys.forest.callback.OnError;
import com.dtflys.forest.callback.OnSuccess;
import com.dtflys.forest.exceptions.ForestRuntimeException;
import com.dtflys.forest.http.ForestRequest;
import com.dtflys.forest.http.ForestResponse;
import com.github.nameserver.cache.NameServerCache;
import com.github.nameserver.event.AppOnLineEvent;
import com.github.nameserver.forest.service.ForestHealthCheckService;
import com.github.nameserver.util.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.*;

@Service
public class HealthCheckService implements InitializingBean , ApplicationRunner , Runnable{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected static final Object lock = new Object();

    private volatile boolean waited = false;

    private ExecutorService healthCheckExecutorService;
    private ScheduledExecutorService scheduledExecutorService;

    @Autowired
    private NameServerCache nameServerCache;

    @Override
    public void afterPropertiesSet() throws Exception {
        healthCheckExecutorService = new ThreadPoolExecutor(10, 50,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue(2048),
                DefaultThreadFactory.defaultThreadFactory("pool-health-check-thread-"),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        scheduledExecutorService = new ScheduledThreadPoolExecutor(1,
                DefaultThreadFactory.defaultThreadFactory("pool-schedule-health-check-watch-thread-")
        );

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            private volatile boolean hasShutdown = false;
            @Override
            public void run() {
                synchronized (lock) {
                    if (!this.hasShutdown) {
                        this.hasShutdown = true;
                        long beginTime = System.currentTimeMillis();
                        if (null != scheduledExecutorService && !scheduledExecutorService.isShutdown()){
                            scheduledExecutorService.shutdown();
                        }
                        if (null != healthCheckExecutorService && !healthCheckExecutorService.isShutdown()){
                            healthCheckExecutorService.shutdown();
                        }
                        long consumingTimeTotal = System.currentTimeMillis() - beginTime;
                        logger.info("Shutdown hook over, consuming total time(ms): {}", consumingTimeTotal);
                    }
                }
            }
        }, "ShutdownHook"));
    }

    @Async
    @EventListener
    @Order(1)
    public void configModifiedSyncListener(AppOnLineEvent appOnLineEvent){
        if (waited) {
            synchronized (lock) {
                lock.notifyAll();
                waited = false;
            }
        }
    }


    private void healCheck(String app) {
        healthCheckExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                Set<String> serverDomains = nameServerCache.getServerList(app);
                if (serverDomains.isEmpty()) {
                    return;
                }
                for (String serverDomain : serverDomains) {
                    ForestHealthCheckService forestHealthCheckService = ForestFactory.createInstance(ForestHealthCheckService.class);
                    forestHealthCheckService.ping(serverDomain, new OnSuccess<String>() {
                        @Override
                        public void onSuccess(String s, ForestRequest forestRequest, ForestResponse forestResponse) {
                            logger.info("ping app [{}] node [{}] success", app, serverDomain);
                        }
                    }, new OnError() {
                        @Override
                        public void onError(ForestRuntimeException e, ForestRequest forestRequest, ForestResponse forestResponse) {
                            // todo 待建不健康列表
                            String requestHost = forestRequest.getHost();
                            int requestPort = forestRequest.getPort();
                            final String serverDomain = requestHost + ":" + requestPort;
                            nameServerCache.serverOffLine(app, serverDomain);
                            logger.error("app [{}] node [{}] offline", app, serverDomain);
                        }
                    });
                }
            }
        });
    }


    @Override
    public void run(ApplicationArguments args) throws Exception {
        scheduledExecutorService.scheduleWithFixedDelay(this, 1, 5, TimeUnit.SECONDS);
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        Set<String> appList = nameServerCache.appList();
        if (!appList.isEmpty()) {
            for (String app : appList) {
                healCheck(app);
            }
        } else {
           synchronized (lock) {
               try {
                   waited = true;
                   lock.wait();
               } catch (InterruptedException interruptedException) {
                   logger.error("lockCondition.await interruptedException:", interruptedException);
               }
           }
        }
    }
}
