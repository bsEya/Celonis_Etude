package com.example.Celonis_Service.Service;

import com.example.Celonis_Service.Model.ProcessLog;
import com.example.Celonis_Service.Repo.ProcessLogRepository;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import io.camunda.zeebe.client.ZeebeClient;
import jakarta.annotation.PostConstruct;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component
public class VerifyApplicantWorker {

    private final ZeebeClient zeebeClient;
    private final ProcessLogRepository logRepo;
    private final RestTemplate restTemplate;
    private static final Logger logger = LoggerFactory.getLogger(VerifyApplicantWorker.class);


    @Value("${product.url}")
    private String productUrl;

    public VerifyApplicantWorker(ZeebeClient zeebeClient,
                                 ProcessLogRepository logRepo) {
        this.zeebeClient = zeebeClient;
        this.logRepo = logRepo;
        this.restTemplate = new RestTemplate();
    }

    @PostConstruct   // Lancement automatique duu worker
    public void startWorker() {

        zeebeClient.newWorker()
                .jobType("verify-applicant")
                .handler(this::handleJob)
                .open();

        System.out.println(" Worker started");
    }

    private void handleJob(JobClient jobClient, ActivatedJob job) {

        long start = System.currentTimeMillis();

        ProcessLog log = initLog(job);

        try {

            // Récupère les variables du process Camunda
            Map<String, Object> vars = job.getVariablesAsMap();

            int salary = getInt(vars, "salary");
            int amount = getInt(vars, "amount");

            //  API CALL SAFE
            Map<String, Object> product = fetchProduct();

            //  BUSINESS CHECK API RESPONSE
            if (product.containsKey("message")) {

                log.setStatus("REJECTED");
                log.setMessage("Product not found");

                completeJob(jobClient, job, false);

                return;
            }

            // DATA EXTRACTION
            String title = getString(product, "title");
            double price = getDouble(product, "price");
            double rating = getDouble(product, "rating");
            int stock = getInt(product, "stock");

            // décision  dossier est approuvé si:
            boolean approved =
                    salary > 1200 &&
                            amount < 20000 &&
                            rating >= 2.0 &&
                            stock > 0;

            // Indique à Camunda que la tâche est terminée & Renvoie des variables au process
            completeJob(jobClient, job, approved);

            log.setStatus(approved ? "APPROVED" : "REJECTED");
            log.setMessage(approved
                    ? "Approved by business rules"
                    : "Rejected by rules");

//        } catch (Exception e) {
//
//            //  INCIDENT TECHNIQUE
//            failJob(jobClient, job, e);
//
//            log.setStatus("INCIDENT");
//            log.setMessage(e.getMessage());
//
        } catch (NumberFormatException e) {

            log.setStatus("PARSING_ERROR");
            log.setMessage("Invalid number format: " + e.getMessage());

            logger.error("PARSING ERROR | process={} | {}",
                    job.getProcessInstanceKey(), e.getMessage());

            failJob(jobClient, job, e);

        } catch (RuntimeException e) {

            if (e.getMessage() != null && e.getMessage().contains("API_ERROR")) {

                log.setStatus("API_ERROR");
                log.setMessage(e.getMessage());

                logger.error("API ERROR | process={} | {}",
                        job.getProcessInstanceKey(), e.getMessage());

            } else {

                log.setStatus("TECHNICAL_ERROR");
                log.setMessage(e.getMessage());

                logger.error("TECHNICAL ERROR | process={} | {}",
                        job.getProcessInstanceKey(), e.getMessage());
            }

            failJob(jobClient, job, e);

        } catch (Exception e) {

            log.setStatus("UNKNOWN_ERROR");
            log.setMessage(e.getMessage());

            logger.error("UNKNOWN ERROR | process={} | {}",
                    job.getProcessInstanceKey(), e.getMessage(), e);

            failJob(jobClient, job, e);

        } finally {

            log.setDurationMs(System.currentTimeMillis() - start);
            saveLog(log);
        }
    }

    // API SAFE CALL
    private Map<String, Object> fetchProduct() {
        try {
            logger.info("Calling product API: {}", productUrl);
            return restTemplate.getForObject(productUrl, Map.class);

        } catch (Exception e) {
            logger.warn("API CALL FAILED: {}", e.getMessage());

            throw new RuntimeException("API_ERROR: " + e.getMessage());
        }
    }

    // COMPLETE JOB
    private void completeJob(JobClient jobClient,
                             ActivatedJob job,
                             boolean approved) {

        jobClient.newCompleteCommand(job.getKey())
                .variables(Map.of("approved", approved))
                .send()
                .join();
    }

    //  FAIL JOB
    private void failJob(JobClient jobClient,
                         ActivatedJob job,
                         Exception e) {

        jobClient.newFailCommand(job.getKey())
                .retries(Math.max(0, job.getRetries() - 1))
                .errorMessage(e.getMessage())
                .send()
                .join();
    }

    //  INIT LOG
    private ProcessLog initLog(ActivatedJob job) {
        ProcessLog log = new ProcessLog();
        log.setProcessInstanceKey(job.getProcessInstanceKey());
        log.setActivity(job.getElementId());
        log.setSource("CAMUNDA");
        log.setTimestamp(LocalDateTime.now());
        return log;
    }

    //  SAVE LOG SAFE
//    private void saveLog(ProcessLog log) {
//        try {
//            logRepo.save(log);
////            System.out.println("LOG SAVED: " + log.getStatus());// le27/04
//            logger.info("LOG SAVED: {}", log.getStatus());
//        } catch (Exception e) {
//            System.out.println(" LOG SAVE FAILED: " + e.getMessage());
//        }
//    }
private void saveLog(ProcessLog log) {
    try {
        // 1. Sauvegarde en base
        logRepo.save(log);

        // 2. Générer ID unique
        String logId = UUID.randomUUID().toString();

        // 3. Sauvegarde fichier
        logger.info("LOG_ID={} | STATUS={} | ACTIVITY={} | MESSAGE={} | DURATION={}ms",
                logId,
                log.getStatus(),
                log.getActivity(),
                log.getMessage(),
                log.getDurationMs());


    } catch (Exception e) {
        logger.error("LOG SAVE FAILED", e);
    }
}

    //  SAFE PARSERS
    private int getInt(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v == null ? 0 : Integer.parseInt(v.toString());
    }

    private double getDouble(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v == null ? 0.0 : Double.parseDouble(v.toString());
    }

    private String getString(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v == null ? "UNKNOWN" : v.toString();
    }
}

