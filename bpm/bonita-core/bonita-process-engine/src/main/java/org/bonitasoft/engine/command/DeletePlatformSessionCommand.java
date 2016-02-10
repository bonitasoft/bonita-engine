package org.bonitasoft.engine.command;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.platform.session.PlatformSessionService;
import org.bonitasoft.engine.platform.session.SSessionNotFoundException;
import org.bonitasoft.engine.service.PlatformServiceAccessor;

/**
 * @author Charles Souillard
 */
public class DeletePlatformSessionCommand extends PlatformCommand {

    public Serializable execute(Map parameters, PlatformServiceAccessor serviceAccessor)
            throws SCommandParameterizationException, SCommandExecutionException
    {
        Long sessionId = (Long)parameters.get("sessionId");
        PlatformSessionService sessionService = serviceAccessor.getPlatformSessionService();
        try {
            sessionService.deleteSession(sessionId.longValue());
        } catch(SSessionNotFoundException e) {
            throw new SCommandExecutionException(e);
        }
        return null;
    }

}