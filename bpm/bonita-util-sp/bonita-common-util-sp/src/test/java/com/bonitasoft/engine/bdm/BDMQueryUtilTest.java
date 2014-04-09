/**
 * 
 */
package com.bonitasoft.engine.bdm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Romain Bioteau
 * 
 */
public class BDMQueryUtilTest {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void should_createQueryNameForUniqueConstraint_return_queryname() throws Exception {
        UniqueConstraint uniqueConstraint = new UniqueConstraint();
        uniqueConstraint.setFieldNames(Arrays.asList("name"));
        String queryNameForUniqueConstraint = BDMQueryUtil.createQueryNameForUniqueConstraint("org.bonita.Employee", uniqueConstraint);
        assertThat(queryNameForUniqueConstraint).isEqualTo("getEmployeeByName");

    }

    @Test
    public void should_createQueryContentForUniqueConstraint_return_query_content_with_parameters() throws Exception {
        UniqueConstraint uniqueConstraint = new UniqueConstraint();
        uniqueConstraint.setFieldNames(Arrays.asList("name"));
        String queryContentForUniqueConstraint = BDMQueryUtil.createQueryContentForUniqueConstraint("org.bonita.Employee", uniqueConstraint);
        assertThat(queryContentForUniqueConstraint).isNotEmpty().isEqualTo("SELECT e\nFROM Employee e\nWHERE e.name=:name");
    }

    @Test
    public void should_createQuerForUniqueConstraint_return_query_with_parameters() throws Exception {
        BusinessObject bo = new BusinessObject();
        bo.setQualifiedName("org.bonita.Employee");
        Field field = new Field();
        field.setName("name");
        field.setType(FieldType.STRING);
        bo.addField(field);
        UniqueConstraint uniqueConstraint = new UniqueConstraint();
        uniqueConstraint.setFieldNames(Arrays.asList("name"));

        Query query = BDMQueryUtil.createQueryForUniqueConstraint(bo, uniqueConstraint);
        assertThat(query).isNotNull();
        assertThat(query.getContent()).isEqualTo("SELECT e\nFROM Employee e\nWHERE e.name=:name");
        assertThat(query.getName()).isEqualTo("getEmployeeByName");
        assertThat(query.getQueryParameters()).hasSize(1);
    }
}
