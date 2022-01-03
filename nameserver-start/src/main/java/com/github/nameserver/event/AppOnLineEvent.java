package com.github.nameserver.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/21 16:55
 */
public class AppOnLineEvent extends ApplicationEvent {

    private String appDomain;

    public AppOnLineEvent(String appDomain) {
        super(appDomain);
        this.appDomain = appDomain;
    }

    public String getAppDomain() {
        return appDomain;
    }
}
