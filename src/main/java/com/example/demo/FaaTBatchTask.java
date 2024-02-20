package com.example.demo;

import lombok.Data;

@Data
public class FaaTBatchTask {

    private String batchId;

    private String batchClass;

    private String batchMethod;

    private String cronExpression;
}
