package com.example.Celonis_Service.Controller;

import com.example.Celonis_Service.DTO.ProcessLogDTO;
import com.example.Celonis_Service.Model.ProcessLog;
import com.example.Celonis_Service.Repo.ProcessLogRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/logs")
public class LogController {

    private final ProcessLogRepository repo;

    public LogController(ProcessLogRepository repo) {
        this.repo = repo;
    }

    //  ENDPOINT UTILISÉ PAR LE WORKER
    @PostMapping
    public ProcessLog create(@RequestBody ProcessLogDTO dto) {

        ProcessLog log = new ProcessLog();

        log.setProcessInstanceKey(dto.getProcessInstanceKey());
        log.setActivity(dto.getActivity());
        log.setStatus(dto.getStatus());
        log.setMessage(dto.getMessage());
        log.setDurationMs(dto.getDurationMs());
        log.setSource(dto.getSource());
        log.setTimestamp(LocalDateTime.now());

        return repo.save(log);
    }

    //  TEST MANUEL
    @PostMapping("/test")
    public ProcessLog createTest() {

        ProcessLog log = new ProcessLog();
        log.setActivity("TEST");
        log.setStatus("OK");
        log.setTimestamp(LocalDateTime.now());

        return repo.save(log);
    }

    //  LIST ALL LOGS
    @GetMapping
    public List<ProcessLog> getLogs() {
        return repo.findAll();
    }
}
//
//    private final ProcessLogRepository repo;
//
//    public LogController(ProcessLogRepository repo) {
//        this.repo = repo;
//    }
////    @PostMapping("/test")
////    public ProcessLog createTest() {
////        System.out.println("➡️ BEFORE SAVE");
////
////        ProcessLog log = new ProcessLog();
////        log.setActivity("TEST");
////        log.setStatus("OK");
////        log.setTimestamp(LocalDateTime.now());
////
////        ProcessLog saved = repo.save(log);
////
////        System.out.println("➡️ AFTER SAVE ID = " + saved.getId());
////
////        return saved;
////    }
//
//    @PostMapping("/test")
//    @Transactional
//    public ProcessLog createTest() {
//
//        System.out.println("🔥 ENTER POST /logs/test");
//
//        ProcessLog log = new ProcessLog();
//        log.setActivity("TEST");
//        log.setStatus("OK");
//        log.setTimestamp(LocalDateTime.now());
//
//        System.out.println("🔥 BEFORE SAVE");
//
//        ProcessLog saved = repo.saveAndFlush(log);
//
//        System.out.println("🔥 AFTER SAVE ID = " + saved.getId());
//
//        return saved;
//    }
//@GetMapping
//    public List<ProcessLog> getLogs() {
//        return repo.findAll();
//    }
//}
