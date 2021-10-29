package ru.kpfu.itis.models;

import com.fasterxml.jackson.annotation.*;

public class TracingContext {
    private String traceID;
    private String spanID;
    private String parentSpanID;

    @JsonProperty("trace_id")
    public String getTraceID() { return traceID; }
    @JsonProperty("trace_id")
    public void setTraceID(String value) { this.traceID = value; }

    @JsonProperty("span_id")
    public String getSpanID() { return spanID; }
    @JsonProperty("span_id")
    public void setSpanID(String value) { this.spanID = value; }

    @JsonProperty("parent_span_id")
    public String getParentSpanID() { return parentSpanID; }
    @JsonProperty("parent_span_id")
    public void setParentSpanID(String value) { this.parentSpanID = value; }
}