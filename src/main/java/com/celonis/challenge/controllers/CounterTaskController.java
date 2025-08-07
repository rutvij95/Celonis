package com.celonis.challenge.controllers;

import com.celonis.challenge.model.CounterTask;
import com.celonis.challenge.model.TaskStatus;
import com.celonis.challenge.services.CounterService;
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

    @GetMapping({"/{taskId}"})
    public ResponseEntity<CounterTask> getCounterTask(@PathVariable String taskId) {
        return ResponseEntity.status(HttpStatus.OK).body(this.counterService.getCounterTask(taskId));
    }

    @PostMapping
    public @Valid ResponseEntity<CounterTask> createCounterTask(@RequestBody CounterTask counterTask) {
        return ResponseEntity.status(HttpStatus.OK).body(this.counterService.createCounterTask(counterTask));
    }

    @PutMapping({"/{taskId}/execute"})
    public ResponseEntity<Map<String, String>> executeCounterTask(@PathVariable String taskId) {
        CounterTask task = this.counterService.getCounterTaskByStatus(taskId, TaskStatus.CREATED);
        this.counterService.runCounterTask(task);
        Map<String, String> response = Map.of("message", "Counter task started successfully", "taskId", taskId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping({"/{taskId}/cancel"})
    public ResponseEntity<Map<String, String>> cancelCounterTask(@PathVariable String taskId) {
        this.counterService.cancelCounterTask(taskId);
        Map<String, String> response = Map.of("message", "Counter task cancelled successfully", "taskId", taskId, "status", "CANCELLED");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
