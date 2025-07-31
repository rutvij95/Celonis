package com.celonis.challenge.services;

import com.celonis.challenge.model.CounterTask;
import com.celonis.challenge.model.TaskStatus;
import com.celonis.challenge.repository.CounterTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class CounterService {

    Logger logger = LoggerFactory.getLogger(CounterService.class);

    @Autowired
    private CounterTaskRepository counterTaskRepository;

    @Async("taskExecutor")
    public CompletableFuture<Void> runCounterTask(String taskId) {
        logger.info("Starting counter task with ID: {}", taskId);

        return CompletableFuture.runAsync(() -> {
            try {
                logger.info("Searching for taskId with Creaetd state ...");
                CounterTask counterTask = counterTaskRepository.findByIdAndTaskStatus(taskId, TaskStatus.CREATED)
                        .orElseThrow(() -> new IllegalArgumentException("Counter task not found with id: " + taskId));
                if (counterTask == null) {
                    throw new IllegalArgumentException("Counter task not found with id: " + taskId + " or in created state");
                }
                logger.info("{} : TaskId changing state to Running", taskId);
                counterTask.setTaskStatus(TaskStatus.RUNNING);
                counterTask.setUpdateDate(new Date());
                saveCounterTask(counterTask);

                logger.info("Starting Execution... ");
                for (int i = counterTask.getStartValue(); i <= counterTask.getEndValue(); i++) {
                    Optional<CounterTask> currentTaskState = counterTaskRepository.findById(taskId);
                    if (currentTaskState.isEmpty() || currentTaskState.get().getTaskStatus() == TaskStatus.CANCELLED) {
                        logger.info("Counter task with ID: {} has been cancelled " +
                                "or does not exist anymore. Stopping the execution.", taskId);
                        return;
                    }
                    counterTask.setCurrentValue(i);
                    counterTask.setUpdateDate(new Date());
                    saveCounterTask(counterTask);
                    Thread.sleep(1000);
                }
                logger.info("Execution compelted for taskId: {}", taskId);

                counterTask.setTaskStatus(TaskStatus.COMPLETED);
                counterTask.setUpdateDate(new Date());
                saveCounterTask(counterTask);
            } catch (IllegalArgumentException e) {
                logger.error(e.getMessage());
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                logger.error("Counter task interrupted: {}", e.getMessage());
                counterTaskRepository.findById(taskId).ifPresent(task -> {
                    task.setTaskStatus(TaskStatus.FAILED);
                    task.setUpdateDate(new Date());
                    saveCounterTask(task);
                });
                Thread.currentThread().interrupt();
                throw new RuntimeException("Counter task interrupted", e);
            } catch (Exception e) {
                logger.error(e.getMessage());
                counterTaskRepository.findById(taskId).ifPresent(task -> {
                    task.setTaskStatus(TaskStatus.FAILED);
                    task.setUpdateDate(new Date());
                    saveCounterTask(task);
                });
                throw new RuntimeException("Counter task failed", e);
            }
        });
    }

    public List<CounterTask> getAllCounterTasks() {
        logger.info("Fetching all counter tasks");
        return counterTaskRepository.findAll();
    }

    public CounterTask createCounterTask(CounterTask counterTask) {
        logger.info("Creating new task ...");
        if (counterTask.getStartValue() >= counterTask.getEndValue()) {
            logger.error("Start value must be less than end value.");
            throw new IllegalArgumentException("Start value must be less than end value.");
        }
        counterTask.setCreationDate(new Date());
        counterTask.setUpdateDate(new Date());
        counterTask.setTaskStatus(TaskStatus.CREATED);
//        return saveCounterTask(counterTask);
        CounterTask ct = saveCounterTask(counterTask);
        logger.info("Counter task created with ID: {}", ct.getId());
        return ct;
    }

    private CounterTask saveCounterTask(CounterTask counterTask) {
        return counterTaskRepository.save(counterTask);
    }

    public CounterTask getCounterTask(String taskId) {
        return counterTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Counter task not found with id: " + taskId));
    }

    public void cancelCounterTask(String taskId) {
        logger.info("Cancelling counter taks with ID: {}", taskId);
        CounterTask counterTask = counterTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Counter task not found with id: " + taskId));

        if (counterTask.getTaskStatus() == TaskStatus.RUNNING) {
            counterTask.setTaskStatus(TaskStatus.CANCELLED);
            counterTask.setUpdateDate(new Date());
            saveCounterTask(counterTask);
            logger.info("Counter task with ID: {} has been cancelled successfully.", taskId);
        } else {
            logger.error("Counter task is not running and cannot be cancelled.");
            throw new IllegalArgumentException("Counter task is not running and cannot be cancelled.");
        }
    }
}
