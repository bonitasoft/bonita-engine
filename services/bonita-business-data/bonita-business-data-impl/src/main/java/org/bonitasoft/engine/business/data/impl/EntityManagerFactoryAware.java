/**
 * Copyright (C) 2025 Bonitasoft S.A.
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
package org.bonitasoft.engine.business.data.impl;

import javax.persistence.EntityManagerFactory;

/**
 * Interface to be implemented by classes that need access to the {@link EntityManagerFactory}.
 * This is typically used in the context of business data repositories.
 * This class was introduced with the use of aspects for business data repository: A proxy class is generated, such that
 * {@link JPABusinessDataRepositoryImpl} cannot be cast directly. Introducing this interface allows
 * JPABusinessDataRepositoryImpl to be cast to this interface (See BDRepositoryLocalIT class).
 */
public interface EntityManagerFactoryAware {

    EntityManagerFactory getEntityManagerFactory();

}
