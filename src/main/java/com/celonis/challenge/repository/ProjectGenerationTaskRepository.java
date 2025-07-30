package com.celonis.challenge.repository;

import com.celonis.challenge.model.ProjectGenerationTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectGenerationTaskRepository extends JpaRepository<ProjectGenerationTask, String> {
}
