/**
 * Copyright (C) 2018 BonitaSoft S.A.
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
package org.bonitasoft.engine.test;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import java.util.Collections;
import java.util.Properties;

import org.bonitasoft.engine.api.ApiAccessType;
import org.bonitasoft.engine.util.APITypeManager;

public class EJBContainerFactory {
    public static void setupBonitaSysProps() {

        System.setProperty("sysprop.bdm.hibernate.transaction.jta_platform", TomeeJtaPlatform.class.getName());
        System.setProperty("sysprop.bonita.hibernate.transaction.jta_platform", TomeeJtaPlatform.class.getName());
        System.setProperty("sysprop.bonita.transaction.manager", "java:comp/TransactionManager");
    }

    public static void startTomee() {
        Properties props = new Properties();
        props.setProperty(EJBContainer.PROVIDER, "tomee-embedded");
        props.setProperty("tomee.ejbcontainer.http.port", "0");//random port
        EJBContainer ejbContainer = EJBContainer.createEJBContainer(props);
        Context ctx = ejbContainer.getContext();
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, TomeeContextFactory.class.getName());
        TomeeContextFactory.setContext(ctx);
    }

    public static void setAPIType() {
        APITypeManager.setAPITypeAndParams(ApiAccessType.EJB3,
                Collections.singletonMap("org.bonitasoft.engine.ejb.naming.reference", "serverAPIBean"));
    }
}
