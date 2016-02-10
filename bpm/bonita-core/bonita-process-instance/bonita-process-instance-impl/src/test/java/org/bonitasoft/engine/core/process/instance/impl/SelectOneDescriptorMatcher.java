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
package org.bonitasoft.engine.core.process.instance.impl;

import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.mockito.ArgumentMatcher;

public class SelectOneDescriptorMatcher extends ArgumentMatcher<SelectOneDescriptor<Long>> {

    private final SelectOneDescriptor<Long> descriptor;

    public SelectOneDescriptorMatcher(final SelectOneDescriptor<Long> countDescriptor) {
        descriptor = countDescriptor;
    }

    @Override
    public boolean matches(final Object object) {
        final SelectOneDescriptor<Long> currentDescriptor = (SelectOneDescriptor<Long>) object;
        return sameQueryName(currentDescriptor) && sameReturnType(currentDescriptor) && sameEntityType(currentDescriptor)
                && sameInputParameters(currentDescriptor);
    }

    private boolean sameQueryName(final SelectOneDescriptor<Long> currentDescriptor) {
        return descriptor.getQueryName().equals(currentDescriptor.getQueryName());
    }

    private boolean sameReturnType(final SelectOneDescriptor<Long> currentDescriptor) {
        return descriptor.getReturnType().equals(currentDescriptor.getReturnType());
    }

    private boolean sameEntityType(final SelectOneDescriptor<Long> currentDescriptor) {
        return descriptor.getEntityType().equals(currentDescriptor.getEntityType());
    }

    private boolean sameInputParameters(final SelectOneDescriptor<Long> currentDescriptor) {
        return descriptor.getInputParameters().equals(currentDescriptor.getInputParameters());
    }

}
