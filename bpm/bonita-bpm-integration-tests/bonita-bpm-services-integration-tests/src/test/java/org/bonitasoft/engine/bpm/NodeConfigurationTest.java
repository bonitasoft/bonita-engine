package org.bonitasoft.engine.bpm;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.api.impl.NodeConfiguration;
import org.bonitasoft.engine.api.impl.NodeConfigurationImpl;
import org.bonitasoft.engine.commons.RestartHandler;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.BeforeClass;
import org.junit.Test;

public class NodeConfigurationTest {

    public static NodeConfiguration nodeConfiguration;

    @BeforeClass
    public static void beforeClass() throws BonitaException {
        PlatformServiceAccessor platformAccessor = null;
        try {
            platformAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
        } catch (Exception ex) {
            throw new BonitaException(ex);
        }
        nodeConfiguration = platformAccessor.getPlaformConfiguration();
    }

    @Cover(classes = NodeConfiguration.class, concept = BPMNConcept.NONE, keywords = { "NodeConfiguration", "RestartHandlers" }, story = "Test set restart handlers.", jira = "ENGINE-612")
    @Test
    public void setRestartHandlers() {
        List<RestartHandler> lRestartHandlers = new ArrayList<RestartHandler>();
        RestartHandler rh = new RestartHandler() {

            @Override
            public void execute() {

            }
        };
        lRestartHandlers.add(rh);
        ((NodeConfigurationImpl) nodeConfiguration).setRestartHandlers(lRestartHandlers);
        assertEquals(1, nodeConfiguration.getRestartHandlers().size());
    }

    @Cover(classes = NodeConfiguration.class, concept = BPMNConcept.NONE, keywords = { "NodeConfiguration", "Scheduler" }, story = "Check result of should start scheduler.", jira = "ENGINE-612")
    @Test
    public void getShouldStartScheduler() {
        boolean startScheduler = ((NodeConfigurationImpl) nodeConfiguration).shouldStartScheduler();
        assertEquals(true, startScheduler);
    }

    @Cover(classes = NodeConfiguration.class, concept = BPMNConcept.NONE, keywords = { "NodeConfiguration", "Restart" }, story = "Check result of should restart element.", jira = "ENGINE-612")
    @Test
    public void getShouldRestartElements() {
        boolean restartElements = ((NodeConfigurationImpl) nodeConfiguration).shouldResumeElements();
        assertEquals(true, restartElements);
    }

    @Cover(classes = NodeConfiguration.class, concept = BPMNConcept.NONE, keywords = { "NodeConfiguration", "Restart" }, story = "Check result of should start event handling job.", jira = "ENGINE-612")
    @Test
    public void getShouldStartEventHandlingJob() {
        boolean startEvent = ((NodeConfigurationImpl) nodeConfiguration).shouldStartEventHandlingJob();
        assertEquals(true, startEvent);
    }

}
