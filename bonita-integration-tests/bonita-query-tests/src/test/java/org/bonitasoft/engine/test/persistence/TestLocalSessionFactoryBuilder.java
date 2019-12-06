package org.bonitasoft.engine.test.persistence;

import org.bonitasoft.engine.persistence.PostgresInterceptor;
import org.bonitasoft.engine.persistence.PostgresMaterializedBlobType;
import org.bonitasoft.engine.persistence.PostgresMaterializedClobType;
import org.bonitasoft.engine.persistence.PostgresXMLType;
import org.bonitasoft.engine.persistence.SQLServerInterceptor;
import org.bonitasoft.engine.persistence.XMLType;
import org.bonitasoft.engine.services.Vendor;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;

public class TestLocalSessionFactoryBuilder extends LocalSessionFactoryBean {


    @Override
    protected SessionFactory buildSessionFactory(LocalSessionFactoryBuilder sfb) {
        Vendor vendor = Vendor.fromHibernateConfiguration(sfb);
        //register type before loading mappings/entities, type should be present before loading JPA entities
        switch (vendor) {
            case ORACLE:
            case MYSQL:
            case OTHER:
                sfb.registerTypeOverride(XMLType.INSTANCE);
                break;
            case SQLSERVER:
                sfb.setInterceptor(new SQLServerInterceptor());
                sfb.registerTypeOverride(XMLType.INSTANCE);
                break;
            case POSTGRES:
                sfb.setInterceptor(new PostgresInterceptor());
                sfb.registerTypeOverride(new PostgresMaterializedBlobType());
                sfb.registerTypeOverride(new PostgresMaterializedClobType());
                sfb.registerTypeOverride(PostgresXMLType.INSTANCE);
                break;
        };
        return super.buildSessionFactory(sfb);
    }
}
