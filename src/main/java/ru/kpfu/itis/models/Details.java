package ru.kpfu.itis.models;

import com.fasterxml.jackson.annotation.*;

public class Details {
    private String bucketID;
    private String objectID;

    @JsonProperty("bucket_id")
    public String getBucketID() { return bucketID; }
    @JsonProperty("bucket_id")
    public void setBucketID(String value) { this.bucketID = value; }

    @JsonProperty("object_id")
    public String getObjectID() { return objectID; }
    @JsonProperty("object_id")
    public void setObjectID(String value) { this.objectID = value; }
}