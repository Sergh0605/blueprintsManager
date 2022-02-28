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
    @Scheduled(cron = "${bpm.cron.reassembly}")
    public void reassembleAllProjectsByScheduler(){
        log.info("Try to reassemble by Scheduler all Projects with reassemblyRequired = true");
        Integer countOfReassembledProjects = projectService.reassembleAll();
        log.info("{} Projects reassembled by Scheduler", countOfReassembledProjects);
    }
}
