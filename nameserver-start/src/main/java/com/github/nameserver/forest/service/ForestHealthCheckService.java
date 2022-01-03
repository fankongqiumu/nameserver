package com.github.nameserver.forest.service;

import com.dtflys.forest.annotation.Get;
import com.dtflys.forest.annotation.Retry;
import com.dtflys.forest.annotation.Var;
import com.dtflys.forest.callback.OnError;
import com.dtflys.forest.callback.OnSuccess;
import com.github.nameserver.constant.Constants;

@Retry(maxRetryCount = "3", maxRetryInterval = "10")
public interface ForestHealthCheckService {

    @Get(url = "http://" + "{serverDomain}" + Constants.UriConstants.HEALTH_CHECK_PATH, async = true)
    void ping(@Var("serverDomain")String serverDomain, OnSuccess<String> onSuccess, OnError onError);
}
