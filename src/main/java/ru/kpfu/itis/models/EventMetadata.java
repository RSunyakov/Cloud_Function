package ru.kpfu.itis.models;

import com.fasterxml.jackson.annotation.*;

public class EventMetadata {
    private String eventID;
    private String eventType;
    private String createdAt;
    private TracingContext tracingContext;
    private String cloudID;
    private String folderID;

    @JsonProperty("event_id")
    public String getEventID() { return eventID; }
    @JsonProperty("event_id")
    public void setEventID(String value) { this.eventID = value; }

    @JsonProperty("event_type")
    public String getEventType() { return eventType; }
    @JsonProperty("event_type")
    public void setEventType(String value) { this.eventType = value; }

    @JsonProperty("created_at")
    public String getCreatedAt() { return createdAt; }
    @JsonProperty("created_at")
    public void setCreatedAt(String value) { this.createdAt = value; }

    @JsonProperty("tracing_context")
    public TracingContext getTracingContext() { return tracingContext; }
    @JsonProperty("tracing_context")
    public void setTracingContext(TracingContext value) { this.tracingContext = value; }

    @JsonProperty("cloud_id")
    public String getCloudID() { return cloudID; }
    @JsonProperty("cloud_id")
    public void setCloudID(String value) { this.cloudID = value; }

    @JsonProperty("folder_id")
    public String getFolderID() { return folderID; }
    @JsonProperty("folder_id")
    public void setFolderID(String value) { this.folderID = value; }
}