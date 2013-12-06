/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package com.bonitasoft.engine.search.descriptor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.search.descriptor.FieldDescriptor;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;

import com.bonitasoft.engine.core.reporting.SReport;
import com.bonitasoft.engine.core.reporting.SReportBuilderFactory;
import com.bonitasoft.engine.reporting.ReportSearchDescriptor;

/**
 * @author Matthieu Chaffotte
 */
public class SearchReportDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> reportKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> reportAllFields;

    SearchReportDescriptor() {
        final SReportBuilderFactory keyProvider = BuilderFactory.get(SReportBuilderFactory.class);
        reportKeys = new HashMap<String, FieldDescriptor>(5);
        reportKeys.put(ReportSearchDescriptor.ID, new FieldDescriptor(SReport.class, keyProvider.getIdKey()));
        reportKeys.put(ReportSearchDescriptor.NAME, new FieldDescriptor(SReport.class, keyProvider.getNameKey()));
        reportKeys.put(ReportSearchDescriptor.PROVIDED, new FieldDescriptor(SReport.class, keyProvider.getProvidedKey()));
        reportKeys.put(ReportSearchDescriptor.INSTALLATION_DATE, new FieldDescriptor(SReport.class, keyProvider.getInstallationDateKey()));
        reportKeys.put(ReportSearchDescriptor.INSTALLED_BY, new FieldDescriptor(SReport.class, keyProvider.getInstalledByKey()));

        reportAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        final Set<String> reportFields = new HashSet<String>(8);
        reportFields.add(keyProvider.getNameKey());
        reportAllFields.put(SReport.class, reportFields);
    }

    @Override
    protected Map<String, FieldDescriptor> getEntityKeys() {
        return reportKeys;
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return reportAllFields;
    }

}
