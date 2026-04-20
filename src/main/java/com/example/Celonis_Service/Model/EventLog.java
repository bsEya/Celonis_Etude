package com.example.Celonis_Service.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_log")
public class EventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String caseId;              // processInstanceKey
    private String activity;            // BPMN task name
    private String eventType;           // START / END / ERROR
    private String status;              // SUCCESS / FAILED
    private LocalDateTime timestamp;
    private Long durationMs;
    private String resource;            // worker/service name

    public Long getId() {
        return id;
    }

    public String getCaseId() {
        return caseId;
    }

    public String getActivity() {
        return activity;
    }

    public String getEventType() {
        return eventType;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public String getResource() {
        return resource;
    }


    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }
}