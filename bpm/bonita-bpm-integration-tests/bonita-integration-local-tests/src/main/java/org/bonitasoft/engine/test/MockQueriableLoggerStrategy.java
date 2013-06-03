package org.bonitasoft.engine.test;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.services.QueriableLoggerStrategy;

/**
 * @author Elias Ricken de Medeiros
 */
public class MockQueriableLoggerStrategy implements QueriableLoggerStrategy {

    private final List<String> loggable;

    public MockQueriableLoggerStrategy() {
        this.loggable = new ArrayList<String>();
        this.loggable.add("execute_connector_:" + SQueriableLogSeverity.BUSINESS);
        this.loggable.add("variable_update_:" + SQueriableLogSeverity.BUSINESS);
        this.loggable.add("execute_connector_:" + SQueriableLogSeverity.INTERNAL);
        this.loggable.add("variable_update_:" + SQueriableLogSeverity.INTERNAL);
    }

    @Override
    public boolean isLoggable(final String actionType, final SQueriableLogSeverity severity) {
        return !this.loggable.contains(actionType + ":" + severity.toString());
    }

}
