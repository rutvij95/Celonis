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

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CounterServiceTest {

    @Mock
    private CounterTaskRepository counterTaskRepository;

    @InjectMocks
    private CounterService counterService;

    private CounterTask testTask;
    private String testTaskId;

    @Before
    public void setUp() {
        testTaskId = "8630925f-c8f4-436a-9e4e-936d1756704b";
        testTask = new CounterTask();
        testTask.setId(testTaskId);
        testTask.setStartValue(1);
        testTask.setEndValue(5);
        testTask.setCurrentValue(1);
        testTask.setTaskStatus(TaskStatus.CREATED);
        testTask.setCreationDate(new Date());
        testTask.setUpdateDate(new Date());
    }

    @Test
    public void testGetAllCounterTasks() {
        // Given
        List<CounterTask> expectedTasks = Arrays.asList(testTask);
        when(counterTaskRepository.findAll()).thenReturn(expectedTasks);

        // When
        List<CounterTask> result = counterService.getAllCounterTasks();

        // Then
        assertEquals(expectedTasks, result);

        verify(counterTaskRepository).findAll(); // Verify that CounterTaskRepository is called.
    }

    @Test
    public void testCreateCounterTask_Success() {
        // Given
        CounterTask newTask = new CounterTask();
        newTask.setStartValue(1);
        newTask.setEndValue(10);

        when(counterTaskRepository.save(any(CounterTask.class))).thenReturn(testTask);

        // When
        CounterTask result = counterService.createCounterTask(newTask);

        // Then
        assertNotNull(result);
        assertEquals(testTask.getId(), result.getId());
        assertEquals(TaskStatus.CREATED, newTask.getTaskStatus());
        assertNotNull(newTask.getCreationDate());
        assertNotNull(newTask.getUpdateDate());

        verify(counterTaskRepository).save(newTask);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCounterTask_InvalidRange() {
        // Given
        CounterTask invalidTask = new CounterTask();
        invalidTask.setStartValue(10);
        invalidTask.setEndValue(5); // End value less than start value

        // When
        counterService.createCounterTask(invalidTask); // Exception will occur.

        // Then - exception expected
    }

    // todo: Can write the same test case for same start and end value also.

    @Test
    public void testGetCounterTask_Success() {
        // Given
        when(counterTaskRepository.findById(testTaskId)).thenReturn(Optional.of(testTask));

        // When
        CounterTask result = counterService.getCounterTask(testTaskId);

        // Then
        assertEquals(testTask, result);

        verify(counterTaskRepository).findById(testTaskId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCounterTask_NotFound() {
        // Given
        when(counterTaskRepository.findById(testTaskId)).thenReturn(Optional.empty());

        // When
        counterService.getCounterTask(testTaskId);
    }

    @Test
    public void testCancelCounterTask_Success() {
        // Given
        testTask.setTaskStatus(TaskStatus.RUNNING);
        when(counterTaskRepository.findById(testTaskId)).thenReturn(Optional.of(testTask));
        when(counterTaskRepository.save(testTask)).thenReturn(testTask);

        // When
        counterService.cancelCounterTask(testTaskId);

        // Then
        assertEquals(TaskStatus.CANCELLED, testTask.getTaskStatus());
        assertNotNull(testTask.getUpdateDate());

        verify(counterTaskRepository).save(testTask);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testCancelCounterTask_NotRunning() {
        // Given
        testTask.setTaskStatus(TaskStatus.CREATED);
        when(counterTaskRepository.findById(testTaskId)).thenReturn(Optional.of(testTask));

        // When
        counterService.cancelCounterTask(testTaskId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCancelCounterTask_AlreadyCompleted() {
        // Given
        testTask.setTaskStatus(TaskStatus.COMPLETED);
        when(counterTaskRepository.findById(testTaskId)).thenReturn(Optional.of(testTask));

        // When
        counterService.cancelCounterTask(testTaskId);
    }

    @Test
    public void testRunCounterTask_TaskFoundAndCompletesSuccessfully() throws InterruptedException {
        // Given
        testTask.setStartValue(1);
        testTask.setEndValue(5);

        when(counterTaskRepository.findByIdAndTaskStatus(testTaskId, TaskStatus.CREATED))
                .thenReturn(Optional.of(testTask));
        when(counterTaskRepository.findById(testTaskId))
                .thenReturn(Optional.of(testTask));
        when(counterTaskRepository.save(any(CounterTask.class)))
                .thenReturn(testTask);

        // When
        CompletableFuture<Void> future = counterService.runCounterTask(testTaskId);

        Thread.sleep(6000); // Should be at least more than 6.

        // Then
        assertTrue(future.isDone()); // Task should complete

        verify(counterTaskRepository, atLeastOnce()).save(any(CounterTask.class));
    }

    @Test
    public void testRunCounterTask_TaskCancelled() throws InterruptedException {
        // Given
        testTask.setStartValue(1);
        testTask.setEndValue(100); // Large range to allow cancellation

        CounterTask cancelledTask = new CounterTask();
        cancelledTask.setId(testTaskId);
        cancelledTask.setTaskStatus(TaskStatus.CANCELLED);

        when(counterTaskRepository.findByIdAndTaskStatus(testTaskId, TaskStatus.CREATED))
                .thenReturn(Optional.of(testTask));
        when(counterTaskRepository.findById(testTaskId))
                .thenReturn(Optional.of(testTask))
                .thenReturn(Optional.of(cancelledTask)); // Return cancelled task on any of the next call.
        when(counterTaskRepository.save(any(CounterTask.class)))
                .thenReturn(testTask);

        // When
        CompletableFuture<Void> future = counterService.runCounterTask(testTaskId);

        Thread.sleep(2000);

        // Then
        assertTrue(future.isDone());
    }

    // Todo can write the test cases for Exceptions and Not found Task.
}
