package com.github.nameserver.forest;


import com.dtflys.forest.annotation.Query;
import com.dtflys.forest.annotation.Request;
import com.dtflys.forest.annotation.Var;
import com.dtflys.forest.callback.OnError;
import com.dtflys.forest.callback.OnSuccess;
import com.github.nameserver.constant.Constants;

import java.util.Set;

public interface NameServerService {

    @Request(url = "http://" + "{nameServerDomain}" + Constants.UriConstants.HEALTH_CHECK_PATH)
    void health(@Var("nameServerDomain")String nameServerDomain);

    @Request(url = "http://" + "{nameServerDomain}" + Constants.UriConstants.CLIENT_REGISTER_PATH, async = true)
    void register(@Var("nameServerDomain")String nameServerDomain, @Query("app")String app, @Query("serverDomain")String serverDomain, OnSuccess onSuccess, OnError onError);

    @Request(url = "http://" + "{nameServerDomain}" + Constants.UriConstants.FETCH_SERVER_DOMAINS_PATH)
    Set<String> serverList(@Var("nameServerDomain")String nameServerDomain, @Query("app")String app);
}
