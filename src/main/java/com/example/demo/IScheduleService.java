package com.example.demo;

public interface IScheduleService {
    void createTask(FaaTBatchTask batchTask);

    void addTask(String taskId, Runnable task, String cronExpression);

    void removeTask(String taskId);

    void updateTask(String taskId, Runnable task, String newCronExpression);
}
