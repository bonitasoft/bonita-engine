/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
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
