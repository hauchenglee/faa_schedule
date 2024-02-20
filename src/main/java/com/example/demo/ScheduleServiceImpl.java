package com.example.demo;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
public class ScheduleServiceImpl {
    private Map<String, ScheduledFuture<?>> jobsMap = new ConcurrentHashMap<>();

    @Autowired
    private TaskScheduler taskScheduler;

    @PostConstruct
    public void initializeTasks() {
        // 从数据库读取batch task
        List<FaaTBatchTask> taskList = List.of();

          for (FaaTBatchTask batchTask : taskList) {
            // 取得taskId
            String taskId = batchTask.getBatchId();

            // 停止正在執行的多線程排程
            terminateTask(taskId);

            // 利用反射取得要執行排程的方法
            Runnable task = () -> createTask(batchTask);

            // 執行task
            runTask(taskId, task, batchTask.getCronExpression());
        }
    }

    public void updateTasks() {
        // 从数据库读取batch task
        List<FaaTBatchTask> taskList = List.of();

        for (FaaTBatchTask batchTask : taskList) {
            // 取得taskId
            String taskId = batchTask.getBatchId();

            // 停止正在執行的多線程排程
            terminateTask(taskId);

            // 利用反射取得要執行排程的方法
            Runnable task = () -> createTask(batchTask);

            // 執行task
            runTask(taskId, task, batchTask.getCronExpression());
        }
    }

    private void createTask(FaaTBatchTask batchTask) {
        try {
            String className = batchTask.getBatchClass();
            String methodName = batchTask.getBatchMethod();

            Class<?> clazz = Class.forName(className);
            Object instance = clazz.getDeclaredConstructor().newInstance();
            Method method = clazz.getDeclaredMethod(methodName);
            method.invoke(instance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void runTask(String taskId, Runnable task, String cronExpression) {
        ScheduledFuture<?> scheduledTask = taskScheduler.schedule(task, new CronTrigger(cronExpression));
        jobsMap.put(taskId, scheduledTask);
    }

    public void terminateTask(String taskId) {
        ScheduledFuture<?> scheduledTask = jobsMap.get(taskId);
        if (scheduledTask != null) {
            scheduledTask.cancel(true);
            jobsMap.remove(taskId);
        }
    }
}
