# CounterTaskController

## Overview

The `CounterTaskController` serves as the front door for all CounterTask functionality. It handles HTTP requests related to counter tasks and delegates operations to the `CounterService`. It provides endpoints for creating, retrieving, executing, and canceling tasks, as well as checking task status.

## Endpoints

- **GET /api/v1/counter-tasks/**  
  Retrieve a list of all counter tasks.

- **GET /api/v1/counter-tasks/{taskId}**  
  Retrieve details of a specific counter task by its ID.

- **POST /api/v1/counter-tasks**  
  Create a new counter task.  
  Validation: `startTime` must be strictly less than `endTime`.

- **PUT /api/v1/counter-tasks/{taskId}/execute**  
  Execute the specified counter task.  
  This will start a new thread to perform the counting operation. Only tasks in the `CREATED` status can be executed.

- **PUT /api/v1/counter-tasks/{taskId}/cancel**  
  Cancel the specified counter task.  
  If the task is currently running, the associated thread will be stopped and the status set to `CANCELLED`.

## Scheduler

A background scheduler runs every minute to automatically abort any counter task that was created more than 7 days ago by setting its status to `ABORTED`.

## Response Format

All endpoints return JSON responses. Successful operations return the relevant data or confirmation messages. Errors return an object with an `error` field describing the issue.
