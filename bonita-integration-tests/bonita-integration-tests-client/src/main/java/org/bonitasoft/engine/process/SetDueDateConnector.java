package org.bonitasoft.engine.process;

import java.util.Date;

import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.exception.UpdateException;

public class SetDueDateConnector extends AbstractConnector {

    static Date dueDate = new Date(1000000);

    @Override
    protected void executeBusinessLogic() throws ConnectorException {
        long activityInstanceId = getExecutionContext().getActivityInstanceId();
        try {
            getAPIAccessor().getProcessAPI().updateDueDateOfTask(activityInstanceId, dueDate);
        } catch (UpdateException e) {
            throw new ConnectorException(e);
        }
    }

    @Override
    public void validateInputParameters() throws ConnectorValidationException {

    }
}
