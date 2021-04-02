/**
 * Copyright (C) 2021 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.business.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import org.junit.Test;

public class ApplicationUpdaterTest {

    @Test
    public void should_set_icon_fields_when_updating_icon() {
        ApplicationUpdater applicationUpdater = new ApplicationUpdater();

        applicationUpdater.setIcon("myIcon.png", new byte[] { 1, 2, 3 });

        assertThat(applicationUpdater.getFields()).containsOnly(
                entry(ApplicationField.ICON_FILE_NAME, "myIcon.png"),
                entry(ApplicationField.ICON_CONTENT, new byte[] { 1, 2, 3 }));
    }
}
