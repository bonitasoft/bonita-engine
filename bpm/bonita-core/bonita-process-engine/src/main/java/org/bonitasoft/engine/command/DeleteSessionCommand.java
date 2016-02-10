package org.bonitasoft.engine.command;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.command.system.CommandWithParameters;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionService;

/**
 * @author Charles Souillard
 */
public class DeleteSessionCommand extends CommandWithParameters {

    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor) throws SCommandParameterizationException, SCommandExecutionException {
        Long sessionId = (Long)parameters.get("sessionId");
        SessionService sessionService = serviceAccessor.getSessionService();
        try {
            sessionService.deleteSession(sessionId.longValue());
        } catch(SSessionNotFoundException e) {
            throw new SCommandExecutionException(e);
        }
        return null;
    }
}
