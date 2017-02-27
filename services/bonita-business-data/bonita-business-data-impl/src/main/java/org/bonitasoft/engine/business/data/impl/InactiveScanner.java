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
package org.bonitasoft.engine.business.data.impl;

import java.util.Collections;
import java.util.Set;

import org.hibernate.jpa.boot.scan.spi.ScanOptions;
import org.hibernate.jpa.boot.scan.spi.ScanResult;
import org.hibernate.jpa.boot.spi.ClassDescriptor;
import org.hibernate.jpa.boot.spi.MappingFileDescriptor;
import org.hibernate.jpa.boot.spi.PackageDescriptor;
import org.hibernate.jpa.boot.spi.PersistenceUnitDescriptor;

/**
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 */
public class InactiveScanner implements org.hibernate.jpa.boot.scan.spi.Scanner {

    @Override
    public ScanResult scan(PersistenceUnitDescriptor persistenceUnit, ScanOptions options) {
        return new ScanResult() {

            @Override
            public Set<PackageDescriptor> getLocatedPackages() {
                return Collections.emptySet();
            }

            @Override
            public Set<ClassDescriptor> getLocatedClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<MappingFileDescriptor> getLocatedMappingFiles() {
                return Collections.emptySet();
            }
        };
    }
}
