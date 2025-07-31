package com.celonis.challenge.controllers;

import com.celonis.challenge.model.CounterTask;
import com.celonis.challenge.model.TaskStatus;
import com.celonis.challenge.services.CounterService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/counter-tasks")
public class CounterTaskController {

    @Autowired
    private CounterService counterService;

    @GetMapping("/")
    public ResponseEntity<List<CounterTask>> getAllCounterTasks() {
        return ResponseEntity.status(HttpStatus.OK).body(counterService.getAllCounterTasks());
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<?> getCounterTask(@PathVariable String taskId) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(counterService.getCounterTask(taskId));
        } catch (Exception e) {
            Map<String, String> errorResponse = Map.of(
                    "error", e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping
    @Valid
    public ResponseEntity<?> createCounterTask(@RequestBody CounterTask counterTask) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(counterService.createCounterTask(counterTask));
        } catch (Exception e) {
            Map<String, String> errorResponse = Map.of(
                    "error", e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }

    }

    @PutMapping("/{taskId}/execute")
    public ResponseEntity<Map<String, String>> executeCounterTask(@PathVariable String taskId) {
        try {
            CounterTask task = counterService.getCounterTaskByStatus(taskId, TaskStatus.CREATED);
            counterService.runCounterTask(task);
            Map<String, String> response = Map.of(
                    "message", "Counter task started successfully",
                    "taskId", taskId
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = Map.of(
                    "error", e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = Map.of(
                    "error", "Failed to start counter task",
                    "taskId", taskId
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/{taskId}/cancel")
    public ResponseEntity<Map<String, String>> cancelCounterTask(@PathVariable String taskId) {
        try {
            counterService.cancelCounterTask(taskId);
            Map<String, String> response = Map.of(
                    "message", "Counter task cancelled successfully",
                    "taskId", taskId,
                    "status", "CANCELLED"
            );
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = Map.of(
                    "error", e.getMessage()

            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = Map.of(
                    "error", "Failed to cancel counter task",
                    "taskId", taskId
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

}
