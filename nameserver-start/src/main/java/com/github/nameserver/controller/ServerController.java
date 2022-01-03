package com.github.nameserver.controller;

import com.github.nameserver.cache.NameServerCache;
import com.github.nameserver.constant.HttpConstants;
import com.github.nameserver.constant.Constants;
import com.github.nameserver.forest.HealthCheckService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
public class ServerController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private NameServerCache nameServerCache;

    @Autowired
    private HealthCheckService healthCheckService;


    @GetMapping(Constants.UriConstants.CLIENT_REGISTER_PATH)
    public void appRegister(String app, String serverDomain){
        nameServerCache.serverOnLine(app, serverDomain);
        logger.info("app [{}] server [{}] online...", app, serverDomain);
    }

    @GetMapping(Constants.UriConstants.FETCH_SERVER_DOMAINS_PATH)
    public Set<String> fetchServerDomains(String app){
        return nameServerCache.getServerList(app);
    }

    @GetMapping(Constants.UriConstants.HEALTH_CHECK_PATH)
    public String health(){
        return HttpConstants.HttpStatus.OK.getMessage();
    }

}
