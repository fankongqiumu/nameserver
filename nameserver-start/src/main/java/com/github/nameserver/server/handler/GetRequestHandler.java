package com.github.nameserver.server.handler;

import com.github.nameserver.exceptioin.NotSupportException;
import com.github.nameserver.util.JsonUtils;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.Charsets;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

//@Component
public class GetRequestHandler implements RequestHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private LocalVariableTableParameterNameDiscoverer parameterNameDiscoverer;

    @Override
    public Object[] handle(FullHttpRequest fullHttpRequest, String[] parameterNames, Class<?>[] parameterTypes) {
        String requestUri = fullHttpRequest.uri();
        return getParams(requestUri, parameterNames, parameterTypes);
    }

    private Object[] getParams(String uri, String[] parameterNames, Class<?>[] parameterTypes) {
        if (ArrayUtils.isEmpty(parameterTypes)){
            return new Object[0];
        }
        QueryStringDecoder queryDecoder = new QueryStringDecoder(uri, Charsets.toCharset(CharEncoding.UTF_8));
        Map<String, List<String>> parameters = queryDecoder.parameters();
        Object[] objects = new Object[parameterTypes.length];
        for (int i = 0; i < parameterNames.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            if (parameterType.isEnum()){
                throw new NotSupportException("enum paramterType");
            }
            String parameterName = parameterNames[i];
            List<String> paramter = parameters.get(parameterName);
            if (CollectionUtils.isEmpty(paramter)){
                continue;
            }
            String paramterString = paramter.get(NumberUtils.INTEGER_ZERO);
            if (parameterType == String.class){
                objects[i] = paramterString;
                continue;
            }
            if (parameterType == Character.class || parameterType == char.class){
                objects[i] = paramterString.charAt(NumberUtils.INTEGER_ZERO);
                continue;
            }
            if (parameterType == Integer.class || parameterType == int.class){
                objects[i] = Integer.valueOf(paramterString);
                continue;
            }
            if (parameterType == Long.class || parameterType == long.class){
                objects[i] = Long.valueOf(paramterString);
                continue;
            }
            if (parameterType == Float.class || parameterType == float.class){
                objects[i] = Float.valueOf(paramterString);
                continue;
            }
            if (parameterType == Double.class || parameterType == double.class){
                objects[i] = Double.valueOf(paramterString);
                continue;
            }
            if (parameterType == Short.class || parameterType == short.class){
                objects[i] = Short.valueOf(paramterString);
                continue;
            }
            if (parameterType == Byte.class || parameterType == byte.class){
                objects[i] = Byte.valueOf(paramterString);
                continue;
            }
            if (parameterType == Boolean.class || parameterType == boolean.class){
                objects[i] = Boolean.valueOf(paramterString);
                continue;
            }
            if (parameterType.isInstance(List.class)){
                objects[i] = JsonUtils.parseList(paramterString, parameterType);
                continue;
            }
            if (parameterType.isInstance(Set.class)){
                objects[i] = JsonUtils.parseSet(paramterString, parameterType);
                continue;
            }
            if (parameterType.isInstance(Map.class)){
                objects[i] = JsonUtils.parseMap(paramterString);
                continue;
            }
            if (parameterType.isArray()){
                objects[i] = JsonUtils.parseArray(paramterString, parameterType);
                continue;
            }
            objects[i] = JsonUtils.parse(paramterString, parameterType);
            // todo 其余类型待补充
        }
        return objects;
    }

}
