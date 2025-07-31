package com.celonis.challenge.services;

import com.celonis.challenge.model.TaskStatus;
import com.celonis.challenge.repository.CounterTaskRepository;
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

    //    @Scheduled(fixedRate = 86400000)
    @Scheduled(cron = "* * 0 * * *")  // Runs every day at 00 Hr
    @Transactional
    public void cleanupOldTasks() {
        logger.info("Starting cleanup of old tasks...");

        Instant minusSevenDaysInstant = Instant.now().minus(7, ChronoUnit.DAYS);
        Date minusSevenDays = Date.from(minusSevenDaysInstant);

        counterTaskRepository.findAllByCreationDateBefore(minusSevenDays)
                .forEach(task -> {
                    logger.info("Soft deleting old task with ID: {}", task.getId());
                    task.setTaskStatus(TaskStatus.ABORTED);
                    counterTaskRepository.save(task);
                });
    }
}
