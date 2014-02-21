/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.data.impl;

import java.util.Collections;
import java.util.Set;

import org.hibernate.jpa.boot.scan.spi.ScanOptions;
import org.hibernate.jpa.boot.scan.spi.ScanResult;
import org.hibernate.jpa.boot.scan.spi.Scanner;
import org.hibernate.jpa.boot.spi.ClassDescriptor;
import org.hibernate.jpa.boot.spi.MappingFileDescriptor;
import org.hibernate.jpa.boot.spi.PackageDescriptor;
import org.hibernate.jpa.boot.spi.PersistenceUnitDescriptor;

/**
 * @author Matthieu Chaffotte
 */
public class InactiveScanner implements Scanner {

    @Override
    public ScanResult scan(final PersistenceUnitDescriptor persistenceUnit, final ScanOptions options) {
        return new ScanResult() {

            @Override
            public Set<PackageDescriptor> getLocatedPackages() {
                return Collections.emptySet();
            }

            @Override
            public Set<MappingFileDescriptor> getLocatedMappingFiles() {
                return Collections.emptySet();
            }

            @Override
            public Set<ClassDescriptor> getLocatedClasses() {
                return Collections.emptySet();
            }
        };
    }
}
