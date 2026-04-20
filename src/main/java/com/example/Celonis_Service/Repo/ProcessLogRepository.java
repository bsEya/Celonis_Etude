package com.example.Celonis_Service.Repo;

import com.example.Celonis_Service.Model.ProcessLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessLogRepository extends JpaRepository<ProcessLog, Long> {
}