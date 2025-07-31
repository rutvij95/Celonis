package com.celonis.challenge.services;

import com.celonis.challenge.model.CounterTask;
import com.celonis.challenge.model.TaskStatus;
import com.celonis.challenge.repository.CounterTaskRepository;
import com.celonis.challenge.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class CounterService {

    private static final Logger logger = LoggerFactory.getLogger(CounterService.class);

    @Autowired
    private CounterTaskRepository counterTaskRepository;

    @Value("${counter.task.sleep.interval:1000}")
    private long sleepInterval;

    @Async("taskExecutor")
    public CompletableFuture<Void> runCounterTask(CounterTask counterTask) {
        logger.info("Searching for taskId with Creaetd state ...");
        return CompletableFuture.runAsync(() -> {
            try {
                logger.info("{} : TaskId changing state to Running", counterTask.getId());
                counterTask.setTaskStatus(TaskStatus.RUNNING);
                counterTask.setUpdateDate(TimeUtil.getCurrentTime());
                saveCounterTask(counterTask);

                long lastSavedTime = System.currentTimeMillis();
                logger.info("Starting Execution... ");
                for (int i = counterTask.getStartValue(); i <= counterTask.getEndValue(); i++) {
                    CounterTask currentTaskState = getCounterTask(counterTask.getId());
                    if (currentTaskState.getTaskStatus() == TaskStatus.CANCELLED) {
                        logger.info("Counter task with ID: {} has been cancelled " +
                                "or does not exist anymore. Stopping the execution.", counterTask.getId());
                        return;
                    }
                    counterTask.setCurrentValue(i);
                    counterTask.setUpdateDate(TimeUtil.getCurrentTime());

                    logger.info("To reduce db call In every 5 sec we are saving the task state. {}", counterTask.getId());
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastSavedTime >= 5000) {
                        saveCounterTask(counterTask);
                        lastSavedTime = currentTime;
                        logger.info("Task state saved...");
                    }

                    Thread.sleep(sleepInterval);
                }
                logger.info("Execution compelted for taskId: {}", counterTask.getId());

                counterTask.setTaskStatus(TaskStatus.COMPLETED);
                counterTask.setUpdateDate(TimeUtil.getCurrentTime());
                saveCounterTask(counterTask);
            } catch (IllegalArgumentException e) {
                logger.error(e.getMessage());
                throw new IllegalArgumentException(e.getMessage());
            } catch (Exception e) {
                logger.error(e.getMessage());
                counterTaskRepository.findById(counterTask.getId()).ifPresent(task -> {
                    task.setTaskStatus(TaskStatus.FAILED);
                    task.setUpdateDate(TimeUtil.getCurrentTime());
                    saveCounterTask(task);
                });
                throw new RuntimeException("Counter task failed", e);
            }
        });
    }

    public List<CounterTask> getAllCounterTasks() {
        logger.info("Fetching all counter tasks");
        return counterTaskRepository.findAllByTaskStatusNot(TaskStatus.ABORTED); // Skipping Tasks with Aborted status
    }

    public CounterTask createCounterTask(CounterTask counterTask) {
        logger.info("Creating new task ...");
        if (counterTask.getStartValue() >= counterTask.getEndValue()) {
            logger.error("Start value must be less than end value.");
            throw new IllegalArgumentException("Start value must be less than end value.");
        }
        counterTask.setCreationDate(TimeUtil.getCurrentTime());
        counterTask.setUpdateDate(TimeUtil.getCurrentTime());
        counterTask.setTaskStatus(TaskStatus.CREATED);

//        return saveCounterTask(counterTask);

        CounterTask ct = saveCounterTask(counterTask);
        logger.info("Counter task created with ID: {}", ct.getId());
        return ct;
    }

    // Ensure that no other transaction can modify the task while this one is running
//    @Transactional(isolation = Isolation.SERIALIZABLE)
    private CounterTask saveCounterTask(CounterTask counterTask) {
        return counterTaskRepository.save(counterTask);
    }

    public CounterTask getCounterTask(String taskId) {
        return counterTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException(taskId + " not found"));
    }

    public CounterTask getCounterTaskByStatus(String taskId, TaskStatus taskStatus) {
        return counterTaskRepository.findByIdAndTaskStatus(taskId, taskStatus)
                .orElseThrow(() -> new IllegalArgumentException(taskId + " not found"));
    }

    public void cancelCounterTask(String taskId) {
        logger.info("Cancelling counter taks with ID: {}", taskId);
        CounterTask counterTask = getCounterTask(taskId);

        if (counterTask.getTaskStatus() == TaskStatus.RUNNING) {
            counterTask.setTaskStatus(TaskStatus.CANCELLED);
            counterTask.setUpdateDate(TimeUtil.getCurrentTime());
            saveCounterTask(counterTask);
            logger.info("Counter task with ID: {} has been cancelled successfully.", taskId);
        } else {
            logger.error("Counter task is not running and cannot be cancelled.");
            throw new IllegalArgumentException("Counter task is not running and cannot be cancelled.");
        }
    }
}
