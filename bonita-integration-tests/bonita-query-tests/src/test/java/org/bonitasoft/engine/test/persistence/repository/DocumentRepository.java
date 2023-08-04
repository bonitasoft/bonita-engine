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
package org.bonitasoft.engine.test.persistence.repository;

import static java.lang.System.currentTimeMillis;

import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.core.document.model.archive.SAMappedDocument;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

/**
 * @author Emmanuel Duchastenier
 */
@Repository
public class DocumentRepository extends TestRepository {

    public DocumentRepository(final SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public SMappedDocument getSMappedDocumentOfProcessWithName(String name, long processInstanceId) {
        final Query namedQuery = getNamedQuery("getSMappedDocumentOfProcessWithName");
        namedQuery.setParameter("name", name);
        namedQuery.setParameter("processInstanceId", processInstanceId);
        return (SMappedDocument) namedQuery.uniqueResult();
    }

    public SAMappedDocument getSAMappedDocumentOfProcessWithName(String name, long processInstanceId) {
        final Query namedQuery = getNamedQuery("getArchivedDocumentList");
        namedQuery.setParameter("name", name);
        namedQuery.setParameter("processInstanceId", processInstanceId);
        namedQuery.setParameter("time", currentTimeMillis() - 100000);
        return (SAMappedDocument) namedQuery.uniqueResult();
    }
}
