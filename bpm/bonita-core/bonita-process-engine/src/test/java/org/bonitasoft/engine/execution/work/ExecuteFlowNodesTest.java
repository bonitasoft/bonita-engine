package org.bonitasoft.engine.execution.work;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Iterator;

import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.impl.SAutomaticTaskInstanceImpl;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.work.BonitaWork;
import org.bonitasoft.engine.work.WorkService;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExecuteFlowNodesTest {

    /**
     * @author Baptiste Mesta
     */
    private final class WrapWorkOfType extends BaseMatcher<BonitaWork> {

        private final Class<?> class1;

        public WrapWorkOfType(final Class<?> class1) {
            this.class1 = class1;
        }

        @Override
        public boolean matches(final Object item) {
            if (item instanceof WrappingBonitaWork) {
                BonitaWork wrappedWork = ((WrappingBonitaWork) item).getWrappedWork();
                if (wrappedWork instanceof WrappingBonitaWork) {
                    WrappingBonitaWork innerWork = (WrappingBonitaWork) wrappedWork;
                    return new WrapWorkOfType(class1).matches(innerWork);
                } else {
                    return class1.isInstance(wrappedWork);
                }
            }
            return false;
        }

        @Override
        public void describeTo(final Description description) {

        }
    }

    @Mock
    private ActivityInstanceService activityInstanceService;

    @Mock
    private Iterator<Long> iterator;

    @Mock
    private TechnicalLoggerService logger;

    @Mock
    private WorkService workService;

    private ExecuteFlowNodes executeFlowNodes;

    @Before
    public void before() {
    }

    private void createExecutorWith(final SFlowNodeInstance... flowNodes) throws Exception {
        ArrayList<Long> nodes = new ArrayList<Long>();
        for (SFlowNodeInstance node : flowNodes) {
            nodes.add(node.getId());
            when(activityInstanceService.getFlowNodeInstance(node.getId())).thenReturn(node);
        }
        executeFlowNodes = new ExecuteFlowNodes(workService, logger, activityInstanceService, nodes.iterator());
    }

    @Test
    public final void execute_with_terminal_flow_node_should_register_notify_work() throws Exception {
        createExecutorWith(createTask(123l, true));

        executeFlowNodes.call();

        verify(workService).registerWork(argThat(new WrapWorkOfType(NotifyChildFinishedWork.class)));

    }

    private SAutomaticTaskInstanceImpl createTask(final long id, final boolean terminal) {
        SAutomaticTaskInstanceImpl sAutomaticTaskInstanceImpl = new SAutomaticTaskInstanceImpl();
        sAutomaticTaskInstanceImpl.setId(id);
        sAutomaticTaskInstanceImpl.setTerminal(terminal);
        sAutomaticTaskInstanceImpl.setLogicalGroup(3, 456l);
        return sAutomaticTaskInstanceImpl;
    }

    @Test
    public final void execute_with_non_terminal_flow_node_should_register_execute_work() throws Exception {
        createExecutorWith(createTask(123l, false));

        executeFlowNodes.call();

        verify(workService).registerWork(argThat(new WrapWorkOfType(ExecuteFlowNodeWork.class)));

    }

    @Test
    public final void execute_21_flow_node_only_execute_20() throws Exception {
        ArrayList<SFlowNodeInstance> list = new ArrayList<SFlowNodeInstance>();
        for (int i = 1; i <= 21; i++) {
            list.add(createTask(123 + i, false));
        }
        createExecutorWith(list.toArray(new SFlowNodeInstance[] {}));

        executeFlowNodes.call();

        assertThat(list.size()).isEqualTo(21);
        verify(workService,times(20)).registerWork(argThat(new WrapWorkOfType(ExecuteFlowNodeWork.class)));
    }

}
