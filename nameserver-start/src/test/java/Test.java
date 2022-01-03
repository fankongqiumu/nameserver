import com.github.nameserver.server.handler.GetRequestHandler;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.Charsets;

import java.util.List;
import java.util.Map;

public class Test {
    public static void main(String[] args) {
        String uri = "/favicon?name=1&id=[1,2,3]";
        QueryStringDecoder queryDecoder = new QueryStringDecoder(uri, Charsets.toCharset(CharEncoding.UTF_8));
        Map<String, List<String>> parameters = queryDecoder.parameters();
        System.out.println(parameters);

        System.out.println(GetRequestHandler.class.isPrimitive());
        System.out.println(Integer.class.isLocalClass());
    }
}
