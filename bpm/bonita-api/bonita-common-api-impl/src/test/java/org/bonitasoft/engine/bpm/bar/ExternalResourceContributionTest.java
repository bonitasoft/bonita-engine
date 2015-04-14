package org.bonitasoft.engine.bpm.bar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.nio.file.Paths;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class ExternalResourceContributionTest {

    @Mock
    BusinessArchive businessArchive;

    @Test
    public void should_not_be_mandatory_contribution() throws Exception {
        //given
        ExternalResourceContribution externalResourceContribution = new ExternalResourceContribution();

        //when then
        assertThat(externalResourceContribution.isMandatory()).isFalse();

    }

    @Test
    public void should_have_a_name() throws Exception {
        //given
        ExternalResourceContribution externalResourceContribution = new ExternalResourceContribution();

        //when then
        assertThat(externalResourceContribution.getName()).isEqualTo("resources");
    }

    @Test
    public void should_retrieve_custom_pages_from_bar() throws Exception {
        //given
        ExternalResourceContribution externalResourceContribution = new ExternalResourceContribution();
        File barFolder = Paths.get(this.getClass().getResource("/barRoot").toURI()).toFile();

        //when
        externalResourceContribution.readFromBarFolder(businessArchive, barFolder);

        //then
        verify(businessArchive).addResource(eq("resources/customPages/custompage_step1.zip"), notNull(byte[].class));
        verify(businessArchive).addResource(eq("resources/customPages/custompage_step2.zip"), notNull(byte[].class));
    }
}