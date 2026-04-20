package com.example.Celonis_Service.DTO;

public class ProcessLogDTO {

    private Long processInstanceKey;
    private String activity;
    private String status;
    private String message;
    private Long durationMs;
    private String source;

    // getters & setters

    public Long getProcessInstanceKey() { return processInstanceKey; }
    public void setProcessInstanceKey(Long processInstanceKey) { this.processInstanceKey = processInstanceKey; }

    public String getActivity() { return activity; }
    public void setActivity(String activity) { this.activity = activity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}