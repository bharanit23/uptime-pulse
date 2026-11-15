package com.uptimepulse.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public class MonitorRequest {

    @NotBlank(message = "Monitor name is required")
    private String name;

    @NotBlank(message = "Target URL is required")
    @URL(message = "Please provide a valid HTTP/HTTPS URL")
    private String url;

    private String httpMethod = "GET";

    @Min(value = 10, message = "Interval must be at least 10 seconds")
    @Max(value = 86400, message = "Interval cannot exceed 24 hours")
    private int intervalSeconds = 60;

    @Min(value = 100, message = "Status code must be a valid HTTP status (>= 100)")
    @Max(value = 599, message = "Status code must be a valid HTTP status (<= 599)")
    private int expectedStatusCode = 200;

    public MonitorRequest() {}

    public MonitorRequest(String name, String url, String httpMethod, int intervalSeconds, int expectedStatusCode) {
        this.name = name;
        this.url = url;
        this.httpMethod = httpMethod;
        this.intervalSeconds = intervalSeconds;
        this.expectedStatusCode = expectedStatusCode;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }

    public int getIntervalSeconds() { return intervalSeconds; }
    public void setIntervalSeconds(int intervalSeconds) { this.intervalSeconds = intervalSeconds; }

    public int getExpectedStatusCode() { return expectedStatusCode; }
    public void setExpectedStatusCode(int expectedStatusCode) { this.expectedStatusCode = expectedStatusCode; }
}
