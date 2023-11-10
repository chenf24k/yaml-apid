package cn.chenf24k.hr.model.entity;

import cn.chenf24k.hr.context.GlobalContext;
import cn.chenf24k.hr.model.enums.METHOD;
import cn.chenf24k.hr.tool.JsonUtil;
import cn.chenf24k.hr.tool.TemplateProcess;
import lombok.Data;
import lombok.NoArgsConstructor;
import ognl.Ognl;
import ognl.OgnlException;
//import org.apache.http.*;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.fluent.ContentResponseHandler;
//import org.apache.http.client.fluent.Request;
//import org.apache.http.client.utils.URIUtils;
//import org.apache.http.entity.ContentType;
//import org.apache.http.message.BasicHeader;
//import org.apache.http.message.BasicNameValuePair;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.fluent.ContentResponseHandler;
import org.apache.hc.client5.http.fluent.Executor;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.message.StatusLine;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Data
@NoArgsConstructor
//@Slf4j
public class HttpRequest {

    private METHOD method;
    private String url;
    private Map<String, String> header;
    private Map<String, String> body;
    private Map<String, String> query;

    private String contentType = "application/json";
    private Request request;
    private int httpStatus;
    private String response;
    private long duration;
    private String reason;
    final Executor executor = Executor.newInstance();

    public String request() {
        preProcessVars();
        handleQueryParam();
        method();
        headers();
        body();
        return execute();
    }

    public void headers() {
        List<Header> headers = new ArrayList<>();
        request.addHeader("user-agent", "YAML_APID");
        if (this.getHeader() == null || this.getHeader().isEmpty()) {
            return;
        }
        this.getHeader().forEach((key, value) -> {
            Header basicHeader = new BasicHeader(key, value);
            if (key.trim().toLowerCase().contains("content-type"))
                this.setContentType(value.trim().toLowerCase());
            headers.add(basicHeader);
        });
        headers.removeIf(header1 ->
                header1.getValue().contains("multipart/form-data")
        );
        request.setHeaders(headers.toArray(new Header[0]));
    }

    public void handleQueryParam() {
        if (this.getQuery() == null || this.getQuery().isEmpty()) {
            return;
        }
        StringBuilder queryParams = new StringBuilder();


        this.getQuery().forEach((key, value) -> {
            if (queryParams.lastIndexOf("&") != url.length() - 1) {
                queryParams.append("&");
            }
            queryParams.append(key).append("=").append(value);
        });

        // 如果url的最后一位刚好是?，可以直接添加参数
        if (this.getUrl().lastIndexOf("?") != this.getUrl().length() - 1) {
            // 判断是否存在?
            if (this.getUrl().contains("?")) {
                this.setUrl(this.getUrl() + queryParams);
            } else {
                this.setUrl(this.getUrl() + "?" + queryParams);
            }
        }
        String url1 = this.getUrl();
        System.out.println(url1);
    }

    public void method() {
        switch (this.getMethod()) {
            case GET:
                request = Request.get(this.getUrl());
                break;
            case POST:
                request = Request.post(this.getUrl());
                break;
            case PUT:
                request = Request.put(this.getUrl());
                break;
            case DELETE:
                request = Request.delete(this.getUrl());
            default:
                throw new RuntimeException("This method is not supported: " + this.getMethod());
        }
    }

    public void preProcessVars() {
        GlobalContext globalContext = GlobalContext.getInstance();
        Map<String, String> temp = new HashMap<>();

        List<String> extracted = new LinkedList<>();

        if (this.getUrl() != null) // url 处理
            extracted.addAll(TemplateProcess.extractAllTemplate(this.getUrl()));
        if (this.getQuery() != null) // query 处理
            extracted.addAll(TemplateProcess.extractAllTemplate(this.getQuery().values().toString()));
        if (this.getHeader() != null) // header 处理
            extracted.addAll(TemplateProcess.extractAllTemplate(this.getHeader().values().toString()));
        if (this.getBody() != null) // body 处理
            extracted.addAll(TemplateProcess.extractAllTemplate(this.getBody().values().toString()));

        for (String template : extracted) {
            Object value = null;
            try {
                value = Ognl.getValue(template, globalContext);
            } catch (OgnlException ignored) {

            }
            temp.put(template, String.valueOf(value));
        }

        // url
        String newUrl = TemplateProcess.processTemplate(this.getUrl(), temp);
        this.setUrl(newUrl);

        // query
        if (this.getQuery() != null) {
            this.getQuery().forEach((key, value) -> {
                String newValue = TemplateProcess.processTemplate(value, temp);
                this.getQuery().replace(key, newValue);
            });
        }

        // header
        if (this.getHeader() != null) {
            this.getHeader().forEach((key, value) -> {
                String newValue = TemplateProcess.processTemplate(value, temp);
                this.getHeader().replace(key, newValue);
            });
        }

        // body
        if (this.getBody() != null) {
            this.getBody().forEach((key, value) -> {
                String newValue = TemplateProcess.processTemplate(value, temp);
                this.getBody().replace(key, newValue);
            });
        }

    }

    public void body() {
        if (this.getBody() == null || this.getBody().isEmpty()) {
            return;
        }
        if (this.getContentType().contains("application/json")) {
            String jsonString = JsonUtil.toJsonString(this.getBody());
            request.bodyString(jsonString, ContentType.APPLICATION_JSON);
        }
        if (this.getContentType().contains("application/x-www-form-urlencoded")) {
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            Set<String> keySet = this.getBody().keySet();
            for (String s : keySet) {
                String value = this.getBody().get(s);
                nameValuePairs.add(new BasicNameValuePair(s, value));
            }
            HttpEntity entity = null;
            entity = new UrlEncodedFormEntity(nameValuePairs);
            request.body(entity);
        }
        if (this.getContentType().contains("multipart/form-data")) {
            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
            this.getBody().forEach((k, v) -> {
                if (k.equals("file")) {
                    multipartEntityBuilder.addBinaryBody("file", new File(v));
                } else {
                    multipartEntityBuilder.addTextBody(k, v);
                }
            });
            request.body(multipartEntityBuilder.build());
        }
    }

    public void printRequestInfo() {
        Map<String, Object> requestInfo = new LinkedHashMap<>();
        requestInfo.put("url", this.getUrl());
        requestInfo.put("method", this.getMethod());
        requestInfo.put("header", this.getHeader());
        requestInfo.put("body", this.getBody());
        // System.out.println("request: \r\n" + JsonUtil.pretty(JsonUtil.toJsonString(requestInfo)));
    }

    public void printResponseInfo() {
        Map<String, Object> responseInfo = new LinkedHashMap<>();
        responseInfo.put("duration", this.getDuration());
        responseInfo.put("httpCode", this.getHttpStatus());
        responseInfo.put("responseText", this.getResponse());
        responseInfo.put("reason", this.getReason());
        // System.out.println("response: \r\n" + JsonUtil.pretty(JsonUtil.toJsonString(responseInfo)));
    }

    public String execute() {
        printRequestInfo();
        String response = "";
        LocalDateTime startTime = LocalDateTime.now();
        try {
            // TODO 响应中无法保留数字类型
            response = executor.execute(
                            request.useExpectContinue()
                                    .version(HttpVersion.HTTP_1_1)
                    ).returnContent()
                    .asString(StandardCharsets.UTF_8);

            // System.out.println("Time Interval in Milliseconds: " + milliseconds + " ms");
            // log.info("Time Interval in Milliseconds: {} ms", milliseconds);
        } catch (IOException e) {
            e.printStackTrace();
            this.setReason(e.getMessage());
        } finally {
            LocalDateTime endTime = LocalDateTime.now();
            Duration duration = Duration.between(startTime, endTime);
            this.setDuration(duration.toMillis());
        }
        printResponseInfo();
        return response;
    }

}
