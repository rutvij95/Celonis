package com.celonis.challenge.services;

import com.celonis.challenge.model.CounterTask;
import com.celonis.challenge.model.TaskStatus;
import com.celonis.challenge.repository.CounterTaskRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TaskCleanupServiceTest {

    @Mock
    private CounterTaskRepository counterTaskRepository;

    @InjectMocks
    private TaskCleanupService taskCleanupService;

    private CounterTask testTask;

    @Before
    public void setUp() {
        testTask = new CounterTask();
        testTask.setId("8630925f-c8f4-436a-9e4e-936d1756704b");
        testTask.setStartValue(1);
        testTask.setEndValue(5);
        testTask.setCurrentValue(1);
        testTask.setTaskStatus(TaskStatus.CREATED);
        testTask.setCreationDate(new Date());
        testTask.setUpdateDate(new Date());
    }

    @Test
    public void testCleanupOldTasks() {
        testTask.setCreationDate(new Date(System.currentTimeMillis() - 8 * 24 * 60 * 60 * 1000L));
        when(counterTaskRepository.findAllByTaskStatusInAndCreationDateBefore(
                any(List.class), any(Date.class))).thenReturn(List.of(testTask));

        // When
        taskCleanupService.cleanupOldTasks();

        // Then
        verify(counterTaskRepository).save(testTask);
        assertEquals(TaskStatus.ABORTED, testTask.getTaskStatus());
    }
}
