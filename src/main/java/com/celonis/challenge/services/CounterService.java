package com.celonis.challenge.services;

import com.celonis.challenge.model.CounterTask;
import com.celonis.challenge.model.TaskStatus;
import com.celonis.challenge.repository.CounterTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CounterService {

    @Autowired
    private CounterTaskRepository counterTaskRepository;

    @Async
    @Transactional
    public void runCounterTask(String taskId) {

        try {
            CounterTask counterTask = counterTaskRepository.findByIdAndTaskStatus(taskId, TaskStatus.CREATED)
                    .orElseThrow(() -> new IllegalArgumentException("Counter task not found with id: " + taskId));
            counterTask.setTaskStatus(TaskStatus.RUNNING);
            counterTaskRepository.save(counterTask);

            for (int i = counterTask.getStartValue(); i <= counterTask.getEndValue(); i++) {

                Optional<CounterTask> currentTaskState = counterTaskRepository.findById(taskId);
                if (currentTaskState.isEmpty() || currentTaskState.get().getTaskStatus() == TaskStatus.CANCELLED) {
                    System.out.println("Task " + taskId + " was cancelled or deleted.");
                    return; // Stop execution
                }
                counterTask.setCurrentValue(i);
                counterTaskRepository.save(counterTask);
                Thread.sleep(1000); // Wait for 1 Second
            }

            counterTask.setTaskStatus(TaskStatus.COMPLETED);
            counterTaskRepository.save(counterTask);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            throw e;
        } catch (InterruptedException e) {
            counterTaskRepository.findById(taskId).ifPresent(task -> {
                task.setTaskStatus(TaskStatus.FAILED);
                counterTaskRepository.save(task);
            });
            Thread.currentThread().interrupt();
            throw new RuntimeException("Counter task interrupted", e);
        }
    }

    public List<CounterTask> getAllCounterTasks() {
        return counterTaskRepository.findAll();
    }

    public CounterTask createCounterTask(CounterTask counterTask) {
        if (counterTask.getStartValue() >= counterTask.getEndValue()) {
            throw new IllegalArgumentException("Start value must be less than end value.");
        }
        counterTask.setCreationDate(new Date());
        counterTask.setUpdateDate(new Date());
        counterTask.setTaskStatus(TaskStatus.CREATED);
        return counterTaskRepository.save(counterTask);
    }

    public CounterTask getCounterTask(String taskId) {
        return counterTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Counter task not found with id: " + taskId));
    }
}
