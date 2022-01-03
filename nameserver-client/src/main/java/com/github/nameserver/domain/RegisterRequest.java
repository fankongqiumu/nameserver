package com.github.nameserver.domain;

import java.io.Serializable;

public class RegisterRequest implements Serializable {
    private static final long serialVersionUID = 6896403181801867L;

    private String app;
    /**
     * eg: 192.168.0.2:8080
     */
    private String serverDomain;

    public RegisterRequest(String app, String serverDomain) {
        this.app = app;
        this.serverDomain = serverDomain;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getServerDomain() {
        return serverDomain;
    }

    public void setServerDomain(String serverDomain) {
        this.serverDomain = serverDomain;
    }
}
