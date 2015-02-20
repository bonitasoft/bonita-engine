package org.bonitasoft.engine.bpm.bar;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.bpm.bar.formmapping.model.FormMappingModel;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.junit.Test;

public class BusinessArchiveBuilderTest {

    @Test
    public void addFormMappingsShouldAddFileWithProperName() throws Exception {
        final FormMappingModel inputModel = new FormMappingModel();
        // when:
        final BusinessArchive archive = new BusinessArchiveBuilder().createNewBusinessArchive()
                .setProcessDefinition(new ProcessDefinitionBuilder().createNewInstance("proc", "1").done())
                .setFormMappings(inputModel).done();

        // then:
        assertThat(archive.getFormMappingModel()).isEqualTo(inputModel);
    }
}
