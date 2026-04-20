package com.example.Celonis_Service.Service;

import com.example.Celonis_Service.Model.ProcessLog;
import com.example.Celonis_Service.Repo.ProcessLogRepository;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class EligibilityWorker {

    private final ProcessLogRepository logRepo;
    private final ZeebeClient zeebeClient;

    public EligibilityWorker(ProcessLogRepository logRepo) {

        this.logRepo = logRepo;

        this.zeebeClient = ZeebeClient.newClientBuilder()
                .gatewayAddress("bd4312ae-4eef-499a-a7ff-deb045cda011.lhr-1.zeebe.camunda.io:443")
                .credentialsProvider(
                        new OAuthCredentialsProviderBuilder()
                                .clientId("4agKJTcJ~T1o2RyRA_2eufxYchBeu4_K")
                                .clientSecret("hzI3TSMxDEoizUhxye7ooZIWSX1rZtUQXGXAUap3Gyqc7o_4M3K11ydATuthqI1J")
                                .audience("zeebe.camunda.io")
                                .build()
                )
                .build();
    }

    @PostConstruct
    public void startWorker() {

        System.out.println(" Worker connected to Camunda Cloud");

        zeebeClient.newWorker()
                .jobType("check-eligibility")
                .handler(this::handleJob)
                .open();
    }
    private int getInt(Map<String, Object> vars, String key) {
        Object value = vars.get(key);

        if (value == null) {
            throw new RuntimeException("Missing variable: " + key);
        }

        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid integer for: " + key);
        }
    }

    private void handleJob(JobClient jobClient, ActivatedJob job) {

        long start = System.currentTimeMillis();

        ProcessLog log = new ProcessLog();
        log.setProcessInstanceKey(job.getProcessInstanceKey());
        log.setActivity(job.getElementId());
        log.setTimestamp(LocalDateTime.now());
        log.setStatus("STARTED");

        try {
            Map<String, Object> vars = job.getVariablesAsMap();

            int salary = getInt(vars, "salary");

            boolean isEligible = salary > 1000;

            jobClient.newCompleteCommand(job.getKey())
                    .variables(Map.of("isEligible", isEligible))
                    .send()
                    .join();

            log.setStatus("SUCCESS");
            log.setMessage("Eligibilité OK");

        } catch (Exception e) {

            jobClient.newFailCommand(job.getKey())
                    .retries(Math.max(0, job.getRetries() - 1))
                    .errorMessage(e.getMessage())
                    .send()
                    .join();

            log.setStatus("ERROR");
            log.setMessage(e.getMessage());

        } finally {
            log.setDurationMs(System.currentTimeMillis() - start);
            logRepo.save(log);
        }
    }
//
//    private void handleJob(JobClient jobClient, ActivatedJob job) {
//
//        long start = System.currentTimeMillis();
//
//        try {
//            Map<String, Object> vars = job.getVariablesAsMap();
//
//            System.out.println("📥 Variables reçues : " + vars);
//
//            int salary = Integer.parseInt(vars.getOrDefault("salary", "0").toString());
//
//            boolean isEligible = salary > 1000;
//
//            saveLog(job, "SUCCESS", "Eligibilité OK", start, "CAMUNDA");
//
//            System.out.println(" JOB RECEIVED: " + job.getElementId());
//
//            jobClient.newCompleteCommand(job.getKey())
//                    .variables(Map.of("isEligible", isEligible))
//                    .send()
//                    .join();
//
//        } catch (Exception e) {
//
//            saveLog(job, "ERROR", e.getMessage(), start, "CAMUNDA");
//
//            jobClient.newFailCommand(job.getKey())
//                    .retries(job.getRetries() - 1)
//                    .errorMessage(e.getMessage())
//                    .send()
//                    .join();
//        }
//    }


    private void saveLog(ActivatedJob job, String status, String msg, long start, String source) {

        ProcessLog log = new ProcessLog();

        log.setProcessInstanceKey(job.getProcessInstanceKey());
        log.setActivity(job.getElementId());
        log.setStatus(status);
        log.setMessage(msg);
        log.setSource(source);
        log.setTimestamp(LocalDateTime.now());
        log.setDurationMs(System.currentTimeMillis() - start);

        logRepo.save(log);

        System.out.println("🧾 LOG SAVED: " + status + " | " + msg);
    }
}


//@Component
//public class EligibilityWorker {
//
//    private final ProcessLogRepository logRepo;
//
//    public EligibilityWorker(ProcessLogRepository logRepo) {
//        this.logRepo = logRepo;
//    }
//
//    @JobWorker(type = "check-eligibility")
//    public void handle(JobClient jobClient, ActivatedJob job) {
//
//        long start = System.currentTimeMillis();
//
//        try {
//            Map<String, Object> vars = job.getVariablesAsMap();
//
//            int salary = Integer.parseInt(vars.getOrDefault("salary", "0").toString());
//
//            boolean isEligible = salary > 1000;
//
//            saveLog(job, "SUCCESS", "OK", start, "CAMUNDA");
//
//            jobClient.newCompleteCommand(job.getKey())
//                    .variables(Map.of("isEligible", isEligible))
//                    .send()
//                    .join();
//
//        } catch (Exception e) {
//
//            saveLog(job, "ERROR", e.getMessage(), start, "CAMUNDA");
//
//            jobClient.newFailCommand(job.getKey())
//                    .retries(job.getRetries() - 1)
//                    .errorMessage(e.getMessage())
//                    .send()
//                    .join();
//        }
//    }
//
//    private void saveLog(ActivatedJob job, String status, String msg, long start, String source) {
//
//        ProcessLog log = new ProcessLog();
//
//        log.setProcessInstanceKey(job.getProcessInstanceKey());
//        log.setActivity(job.getElementId());
//        log.setStatus(status);
//        log.setMessage(msg);
//        log.setSource(source);
//        log.setTimestamp(LocalDateTime.now());
//        log.setDurationMs(System.currentTimeMillis() - start);
//
//        logRepo.save(log);
//
//        System.out.println("🧾 LOG SAVED: " + status);
//    }
//}