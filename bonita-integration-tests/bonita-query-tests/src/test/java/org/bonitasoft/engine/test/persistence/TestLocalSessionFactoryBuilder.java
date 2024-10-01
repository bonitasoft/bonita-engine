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
package org.bonitasoft.engine.test.persistence;

import org.bonitasoft.engine.persistence.PostgresMaterializedBlobType;
import org.bonitasoft.engine.persistence.PostgresMaterializedClobType;
import org.bonitasoft.engine.persistence.PostgresXMLType;
import org.bonitasoft.engine.persistence.XMLType;
import org.bonitasoft.engine.services.Vendor;
import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.orm.hibernate5.LocalSessionFactoryBuilder;

public class TestLocalSessionFactoryBuilder extends LocalSessionFactoryBean {

    private Interceptor interceptor;

    @Override
    protected SessionFactory buildSessionFactory(LocalSessionFactoryBuilder sfb) {
        Vendor vendor = Vendor.fromHibernateConfiguration(sfb);
        //register type before loading mappings/entities, type should be present before loading JPA entities
        switch (vendor) {
            case ORACLE, OTHER, SQLSERVER:
                sfb.registerTypeOverride(XMLType.INSTANCE);
                break;
            case MYSQL:
                System.setProperty("hibernate.dialect.storage_engine", "innodb");
                sfb.registerTypeOverride(XMLType.INSTANCE);
                break;
            case POSTGRES:
                sfb.registerTypeOverride(new PostgresMaterializedBlobType());
                sfb.registerTypeOverride(new PostgresMaterializedClobType());
                sfb.registerTypeOverride(PostgresXMLType.INSTANCE);
                break;
        }
        if (interceptor != null) {
            sfb.setInterceptor(interceptor);
        }
        return super.buildSessionFactory(sfb);
    }

    public void setInterceptor(Interceptor interceptor) {
        this.interceptor = interceptor;
    }

}
