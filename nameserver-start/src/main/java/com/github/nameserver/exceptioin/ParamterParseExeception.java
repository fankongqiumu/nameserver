package com.github.nameserver.exceptioin;

public class ParamterParseExeception extends RuntimeException{

    public ParamterParseExeception(String errorMsg, Throwable throwable){
        super(errorMsg, throwable);
    }
}
