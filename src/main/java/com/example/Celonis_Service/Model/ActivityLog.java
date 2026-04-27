package com.example.Celonis_Service.Model;



import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.Instant;

@Document(indexName = "camunda-activity-logs")
public class ActivityLog {

    @Id
    private String id;

    private String processInstanceId;
    private String processDefinitionKey;
    private String activityId;
    private String activityName;
    private String activityType;
    private String status;         // completed, failed, active
    private String incidentType;   // null si pas d'incident
    private String incidentMessage;

    private Instant startTime;
    private Instant endTime;
    private Long durationInMillis;

    // getters / setters





    public void setIncidentMessage(String incidentMessage) {
        this.incidentMessage = incidentMessage;
    }

    public void setIncidentType(String incidentType) {
        this.incidentType = incidentType;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public void setActivityId(String activityId) {  this.activityId = activityId; }
    public void setActivityName(String activityName) {  this.activityName = activityName; }
    public void setActivityType(String activityType) { this.activityType = activityType; }
    public void setProcessInstanceId(String processInstanceId) { this.processInstanceId = processInstanceId; }
    public void setProcessDefinitionKey(String processDefinitionKey) { this.processDefinitionKey = processDefinitionKey; }


    public void setStartTime(Instant instant) {this.startTime = instant;}
    public void setEndTime(Instant instant) {this.endTime = instant;}
    public void setDurationInMillis(Long durationInMillis) {this.durationInMillis = durationInMillis;}
    public String getId() { return id; }
    public String getProcessInstanceId() { return processInstanceId; }
    public String getProcessDefinitionKey() { return processDefinitionKey; }
    public String getActivityId() { return activityId; }
    public String getActivityName() { return activityName; }
    public String getActivityType() { return activityType; }
    public String getStatus() { return status; }
    public String getIncidentType() { return incidentType; }
    public String getIncidentMessage() { return incidentMessage; }
    public Instant getStartTime() { return startTime; }
    public Instant getEndTime() { return endTime; }
    public Long getDurationInMillis() { return durationInMillis; }
    public void setId(String id) { this.id = id; }

}
