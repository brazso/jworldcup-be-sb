package com.zematix.jworldcup.backend.configuration;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.scheduler.SchedulerService;
import com.zematix.jworldcup.backend.service.ServiceBase;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name="app.scheduler.enabled", matchIfMissing = true)
public class QuartzConfig extends ServiceBase {

	@Inject
	private SchedulerService schedulerService;
	
	@PostConstruct
	public void init() throws ServiceException {
		logger.trace("QuartzConfig: init");
		schedulerService.init();
	}
}
