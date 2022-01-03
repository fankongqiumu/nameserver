package com.github.nameserver.forest;

import com.dtflys.forest.annotation.BindingVar;
import com.dtflys.forest.reflection.ForestMethod;
import com.dtflys.forest.reflection.MetaRequest;
import com.github.nameserver.cache.NameServerCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NameServerForestConfiguration {

    @Autowired
    private NameServerCache nameServerCache;


    /**
     * 使用 @BindingVar 注解
     * 将变量名 baseUrl 和一段方法代码绑定
     * 该方法可以有一个 ForestMethod 类型的参数
     */
    @BindingVar("baseUrl")
    public String getBaseUrl(ForestMethod method) {
        // method: Forest 接口方法对象，即对请求所对应的方法的封装对象
        // method.getMethodName() 获得请求所对应的方法的方法名
        String methodName = method.getMethodName();
        MetaRequest metaRequest = method.getMetaRequest();
        if (methodName.equals("getData")) {
            // 若调用的是 getData 方法，则返回 192.168.0.2
            return "192.168.0.2";
        }
        // 默认返回 192.168.0.1
        return "192.168.0.1";
    }


}
