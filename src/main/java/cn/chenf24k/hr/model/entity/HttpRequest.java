package cn.chenf24k.hr.model.entity;

import cn.chenf24k.hr.model.Context;
import cn.chenf24k.hr.model.enums.METHOD;
import cn.chenf24k.hr.tool.TemplateProcess;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.fluent.Request;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
//@Slf4j
public class HttpRequest {

    private METHOD method;
    private String url;
    private Map<String, String> header;
    private Map<String, String> body;

    public String request() {
        Request request;
        switch (this.getMethod()) {
            case GET:
                request = Request.Get(this.getUrl());
                break;
            case POST:
                request = Request.Post(this.getUrl()).body(this.body());
                break;
            case PUT:
            case DELETE:
            default:
                request = Request.Get(this.getUrl());
                break;
        }
        headers(request);
        String response;

        try {
            LocalDateTime startTime = LocalDateTime.now();
            response = request
                    .execute()
                    .returnContent()
                    .asString(StandardCharsets.UTF_8);
            LocalDateTime endTime = LocalDateTime.now();
            Duration duration = Duration.between(startTime, endTime);
            long milliseconds = duration.toMillis();
            System.out.println("Time Interval in Milliseconds: " + milliseconds + " ms");
            // log.info("Time Interval in Milliseconds: {} ms", milliseconds);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    public void headers(Request request) {
        if (this.getHeader() == null || this.getHeader().isEmpty()) {
            return;
        }
        List<Header> headers = new ArrayList<>(this.getHeader().size());
        this.getHeader().forEach((key, value) -> {
            // 变量替换
            String newValue = TemplateProcess.processTemplate(value, Context.vars);
            Header basicHeader = new BasicHeader(key, newValue);
            headers.add(basicHeader);
        });
        request.setHeaders(headers.toArray(new Header[0]));
    }

    public HttpEntity body() {
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        if (!this.getBody().isEmpty()) {
            Set<String> keySet = this.getBody().keySet();
            for (String s : keySet) {
                String value = this.getBody().get(s);
                // 变量替换
                String newValue = TemplateProcess.processTemplate(value, Context.vars);
                nameValuePairs.add(new BasicNameValuePair(s, newValue));
            }
        } else {
            return null;
        }

        HttpEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(nameValuePairs);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return entity;
    }

}
