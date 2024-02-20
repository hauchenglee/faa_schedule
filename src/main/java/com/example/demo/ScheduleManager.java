package com.example.demo;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Component
public class ScheduleManager {

    @Autowired
    private IScheduleService scheduleService;

    // 应用启动时初始化任务
    @PostConstruct
    public void initializeTasks() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        List<FaaTBatchTask> taskList = new ArrayList<>();

        for (FaaTBatchTask batchTask : taskList) {
            String className = batchTask.getBatchClass();
            String methodName = batchTask.getBatchMethod();
            String batchCronExpression = batchTask.getCronExpression();
            String taskId = className + "." + methodName;
            Runnable task = createTaskRunnable(batchTask);
            scheduleService.addTask(taskId, task, batchCronExpression);
        }
    }

    public Runnable createTaskRunnable(FaaTBatchTask batchTask) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        String className = batchTask.getBatchClass();
        String methodName = batchTask.getBatchMethod();

        Class<?> clazz = Class.forName(className); // 加载指定的类
        Object instance = clazz.newInstance(); // 创建类的实例
        Method method = clazz.getDeclaredMethod(methodName); // 获取指定的方法

        return () -> {
            try {
                method.invoke(instance); // 执行实例的方法
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        };
    }

    public void updateTask(FaaTBatchTask batchTask) {
        String className = batchTask.getBatchClass();
        String methodName = batchTask.getBatchMethod();
        String batchCronExpression = batchTask.getCronExpression();
        String taskId = className + "." + methodName;

        // 首先移除现有的任务
        scheduleService.removeTask(taskId);

        // 创建新的任务Runnable
        Runnable newTask = null;
        try {
            newTask = createTaskRunnable(batchTask);
        } catch (Exception e) {
            e.printStackTrace();
            // 处理异常，可能是类名、方法名无效或方法不可访问等
        }

        // 添加新的任务
        if (newTask != null) {
            scheduleService.addTask(taskId, newTask, batchCronExpression);
        }
    }
}
