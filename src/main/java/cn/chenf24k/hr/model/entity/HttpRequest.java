package cn.chenf24k.hr.model.entity;

import cn.chenf24k.hr.context.GlobalContext;
import cn.chenf24k.hr.model.enums.METHOD;
import cn.chenf24k.hr.tool.JsonUtil;
import cn.chenf24k.hr.tool.TemplateProcess;
import lombok.Data;
import lombok.NoArgsConstructor;
import ognl.Ognl;
import ognl.OgnlException;
import org.apache.http.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.fluent.ContentResponseHandler;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

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

    public String request() {
        preProcessVars();
        method();
        headers();
        body();
        return execute();
    }

    public void headers() {
        if (this.getHeader() == null || this.getHeader().isEmpty()) {
            return;
        }
        List<Header> headers = new ArrayList<>(this.getHeader().size());
        headers.add(new BasicHeader("user-agent", "yaml-apid"));
        this.getHeader().forEach((key, value) -> {
            Header basicHeader = new BasicHeader(key, value);
            if (key.trim().toLowerCase().contains("content-type"))
                this.setContentType(value.trim().toLowerCase());
            headers.add(basicHeader);
        });
        request.setHeaders(headers.toArray(new Header[0]));
    }

    public void method() {
        switch (this.getMethod()) {
            case GET:
                request = Request.Get(this.getUrl());
                break;
            case POST:
                request = Request.Post(this.getUrl());
                break;
            case PUT:
                request = Request.Put(this.getUrl());
                break;
            case DELETE:
                request = Request.Delete(this.getUrl());
            default:
                throw new RuntimeException("This method is not supported: " + this.getMethod());
        }
    }

    public void preProcessVars() {
        GlobalContext globalContext = GlobalContext.getInstance();
        Map<String, String> temp = new HashMap<>();

        List<String> extracted = TemplateProcess.extractAllTemplate(this.getUrl());
        if (this.getHeader() != null)
            extracted.addAll(TemplateProcess.extractAllTemplate(this.getHeader().values().toString()));
        if (this.getBody() != null)
            extracted.addAll(TemplateProcess.extractAllTemplate(this.getBody().values().toString()));

        for (String template : extracted) {
            Object value = null;
            try {
                value = Ognl.getValue(template, globalContext);
            } catch (OgnlException ignored) {

            }
            temp.put(template, (String) value);
        }
        String newUrl = TemplateProcess.processTemplate(this.getUrl(), temp);
        this.setUrl(newUrl);

        if (this.getHeader() != null) {
            this.getHeader().forEach((key, value) -> {
                String newValue = TemplateProcess.processTemplate(value, temp);
                this.getHeader().replace(key, newValue);
            });
        }
        if (this.getBody() != null) {
            this.getBody().forEach((key, value) -> {
                String newValue = TemplateProcess.processTemplate(value, temp);
                this.getBody().replace(key, newValue);
            });
        }
    }

    public void body() {
        if (this.getBody() == null) {
            return;
        }
        if (this.getContentType().contains("application/json")) {
            String jsonString = JsonUtil.toJsonString(this.getBody());
            request.bodyString(jsonString, ContentType.APPLICATION_JSON);
        }
        if (this.getContentType().contains("application/x-www-form-urlencoded")) {
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            if (!this.getBody().isEmpty()) {
                Set<String> keySet = this.getBody().keySet();
                for (String s : keySet) {
                    String value = this.getBody().get(s);
                    nameValuePairs.add(new BasicNameValuePair(s, value));
                }
            }
            HttpEntity entity = null;
            try {
                entity = new UrlEncodedFormEntity(nameValuePairs);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            request.body(entity);
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
            HttpResponse httpResponse = request.execute().returnResponse();
            StatusLine statusLine = httpResponse.getStatusLine();
            setHttpStatus(statusLine.getStatusCode());

            response = new ContentResponseHandler()
                    .handleResponse(httpResponse)
                    .asString(StandardCharsets.UTF_8);
            setResponse(response);

            // System.out.println("Time Interval in Milliseconds: " + milliseconds + " ms");
            // log.info("Time Interval in Milliseconds: {} ms", milliseconds);
        } catch (IOException e) {
            // e.printStackTrace();
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
