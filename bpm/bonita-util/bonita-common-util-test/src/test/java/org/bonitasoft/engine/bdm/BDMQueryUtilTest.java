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
package org.bonitasoft.engine.bdm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;

import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.Query;
import org.bonitasoft.engine.bdm.model.field.FieldType;
import org.bonitasoft.engine.bdm.model.field.RelationField;
import org.bonitasoft.engine.bdm.model.field.RelationField.Type;
import org.bonitasoft.engine.bdm.model.field.SimpleField;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Romain Bioteau
 */
public class BDMQueryUtilTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private FindQueryGenerator findQueryGenerator = new FindQueryGenerator();

    @Test
    public void createProvidedQueriesForBOShouldNotGenerateGetAllForUniqueConstraint() {
        // given:
        final BusinessObject bo = new BusinessObject();
        bo.setQualifiedName("com.corp.Arrival");
        final SimpleField field = new SimpleField();
        field.setName("people");
        field.setType(FieldType.INTEGER);
        bo.addField(field);
        bo.addUniqueConstraint("someName", "people");

        // when:
        final List<Query> queries = BDMQueryUtil.createProvidedQueriesForBusinessObject(bo);

        // then:
        assertThat(queries).extracting(Query::getName).doesNotContain("getAllArrivalByPeople");
    }

    @Test
    public void createProvidedQueriesForBOWithOneUniqueAndOneNonUniqueFieldShouldGenerate3Queries() {
        // given:
        final BusinessObject bo = new BusinessObject();
        bo.setQualifiedName("com.corp.Arrival");
        bo.addField(aStringField("unikAttr"));
        bo.addField(aStringField("unconstrainedAttr"));
        bo.addUniqueConstraint("someConstraintName", "unikAttr");

        // when:
        final List<Query> queries = BDMQueryUtil.createProvidedQueriesForBusinessObject(bo);

        // then:
        assertThat(queries).extracting(Query::getName)
                .containsOnly("find", "countForFind", "findByPersistenceId", "findByUnikAttr",
                        "findByUnconstrainedAttr", "countForFindByUnconstrainedAttr");
    }

    @Test
    public void createProvidedQueriesForShouldNotGenerateAQueryForRelationField() {
        // given:
        final BusinessObject bo = new BusinessObject();
        bo.setQualifiedName("com.corp.Arrival");
        bo.addField(aStringField("unikAttr"));
        bo.addField(aRelationField("employee"));

        // when:
        final List<Query> queries = BDMQueryUtil.createProvidedQueriesForBusinessObject(bo);

        // then:
        assertThat(queries).extracting(Query::getName)
                .containsOnly("find", "countForFind", "findByPersistenceId", "findByUnikAttr","countForFindByUnikAttr");
    }

    protected SimpleField aStringField(final String name) {
        final SimpleField field = new SimpleField();
        field.setName(name);
        field.setType(FieldType.STRING);
        return field;
    }

    protected RelationField aRelationField(final String name) {
        final RelationField field = new RelationField();
        field.setName(name);
        field.setType(Type.COMPOSITION);
        field.setReference(null);
        return field;
    }

    @Test
    public void should_createSelectAllQueryreturn_query_without_parameters() {
        final BusinessObject bo = new BusinessObject();
        bo.setQualifiedName("org.bonita.Employee");
        final SimpleField field = new SimpleField();
        field.setName("name");
        field.setType(FieldType.STRING);
        bo.addField(field);

        final Query query = findQueryGenerator.createSelectAllQueryForBusinessObject(bo);
        assertThat(query).isNotNull();
        assertThat(query.getContent()).contains("SELECT e\nFROM Employee e");
        assertThat(query.getContent()).doesNotContain("WHERE");
        assertThat(query.getName()).isEqualTo("find");
        assertThat(query.getReturnType()).isEqualTo(List.class.getName());
        assertThat(query.getQueryParameters()).isEmpty();
    }

    @Test
    public void should_getAllProvidedQueriesNameForBusinessObject_not_return_query_for_RelationFields() {
        final BusinessObject bo = new BusinessObject();
        bo.setQualifiedName("org.bonita.Employee");
        final SimpleField field = new SimpleField();
        field.setName("name");
        field.setType(FieldType.STRING);
        bo.addField(field);

        final RelationField f2 = new RelationField();
        f2.setName("name2");
        f2.setType(Type.COMPOSITION);
        f2.setReference(bo);
        bo.addField(f2);

        assertThat(BDMQueryUtil.getAllProvidedQueriesNameForBusinessObject(bo)).doesNotContain("findByName2");
    }

    @Test
    public void should_generate_findByPersistenceId_query_in_provided_queries_names() throws Exception {
        final BusinessObject bo = new BusinessObject();
        bo.setQualifiedName("org.bonita.Employee");
        final SimpleField field = new SimpleField();
        field.setName("name");
        field.setType(FieldType.STRING);
        bo.addField(field);

        assertThat(BDMQueryUtil.getAllProvidedQueriesNameForBusinessObject(bo)).contains("findByPersistenceId");
    }

    @Test
    public void should_generate_findByPersistenceId_query_in_provided_queries() throws Exception {
        final BusinessObject bo = new BusinessObject();
        bo.setQualifiedName("org.bonita.Employee");

        assertThat(BDMQueryUtil.createProvidedQueriesForBusinessObject(bo)).extracting("name", "content", "returnType")
                .contains(tuple("findByPersistenceId", "SELECT e\nFROM Employee e\nWHERE e.persistenceId= :persistenceId\n", "org.bonita.Employee"));
    }

    @Test
    public void should_not_generate_findByPersistenceId_query_in_provided_queries_names_if_already_defined_in_custom_queries() throws Exception {
        final BusinessObject bo = new BusinessObject();
        bo.setQualifiedName("org.bonita.Employee");
        final SimpleField field = new SimpleField();
        field.setName("name");
        field.setType(FieldType.STRING);
        bo.addField(field);
        bo.getQueries().add(new Query("findByPersistenceId", "", ""));
        assertThat(BDMQueryUtil.getAllProvidedQueriesNameForBusinessObject(bo)).doesNotContain("findByPersistenceId");
    }

    @Test
    public void should_return_related_count_query_name() throws Exception {
        assertThat(BDMQueryUtil.getCountQueryName("find")).isEqualTo("countForFind");
        assertThat(BDMQueryUtil.getCountQueryName("findByStreet")).isEqualTo("countForFindByStreet");
        assertThat(BDMQueryUtil.getCountQueryName("myQuery")).isEqualTo("countForMyQuery");
    }


}
