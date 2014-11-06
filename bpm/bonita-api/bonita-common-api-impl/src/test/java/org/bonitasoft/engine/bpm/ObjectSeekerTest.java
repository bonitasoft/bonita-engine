package org.bonitasoft.engine.bpm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.bpm.data.DataDefinition;
import org.bonitasoft.engine.bpm.data.impl.DataDefinitionImpl;
import org.junit.Test;

public class ObjectSeekerTest {

    @Test
    public void seekANamedElementObject() {
        final List<DataDefinition> dataDefinitions = new ArrayList<DataDefinition>();
        final DataDefinitionImpl dataDefinitionImpl = new DataDefinitionImpl("var1", null);
        dataDefinitions.add(dataDefinitionImpl);
        dataDefinitions.add(new DataDefinitionImpl("var2", null));

        final DataDefinition dataDefinition = ObjectSeeker.getNamedElement(dataDefinitions, "var1");

        assertThat(dataDefinition).isEqualTo(dataDefinitionImpl);
    }

    @Test
    public void seekANullObjectReturnsNull() {
        final List<DataDefinition> dataDefinitions = new ArrayList<DataDefinition>();
        dataDefinitions.add(new DataDefinitionImpl("var1", null));
        dataDefinitions.add(new DataDefinitionImpl("var2", null));

        final DataDefinition dataDefinition = ObjectSeeker.getNamedElement(dataDefinitions, null);

        assertThat(dataDefinition).isNull();
    }

    @Test
    public void seekAnUnknownObjectReturnsNull() {
        final List<DataDefinition> dataDefinitions = new ArrayList<DataDefinition>();
        dataDefinitions.add(new DataDefinitionImpl("var1", null));
        dataDefinitions.add(new DataDefinitionImpl("var2", null));

        final DataDefinition dataDefinition = ObjectSeeker.getNamedElement(dataDefinitions, "var3");

        assertThat(dataDefinition).isNull();
    }

    @Test
    public void seekAObjectInANullListReturnsNull() {
        final DataDefinition dataDefinition = ObjectSeeker.getNamedElement(null, "var1");

        assertThat(dataDefinition).isNull();
    }

    @Test
    public void seekAnObjectInAnEmptyListReturnNull() {
        final List<DataDefinition> dataDefinitions = new ArrayList<DataDefinition>();

        final DataDefinition dataDefinition = ObjectSeeker.getNamedElement(dataDefinitions, "var3");

        assertThat(dataDefinition).isNull();
    }
}
