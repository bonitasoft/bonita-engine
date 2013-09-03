package org.bonitasoft.engine.scheduler.impl;

import org.quartz.DisallowConcurrentExecution;

@DisallowConcurrentExecution
public class NonConcurrentQuartzJob extends QuartzJob {

}
