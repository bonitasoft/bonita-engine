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
package org.bonitasoft.platform.setup.jndi;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jndi.JndiTemplate;
import org.springframework.stereotype.Component;

@Component
public class MemoryJNDISetup implements DisposableBean, InitializingBean {

    public static final String BONITA_NON_MANAGED_DS_JNDI_NAME = "java:comp/env/bonitaSequenceManagerDS";
    private final Logger logger = LoggerFactory.getLogger(MemoryJNDISetup.class.getSimpleName());

    private final JndiTemplate jndiTemplate;
    private final DataSource datasource;

    @Autowired
    public MemoryJNDISetup(final DataSource datasource) throws NamingException {
        super();
        this.datasource = datasource;
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.bonitasoft.platform.setup.jndi.SimpleMemoryContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES, "org.bonitasoft.platform.setup.jndi");
        jndiTemplate = new JndiTemplate();
    }

    public void afterPropertiesSet() throws NamingException {
        logger.info("Binding " + BONITA_NON_MANAGED_DS_JNDI_NAME + " @ " + datasource.toString());
        jndiTemplate.bind(BONITA_NON_MANAGED_DS_JNDI_NAME, datasource);
    }

    public void destroy() throws NamingException {
        logger.info("Unbinding " + BONITA_NON_MANAGED_DS_JNDI_NAME);
        jndiTemplate.unbind(BONITA_NON_MANAGED_DS_JNDI_NAME);
    }

}
