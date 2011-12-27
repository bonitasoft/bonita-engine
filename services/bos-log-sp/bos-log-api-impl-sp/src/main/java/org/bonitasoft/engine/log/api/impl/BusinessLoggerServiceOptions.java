package org.bonitasoft.engine.log.api.impl;

import java.util.List;

import org.bonitasoft.engine.businesslogger.model.SBusinessLogSeverity;
import org.bonitasoft.engine.services.BusinessLoggerServiceConfiguration;

public class BusinessLoggerServiceOptions implements BusinessLoggerServiceConfiguration {

    private boolean needsInferCall;

    private List<String> loggableLevels;

    public BusinessLoggerServiceOptions(boolean needsInferCall, List<String> loggableLevels) {
        this.needsInferCall = needsInferCall;
        this.loggableLevels = loggableLevels;
    }

    @Override
    public boolean needsInferCaller() {
        return needsInferCall;
    }

    @Override
    public boolean isLogable(String actionType, SBusinessLogSeverity severity) {
        if (loggableLevels == null) {
            return false;
        }
        return loggableLevels.contains(actionType + ":" + severity);
    }
    
}
