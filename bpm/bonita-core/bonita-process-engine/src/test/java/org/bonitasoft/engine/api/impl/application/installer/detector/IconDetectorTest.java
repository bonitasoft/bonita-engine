/**
 * Copyright (C) 2023 Bonitasoft S.A.
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
package org.bonitasoft.engine.api.impl.application.installer.detector;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IconDetectorTest {

    private IconDetector iconDetector;

    @BeforeEach
    void setup() throws Exception {
        iconDetector = new IconDetector();
    }

    @Test
    void isCompliant() throws Exception {
        var pngIcon = new File(IconDetectorTest.class.getResource("/icon.png").getFile());
        var jpegIcon = new File(IconDetectorTest.class.getResource("/icon.JPEG").getFile());

        assertThat(iconDetector.isCompliant(pngIcon)).isTrue();
        assertThat(iconDetector.isCompliant(jpegIcon)).isTrue();
    }

    @Test
    void isNotCompliant() throws Exception {
        var icon = new File(IconDetectorTest.class.getResource("/icon.docx").getFile());

        assertThat(iconDetector.isCompliant(icon)).isFalse();
    }
}
