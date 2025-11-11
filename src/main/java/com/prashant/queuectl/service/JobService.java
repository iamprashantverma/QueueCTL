package com.prashant.queuectl.service;

import com.prashant.queuectl.dto.JobRequestDTO;
import com.prashant.queuectl.dto.JobResponseDTO;
import com.prashant.queuectl.dto.QueueOverviewDTO;
import com.prashant.queuectl.entity.Job;
import com.prashant.queuectl.entity.enums.State;
import com.prashant.queuectl.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final ModelMapper modelMapper;
    private final JobWorker jobWorker;

    @Transactional
    public JobResponseDTO enqueueJob(JobRequestDTO jobRequestDTO) {

        Job job = mapToEntity(jobRequestDTO);

        Job savedJob = jobRepository.save(job);
        log.info("Enqueued new job with ID: {}", savedJob.getId());

        return mapToResponseDTO(savedJob);
    }

    public List<JobResponseDTO> jobsByState(State state) {
        List<Job> jobs = jobRepository.findByState(state);

        return jobs.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public QueueOverviewDTO getQueueOverview() {
        long pending = jobRepository.countByState(State.PENDING);
        long processing = jobRepository.countByState(State.PROCESSING);
        long completed = jobRepository.countByState(State.COMPLETED);
        long failed = jobRepository.countByState(State.FAILED);
        long dead = jobRepository.countByState(State.DEAD);

    int count = jobWorker.getActiveWorkerCount();;

        List<JobResponseDTO> jobs = jobRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(job -> modelMapper.map(job, JobResponseDTO.class))
                .collect(Collectors.toList());

        return new QueueOverviewDTO(pending, processing, completed, failed, dead, count, jobs);
    }


    private Job mapToEntity(JobRequestDTO jobRequestDTO) {
        return modelMapper.map(jobRequestDTO, Job.class);
    }

    private JobResponseDTO mapToResponseDTO(Job job) {
        return modelMapper.map(job, JobResponseDTO.class);
    }


    @Transactional
    public JobResponseDTO retryFromDlq(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (job.getState() != State.DEAD) {
            throw new RuntimeException("Job is not in DLQ");
        }

        job.setState(State.PENDING);
        job.setAttempts(0);
        Job savedJob = jobRepository.save(job);
        return mapToResponseDTO(savedJob);
    }

}
