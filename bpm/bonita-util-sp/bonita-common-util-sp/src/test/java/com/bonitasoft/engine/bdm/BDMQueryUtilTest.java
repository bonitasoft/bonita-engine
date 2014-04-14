package com.bonitasoft.engine.bdm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * @author Romain Bioteau
 */
public class BDMQueryUtilTest {

    @Test
    public void should_createQueryNameForUniqueConstraint_return_queryname() throws Exception {
        final UniqueConstraint uniqueConstraint = new UniqueConstraint();
        uniqueConstraint.setFieldNames(Arrays.asList("name"));
        final String queryNameForUniqueConstraint = BDMQueryUtil.createQueryNameForUniqueConstraint("org.bonita.Employee", uniqueConstraint);
        assertThat(queryNameForUniqueConstraint).isEqualTo("getEmployeeByName");

    }

    @Test
    public void should_createQueryContentForUniqueConstraint_return_query_content_with_parameters() throws Exception {
        final UniqueConstraint uniqueConstraint = new UniqueConstraint();
        uniqueConstraint.setFieldNames(Arrays.asList("name"));
        final String queryContentForUniqueConstraint = BDMQueryUtil.createQueryContentForUniqueConstraint("org.bonita.Employee", uniqueConstraint);
        assertThat(queryContentForUniqueConstraint).isNotEmpty().isEqualTo("SELECT e\nFROM Employee e\nWHERE e.name=:name");
    }

    @Test
    public void should_createQuerForUniqueConstraint_return_query_with_parameters() throws Exception {
        final BusinessObject bo = new BusinessObject();
        bo.setQualifiedName("org.bonita.Employee");
        final Field field = new Field();
        field.setName("name");
        field.setType(FieldType.STRING);
        bo.addField(field);
        final UniqueConstraint uniqueConstraint = new UniqueConstraint();
        uniqueConstraint.setFieldNames(Arrays.asList("name"));

        final Query query = BDMQueryUtil.createQueryForUniqueConstraint(bo, uniqueConstraint);
        assertThat(query).isNotNull();
        assertThat(query.getContent()).isEqualTo("SELECT e\nFROM Employee e\nWHERE e.name=:name");
        assertThat(query.getName()).isEqualTo("getEmployeeByName");
        assertThat(query.getReturnType()).isEqualTo(bo.getQualifiedName());
        assertThat(query.getQueryParameters()).hasSize(1);
    }

    @Test
    public void should_createQueryFoField_return_query_with_parameters() throws Exception {
        final BusinessObject bo = new BusinessObject();
        bo.setQualifiedName("org.bonita.Employee");
        final Field field = new Field();
        field.setName("name");
        field.setType(FieldType.STRING);
        bo.addField(field);

        final Query query = BDMQueryUtil.createQueryForField(bo, field);
        assertThat(query).isNotNull();
        assertThat(query.getContent()).isEqualTo("SELECT e\nFROM Employee e\nWHERE e.name=:name");
        assertThat(query.getName()).isEqualTo("getEmployeeByName");
        assertThat(query.getReturnType()).isEqualTo(List.class.getName());
        assertThat(query.getQueryParameters()).hasSize(1);
    }

    @Test
    public void should_createSelectAllQueryreturn_query_without_parameters() throws Exception {
        final BusinessObject bo = new BusinessObject();
        bo.setQualifiedName("org.bonita.Employee");
        final Field field = new Field();
        field.setName("name");
        field.setType(FieldType.STRING);
        bo.addField(field);

        final Query query = BDMQueryUtil.createSelectAllQueryForBusinessObject(bo);
        assertThat(query).isNotNull();
        assertThat(query.getContent()).isEqualTo("SELECT e\nFROM Employee e");
        assertThat(query.getName()).isEqualTo("getAllEmployee");
        assertThat(query.getReturnType()).isEqualTo(List.class.getName());
        assertThat(query.getQueryParameters()).isEmpty();
    }
}
