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

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.profile.xml.ProfileEntryNode;
import org.junit.Test;

public class ProfileEntryTest {

    private static final String OTHER_NAME = "other name";

    private static final String NAME = "name";

    private ProfileEntryNode entry1;

    private ProfileEntryNode entry2;

    private ProfileEntryNode entry3;

    @Test
    public void testEqual_sameEntry() {
        // given
        entry1 = new ProfileEntryNode(NAME);
        entry2 = new ProfileEntryNode(NAME);
        entry3 = new ProfileEntryNode(OTHER_NAME);

        // when
        shouldBeEquals(entry1, entry2);
        // then
        shouldNotBeEquals(entry1, entry3);

    }

    private void shouldBeEquals(final ProfileEntryNode entryA, final ProfileEntryNode entryB) {
        assertThat(entryA).as("should be equals").isEqualTo(entryB);
        assertThat(entryA.hashCode()).as("hash code should be equals").isEqualTo(entryB.hashCode());
    }

    private void shouldNotBeEquals(final ProfileEntryNode entryA, final ProfileEntryNode entryB) {
        if (null != entryA && null != entryB)
        {
            assertThat(entryA).as("should not be equals").isNotEqualTo(entryB);
            assertThat(entryA.hashCode()).as("hash code should not be equals").isNotEqualTo(entryB.hashCode());
        }
    }

    @Test
    public void testEqual_with_null() {
        // given
        entry1 = new ProfileEntryNode(NAME);

        // when then
        shouldNotBeEquals(entry1, null);
        shouldNotBeEquals(null, null);
        shouldNotBeEquals(null, entry1);

    }

    @Test
    public void should_get_error() {
        // given
        final ProfileEntryNode entry = new ProfileEntryNode(null);

        // when
        final ImportError hasError = entry.getError();

        // then
        assertThat(hasError).isNotNull();

    }

    @Test
    public void should_get_no_error() {
        // given
        final ProfileEntryNode entry = new ProfileEntryNode("name");
        entry.setPage("page");

        // when
        final ImportError hasError = entry.getError();

        // then
        assertThat(hasError).isNull();

    }

    @Test
    public void should_has_error_when_page_is_null
            () {
        // given
        final ProfileEntryNode entry = new ProfileEntryNode("name");
        entry.setPage(null);

        // when
        final boolean hasError = entry.hasError();

        // then
        assertThat(hasError).isTrue();

    }
}
