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
import static org.mockito.Mockito.doReturn;

import java.io.File;

import org.bonitasoft.engine.business.data.impl.filter.OnlyDAOImplementationFileFilter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Romain
 * @author Laurent Leseigneur
 */
@RunWith(MockitoJUnitRunner.class)
public class OnlyDAOImplementationFileFilterTest {

    private OnlyDAOImplementationFileFilter fileFilter;

    @Mock
    File file;

    /**
     * @throws Exception
     */
    @Before
    public void setUp() {
        fileFilter = new OnlyDAOImplementationFileFilter();
    }

    @Test
    public void should_accept_return_true() {
        File f = new File("EmployeeDAO.class");
        assertThat(fileFilter.accept(f)).isFalse();

        f = new File("EmployeeDAOImpl.class");
        assertThat(fileFilter.accept(f)).isTrue();

        checkAcceptShouldReturns("org", "bonitasoft", "class", true);
        checkAcceptShouldReturns("com", "bonitasoft", "java", false);
        checkAcceptShouldReturns("com", "company", "java", false);

    }

    @Test
    public void should_accept_return_false() {
        File f = new File("Employee.class");
        assertThat(fileFilter.accept(f)).isFalse();

        f = new File("Employee.java.txt");
        assertThat(fileFilter.accept(f)).isFalse();
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
