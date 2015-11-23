package org.bonitasoft.engine.bdm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.bdm.builder.BusinessObjectBuilder.ADDRESS_QUALIFIED_CLASS_NAME;
import static org.bonitasoft.engine.bdm.builder.BusinessObjectBuilder.EMPLOYEE_QUALIFIED_CLASS_NAME;
import static org.bonitasoft.engine.bdm.builder.FieldBuilder.aRelationField;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.bdm.builder.BusinessObjectBuilder;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.Query;
import org.bonitasoft.engine.bdm.model.UniqueConstraint;
import org.bonitasoft.engine.bdm.model.assertion.QueryAssert;
import org.bonitasoft.engine.bdm.model.field.Field;
import org.bonitasoft.engine.bdm.model.field.FieldType;
import org.bonitasoft.engine.bdm.model.field.RelationField;
import org.bonitasoft.engine.bdm.model.field.SimpleField;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Laurent Leseigneur
 */
public class FindQueryGeneratorTest {

    private FindQueryGenerator queryGenerator;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void before() {
        queryGenerator = new FindQueryGenerator();
    }

    @Test
    public void should_generate_query_name() {
        //when then
        assertThat(queryGenerator.getQueryName()).isEqualTo("find");
        assertThat(queryGenerator.getQueryName(Field.PERSISTENCE_ID)).isEqualTo("findByPersistenceId");
        assertThat(queryGenerator.getQueryName("lastName", "firstName")).isEqualTo("findByLastNameAndFirstName");
    }

    @Test
    public void should_createQueryNameForUniqueConstraint_throwException_when_null_value() {
        //then
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("uniqueConstraint cannot be null");

        //when
        queryGenerator.createQueryNameForUniqueConstraint(null);
    }

    @Test
    public void should_createCountQueryNameForUniqueConstraint_return_queryname() {
        //given
        final UniqueConstraint uniqueConstraint = new UniqueConstraint();
        uniqueConstraint.setFieldNames(Arrays.asList("name"));

        //when
        final String countQueryNameForUniqueConstraint = queryGenerator.createQueryNameForUniqueConstraint(uniqueConstraint);

        //then
        assertThat(countQueryNameForUniqueConstraint).isEqualTo("findByName");

    }

    @Test
    public void should_create_query_for_field() {
        final BusinessObject bo = new BusinessObject();
        bo.setQualifiedName("org.bonita.Employee");
        final SimpleField field = new SimpleField();
        field.setName("name");
        field.setType(FieldType.STRING);
        bo.addField(field);

        //when
        final Query query = queryGenerator.createQueryForField(bo, field);

        //then
        QueryAssert.assertThat(query)
                .hasName("findByName")
                .hasContent("SELECT e\nFROM Employee e\nWHERE e.name= :name\nORDER BY e.persistenceId")
                .hasReturnType(List.class.getName());
        assertThat(query.getQueryParameters()).hasSize(1);

    }

    @Test
    public void should_create_query_for_unique_constraint() {
        //given
        final BusinessObject bo = new BusinessObject();
        bo.setQualifiedName("org.bonita.Employee");
        final SimpleField field = new SimpleField();
        field.setName("name");
        field.setType(FieldType.STRING);
        bo.addField(field);
        final UniqueConstraint uniqueConstraint = new UniqueConstraint();
        uniqueConstraint.setFieldNames(Arrays.asList("name"));

        //when
        final Query query = queryGenerator.createQueryForUniqueConstraint(bo, uniqueConstraint);

        //then
        QueryAssert.assertThat(query)
                .hasName("findByName")
                .hasContent("SELECT e\nFROM Employee e\nWHERE e.name= :name\n")
                .hasReturnType(bo.getQualifiedName());
        assertThat(query.getQueryParameters()).hasSize(1);
    }

    @Test
    public void should_generate_query_for_lazy_field() {
        //given
        BusinessObject employeeBo = BusinessObjectBuilder.aBO(EMPLOYEE_QUALIFIED_CLASS_NAME).build();
        BusinessObject addressBo = BusinessObjectBuilder.aBO(ADDRESS_QUALIFIED_CLASS_NAME).build();
        RelationField multipleRelation = aRelationField().withName("addresses").ofType(RelationField.Type.AGGREGATION).lazy().referencing(addressBo).multiple()
                .build();
        RelationField singleRelation = aRelationField().withName("address").ofType(RelationField.Type.AGGREGATION).lazy().referencing(addressBo).build();

        //when
        final Query queryForMultipleLazyField = queryGenerator.createQueryForLazyField(employeeBo, multipleRelation);
        final Query queryForSingleLazyField = queryGenerator.createQueryForLazyField(employeeBo, singleRelation);

        //then
        QueryAssert.assertThat(queryForMultipleLazyField)
                .hasName("findAddressesByEmployeePersistenceId")
                .hasReturnType(List.class.getName())
                .hasContent(
                        "SELECT addresses_1 FROM Employee employee_0 JOIN employee_0.addresses as addresses_1 WHERE employee_0.persistenceId= :persistenceId");

        QueryAssert.assertThat(queryForSingleLazyField)
                .hasName("findAddressByEmployeePersistenceId")
                .hasReturnType(ADDRESS_QUALIFIED_CLASS_NAME)
                .hasContent("SELECT address_1 FROM Employee employee_0 JOIN employee_0.address as address_1 WHERE employee_0.persistenceId= :persistenceId");

    }

}
