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
