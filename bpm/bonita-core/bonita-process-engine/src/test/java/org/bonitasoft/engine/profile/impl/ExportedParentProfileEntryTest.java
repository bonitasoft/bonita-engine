/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
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
    public void testEqual_sameEntry() {
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
    public void testEqual_with_null() {
        // given
        entry1 = new ExportedParentProfileEntry(NAME);

        // when then
        shouldNotBeEquals(entry1, null);
        shouldNotBeEquals(null, null);
        shouldNotBeEquals(null, entry1);

    }

    @Test
    public void should_get_one_errors() {
        // given
        final ExportedParentProfileEntry parentEntry = new ExportedParentProfileEntry(null);

        // when
        final List<ImportError> errors = parentEntry.getErrors();

        // then
        assertThat(errors).isNotEmpty().hasSize(1);
        assertThat(parentEntry.hasErrors()).isTrue();

    }

    @Test
    public void should_get_child_errors() {
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
    public void should_get_no_errors_with_valid_children() {
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
    public void should_get_no_errors() {
        // given
        final ExportedParentProfileEntry parentEntry = new ExportedParentProfileEntry("name");

        // when
        final List<ImportError> errors = parentEntry.getErrors();

        // then
        assertThat(errors).isNull();
        assertThat(parentEntry.hasErrors()).isFalse();

    }

    @Test
    public void should_hasCustomPage_beTrue() {
        // given
        final ExportedParentProfileEntry parentEntry = new ExportedParentProfileEntry("name");
        parentEntry.setCustom(true);

        // when then
        assertThat(parentEntry.hasCustomPages()).isTrue();

    }

    @Test
    public void should_hasCustomPage_child_beTrue() {
        // given
        final ExportedParentProfileEntry parentEntry = new ExportedParentProfileEntry("name");
        parentEntry.setCustom(false);
        final ExportedProfileEntry childEntry = new ExportedProfileEntry("name");
        childEntry.setPage("page");
        childEntry.setCustom(true);

        parentEntry.setChildProfileEntries(Arrays.asList(childEntry));
        // when then
        assertThat(parentEntry.hasCustomPages()).isTrue();

    }

    @Test
    public void should_hasCustomPage_beFalse() {
        // given
        final ExportedParentProfileEntry parentEntry = new ExportedParentProfileEntry("name");
        parentEntry.setCustom(false);

        // when then
        assertThat(parentEntry.hasCustomPages()).isFalse();

    }
}
