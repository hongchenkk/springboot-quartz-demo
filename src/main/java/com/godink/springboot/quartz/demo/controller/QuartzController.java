package com.godink.springboot.quartz.demo.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.godink.springboot.quartz.demo.domain.JobInfo;
import com.godink.springboot.quartz.demo.handler.JobHandler;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/job")
@Slf4j
public class QuartzController {
	
	@Autowired
	private JobHandler jobHandler;
	@Autowired
	private Scheduler scheduler;
	
	/**
	 * http://localhost:23080/job/all
	 */
	@PostMapping("/all")
	public List<JobInfo> list() throws SchedulerException {
		List<JobInfo> jobInfos = new ArrayList<>();
		List<String> triggerGroupNames = scheduler.getTriggerGroupNames();
		for (String triggerGroupName : triggerGroupNames) {
			Set<TriggerKey> triggerKeys = scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(triggerGroupName));
			for (TriggerKey triggerKey : triggerKeys) {
				Trigger trigger = scheduler.getTrigger(triggerKey);
				JobKey jobKey = trigger.getJobKey();
				JobInfo jobInfo = jobHandler.getJobInfo(jobKey.getGroup(), jobKey.getName());
				jobInfos.add(jobInfo);
			}
		}
		return jobInfos;
		
	}
	
    /**
     * http://localhost:23080/job/add
     *
     * {
     * 	"className": "com.godink.springboot.quartz.demo.job.PlanRemindJob",
     * 	"config": "配置信息，例如存储json",
     * 	"cron": "0/3 * * * * ?",
     * 	"jobGroup": "STANDARD_JOB_GROUP",
     * 	"jobName": "计划任务通知任务",
     * 	"triggerGroup": "STANDARD_TRIGGER_GROUP",
     * 	"triggerName": "计划任务通知触发器"
     * }
     *
     * {
     * 	"className": "com.godink.springboot.quartz.demo.job.TimeEventJob",
     * 	"config": "配置信息，例如存储json",
     * 	"cron": "0/10 * * * * ?",
     * 	"jobGroup": "STANDARD_JOB_GROUP",
     * 	"jobName": "时间通知任务",
     * 	"triggerGroup": "STANDARD_TRIGGER_GROUP",
     * 	"triggerName": "时间通知触发器"
     * }
     */
	@PostMapping("/add")
	public JobInfo addJob(@RequestBody JobInfo jobInfo) throws ClassNotFoundException, SchedulerException {
		jobHandler.addJob(jobInfo);
		return jobInfo;
	}
	
    /**
     * http://localhost:23080/job/pause?jobGroup=STANDARD_JOB_GROUP&jobName=计划任务通知任务
     * http://localhost:23080/job/pause?jobGroup=STANDARD_JOB_GROUP&jobName=时间通知任务
     */
	@PostMapping("/pause")
	public void pauseJob(@RequestParam("jobGroup") String jobGroup, @RequestParam("jobName") String jobName)
			throws SchedulerException {
		jobHandler.pauseJob(jobGroup, jobName);
	}
	
    /**
     * http://localhost:23080/job/continue?jobGroup=STANDARD_JOB_GROUP&jobName=计划任务通知任务
     * http://localhost:23080/job/continue?jobGroup=STANDARD_JOB_GROUP&jobName=时间通知任务
     */
	@PostMapping("/continue")
	public void continueJob(@RequestParam("jobGroup") String jobGroup, @RequestParam("jobName") String jobName)
			throws SchedulerException {
		jobHandler.continueJob(jobGroup, jobName);
	}
	
    /**
     * http://localhost:23080/job/delete?jobGroup=STANDARD_JOB_GROUP&jobName=计划任务通知任务
     * http://localhost:23080/job/delete?jobGroup=STANDARD_JOB_GROUP&jobName=时间通知任务
     */
	@PostMapping("/delete")
	public boolean deleteJob(@RequestParam("jobGroup") String jobGroup, @RequestParam("jobName") String jobName)
			throws SchedulerException {
		return jobHandler.deleteJob(jobGroup, jobName);
	}
	
}
