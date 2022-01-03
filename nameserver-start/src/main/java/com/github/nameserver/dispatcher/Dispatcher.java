package com.github.nameserver.dispatcher;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.github.nameserver.annotation.Mapping;
import com.github.nameserver.annotation.NameServerController;
import com.github.nameserver.constant.HttpMethod;
import com.github.nameserver.domain.Result;
import com.github.nameserver.exceptioin.BadRequestException;
import com.github.nameserver.exceptioin.MappingException;
import com.github.nameserver.server.handler.RequestHandler;
import com.github.nameserver.server.handler.RequestHandlerFactory;
import com.github.nameserver.util.ExceptionUtils;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

//@Component
public class Dispatcher implements InitializingBean, DisposableBean, ApplicationContextAware {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * guava中的Multimap，多值map,对map的增强，一个key可以保持多个value
     */
    private Map<String, NameServerControllerInfo> uriMapping = new ConcurrentHashMap<>(128);


    @Autowired
    private LocalVariableTableParameterNameDiscoverer parameterNameDiscoverer;

    @Autowired
    private ExecutorService targetMethodExecuteService;

    private ApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, Object> nameServerControllers = applicationContext.getBeansWithAnnotation(NameServerController.class);
        if (nameServerControllers.isEmpty()){
            logger.warn("no controllers for this server...");
            return;
        }
        for (Object controller : nameServerControllers.values()) {
            Class<?> controllerClass = controller.getClass();
            Method[] declaredMethods = controllerClass.getDeclaredMethods();
            for (Method method : declaredMethods) {
                method.setAccessible(true);
                if (Modifier.PUBLIC != method.getModifiers()){
                    continue;
                }
                boolean annotationPresentMapping = method.isAnnotationPresent(Mapping.class);
                if (!annotationPresentMapping){
                    continue;
                }
                Mapping mapping = method.getAnnotation(Mapping.class);
                mappingInfoValidate(mapping);
                String uri = mapping.uri();
                HttpMethod httpMethod = mapping.httpMethod();
                String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
                uriMapping.put(uri,
                        new NameServerControllerInfo(uri, httpMethod,
                                controller, method.getName(), method.getParameterTypes(),
                                parameterNames, MethodAccess.get(controllerClass)
                        ));
            }
        }
    }


    public Future<Result<?>> dispatcher(final FullHttpRequest fullHttpRequest){
        String uri = fullHttpRequest.uri();
        if (uri.contains("?")){
            uri = uri.substring(0, uri.indexOf("?"));
        }
        if (!uriMapping.containsKey(uri)) {
            throw new BadRequestException(HttpResponseStatus.NOT_FOUND);
        }
        io.netty.handler.codec.http.HttpMethod httpMethod = fullHttpRequest.method();
        NameServerControllerInfo controllerInfo = uriMapping.get(uri);
        HttpMethod realHttpMethod = controllerInfo.getHttpMethod();
        if (!realHttpMethod.getHttpMethod().equals(httpMethod)){
            throw new BadRequestException(HttpResponseStatus.METHOD_NOT_ALLOWED);
        }
        RequestHandler requestHandler = RequestHandlerFactory.create(httpMethod);
        Object[] objects = requestHandler.handle(fullHttpRequest, controllerInfo.getParameterNames(), controllerInfo.parameterTypes);
        return invokeTarget(controllerInfo, objects);
    }

    private Future<Result<?>> invokeTarget(NameServerControllerInfo controllerInfo, Object[] objects){
       return targetMethodExecuteService.submit(new Callable<Result<?>>() {
            @Override
            public Result<?> call() throws Exception {
                MethodAccess methodAccess = controllerInfo.getMethodAccess();
                Result<Object> result = Result.create();
                try {
                    result.setData(methodAccess.invoke(controllerInfo.getBean(), controllerInfo.getDealMethodName(), objects));
                    result.setSuccessTrue();
                } catch (Exception exception){
                    result.setSuccess(false);
                    String exceptionMsg = ExceptionUtils.getExceptionMsg(exception);
                    result.setExceptionStack(exceptionMsg);
                }
                return result;
            }
        });
    }

    private void mappingInfoValidate(Mapping mapping) {
        String uri = mapping.uri();
        if (StringUtils.isBlank(uri)){
            throw new MappingException("", "uri isBlank...");
        }
        if (uriMapping.containsKey(uri)){
            NameServerControllerInfo serverControllerInfo = uriMapping.get(uri);
            throw new MappingException(uri, "the uri already exist in " + serverControllerInfo.getBean().getClass().getName());
        }
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    private static class NameServerControllerInfo {
        private final String uri;
        private final HttpMethod httpMethod;
        private final Object bean;
        private final String dealMethodName;
        private final Class<?>[] parameterTypes;
        private final String[] parameterNames;
        private final MethodAccess methodAccess;

        public NameServerControllerInfo(String uri, HttpMethod httpMethod,
                                        Object bean, String dealMethodName,
                                        Class<?>[] parameterTypes,
                                        String[] parameterNames,
                                        MethodAccess methodAccess) {
            this.uri = uri;
            this.httpMethod = httpMethod;
            this.bean = bean;
            this.dealMethodName = dealMethodName;
            this.parameterTypes = parameterTypes;
            this.parameterNames = parameterNames;
            this.methodAccess = methodAccess;
        }

        public String getUri() {
            return uri;
        }

        public HttpMethod getHttpMethod() {
            return httpMethod;
        }

        public Object getBean() {
            return bean;
        }

        public String getDealMethodName() {
            return dealMethodName;
        }

        public Class<?>[] getParameterTypes() {
            return parameterTypes;
        }

        public String[] getParameterNames() {
            return parameterNames;
        }

        public MethodAccess getMethodAccess() {
            return methodAccess;
        }
    }

    @Override
    public void destroy() throws Exception {
        if (null != targetMethodExecuteService && !targetMethodExecuteService.isShutdown()) {
            targetMethodExecuteService.shutdown();
        }
    }

}
