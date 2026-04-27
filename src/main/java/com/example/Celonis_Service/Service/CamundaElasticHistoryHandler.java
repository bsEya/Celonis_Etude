package com.example.Celonis_Service.Service;

import com.example.Celonis_Service.Model.ActivityLog;
import com.example.Celonis_Service.Repo.ActivityLogRepository;
import org.camunda.bpm.engine.impl.history.event.HistoricActivityInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricIncidentEventEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;

import java.util.List;

@Component
public class CamundaElasticHistoryHandler implements HistoryEventHandler {

    private final ActivityLogRepository repository;

    // @Lazy évite que Spring essaie d'instancier ES avant que le contexte soit prêt
    public CamundaElasticHistoryHandler(@Lazy ActivityLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public void handleEvent(HistoryEvent event) {
        try {
            ActivityLog log = new ActivityLog();
            log.setId(event.getId());
            log.setProcessInstanceId(event.getProcessInstanceId());

            if (event instanceof HistoricActivityInstanceEventEntity activity) {
                log.setActivityName(activity.getActivityName());
                log.setActivityType(activity.getActivityType());
                log.setStatus(activity.getActivityInstanceState() == 0 ? "completed" : "active");
                if (activity.getStartTime() != null)
                    log.setStartTime(activity.getStartTime().toInstant());
                if (activity.getEndTime() != null) {
                    log.setEndTime(activity.getEndTime().toInstant());
                    log.setDurationInMillis(activity.getDurationInMillis());
                }
            }

            if (event instanceof HistoricIncidentEventEntity incident) {
                log.setStatus("failed");
                log.setIncidentType(incident.getIncidentType());
                log.setIncidentMessage(incident.getIncidentMessage());
            }

            repository.save(log);

        } catch (Exception e) {
            // on ne bloque pas le moteur Camunda si ES est indisponible
            System.err.println("Erreur indexation ES : " + e.getMessage());
        }
    }

    @Override
    public void handleEvents(List<HistoryEvent> events) {
        events.forEach(this::handleEvent);
    }
}

//@Component
//public class CamundaElasticHistoryHandler implements HistoryEventHandler {
//
//    @Autowired
//    private ActivityLogRepository repository;
//
//    @Override
//    public void handleEvent(HistoryEvent event) {
//
//        ActivityLog log = new ActivityLog();
//        log.setId(event.getId());
//        log.setProcessInstanceId(event.getProcessInstanceId());
//
//        // --- Activité (start / end / update)
//        if (event instanceof HistoricActivityInstanceEventEntity activity) {
//            log.setActivityId(activity.getActivityId());
//            log.setActivityName(activity.getActivityName());
//            log.setActivityType(activity.getActivityType());
//            log.setStatus(activity.getActivityInstanceState() == 0 ? "completed" : "active");
//            log.setStartTime(activity.getStartTime().toInstant());
//            if (activity.getEndTime() != null) {
//                log.setEndTime(activity.getEndTime().toInstant());
//                log.setDurationInMillis(activity.getDurationInMillis());
//            }
//        }
//
//        // --- Incident
//        if (event instanceof HistoricIncidentEventEntity incident) {
//            log.setStatus("failed");
//            log.setIncidentType(incident.getIncidentType());
//            log.setIncidentMessage(incident.getIncidentMessage());
//        }
//
//        repository.save(log);
//    }
//
//    @Override
//    public void handleEvents(List<HistoryEvent> events) {
//        events.forEach(this::handleEvent);
//    }
//}