/*
 * Copyright (C) 2015-2021 52°North Spatial Information Research GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public License
 * version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 */
package org.n52.sensorweb.server.helgoland.adapters.harvest;

import org.n52.io.task.ScheduledJob;
import org.n52.sensorweb.server.helgoland.adapters.config.ConfigurationReader;
import org.n52.sensorweb.server.helgoland.adapters.config.DataSourceConfiguration;
import org.n52.sensorweb.server.helgoland.adapters.da.InsertRepository;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class DataSourceHarvesterScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceHarvesterScheduler.class);
    private ConfigurationReader configurationProvider;
    private List<ScheduledJob> scheduledJobs = new ArrayList<>();
    private int startupDelayInSeconds = 5;
    private Scheduler scheduler;
    private boolean enabled = true;

    @Autowired
    private InsertRepository insertRepository;

    public void init() {
        if (!enabled) {
            LOGGER.info("Job schedular disabled. No jobs will be triggered." +
                        " This is also true for particularly enabled jobs.");
            return;
        }

        Set<DataSourceConfiguration> configuredServices = configurationProvider.getDataSource()
                .stream()
                .filter(t -> t.getJob().isEnabled())
                .collect(toSet());
        insertRepository.removeNonMatchingServices(configuredServices);

        configurationProvider.getDataSource().stream()
                .peek(config -> LOGGER.info("{} {}", config.getItemName(), config.getUrl()))
                .map(config -> {
                    DataSourceHarvesterJob job = new DataSourceHarvesterJob();
                    job.init(config);
                    return job;
                }).forEach(this::scheduleJob);

        try {
            scheduler.startDelayed(startupDelayInSeconds);
            LOGGER.info("Scheduler will start jobs in {}s ...", startupDelayInSeconds);
        } catch (SchedulerException e) {
            LOGGER.error("Could not start scheduler.", e);
        }
    }

    private void scheduleJob(ScheduledJob taskToSchedule) {
        try {
            if (taskToSchedule.isEnabled()) {
                JobDetail details = taskToSchedule.createJobDetails();
                Trigger trigger = taskToSchedule.createTrigger(details.getKey());
                scheduler.scheduleJob(details, trigger);
                if (taskToSchedule.isTriggerAtStartup()) {
                    LOGGER.debug("Schedule job '{}' to run once at startup.", details.getKey());
                    Trigger onceAtStartup = TriggerBuilder.newTrigger()
                            .withIdentity(details.getKey() + "_onceAtStartup")
                            .forJob(details.getKey()).build();
                    scheduler.scheduleJob(onceAtStartup);
                }
            }
        } catch (SchedulerException e) {
            LOGGER.warn("Could not schdule Job '{}'.", taskToSchedule.getJobName(), e);
        }
    }

    /**
     * Shuts down the task scheduler without waiting tasks to be finished.
     */
    public void shutdown() {
        try {
            scheduler.shutdown(true);
            LOGGER.info("Shutdown scheduler");
        } catch (SchedulerException e) {
            LOGGER.error("Could not scheduler.", e);
        }
    }

    public List<ScheduledJob> getScheduledJobs() {
        return scheduledJobs;
    }

    public void setScheduledJobs(List<ScheduledJob> scheduledJobs) {
        this.scheduledJobs = scheduledJobs;
    }

    public int getStartupDelayInSeconds() {
        return startupDelayInSeconds;
    }

    public void setStartupDelayInSeconds(int startupDelayInSeconds) {
        this.startupDelayInSeconds = startupDelayInSeconds;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ConfigurationReader getConfigurationProvider() {
        return configurationProvider;
    }

    public void setConfigurationProvider(ConfigurationReader configurationProvider) {
        this.configurationProvider = configurationProvider;
    }

}
