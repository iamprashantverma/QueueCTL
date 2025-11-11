package com.prashant.queuectl.cli;

import com.prashant.queuectl.config.AppConfig;
import com.prashant.queuectl.dto.JobRequestDTO;
import com.prashant.queuectl.dto.JobResponseDTO;
import com.prashant.queuectl.dto.QueueOverviewDTO;
import com.prashant.queuectl.entity.enums.State;
import com.prashant.queuectl.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.List;

@ShellComponent
@RequiredArgsConstructor
@Slf4j
public class JobShellCommand {

    private final JobService jobService;
    private final AppConfig appConfig;

    private JobRequestDTO createJobRequestDto(String command) {
        return JobRequestDTO.builder()
                .command(command)
                .state(State.PENDING)
                .maxRetries(appConfig.getMaxRetries())
                .build();
    }

    @ShellMethod(key = "enqueue", value = "Add a new job to the queue")
    public JobResponseDTO enqueue(@ShellOption(help = "Command to execute") String command) {

        JobRequestDTO jobRequestDTO = createJobRequestDto(command);
        return jobService.enqueueJob(jobRequestDTO);
    }

    @ShellMethod(key = "status", value = "Show summary of all job states")
    public QueueOverviewDTO status() {
        return jobService.getQueueOverview();
    }

    @ShellMethod(key = "list", value = "List jobs by state")
    public List<JobResponseDTO> listJobs(@ShellOption(help = "State to filter") State state) {
        return jobService.jobsByState(state);
    }
}
