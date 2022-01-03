package com.github.nameserver.util;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/17 14:20
 */
public class ExceptionUtils {

    public static String getExceptionMsg(Throwable e) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return "\r\n" + sw.toString() + "\r\n";
        } catch (Exception e2) {
            return "bad getErrorInfoFromException";
        }
    }

    public static String generateTraceId() {
        return UUID.randomUUID().toString().replaceAll("-", "").toLowerCase();
    }

}
