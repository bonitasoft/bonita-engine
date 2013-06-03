package org.bonitasoft.engine.persistence.dialect;

import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;

public class MyH2Dialect extends H2Dialect {

    public MyH2Dialect() {
        super();
        registerFunction("concat", new StandardSQLFunction("concat", StandardBasicTypes.STRING));
    }

}
