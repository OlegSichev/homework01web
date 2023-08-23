package oleg.sichev;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {
    private String method;
    private String path;
    private String httpVersion;
    private Map<String, String> queryParams;

    public Request(String method, String path, String httpVersion, String query) {
        this.method = method;
        this.path = path;
        this.httpVersion = httpVersion;
        this.queryParams = new HashMap<>();

        if (query != null && !query.isEmpty()) {
            List<NameValuePair> params = URLEncodedUtils.parse(URI.create("?" + query), StandardCharsets.UTF_8);
            params.forEach(pair -> queryParams.put(pair.getName(), pair.getValue()));
        }
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public String getQueryParam(String name) {
        return queryParams.get(name);
    }
}