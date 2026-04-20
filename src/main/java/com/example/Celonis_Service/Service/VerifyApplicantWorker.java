package com.example.Celonis_Service.Service;

import com.example.Celonis_Service.Model.ProcessLog;
import com.example.Celonis_Service.Repo.ProcessLogRepository;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import lombok.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import io.camunda.zeebe.client.ZeebeClient;
import jakarta.annotation.PostConstruct;
import org.springframework.web.client.RestTemplate;

@Component
public class VerifyApplicantWorker {

    private final ZeebeClient zeebeClient;
    private final ProcessLogRepository logRepo;
    private final RestTemplate restTemplate;

    private static final String PRODUCT_URL =
            "https://dummyjson.com/products/99999999999999";

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

            // RULES
            boolean approved =
                    salary > 1200 &&
                            amount < 20000 &&
                            rating >= 2.0 &&
                            stock > 0;

            // CAMUNDA TERMINEE
            completeJob(jobClient, job, approved);

            log.setStatus(approved ? "APPROVED" : "REJECTED");
            log.setMessage(approved
                    ? "Approved by business rules"
                    : "Rejected by rules");

        } catch (Exception e) {

            // 🔴 INCIDENT TECHNIQUE
            failJob(jobClient, job, e);

            log.setStatus("INCIDENT");
            log.setMessage(e.getMessage());

        } finally {

            log.setDurationMs(System.currentTimeMillis() - start);
            saveLog(log);
        }
    }

    // API SAFE CALL
    private Map<String, Object> fetchProduct() {
        try {
            return restTemplate.getForObject(PRODUCT_URL, Map.class);
        } catch (Exception e) {
            System.out.println("⚠️ API FAILED: " + e.getMessage());
            return Map.of("message", "API_ERROR");
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
    private void saveLog(ProcessLog log) {
        try {
            logRepo.save(log);
            System.out.println("🧾 LOG SAVED: " + log.getStatus());
        } catch (Exception e) {
            System.out.println("❌ LOG SAVE FAILED: " + e.getMessage());
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

//                                "https://dummyjson.com/product/10000000000",
//                                Map.class
//                        );
//
//                        double price = Double.parseDouble(product.get("price").toString());
//                        double rating = Double.parseDouble(product.get("rating").toString());
//                        int stock = Integer.parseInt(product.get("stock").toString());
//                        String title = product.get("title").toString();
//
//                        // décision  dossier est approuvé si:
//                        boolean approved =
//                                salary > 1200 &&
//                                        amount < 20000 &&
//                                        rating >= 2.0 &&
//                                        stock > 0;
//
//                        long duration = System.currentTimeMillis() - start;
//
//                        // Indique à Camunda que la tâche est terminée & Renvoie des variables au process
//                        jobClient.newCompleteCommand(job.getKey())
//                                .variables(Map.of(
//                                        "approved", approved,
//                                        "productTitle", title,
//                                        "price", price,
//                                        "rating", rating,
//                                        "stock", stock
//                                ))
//                                .send()
//                                .join();
//
//                        System.out.println("✅ approved = " + approved);
//
//                        // LOGGING API Envoie un log vers une API interne
//
//                        //Contient :processInstanceKey, activité , statut (SUCCESS / REJECTED),durée
//                        Map<String, Object> log = Map.of(
//                                "processInstanceKey", job.getProcessInstanceKey(),
//                                "activity", job.getElementId(),
//                                "status", approved ? "SUCCESS" : "REJECTED",
//                                "message", "Verification completed",
//                                "durationMs", duration,
//                                "source", "CAMUNDA"
//                        );
//
//                        restTemplate.postForObject(
//                                "http://localhost:8082/logs",
//                                log,
//                                Void.class
//                        );
//
//                    } catch (Exception e) {
//
//                        long duration = System.currentTimeMillis() - start;
//
//                        System.out.println("❌ ERROR: " + e.getMessage());
//
//                        jobClient.newFailCommand(job.getKey())
//                                .retries(job.getRetries() - 1)
//                                .errorMessage(e.getMessage())
//                                .send()
//                                .join();
//
//                        // 🧾 LOG ERROR
//                        try {
//                            RestTemplate restTemplate = new RestTemplate();
//
//                            Map<String, Object> log = Map.of(
//                                    "processInstanceKey", job.getProcessInstanceKey(),
//                                    "activity", job.getElementId(),
//                                    "status", "ERROR",
//                                    "message", e.getMessage(),
//                                    "durationMs", duration,
//                                    "source", "CAMUNDA"
//                            );
//
//                            restTemplate.postForObject(
//                                    "http://localhost:8080/logs",
//                                    log,
//                                    Void.class
//                            );
//                        } catch (Exception ex) {
//                            System.out.println("❌ Logging failed: " + ex.getMessage());
//                        }
//                    }
//                })
//                .open();
//
//        System.out.println("🚀 Worker verify-applicant started");
//    }
//}


//
//    @JobWorker(type = "verify-applicant")
//    public void handle(JobClient client, ActivatedJob job) {
//
//        Map<String, Object> vars = job.getVariablesAsMap();
//
//        System.out.println("📥 Variables reçues: " + vars);
//
//        int salary = Integer.parseInt(vars.get("salary").toString());
//
//        boolean approved = salary > 1200;
//
//        client.newCompleteCommand(job.getKey())
//                .variables(Map.of("approved", approved))
//                .send()
//                .join();
//    }
//}