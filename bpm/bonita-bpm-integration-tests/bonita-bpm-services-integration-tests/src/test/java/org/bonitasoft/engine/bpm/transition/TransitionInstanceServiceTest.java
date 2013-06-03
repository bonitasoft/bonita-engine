package org.bonitasoft.engine.bpm.transition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.TransitionService;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.STransitionInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.core.process.instance.model.builder.STransitionInstanceBuilder;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 */
public class TransitionInstanceServiceTest extends CommonBPMServicesTest {

    private TransitionService getTransitionService() {
        return getServicesBuilder().getTransitionInstanceService();
    }

    private BPMInstanceBuilders getBpmInstanceBuilders() {
        return getServicesBuilder().getBPMInstanceBuilders();
    }

    private List<STransitionInstance> searchTransitionInstances(final QueryOptions searchOptions) throws SBonitaException {
        getTransactionService().begin();
        final List<STransitionInstance> transitions = getTransitionService().search(searchOptions);
        getTransactionService().complete();

        return transitions;
    }

    private long getNbOfTransitionInstances(final QueryOptions countOptions) throws SBonitaException {
        getTransactionService().begin();
        final long nbOfTransitionInst = getTransitionService().getNumberOfTransitionInstances(countOptions);
        getTransactionService().complete();

        return nbOfTransitionInst;
    }

    private void createTransitionInstance(final String name, final long rootProcessInstanceId, final long processDefinitionId,
            final long parentProcessInstanceId) throws SBonitaException {
        final STransitionInstance transitionInstance = getBpmInstanceBuilders().getSTransitionInstanceBuilder()
                .createNewInstance(name, rootProcessInstanceId, processDefinitionId, parentProcessInstanceId).done();
        getTransactionService().begin();
        getTransitionService().create(transitionInstance);
        getTransactionService().complete();
    }

    @Test
    public void testSearchTransitionInstances() throws Exception {
        final STransitionInstanceBuilder transitionInstanceBuilder = getBpmInstanceBuilders().getSTransitionInstanceBuilder();
        final SProcessInstance procInst1 = createSProcessInstance();
        final SProcessInstance procInst2 = createSProcessInstance();

        final OrderByOption oderByOption = new OrderByOption(STransitionInstance.class, transitionInstanceBuilder.getNameKey(), OrderByType.ASC);
        final List<FilterOption> filterOptions = Collections.emptyList();
        final QueryOptions queryOptions = new QueryOptions(0, 10, Collections.singletonList(oderByOption), filterOptions, null);
        final QueryOptions countOptions = new QueryOptions(0, 10);

        // search: no result expected
        List<STransitionInstance> transitionInstances = searchTransitionInstances(queryOptions);
        final long initialNbOfTransitionInstances = getNbOfTransitionInstances(countOptions);
        assertEquals(transitionInstances.size(), initialNbOfTransitionInstances);

        // create transitions
        createTransitions(procInst1, procInst2);

        // search: created flow nodes must be retrieved
        transitionInstances = searchTransitionInstances(queryOptions);
        final long nbOfTransitionInstances = getNbOfTransitionInstances(countOptions);
        assertEquals(initialNbOfTransitionInstances + 4, transitionInstances.size());
        assertEquals(initialNbOfTransitionInstances + 4, nbOfTransitionInstances);

        // delete process instances
        deleteSProcessInstance(procInst1);
        deleteSProcessInstance(procInst2);

        // transitionInstances = searchTransitionInstances(queryOptions);
        // nbOfTransitionInstances = getNbOfTransitionInstances(countOptions);
        // assertEquals(0, transitionInstances.size());
        // assertEquals(0, nbOfTransitionInstances);
    }

    private void createTransitions(final SProcessInstance procInst1, final SProcessInstance procInst2) throws Exception {
        createTransitionInstance("t1", procInst1.getId(), 2, procInst1.getId());
        createTransitionInstance("t2", procInst1.getId(), 2, procInst1.getId());
        createTransitionInstance("t3", procInst2.getId(), 2, procInst2.getId());
        createTransitionInstance("t4", procInst2.getId(), 2, procInst2.getId());
    }

    @Test
    public void testSearchTransitionInstancesWithFilter() throws Exception {
        final STransitionInstanceBuilder transitionInstanceBuilder = getBpmInstanceBuilders().getSTransitionInstanceBuilder();
        final SProcessInstance procInst1 = createSProcessInstance();
        final SProcessInstance procInst2 = createSProcessInstance();

        final List<FilterOption> filterOptions = new ArrayList<FilterOption>(2);
        filterOptions.add(new FilterOption(STransitionInstance.class, transitionInstanceBuilder.getParentProcessInstanceKey(), procInst1.getId()));
        filterOptions.add(new FilterOption(STransitionInstance.class, transitionInstanceBuilder.getTerminalKey(), false));
        final OrderByOption oderByOption = new OrderByOption(STransitionInstance.class, transitionInstanceBuilder.getNameKey(), OrderByType.ASC);
        final QueryOptions queryOptions = new QueryOptions(0, 10, Collections.singletonList(oderByOption), filterOptions, null);
        final QueryOptions countOptions = new QueryOptions(0, 10, null, filterOptions, null);

        // search: no result expected
        List<STransitionInstance> transitionInstances = searchTransitionInstances(queryOptions);
        long nbOfTransitionInstances = getNbOfTransitionInstances(countOptions);
        assertTrue(transitionInstances.isEmpty());
        assertEquals(0, nbOfTransitionInstances);

        // create transitions
        createTransitions(procInst1, procInst2);

        // search: created flow nodes must be retrieved
        transitionInstances = searchTransitionInstances(queryOptions);
        nbOfTransitionInstances = getNbOfTransitionInstances(countOptions);
        assertEquals(2, transitionInstances.size());
        assertEquals(2, nbOfTransitionInstances);

        // delete process instances
        deleteSProcessInstance(procInst1);
        deleteSProcessInstance(procInst2);
    }

}
