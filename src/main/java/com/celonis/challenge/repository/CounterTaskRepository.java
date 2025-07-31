package com.celonis.challenge.repository;

import com.celonis.challenge.model.CounterTask;
import com.celonis.challenge.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface CounterTaskRepository extends JpaRepository<CounterTask, String> {

    Optional<CounterTask> findByIdAndTaskStatus(String taskId, TaskStatus taskStatus);

    List<CounterTask> findAllByTaskStatusNot(TaskStatus taskStatus);

//    Iterable<CounterTask> findAllByTaskStatusAndCreationDateBefore(TaskStatus taskStatus, Date from);

    @Query("SELECT ct FROM CounterTask ct WHERE ct.taskStatus IN :statuses AND ct.creationDate > :creationDate")
    List<CounterTask> findAllByTaskStatusInAndCreationDateAfter(@Param("statuses") List<TaskStatus> statuses, @Param("creationDate") Date creationDate);
}
