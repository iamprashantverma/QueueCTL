package com.prashant.queuectl.service;

import com.prashant.queuectl.config.AppConfig;
import com.prashant.queuectl.entity.Job;
import com.prashant.queuectl.entity.enums.State;
import com.prashant.queuectl.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobWorker {

    private final JobRepository jobRepository;
    private final AppConfig appConfig;
    private volatile boolean running = true;

    private final Set<String> activeWorkers = ConcurrentHashMap.newKeySet();

    @Async("jobExecutor")
    public void processJobs() {
        String workerName = Thread.currentThread().getName();

        if (!activeWorkers.add(workerName)) {
            log.warn("Worker {} already running, skipping start.", workerName);
            return;
        }

        log.info("Worker started: {}", workerName);

        try {
            while (running) {
                try {
                    processPendingJobs();
                } catch (Exception e) {
                    log.error("Unexpected exception in worker thread {}", workerName, e);
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    running = false;
                    log.info("Worker {} interrupted, shutting down...", workerName);
                }
            }
        } finally {
            activeWorkers.remove(workerName);
            log.info("Worker {} stopped gracefully.", workerName);
        }
    }

    @Transactional
    public void processPendingJobs() {
        List<Job> pendingJobs = jobRepository.findPendingJobsForUpdate(State.PENDING);

        for (Job job : pendingJobs) {
            if (job.getNextAttemptTime() != null && job.getNextAttemptTime().isAfter(LocalDateTime.now())) {
                continue;
            }

            try {
                executeJob(job);
            } catch (Exception e) {
                log.error("Failed to execute job {}: ", job.getId(), e);
            }
        }
    }

    @Transactional
    private void executeJob(Job job) {
        job.setState(State.PROCESSING);
        jobRepository.save(job);

        log.info("Executing job {}: {}", job.getId(), job.getCommand());

        try {
            ProcessBuilder pb = new ProcessBuilder(job.getCommand().split(" "));
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                reader.lines().forEach(log::info);
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                job.setState(State.COMPLETED);
                log.info("Job {} completed successfully", job.getId());
            } else {
                log.warn("Job {} failed with exit code {}", job.getId(), exitCode);
                handleFailure(job);
            }

        } catch (Exception e) {
            log.error("Job {} execution failed", job.getId(), e);
            handleFailure(job);
        } finally {
            jobRepository.save(job);
        }
    }

    @Transactional
    private void handleFailure(Job job) {
        job.setAttempts(job.getAttempts() + 1);

        if (job.getAttempts() > appConfig.getMaxRetries()) {
            job.setState(State.DEAD);
            log.warn("Job {} moved to Dead Letter Queue", job.getId());
        } else {
            long delay = (long) Math.pow(appConfig.getBackoffSeconds(), job.getAttempts());
            job.setNextAttemptTime(LocalDateTime.now().plusSeconds(delay));
            job.setState(State.PENDING);
            log.info("Job {} failed, will retry after {} seconds", job.getId(), delay);
        }

        jobRepository.save(job);
    }

    public void shutdown() {
        running = false;
        log.info("Shutting down JobWorker gracefully...");
    }

    public boolean allWorkersStopped() {
        return activeWorkers.isEmpty();
    }

    public int getActiveWorkerCount() {
        return activeWorkers.size();
    }
}
