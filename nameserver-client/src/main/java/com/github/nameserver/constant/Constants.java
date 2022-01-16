package com.github.nameserver.constant;

public interface Constants {

    String NAMESERVER_MOMAIN = "nameserver.domain";

    String DEFAULT_DELIMITER = ",";

    int DEFAULT_RETRY = 3;


    interface UriConstants {
        String HEALTH_CHECK_PATH = "/health";

        String CLIENT_REGISTER_PATH = "/register";

        String FETCH_SERVER_DOMAINS_PATH = "/server/domain/list";
    }

    interface NumberConstants {
        int INTEGER_ZERO = 0;
        int INTEGER_ONE = 1;
    }

    interface BeanNameConstants {
        String NAMESERVER_CLIENT = "nameServerClient";
    }

}
