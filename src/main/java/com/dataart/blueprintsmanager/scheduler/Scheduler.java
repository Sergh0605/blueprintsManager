package com.dataart.blueprintsmanager.scheduler;

import com.dataart.blueprintsmanager.service.ProjectService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class Scheduler {
    private final ProjectService projectService;

    // Every day at 01:00am
    @Scheduled(cron = "0 0 1 ? * *")
    public void reassembleAllProjectsByScheduler(){
        projectService.reassembleAll();
    }
}
