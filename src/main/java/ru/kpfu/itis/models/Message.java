package ru.kpfu.itis.models;

import com.fasterxml.jackson.annotation.*;
import ru.kpfu.itis.models.Details;
import ru.kpfu.itis.models.EventMetadata;

public class Message {
    private EventMetadata eventMetadata;
    private Details details;

    @JsonProperty("event_metadata")
    public EventMetadata getEventMetadata() { return eventMetadata; }
    @JsonProperty("event_metadata")
    public void setEventMetadata(EventMetadata value) { this.eventMetadata = value; }

    @JsonProperty("details")
    public Details getDetails() { return details; }
    @JsonProperty("details")
    public void setDetails(Details value) { this.details = value; }
}
