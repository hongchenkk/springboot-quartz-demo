package com.godink.springboot.quartz.demo.handler;

import org.quartz.SchedulerException;

import com.godink.springboot.quartz.demo.domain.JobInfo;

public interface JobHandler {

	void addJob(JobInfo jobInfo) throws SchedulerException, ClassNotFoundException;

	JobInfo getJobInfo(String jobGroup, String jobName) throws SchedulerException;

	boolean deleteJob(String jobGroup, String jobName) throws SchedulerException;

	void continueJob(String jobGroup, String jobName) throws SchedulerException;

	void pauseJob(String jobGroup, String jobName) throws SchedulerException;

}
