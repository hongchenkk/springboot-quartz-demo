package com.godink.springboot.quartz.demo.handler;

import java.util.List;
import java.util.Objects;

import javax.annotation.Resource;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.godink.springboot.quartz.demo.domain.JobInfo;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JobHandlerImpl implements JobHandler{
	
	@Resource
	private Scheduler scheduler;
	
	@Override
	public void addJob(JobInfo jobInfo) throws SchedulerException, ClassNotFoundException {
		Objects.requireNonNull(jobInfo, "任务信息不能为空");
		
		//生成job key
		JobKey jobKey = JobKey.jobKey(jobInfo.getJobName(), jobInfo.getJobGroup());
		//当前任务不存在才进行添加
		if(!scheduler.checkExists(jobKey)) {
			Class<Job> jobClass = (Class<Job>)Class.forName(jobInfo.getClassName());
			//任务明细
			JobDetail jobDetail = JobBuilder
			.newJob(jobClass)
			.withIdentity(jobKey)
//			.withIdentity(jobInfo.getJobName(), jobInfo.getJobGroup())
			.withDescription(jobInfo.getJobName())
			.build();
			//配置信息
			jobDetail.getJobDataMap().put("config", jobInfo.getConfig());
			//定义触发器
			TriggerKey triggerKey = TriggerKey.triggerKey(jobInfo.getTriggerName(), jobInfo.getTriggerGroup());
			CronTrigger trigger = TriggerBuilder.newTrigger()
			.withIdentity(triggerKey)
			.withSchedule(CronScheduleBuilder.cronSchedule(jobInfo.getCron()))
			.build();
			scheduler.scheduleJob(jobDetail, trigger);
		}else {
			throw new SchedulerException(jobInfo.getJobName()+"任务已存在，无需重复添加");
		}
		
	}
	
	@Override
	public void pauseJob(String jobGroup, String jobName) throws SchedulerException{
		JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
		if(scheduler.checkExists(jobKey)) {
			scheduler.pauseJob(jobKey);
		}
	}
	
	@Override
	public void continueJob(String jobGroup, String jobName) throws SchedulerException {
		JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
		if(scheduler.checkExists(jobKey)) {
			scheduler.resumeJob(jobKey);
		}
	}
	
	@Override
	public boolean deleteJob(String jobGroup, String jobName) throws SchedulerException {
		JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
		if(scheduler.checkExists(jobKey)) {
			return scheduler.deleteJob(jobKey);
		}
		return false;
	}
	
	@Override
	public JobInfo getJobInfo(String jobGroup, String jobName) throws SchedulerException {
		//拿到触发器
		JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
		if(!scheduler.checkExists(jobKey)) {
			log.error("任务信息不存在: {}-{}", jobGroup, jobName);
			return null;
		}
		List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
		if(Objects.isNull(triggers)) {
			throw new SchedulerException("未获取到触发器信息");
		}
		Trigger trigger = triggers.get(0);
		
		//拿到触发器key
		TriggerKey triggerKey = trigger.getKey();
		//拿到触发器state
		TriggerState triggerState = scheduler.getTriggerState(triggerKey);
		//拿到任务detail
		JobDetail jobDetail = scheduler.getJobDetail(jobKey);
		
		//封装任务信息
		JobInfo jobInfo = new JobInfo();
		jobInfo.setJobName(jobName);
		jobInfo.setJobGroup(jobGroup);
		jobInfo.setTriggerName(triggerKey.getName());
		jobInfo.setTriggerGroup(triggerKey.getGroup());
		jobInfo.setClassName(jobDetail.getJobClass().getName());
		jobInfo.setStatus(triggerState.toString());
		
		if(Objects.nonNull(jobDetail.getJobDataMap())) {
			jobInfo.setConfig(JSONObject.toJSONString(jobDetail.getJobDataMap()));
		}
		
		//获取触发器-执行表达式，既cron表达式
		CronTrigger cronTrigger = (CronTrigger)trigger;
		jobInfo.setCron(cronTrigger.getCronExpression());
		
		return jobInfo;
	}

}
