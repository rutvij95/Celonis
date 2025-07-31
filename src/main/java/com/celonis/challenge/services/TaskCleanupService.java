package com.celonis.challenge.services;

import com.celonis.challenge.model.CounterTask;
import com.celonis.challenge.model.TaskStatus;
import com.celonis.challenge.repository.CounterTaskRepository;
import com.celonis.challenge.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;


@Service
public class TaskCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(TaskCleanupService.class);

    @Autowired
    private CounterTaskRepository counterTaskRepository;

    @Scheduled(fixedRate = 30000) // Every minute
//    @Scheduled(cron = "1 * * * * *")  // Every minute
    @Transactional
    public void cleanupOldTasks() {
        logger.info("Starting cleanup of old tasks...");

        Instant minusInstant = TimeUtil.getCurrentTime().toInstant().minus(30, ChronoUnit.SECONDS);

        counterTaskRepository.findAllByTaskStatusAndCreationDateBefore(TaskStatus.CREATED, Date.from(minusInstant))
                .forEach(task -> {
                    logger.info("Soft deleting old task with ID: {}", task.getId());
                    task.setTaskStatus(TaskStatus.ABORTED);
                    counterTaskRepository.save(task);
                    logger.info("Task with ID: {} has been marked as ABORTED", task.getId());
                });
    }
}
