package org.bonitasoft.engine.profile.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.api.ImportError;
import org.junit.Test;

public class ExportedParentProfileEntryTest {

    private static final String OTHER_NAME = "other name";

    private static final String NAME = "name";

    private ExportedParentProfileEntry entry1;

    private ExportedParentProfileEntry entry2;

    private ExportedParentProfileEntry entry3;

    @Test
    public void testEqual_sameEntry() throws Exception {
        // given
        entry1 = new ExportedParentProfileEntry(NAME);
        entry2 = new ExportedParentProfileEntry(NAME);
        entry3 = new ExportedParentProfileEntry(OTHER_NAME);

        // when
        shouldBeEquals(entry1, entry2);
        // then
        shouldNotBeEquals(entry1, entry3);

    }

    private void shouldBeEquals(final ExportedParentProfileEntry entryA, final ExportedParentProfileEntry entryB) {
        assertThat(entryA).as("should be equals").isEqualTo(entryB);
        assertThat(entryA.hashCode()).as("hash code should be equals").isEqualTo(entryB.hashCode());
    }

    private void shouldNotBeEquals(final ExportedParentProfileEntry entryA, final ExportedParentProfileEntry entryB) {
        if (null != entryA && null != entryB)
        {
            assertThat(entryA).as("should not be equals").isNotEqualTo(entryB);
            assertThat(entryA.hashCode()).as("hash code should not be equals").isNotEqualTo(entryB.hashCode());
        }
    }

    @Test
    public void testEqual_with_null() throws Exception {
        // given
        entry1 = new ExportedParentProfileEntry(NAME);

        // when then
        shouldNotBeEquals(entry1, null);
        shouldNotBeEquals(null, null);
        shouldNotBeEquals(null, entry1);

    }

    @Test
    public void should_get_one_errors() throws Exception {
        // given
        final ExportedParentProfileEntry parentEntry = new ExportedParentProfileEntry(null);

        // when
        final List<ImportError> errors = parentEntry.getErrors();

        // then
        assertThat(errors).isNotEmpty().hasSize(1);
        assertThat(parentEntry.hasErrors()).isTrue();

    }

    @Test
    public void should_get_child_errors() throws Exception {
        // given
        final ExportedParentProfileEntry parentEntry = new ExportedParentProfileEntry("name");
        final ExportedProfileEntry childEntry = new ExportedProfileEntry("name");
        childEntry.setPage(null);
        parentEntry.setChildProfileEntries(Arrays.asList(childEntry));

        // when
        final List<ImportError> errors = parentEntry.getErrors();

        // then
        assertThat(errors).isNotEmpty().hasSize(1);
        assertThat(parentEntry.hasErrors()).isTrue();

    }

    @Test
    public void should_get_no_errors_with_valid_children() throws Exception {
        // given
        final ExportedParentProfileEntry parentEntry = new ExportedParentProfileEntry("name");
        final ExportedProfileEntry childEntry = new ExportedProfileEntry("name");
        childEntry.setPage("page");

        // when
        final List<ImportError> errors = parentEntry.getErrors();

        // then
        assertThat(errors).isNull();
        assertThat(parentEntry.hasErrors()).isFalse();

    }

    @Test
    public void should_get_no_errors() throws Exception {
        // given
        final ExportedParentProfileEntry parentEntry = new ExportedParentProfileEntry("name");

        // when
        final List<ImportError> errors = parentEntry.getErrors();

        // then
        assertThat(errors).isNull();
        assertThat(parentEntry.hasErrors()).isFalse();

    }

}
