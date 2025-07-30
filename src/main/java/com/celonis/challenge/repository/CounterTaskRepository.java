package com.celonis.challenge.repository;

import com.celonis.challenge.model.CounterTask;
import com.celonis.challenge.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CounterTaskRepository extends JpaRepository<CounterTask, String> {

    Optional<CounterTask> findByIdAndTaskStatus(String taskId, TaskStatus taskStatus);
}
