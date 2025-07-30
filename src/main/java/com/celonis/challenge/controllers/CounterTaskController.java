package com.celonis.challenge.controllers;

import com.celonis.challenge.model.CounterTask;
import com.celonis.challenge.services.CounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<Void> executeCounterTask(@PathVariable String taskId) {
        counterService.runCounterTask(taskId);
        return ResponseEntity.accepted().build();
    }

}
