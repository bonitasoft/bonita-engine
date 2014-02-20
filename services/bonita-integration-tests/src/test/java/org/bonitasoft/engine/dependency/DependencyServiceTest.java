package org.bonitasoft.engine.dependency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.CommonServiceTest;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.dependency.model.builder.SDependencyBuilderFactory;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.Test;

/**
 * @author Charles Souillard
 */
public class DependencyServiceTest extends CommonServiceTest {

    private static DependencyService dependencyService;

    static {
        dependencyService = getServicesBuilder().buildDependencyService();
    }

    private final String defaultName = "abc";

    private final String defaultFileName = "dfv.cu";

    private final byte[] defaultValue = new byte[] { 12, 33 };

    private SDependency buildDefaultDependency() {
        return BuilderFactory.get(SDependencyBuilderFactory.class)
                .createNewInstance(defaultName, 2L, ScopeType.PROCESS, defaultFileName, defaultValue).done();
    }

    @Test
    public void testLifeCyle() throws Exception {
        getTransactionService().begin();

        final SDependency dependency = buildDefaultDependency();
        dependencyService.createDependency(dependency);

        assertEquals(dependency, dependencyService.getDependency(dependency.getId()));

        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(BuilderFactory.get(SDependencyBuilderFactory.class).getDescriptionKey(), "updated description");
        descriptor.addField(BuilderFactory.get(SDependencyBuilderFactory.class).getFileNameKey(), "updated filename");

        dependencyService.updateDependency(dependency, descriptor);

        final SDependency updatedDependency = dependencyService.getDependency(dependency.getId());

        assertEquals("updated description", updatedDependency.getDescription());
        assertEquals("updated filename", updatedDependency.getFileName());

        dependencyService.deleteDependency(dependency.getId());

        try {
            dependencyService.getDependency(dependency.getId());
            fail("dependency with id: " + dependency.getId() + " must not be found!");
        } catch (final SDependencyNotFoundException e) {
            // OK
        }

        getTransactionService().complete();
    }

    @Test
    public void testDeleteAllDependencies() throws Exception {
        getTransactionService().begin();

        final SDependency dependency = buildDefaultDependency();
        dependencyService.createDependency(dependency);

        dependencyService.deleteAllDependencies();

        List<Long> ids = new ArrayList<Long>();
        ids.add(dependency.getId());
        assertEquals(0, dependencyService.getDependencies(ids).size());

        getTransactionService().complete();
    }

}
