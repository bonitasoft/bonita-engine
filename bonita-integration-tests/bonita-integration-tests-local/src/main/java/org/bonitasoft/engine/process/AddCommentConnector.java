package org.bonitasoft.engine.process;

import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.exception.CreationException;

public class AddCommentConnector extends AbstractConnector {
    @Override
    protected void executeBusinessLogic() throws ConnectorException {
        try {
            getAPIAccessor().getProcessAPI().addProcessComment(getExecutionContext().getRootProcessInstanceId(), "comment added by connector");
        } catch (CreationException e) {
            throw new ConnectorException(e);
        }
    }
    @Override
    public void validateInputParameters() {

    }
}
