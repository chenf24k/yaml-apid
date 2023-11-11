package cn.chenf24k.hr.model.entity;

import cn.chenf24k.hr.context.GlobalContext;
import cn.chenf24k.hr.model.enums.METHOD;
import cn.chenf24k.hr.tool.JsonUtil;
import cn.chenf24k.hr.tool.TemplateProcess;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ognl.Ognl;
import ognl.OgnlException;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.fluent.*;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicNameValuePair;


import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Data
@NoArgsConstructor
@Slf4j
public class HttpRequest {

    private METHOD method;
    private String url;
    private Map<String, String> header;
    private Map<String, String> body;
    private Map<String, String> query;

    private String contentType = "application/json";
    private Request request;

    private CustomResponse customResponse;
    final Executor executor = Executor.newInstance();

    public CustomResponse request() {
        preProcessVars();
        handleQueryParam();
        handleHttpMethod();
        handleHeaders();
        handleBody();
        return execute();
    }

    public void handleHeaders() {
        request.addHeader("user-agent", "YAML_APID");
        if (this.getHeader() == null || this.getHeader().isEmpty()) {
            return;
        }
        List<Header> headers = new ArrayList<>();
        this.getHeader().forEach((key, value) -> {
            Header basicHeader = new BasicHeader(key, value);
            if (key.trim().toLowerCase().contains("content-type"))
                this.setContentType(value.trim().toLowerCase());
            headers.add(basicHeader);
        });
        headers.removeIf(dataHeader -> dataHeader.getValue().contains("multipart/form-data"));
        request.setHeaders(headers.toArray(new Header[0]));
    }

    public void handleQueryParam() {
        if (this.getQuery() == null || this.getQuery().isEmpty()) {
            return;
        }
        StringBuilder queryParams = new StringBuilder();
        this.getQuery().forEach((key, value) -> {
            queryParams.append("&").append(key).append("=").append(value);
        });

        String paramsString = queryParams.toString();
        paramsString = paramsString.replaceFirst("&", "");

        // 如果url的最后一位刚好是?，可以直接添加参数
        if (this.getUrl().lastIndexOf("?") > -1)
            this.setUrl(this.getUrl() + paramsString);
        else
            this.setUrl(this.getUrl() + "?" + paramsString);
    }

    public void handleHttpMethod() {
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

    public void handleBody() {
        if (this.getBody() == null || this.getBody().isEmpty()) {
            return;
        }
        if (this.getContentType().contains("application/json")) {
            String jsonString = JsonUtil.toJsonString(this.getBody());
            request.bodyString(jsonString, ContentType.APPLICATION_JSON);
        }
        if (this.getContentType().contains("application/x-www-form-urlencoded")) {
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            for (Map.Entry<String, String> entry : this.getBody().entrySet()) {
                nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            HttpEntity entity;
            entity = new UrlEncodedFormEntity(nameValuePairs);
            request.body(entity);
        }
        if (this.getContentType().contains("multipart/form-data")) {
            final MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
            for (Map.Entry<String, String> entry : this.getBody().entrySet()) {
                if (entry.getKey().equalsIgnoreCase("file")) {
                    // TODO 考虑如果上传为其它关键字如何处理
                    multipartEntityBuilder.addBinaryBody("file", new File(entry.getValue()));
                } else {
                    multipartEntityBuilder.addTextBody(entry.getKey(), entry.getValue());
                }
            }
            request.body(multipartEntityBuilder.build());
        }
    }

    public void printRequestInfo() {
        log.info("[{}] {}", this.getMethod(), this.getUrl());
        log.info("Header: {}", this.getHeader());
        log.info("Body: {}", this.getBody());
    }

    public void printResponseInfo(CustomResponse response) {
        log.info("Http Status: {}", response.getStatus());
        log.info("Duration: {} ms", response.getDuration());
        log.info("Response: {}", response.getResponse());
    }

    public CustomResponse execute() {
        printRequestInfo();
        CustomResponse response = new CustomResponse();
        LocalDateTime startTime = LocalDateTime.now();
        try {
            log.info("execute ......");
            // TODO 响应中无法保留数字类型
            Response executed = executor.execute(request.useExpectContinue().version(HttpVersion.HTTP_1_1));
            response = executed.handleResponse(classicHttpResponse -> {
                CustomResponse customResponse = new CustomResponse();
                final int status = classicHttpResponse.getCode();
                customResponse.setStatus(status);
                final HttpEntity entity = classicHttpResponse.getEntity();
                if (status >= HttpStatus.SC_REDIRECTION) {
                    customResponse.setReason(classicHttpResponse.getReasonPhrase());
                }
                if (entity == null) {
                    return customResponse;
                }
                String stringify = entityStringify(entity);
                customResponse.setResponse(stringify);
                return customResponse;
            });
        } catch (IOException e) {
            response.setReason(e.getMessage());
        } finally {
            LocalDateTime endTime = LocalDateTime.now();
            Duration duration = Duration.between(startTime, endTime);
            response.setDuration(duration.toMillis());
        }
        printResponseInfo(response);
        return response;
    }

    /**
     * HttpEntity转字符串
     *
     * @param entity HttpEntity
     * @return String
     * @auther chenf24k
     */
    private String entityStringify(HttpEntity entity) {
        StringBuilder buf = null;
        try {
            Reader reader = new InputStreamReader(entity.getContent());
            int ch;
            buf = new StringBuilder();
            while ((ch = reader.read()) != -1) {
                buf.append((char) ch);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (buf == null) return null;
        return buf.toString();
    }

}
