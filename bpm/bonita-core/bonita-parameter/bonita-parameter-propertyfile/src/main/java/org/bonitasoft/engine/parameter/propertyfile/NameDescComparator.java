/*******************************************************************************
 * Copyright (C) 2009, 2012 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.parameter.propertyfile;

import java.util.Comparator;

import org.bonitasoft.engine.parameter.SParameter;

/**
 * @author Matthieu Chaffotte
 */
public class NameDescComparator implements Comparator<SParameter> {

    private final String nullValue;

    public NameDescComparator(final String nullValue) {
        this.nullValue = nullValue;
    }

    @Override
    public int compare(final SParameter o1, final SParameter o2) {
        final String name1 = o1.getName();
        final String name2 = o2.getName();
        if (nullValue.equals(name1)) {
            return -1;
        }
        if (nullValue.equals(name2)) {
            return 1;
        }
        return name2.compareTo(name1);
    }

}
