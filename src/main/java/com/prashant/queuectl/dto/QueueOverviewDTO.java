package com.prashant.queuectl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class QueueOverviewDTO {
    private long pending;
    private long processing;
    private long completed;
    private long failed;
    private long dead;
    private int activeWorkers;
    private List<JobResponseDTO> jobs;
}
