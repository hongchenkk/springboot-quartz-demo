package com.godink.springboot.quartz.demo.job;

import java.time.LocalDateTime;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@DisallowConcurrentExecution
@Slf4j
public class TimeEventJob implements Job{

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		log.info("TimeEventJob正在执行...{}", LocalDateTime.now());
	}

}
