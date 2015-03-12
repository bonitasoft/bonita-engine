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
package org.bonitasoft.engine.business.data.impl.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WithoutDAOImplementationFileFilterTest {

    private WithoutDAOImplementationFileFilter fileFilter;

    @Mock
    File file;

    /**
     * @throws Exception
     */
    @Before
    public void setUp() {
        fileFilter = new WithoutDAOImplementationFileFilter();
    }

    @Test
    public void should_accept_return_false() {
        File f = new File("EmployeeDAOImpl.class");
        assertThat(fileFilter.accept(f)).isFalse();

        f = new File("EmployeeDAOImpl.java");
        assertThat(fileFilter.accept(f)).isFalse();
    }

    @Test
    public void should_accept_exclude_reserved_package() {
        checkAcceptShouldReturns("net", "bonitasoft", "class", true);
        checkAcceptShouldReturns("net", "bonitasoft", "java", true);
    }

    private void checkAcceptShouldReturns(String domain, String subDomain, String extension, boolean expectedResult) {
        // given
        doReturn("Employee." + extension).when(file).getName();
        doReturn(domain + File.separatorChar + subDomain + File.separatorChar + "model" + File.separatorChar + "Employee." + extension).when(file)
                .getAbsolutePath();

        // when then
        assertThat(fileFilter.accept(file)).as("should return " + expectedResult + " when accepting file " + file.getAbsolutePath()).isEqualTo(expectedResult);
    }
}
