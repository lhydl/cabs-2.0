package org.cabs.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cabs.entity.Appointment;
import org.cabs.service.QueueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/queue")
@Slf4j
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;

    @GetMapping("/getTodayApptQueue")
    public List<Appointment> getTodayApptQueue() {
        return queueService.getTodayApptQueue();
    }

    @PostMapping("/updateQueueStatus")
    public Integer updateQueueStatus(
        @RequestParam("id") Integer id,
        @RequestParam("status") Integer status
    ) {
        return queueService.updateQueueStatus(id, status);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body("Service is up and running");
    }

}
