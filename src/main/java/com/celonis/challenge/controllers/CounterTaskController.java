package com.celonis.challenge.controllers;

import com.celonis.challenge.model.CounterTask;
import com.celonis.challenge.services.CounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/counter-tasks")
public class CounterTaskController {

    @Autowired
    private CounterService counterService;

    @GetMapping("/")
    public List<CounterTask> getAllCounterTasks() {
        return counterService.getAllCounterTasks();
    }

    @GetMapping("/{taskId}")
    public CounterTask getCounterTask(@PathVariable String taskId) {
        return counterService.getCounterTask(taskId);
    }

    @PostMapping
    public CounterTask createCounterTask(@RequestBody CounterTask counterTask) {
        return counterService.createCounterTask(counterTask);
    }

    @PutMapping("/{taskId}/execute")
    public ResponseEntity<Map<String, String>> executeCounterTask(@PathVariable String taskId) {
        try {
            // Start the task asynchronously
            counterService.runCounterTask(taskId);

            // Return immediate response indicating task has started
            Map<String, String> response = Map.of(
                    "message", "Counter task started successfully",
                    "taskId", taskId,
                    "status", "RUNNING"
            );

            return ResponseEntity.accepted().body(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = Map.of(
                    "error", e.getMessage(),
                    "taskId", taskId
            );
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = Map.of(
                    "error", "Failed to start counter task",
                    "taskId", taskId
            );
            return ResponseEntity.unprocessableEntity().body(errorResponse);
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
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = Map.of(
                    "error", e.getMessage(),
                    "taskId", taskId
            );
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = Map.of(
                    "error", "Failed to cancel counter task",
                    "taskId", taskId
            );
            return ResponseEntity.unprocessableEntity().body(errorResponse);
        }
    }

}
