package com.example.Celonis_Service.Repo;

import com.example.Celonis_Service.Model.ActivityLog;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ActivityLogRepository
        extends ElasticsearchRepository<ActivityLog, String> {

    List<ActivityLog> findByProcessInstanceId(String processInstanceId);
    List<ActivityLog> findByStatus(String status);
    List<ActivityLog> findByIncidentTypeNotNull();
}