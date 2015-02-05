/*******************************************************************************
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
 ******************************************************************************/
package org.bonitasoft.engine.business.data.impl.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.bonitasoft.engine.business.data.impl.filter.WithoutDAOImplementationFileFilter;
import org.junit.Before;
import org.junit.Test;

public class WithoutDAOImplementationFileFilterTest {

    private WithoutDAOImplementationFileFilter fileFilter;

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
    public void should_accept_return_true() {
        File f = new File("Employee.class");
        assertThat(fileFilter.accept(f)).isTrue();

        f = new File("Employee.java");
        assertThat(fileFilter.accept(f)).isTrue();
    }

}
