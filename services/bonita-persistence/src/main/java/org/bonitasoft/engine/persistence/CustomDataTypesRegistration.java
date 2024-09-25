/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.persistence;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.boot.spi.SessionFactoryBuilderFactory;
import org.hibernate.boot.spi.SessionFactoryBuilderImplementor;
import org.hibernate.type.BasicType;
import org.slf4j.LoggerFactory;

public class CustomDataTypesRegistration implements SessionFactoryBuilderFactory {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CustomDataTypesRegistration.class);

    @Getter
    private static Set<BasicType> typeOverrides = new HashSet<>();

    @Override
    public SessionFactoryBuilder getSessionFactoryBuilder(final MetadataImplementor metadata,
            final SessionFactoryBuilderImplementor defaultBuilder) {
        for (BasicType typeOverride : typeOverrides) {
            logger.debug("Registering custom Hibernate data type {}", typeOverride);
            metadata.getTypeResolver().registerTypeOverride(typeOverride);
        }
        return defaultBuilder;
    }

    public static void addTypeOverride(BasicType type) {
        typeOverrides.add(type);
    }

}
