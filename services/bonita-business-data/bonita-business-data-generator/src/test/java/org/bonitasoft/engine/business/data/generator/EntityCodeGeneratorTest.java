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
package org.bonitasoft.engine.business.data.generator;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;

import java.io.StringWriter;
import java.io.Writer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.Version;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JAnnotationValue;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JFormatter;
import com.sun.codemodel.JMethod;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.field.Field;
import org.bonitasoft.engine.bdm.model.field.FieldType;
import org.bonitasoft.engine.bdm.model.field.RelationField;
import org.bonitasoft.engine.bdm.model.field.SimpleField;
import org.hibernate.annotations.GenericGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

public class EntityCodeGeneratorTest {

    private static final String EMPLOYEE_QUALIFIED_NAME = "org.bonitasoft.hr.Employee";
    private static final String UNIQUE_RELATION_QUALIFIED_NAME = "org.bonitasoft.hr.Unique";

    private EntityCodeGenerator entityCodeGenerator;

    private CodeGenerator codeGenerator;

    private BusinessObjectModel bom = new BusinessObjectModel();

    private ListAppender<ILoggingEvent> warnings;

    @Before
    public void setUp() {
        codeGenerator = new CodeGenerator();
        entityCodeGenerator = new EntityCodeGenerator(codeGenerator, bom);
        warnings = new ListAppender<>();
        warnings.start();
        ((Logger) LoggerFactory.getLogger(EntityCodeGenerator.class)).addAppender(warnings);
    }

    @After
    public void tearDown() {
        ((Logger) LoggerFactory.getLogger(EntityCodeGenerator.class)).detachAppender(warnings);
    }

    private List<String> warnMessages() {
        return warnings.list.stream()
                .filter(event -> Level.WARN == event.getLevel())
                .map(ILoggingEvent::getFormattedMessage)
                .toList();
    }

    @Test
    public void shouldAddEntity_CreateAValidEntityFromBusinessObject() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        entityCodeGenerator.addEntity(employeeBO);
        final JDefinedClass definedClass = codeGenerator.getModel()._getClass(employeeBO.getQualifiedName());
        assertThat(definedClass).isNotNull();
        assertThat(definedClass._package().name()).isEqualTo("org.bonitasoft.hr");
        final Iterator<JClass> it = definedClass._implements();
        final JClass jClass = it.next();
        assertThat(jClass.fullName()).isEqualTo(org.bonitasoft.engine.bdm.Entity.class.getName());
        assertThat(definedClass.annotations()).hasSize(3);
        final Iterator<JAnnotationUse> iterator = definedClass.annotations().iterator();
        final JAnnotationUse entityAnnotation = iterator.next();
        assertThat(entityAnnotation.getAnnotationClass().fullName()).isEqualTo(Entity.class.getName());
        assertThat(entityAnnotation.getAnnotationMembers()).hasSize(1);

        final JAnnotationUse tableAnnotation = iterator.next();
        assertThat(tableAnnotation.getAnnotationClass().fullName()).isEqualTo(Table.class.getName());
        assertThat(tableAnnotation.getAnnotationMembers()).hasSize(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldAddEntity_ThrowAnIllegalArgumentException() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName("java.lang.String");
        entityCodeGenerator.addEntity(employeeBO);
    }

    @Test
    public void shouldAddNamedQueries_InDefinedClass() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        employeeBO.addQuery("getEmployees", "SELECT e FROM Employee e", List.class.getName());
        final JDefinedClass entity = entityCodeGenerator.addEntity(employeeBO);

        final JAnnotationUse namedQueriesAnnotation = getAnnotation(entity, NamedQueries.class.getName());
        assertThat(namedQueriesAnnotation).isNotNull();
        final Map<String, JAnnotationValue> annotationMembers = namedQueriesAnnotation.getAnnotationMembers();
        assertThat(annotationMembers).hasSize(1);
    }

    @Test
    public void shouldAddPersistenceIdFieldAndAccessors_AddPersistenceId() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final JDefinedClass definedClass = codeGenerator.addClass(EMPLOYEE_QUALIFIED_NAME);
        entityCodeGenerator.addPersistenceIdFieldAndAccessors(definedClass, "postgres");

        final JFieldVar idFieldVar = definedClass.fields().get(Field.PERSISTENCE_ID);
        assertThat(idFieldVar).isNotNull();
        assertThat(idFieldVar.type()).isEqualTo(codeGenerator.getModel().ref(Long.class.getName()));
        assertThat(idFieldVar.annotations()).hasSize(3);
        final Iterator<JAnnotationUse> iterator = idFieldVar.annotations().iterator();
        JAnnotationUse annotationUse = iterator.next();
        assertThat(annotationUse.getAnnotationClass().fullName()).isEqualTo(Id.class.getName());
        annotationUse = iterator.next();
        assertThat(annotationUse.getAnnotationClass().fullName()).isEqualTo(GeneratedValue.class.getName());
        annotationUse = iterator.next();
        assertThat(annotationUse.getAnnotationClass().fullName()).isEqualTo(GenericGenerator.class.getName());
    }

    @Test
    public void should_add_sequence_strategy_to_fields_with_generatedValue_in_h2() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final JDefinedClass definedClass = codeGenerator.addClass(EMPLOYEE_QUALIFIED_NAME);
        entityCodeGenerator.addPersistenceIdFieldAndAccessors(definedClass, "h2");

        final JFieldVar idFieldVar = definedClass.fields().get(Field.PERSISTENCE_ID);
        assertThat(idFieldVar.annotations()).hasSize(3);
        final Iterator<JAnnotationUse> iterator = idFieldVar.annotations().iterator();
        iterator.next();
        JAnnotationUse annotationUse = iterator.next();
        assertThat(getAnnotationParamValue(annotationUse, "generator")).isEqualTo("default_bonita_seq_generator");
    }

    @Test
    public void should_add_sequence_strategy_to_fields_with_generatedValue_in_postgres() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final JDefinedClass definedClass = codeGenerator.addClass(EMPLOYEE_QUALIFIED_NAME);
        entityCodeGenerator.addPersistenceIdFieldAndAccessors(definedClass, "postgres");

        final JFieldVar idFieldVar = definedClass.fields().get(Field.PERSISTENCE_ID);
        assertThat(idFieldVar.annotations()).hasSize(3);
        final Iterator<JAnnotationUse> iterator = idFieldVar.annotations().iterator();
        iterator.next();
        JAnnotationUse annotationUse = iterator.next();
        assertThat(getAnnotationParamValue(annotationUse, "generator")).isEqualTo("default_bonita_seq_generator");
    }

    @Test
    public void should_add_sequence_strategy_to_fields_with_generatedValue_in_oracle() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final JDefinedClass definedClass = codeGenerator.addClass(EMPLOYEE_QUALIFIED_NAME);
        entityCodeGenerator.addPersistenceIdFieldAndAccessors(definedClass, "oracle");

        final JFieldVar idFieldVar = definedClass.fields().get(Field.PERSISTENCE_ID);
        assertThat(idFieldVar.annotations()).hasSize(3);
        final Iterator<JAnnotationUse> iterator = idFieldVar.annotations().iterator();
        iterator.next();
        JAnnotationUse annotationUse = iterator.next();
        assertThat(getAnnotationParamValue(annotationUse, "generator")).isEqualTo("default_bonita_seq_generator");
    }

    @Test
    public void should_add_identity_strategy_to_fields_with_generatedValue_in_mysql() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final JDefinedClass definedClass = codeGenerator.addClass(EMPLOYEE_QUALIFIED_NAME);
        entityCodeGenerator.addPersistenceIdFieldAndAccessors(definedClass, "mysql");

        final JFieldVar idFieldVar = definedClass.fields().get(Field.PERSISTENCE_ID);
        assertThat(idFieldVar.annotations()).hasSize(2);
        final Iterator<JAnnotationUse> iterator = idFieldVar.annotations().iterator();
        iterator.next();
        JAnnotationUse annotationUse = iterator.next();
        assertThat(getAnnotationParamValue(annotationUse, "strategy"))
                .isEqualTo("jakarta.persistence.GenerationType.IDENTITY");
    }

    @Test
    public void should_add_identity_strategy_to_fields_with_generatedValue_in_SQLServer() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final JDefinedClass definedClass = codeGenerator.addClass(EMPLOYEE_QUALIFIED_NAME);
        entityCodeGenerator.addPersistenceIdFieldAndAccessors(definedClass, "sqlserver");

        final JFieldVar idFieldVar = definedClass.fields().get(Field.PERSISTENCE_ID);
        assertThat(idFieldVar.annotations()).hasSize(2);
        final Iterator<JAnnotationUse> iterator = idFieldVar.annotations().iterator();
        iterator.next();
        JAnnotationUse annotationUse = iterator.next();
        assertThat(getAnnotationParamValue(annotationUse, "strategy"))
                .isEqualTo("jakarta.persistence.GenerationType.IDENTITY");
    }

    @Test
    public void shouldAddAccessors_AddAccessorMethods_InDefinedClass() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final SimpleField nameField = new SimpleField();
        nameField.setName("name");
        nameField.setType(FieldType.STRING);
        final JDefinedClass definedClass = codeGenerator.addClass(EMPLOYEE_QUALIFIED_NAME);
        final JFieldVar basicField = entityCodeGenerator.addField(definedClass, nameField, "h2");

        entityCodeGenerator.addAccessors(definedClass, basicField);

        assertThat(definedClass.methods()).hasSize(2);
        final JMethod setter = (JMethod) definedClass.methods().toArray()[0];
        assertThat(setter.name()).isEqualTo("setName");

        final JMethod getter = (JMethod) definedClass.methods().toArray()[1];
        assertThat(getter.name()).isEqualTo("getName");
    }

    @Test
    public void shouldAddBasicField_AddAFieldWithTemporalAnnotation_InDefinedClass() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final SimpleField nameField = new SimpleField();
        nameField.setName("name");
        nameField.setType(FieldType.DATE);
        nameField.setNullable(Boolean.FALSE);
        final JDefinedClass definedClass = codeGenerator.addClass(EMPLOYEE_QUALIFIED_NAME);
        entityCodeGenerator.addField(definedClass, nameField, "h2");

        final JFieldVar nameFieldVar = definedClass.fields().get("name");
        assertThat(nameFieldVar).isNotNull();
        assertThat(nameFieldVar.type()).isEqualTo(codeGenerator.getModel().ref(Date.class.getName()));
        assertThat(nameFieldVar.annotations()).hasSize(2);
        final Iterator<JAnnotationUse> iterator = nameFieldVar.annotations().iterator();
        JAnnotationUse annotationUse = iterator.next();
        assertThat(annotationUse.getAnnotationClass().fullName()).isEqualTo(Column.class.getName());

        final String name = getAnnotationParamValue(annotationUse, "name");
        assertThat(name).isNotNull().isEqualTo("NAME");
        final String nullable = getAnnotationParamValue(annotationUse, "nullable");
        assertThat(nullable).isNotNull().isEqualTo("false");

        annotationUse = iterator.next();
        assertThat(annotationUse.getAnnotationClass().fullName()).isEqualTo(Temporal.class.getName());
        assertThat(annotationUse.getAnnotationMembers()).hasSize(1);
        final String value = getAnnotationParamValue(annotationUse, "value");
        assertThat(value).isNotNull().isEqualTo("jakarta.persistence.TemporalType.TIMESTAMP");
    }

    private String getAnnotationParamValue(final JAnnotationUse annotationUse, final String paramName) {
        final Map<String, JAnnotationValue> annotationParams = annotationUse.getAnnotationMembers();
        final JAnnotationValue nullableValue = annotationParams.get(paramName);
        final StringWriter writer = new StringWriter();
        nullableValue.generate(new JFormatter(writer));
        return writer.toString().replace("\"", "");
    }

    @Test
    public void shouldAddBooleanAccessors_AddAccessorMethods_InDefinedClass() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final SimpleField foundField = new SimpleField();
        foundField.setName("found");
        foundField.setType(FieldType.BOOLEAN);
        final JDefinedClass definedClass = codeGenerator.addClass(EMPLOYEE_QUALIFIED_NAME);
        final JFieldVar basicField = entityCodeGenerator.addField(definedClass, foundField, "h2");

        entityCodeGenerator.addAccessors(definedClass, basicField);

        assertThat(definedClass.methods()).extracting(JMethod::name)
                .containsExactly("setFound", "isFound");
    }

    /**
     * BPA-833: Groovy 4 does not accept an <code>isX()</code> accessor returning the wrapper <code>Boolean</code>
     * as a property getter, so the <code>entity.found</code> shorthand used in process expressions needs the
     * <code>getFound()</code> alias. Driven through {@link EntityCodeGenerator#addEntity}, the alias being added in
     * a second pass.
     */
    @Test
    public void shouldAddEntity_AddAGetterAliasReadableAsAGroovyProperty_ForABooleanAttribute() throws Exception {
        //given
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final SimpleField foundField = new SimpleField();
        foundField.setName("found");
        foundField.setType(FieldType.BOOLEAN);
        employeeBO.addField(foundField);

        //when
        final JDefinedClass entityClass = entityCodeGenerator.addEntity(employeeBO);

        //then
        final JClass booleanType = codeGenerator.getModel().ref(Boolean.class.getName());
        assertThat(entityClass.methods()).extracting(JMethod::name).contains("isFound", "getFound");
        assertThat(entityClass.methods()).filteredOn(method -> "getFound".equals(method.name()))
                .singleElement()
                .satisfies(alias -> assertThat(alias.type()).isEqualTo(booleanType));
    }

    /**
     * BPA-833: two attributes whose names differ only in the case of their first letter both yield
     * <code>getHungry()</code>, and such a model deploys today (columns HUNGRY and HUNGRY_PID), so the alias must
     * give way rather than emit a duplicate method the entity could not compile with. The BOOLEAN is declared
     * first, the order in which a single-pass implementation would emit the alias before the colliding accessor.
     */
    @Test
    public void shouldAddEntity_NotAddAGetterAliasCollidingWithAnotherAttributeAccessor() throws Exception {
        //given
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final SimpleField hungryField = new SimpleField();
        hungryField.setName("hungry");
        hungryField.setType(FieldType.BOOLEAN);
        employeeBO.addField(hungryField);

        final BusinessObject relatedBO = new BusinessObject();
        relatedBO.setQualifiedName(UNIQUE_RELATION_QUALIFIED_NAME);
        final RelationField hungryRelation = new RelationField();
        hungryRelation.setName("Hungry");
        // EAGER stated rather than left to the default: both skip guards give the same outcome on this model, so a
        // change to that default upstream would silently move the test onto the other branch and it would still pass
        hungryRelation.setFetchType(RelationField.FetchType.EAGER);
        hungryRelation.setReference(relatedBO);
        employeeBO.addField(hungryRelation);

        //when
        final JDefinedClass entityClass = entityCodeGenerator.addEntity(employeeBO);

        //then the relation keeps its accessor and the BOOLEAN alias is skipped, so the source still compiles
        assertThat(entityClass.methods()).filteredOn(method -> "getHungry".equals(method.name()))
                .singleElement()
                .satisfies(getter -> assertThat(getter.type().fullName()).isEqualTo(UNIQUE_RELATION_QUALIFIED_NAME));
        assertThat(entityClass.methods()).extracting(JMethod::name).contains("isHungry");
    }

    /**
     * BPA-833: the alias must also give way to an inherited method. A BOOLEAN attribute named <code>Class</code>
     * yields <code>getClass()</code>, which cannot override the final {@link Object#getClass()}, so emitting it
     * breaks compilation of a model that installs today.
     */
    @Test
    public void shouldAddEntity_NotAddAGetterAliasNamedAfterAnInheritedMethod() throws Exception {
        //given
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final SimpleField clazzField = new SimpleField();
        clazzField.setName("Class");
        clazzField.setType(FieldType.BOOLEAN);
        employeeBO.addField(clazzField);

        //when
        final JDefinedClass entityClass = entityCodeGenerator.addEntity(employeeBO);

        //then
        assertThat(entityClass.methods()).extracting(JMethod::name)
                .contains("isClass")
                .doesNotContain("getClass");
    }

    /**
     * BPA-833: the client lazy-loading proxy keys accessors on the lower-cased name, so <code>getHungry()</code> and
     * <code>getHUNGRY()</code> share one key and the alias would mark that lazy relation as already loaded. The two
     * accessor names differ, so this is a runtime hazard rather than a compilation one.
     */
    @Test
    public void shouldAddEntity_NotAddAGetterAliasCollidingInCaseWithAnotherAttribute() throws Exception {
        //given
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final SimpleField hungryField = new SimpleField();
        hungryField.setName("hungry");
        hungryField.setType(FieldType.BOOLEAN);
        employeeBO.addField(hungryField);

        final BusinessObject relatedBO = new BusinessObject();
        relatedBO.setQualifiedName(UNIQUE_RELATION_QUALIFIED_NAME);
        final RelationField hungryRelation = new RelationField();
        hungryRelation.setName("HUNGRY");
        hungryRelation.setFetchType(RelationField.FetchType.LAZY);
        hungryRelation.setReference(relatedBO);
        employeeBO.addField(hungryRelation);

        //when
        final JDefinedClass entityClass = entityCodeGenerator.addEntity(employeeBO);

        //then the relation keeps its own accessor, and the BOOLEAN gets no alias
        assertThat(entityClass.methods()).extracting(JMethod::name)
                .contains("isHungry", "getHUNGRY")
                .doesNotContain("getHungry");
    }

    /**
     * BPA-833: the case-insensitive skip is restricted to <em>lazy</em> relations, the client proxy's lazy-loading
     * bookkeeping being the whole hazard. An eager relation consults none of it, so the alias must still be
     * generated rather than forfeit the fix for that attribute.
     */
    @Test
    public void shouldAddEntity_StillAddTheGetterAlias_WhenTheCaseClashIsWithAnEagerRelation() throws Exception {
        //given
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final SimpleField hungryField = new SimpleField();
        hungryField.setName("hungry");
        hungryField.setType(FieldType.BOOLEAN);
        employeeBO.addField(hungryField);

        final BusinessObject relatedBO = new BusinessObject();
        relatedBO.setQualifiedName(UNIQUE_RELATION_QUALIFIED_NAME);
        final RelationField hungryRelation = new RelationField();
        hungryRelation.setName("HUNGRY");
        hungryRelation.setFetchType(RelationField.FetchType.EAGER);
        hungryRelation.setReference(relatedBO);
        employeeBO.addField(hungryRelation);

        //when
        final JDefinedClass entityClass = entityCodeGenerator.addEntity(employeeBO);

        //then both accessors exist - distinct method names, so nothing collides
        assertThat(entityClass.methods()).extracting(JMethod::name)
                .contains("isHungry", "getHungry", "getHUNGRY");
        assertThat(warnMessages()).isEmpty();
    }

    /**
     * BPA-833: a multiple BOOLEAN attribute is a <code>List&lt;Boolean&gt;</code>, so it already has a
     * <code>getX()</code> accessor and an alias would duplicate it. Guards the type check the alias pass relies on.
     */
    @Test
    public void shouldAddEntity_NotAddAnyGetterAlias_ForAMultipleBooleanAttribute() throws Exception {
        //given
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final SimpleField flagsField = new SimpleField();
        flagsField.setName("flags");
        flagsField.setType(FieldType.BOOLEAN);
        flagsField.setCollection(Boolean.TRUE);
        employeeBO.addField(flagsField);

        //when
        final JDefinedClass entityClass = entityCodeGenerator.addEntity(employeeBO);

        //then exactly one read accessor, and it is the get-prefixed one a collection always gets
        assertThat(entityClass.methods()).extracting(JMethod::name)
                .containsOnlyOnce("getFlags")
                .doesNotContain("isFlags");
    }

    /**
     * BPA-833: a skipped alias leaves the attribute with the defect, and this log line is the only signal an
     * administrator gets. An attribute named <code>Class</code> is the clearest way into this branch, not the only
     * one. {@code shouldAddEntity_NotAddAGetterAliasCollidingWithAnotherAttributeAccessor} reaches it through
     * another attribute, which is why the message names that cause too.
     */
    @Test
    public void shouldAddEntity_WarnWithTheAttributeAndTheAccessorToUse_WhenTheAliasNameIsTaken() throws Exception {
        //given a BOOLEAN whose alias would be the inherited, final Object.getClass()
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final SimpleField clazzField = new SimpleField();
        clazzField.setName("Class");
        clazzField.setType(FieldType.BOOLEAN);
        employeeBO.addField(clazzField);

        //when
        entityCodeGenerator.addEntity(employeeBO);

        //then
        assertThat(warnMessages()).singleElement(as(STRING))
                .contains(EMPLOYEE_QUALIFIED_NAME)
                .contains("'Class'")
                .contains("already taken")
                .contains("<yourVariable>.isClass()")
                // the remedy has to match the cause: there is no "either attribute" to rename here
                .contains("Rename this attribute.")
                .doesNotContain("Rename either attribute");
    }

    /**
     * BPA-833: the other skip reason, a name matching another one case-insensitively, which would break the client
     * proxy's lazy-loading bookkeeping. It must be reported differently, the remedy a reader needs not being the
     * same.
     */
    @Test
    public void shouldAddEntity_WarnAboutLazyLoading_WhenAnotherAttributeDiffersOnlyInCase() throws Exception {
        //given
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final SimpleField hungryField = new SimpleField();
        hungryField.setName("hungry");
        hungryField.setType(FieldType.BOOLEAN);
        employeeBO.addField(hungryField);
        final BusinessObject relatedBO = new BusinessObject();
        relatedBO.setQualifiedName(UNIQUE_RELATION_QUALIFIED_NAME);
        final RelationField hungryRelation = new RelationField();
        hungryRelation.setName("HUNGRY");
        hungryRelation.setFetchType(RelationField.FetchType.LAZY);
        hungryRelation.setReference(relatedBO);
        employeeBO.addField(hungryRelation);

        //when
        entityCodeGenerator.addEntity(employeeBO);

        //then
        assertThat(warnMessages()).singleElement(as(STRING))
                .contains("'hungry'")
                .contains("the same name in a different case")
                .contains("read as null")
                .contains("Rename either attribute so that the two names differ by more than case.");
    }

    /**
     * BPA-833: two BOOLEAN attributes differing only in the case of their first letter both want the same
     * <code>getHungry()</code> alias, and only one can have it. Pins the alias pass to emitting exactly one and to
     * warning about the attribute that lost, the one place the alias output depends on attribute ordering.
     * <p>
     * Asserted on the alias pass alone, the model being undeployable anyway for reasons that predate BPA-833: see
     * {@code shouldAddEntity_DuplicateThePrimaryAccessors_WhenTwoAttributesDifferOnlyInCase}.
     */
    @Test
    public void shouldAddEntity_GiveTheAliasToTheFirstAttribute_WhenTwoBooleansWantIt() throws Exception {
        //given
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final SimpleField lowerCaseField = new SimpleField();
        lowerCaseField.setName("hungry");
        lowerCaseField.setType(FieldType.BOOLEAN);
        employeeBO.addField(lowerCaseField);
        final SimpleField upperCaseField = new SimpleField();
        upperCaseField.setName("Hungry");
        upperCaseField.setType(FieldType.BOOLEAN);
        employeeBO.addField(upperCaseField);

        //when
        final JDefinedClass entityClass = entityCodeGenerator.addEntity(employeeBO);

        //then the alias pass emitted exactly one getHungry(), and said which attribute went without
        assertThat(entityClass.methods()).extracting(JMethod::name).containsOnlyOnce("getHungry");
        assertThat(warnMessages()).singleElement(as(STRING))
                .contains("'Hungry'")
                .contains("already taken")
                .contains("Rename this attribute.");
    }

    /**
     * Records a defect that predates BPA-833, in its own test so that repairing it fails a test named after it
     * rather than one named after the alias pass: for two attributes differing only in the case of their first
     * letter, the <em>primary</em> accessor pass emits <code>isHungry()</code> and <code>setHungry()</code> twice
     * each, so the generated source does not compile. Such a model passes validation and so reaches the generator,
     * though both fields map to column <code>HUNGRY</code> and it could never deploy.
     * <p>
     * If this test fails because the duplication is gone, delete it; it asserts a defect, not a requirement.
     */
    @Test
    public void shouldAddEntity_DuplicateThePrimaryAccessors_WhenTwoAttributesDifferOnlyInCase() throws Exception {
        //given
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final SimpleField lowerCaseField = new SimpleField();
        lowerCaseField.setName("hungry");
        lowerCaseField.setType(FieldType.BOOLEAN);
        employeeBO.addField(lowerCaseField);
        final SimpleField upperCaseField = new SimpleField();
        upperCaseField.setName("Hungry");
        upperCaseField.setType(FieldType.BOOLEAN);
        employeeBO.addField(upperCaseField);

        //when
        final JDefinedClass entityClass = entityCodeGenerator.addEntity(employeeBO);

        //then
        assertThat(entityClass.methods()).extracting(JMethod::name)
                .as("the accessor pass, not the BPA-833 alias pass, emits each of these twice")
                .filteredOn(name -> "isHungry".equals(name) || "setHungry".equals(name)).hasSize(4);
    }

    /**
     * BPA-833: the ordinary case must stay silent - a warning on every BOOLEAN attribute would be noise in every
     * BDM install log.
     */
    @Test
    public void shouldAddEntity_NotWarn_WhenTheAliasIsGenerated() throws Exception {
        //given
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final SimpleField foundField = new SimpleField();
        foundField.setName("found");
        foundField.setType(FieldType.BOOLEAN);
        employeeBO.addField(foundField);

        //when
        entityCodeGenerator.addEntity(employeeBO);

        //then
        assertThat(warnMessages()).isEmpty();
    }

    /**
     * BPA-833: non-BOOLEAN attributes already use a regular <code>getX()</code> accessor, so no alias is
     * generated for them.
     */
    @Test
    public void shouldAddAccessors_NotAddAnyGetterAlias_ForNonBooleanField() throws Exception {
        //given
        final SimpleField nameField = new SimpleField();
        nameField.setName("name");
        nameField.setType(FieldType.STRING);
        final JDefinedClass definedClass = codeGenerator.addClass(EMPLOYEE_QUALIFIED_NAME);
        final JFieldVar basicField = entityCodeGenerator.addField(definedClass, nameField, "h2");

        //when
        entityCodeGenerator.addAccessors(definedClass, basicField);

        //then
        assertThat(definedClass.methods()).extracting(JMethod::name).containsExactly("setName", "getName");
    }

    @Test
    public void shouldAddColumnField_CreatePrimitiveAttribute_InDefinedClass() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final SimpleField nameField = new SimpleField();
        nameField.setName("name");
        nameField.setType(FieldType.STRING);
        nameField.setLength(Integer.valueOf(45));
        final JDefinedClass definedClass = codeGenerator.addClass(EMPLOYEE_QUALIFIED_NAME);
        entityCodeGenerator.addField(definedClass, nameField, "h2");

        final JFieldVar nameFieldVar = definedClass.fields().get("name");
        assertThat(nameFieldVar).isNotNull();
        assertThat(nameFieldVar.type()).isEqualTo(codeGenerator.getModel().ref(String.class.getName()));
        assertThat(nameFieldVar.annotations()).hasSize(1);
        final JAnnotationUse annotationUse = nameFieldVar.annotations().iterator().next();
        assertThat(annotationUse.getAnnotationClass().fullName()).isEqualTo(Column.class.getName());

        final String name = getAnnotationParamValue(annotationUse, "name");
        assertThat(name).isNotNull().isEqualTo("NAME");
        final String nullable = getAnnotationParamValue(annotationUse, "nullable");
        assertThat(nullable).isNotNull().isEqualTo("true");
        final String length = getAnnotationParamValue(annotationUse, "length");
        assertThat(length).isNotNull().isEqualTo("45");
    }

    @Test
    public void shouldAddColumnField() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final SimpleField nameField = new SimpleField();
        nameField.setName("description");
        nameField.setType(FieldType.TEXT);
        final JDefinedClass definedClass = codeGenerator.addClass(EMPLOYEE_QUALIFIED_NAME);

        entityCodeGenerator.addField(definedClass, nameField, "h2");

        final JFieldVar nameFieldVar = definedClass.fields().get("description");
        assertTextField(nameFieldVar);
    }

    private void assertTextField(final JFieldVar fieldVar) {
        final Collection<JAnnotationUse> annotations = fieldVar.annotations();
        assertThat(annotations).hasSize(2);
        final Iterator<JAnnotationUse> iterator = annotations.iterator();
        JAnnotationUse annotationUse = iterator.next();
        assertThat(annotationUse.getAnnotationClass().fullName()).isEqualTo(Column.class.getName());
        annotationUse = iterator.next();
        assertThat(annotationUse.getAnnotationClass().fullName()).isEqualTo(Lob.class.getName());
    }

    @Test
    public void shouldAddColumnField_WithCustomTypeForPostgreSQL() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final SimpleField descriptionField = new SimpleField();
        descriptionField.setName("description");
        descriptionField.setType(FieldType.TEXT);
        final JDefinedClass definedClass = codeGenerator.addClass(EMPLOYEE_QUALIFIED_NAME);

        entityCodeGenerator.addField(definedClass, descriptionField, "postgres");

        final JFieldVar descriptionFieldVar = definedClass.fields().get("description");
        assertTextFieldForPostgreSQL(descriptionFieldVar);
    }

    @Test
    public void shouldAddColumnField_WithLobForMySQL() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final SimpleField descriptionField = new SimpleField();
        descriptionField.setName("description");
        descriptionField.setType(FieldType.TEXT);
        final JDefinedClass definedClass = codeGenerator.addClass(EMPLOYEE_QUALIFIED_NAME);

        entityCodeGenerator.addField(definedClass, descriptionField, "mysql");

        final JFieldVar descriptionFieldVar = definedClass.fields().get("description");
        // For non-PostgreSQL databases, should still use @Lob
        assertTextField(descriptionFieldVar);
    }

    private void assertTextFieldForPostgreSQL(final JFieldVar fieldVar) {
        final Collection<JAnnotationUse> annotations = fieldVar.annotations();
        assertThat(annotations).hasSize(2);
        final Iterator<JAnnotationUse> iterator = annotations.iterator();
        JAnnotationUse annotationUse = iterator.next();
        assertThat(annotationUse.getAnnotationClass().fullName()).isEqualTo(Column.class.getName());
        annotationUse = iterator.next();
        // For PostgreSQL, should use @Type annotation instead of @Lob
        assertThat(annotationUse.getAnnotationClass().fullName())
                .isEqualTo(org.hibernate.annotations.Type.class.getName());
        // Verify the type parameter value
        assertThat(annotationUse.getAnnotationMembers()).hasSize(1);
        final String typeValue = getAnnotationParamValue(annotationUse, "type");
        assertThat(typeValue).isEqualTo("org.bonitasoft.engine.persistence.PostgresMaterializedClobType");
    }

    @Test
    public void shouldAddPersistenceVersionFieldAndAccessors_AddPersistenceVersion() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final JDefinedClass definedClass = codeGenerator.addClass(EMPLOYEE_QUALIFIED_NAME);
        entityCodeGenerator.addPersistenceVersionFieldAndAccessors(definedClass);

        final JFieldVar versionFieldVar = definedClass.fields().get(Field.PERSISTENCE_VERSION);
        assertThat(versionFieldVar).isNotNull();
        assertThat(versionFieldVar.type()).isEqualTo(codeGenerator.getModel().ref(Long.class.getName()));
        assertThat(versionFieldVar.annotations()).hasSize(1);
        final Iterator<JAnnotationUse> iterator = versionFieldVar.annotations().iterator();
        final JAnnotationUse annotationUse = iterator.next();
        assertThat(annotationUse.getAnnotationClass().fullName()).isEqualTo(Version.class.getName());
    }

    public JAnnotationUse getAnnotation(final JDefinedClass definedClass, final String annotationClassName) {
        final Iterator<JAnnotationUse> iterator = definedClass.annotations().iterator();
        JAnnotationUse annotation = null;
        while (annotation == null && iterator.hasNext()) {
            final JAnnotationUse next = iterator.next();
            if (next.getAnnotationClass().fullName().equals(annotationClassName)) {
                annotation = next;
            }
        }
        return annotation;
    }

    @Test
    public void should_add_real_columnName_in_unique_key() throws Exception {
        //given
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);

        final BusinessObject uniqueRelationBO = new BusinessObject();
        uniqueRelationBO.setQualifiedName(UNIQUE_RELATION_QUALIFIED_NAME);

        final RelationField relationField = new RelationField();
        relationField.setName("uniqueRelation");
        relationField.setReference(uniqueRelationBO);
        employeeBO.addField(relationField);
        employeeBO.addUniqueConstraint("unique_constraint", "uniqueRelation");

        //when
        final JDefinedClass jDefinedClass = entityCodeGenerator.addEntity(employeeBO);

        //then
        final Writer writer = new StringWriter();
        final JAnnotationUse tableAnnotation = getAnnotation(jDefinedClass, Table.class.getCanonicalName());
        assertThat(tableAnnotation).as("should have Table Annotation").isNotNull();
        tableAnnotation.generate(new JFormatter(writer));
        final String EOL = System.getProperty("line.separator");
        assertThat(writer.toString()).as("should rename relation field to real column name")
                .isEqualTo("@jakarta.persistence.Table(name = \"EMPLOYEE\", uniqueConstraints = {" + EOL +
                        "    @jakarta.persistence.UniqueConstraint(name = \"UNIQUE_CONSTRAINT\", columnNames = {" + EOL
                        +
                        "        \"UNIQUERELATION_PID\"" + EOL +
                        "    })" + EOL +
                        "})");
    }

    @Test
    public void annotateSimpleField_should_generate_the_correct_annotation_for_localDate_type()
            throws JClassAlreadyExistsException {

        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        SimpleField nameField = new SimpleField();
        nameField.setName("name");
        nameField.setType(FieldType.LOCALDATE);
        nameField.setNullable(Boolean.FALSE);
        final JDefinedClass definedClass = codeGenerator.addClass(EMPLOYEE_QUALIFIED_NAME);
        entityCodeGenerator.addField(definedClass, nameField, "h2");
        final JFieldVar nameFieldVar = definedClass.fields().get("name");

        assertThat(nameFieldVar).isNotNull();
        assertThat(nameFieldVar.type()).isEqualTo(codeGenerator.getModel().ref(LocalDate.class.getName()));
        assertThat(nameFieldVar.annotations().size()).isEqualTo(2);
        final Iterator<JAnnotationUse> iterator = nameFieldVar.annotations().iterator();
        JAnnotationUse annotationUse = iterator.next();
        assertThat(annotationUse.getAnnotationClass().fullName()).isEqualTo(Column.class.getName());
        final String name = getAnnotationParamValue(annotationUse, "name");
        assertThat(name).isNotNull().isEqualTo("NAME");
        final String nullable = getAnnotationParamValue(annotationUse, "nullable");
        assertThat(nullable).isNotNull().isEqualTo("false");
        final int length = Integer.parseInt(getAnnotationParamValue(annotationUse, "length"));
        assertThat(length).isEqualTo(10);
        annotationUse = iterator.next();
        assertThat(annotationUse.getAnnotationClass().fullName()).isEqualTo(Convert.class.getName());
        assertThat(annotationUse.getAnnotationMembers()).hasSize(1);
        final String value = getAnnotationParamValue(annotationUse, "converter");
        assertThat(value).isNotNull().isEqualTo("org.bonitasoft.engine.business.data.generator.DateConverter.class");

    }

    @Test
    public void annotateSimpleField_should_generate_the_correct_annotation_for_localDateAndTime_type()
            throws JClassAlreadyExistsException {

        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        SimpleField nameField = new SimpleField();
        nameField.setName("name");
        nameField.setType(FieldType.LOCALDATETIME);
        nameField.setNullable(Boolean.FALSE);
        final JDefinedClass definedClass = codeGenerator.addClass(EMPLOYEE_QUALIFIED_NAME);
        entityCodeGenerator.addField(definedClass, nameField, "h2");
        final JFieldVar nameFieldVar = definedClass.fields().get("name");

        assertThat(nameFieldVar).isNotNull();
        assertThat(nameFieldVar.type()).isEqualTo(codeGenerator.getModel().ref(LocalDateTime.class.getName()));
        assertThat(nameFieldVar.annotations().size()).isEqualTo(2);
        final Iterator<JAnnotationUse> iterator = nameFieldVar.annotations().iterator();
        JAnnotationUse annotationUse = iterator.next();
        assertThat(annotationUse.getAnnotationClass().fullName()).isEqualTo(Column.class.getName());

        final String name = getAnnotationParamValue(annotationUse, "name");
        assertThat(name).isNotNull().isEqualTo("NAME");
        final String nullable = getAnnotationParamValue(annotationUse, "nullable");
        assertThat(nullable).isNotNull().isEqualTo("false");
        final int length = Integer.parseInt(getAnnotationParamValue(annotationUse, "length"));
        assertThat(length).isEqualTo(30);
        annotationUse = iterator.next();
        assertThat(annotationUse.getAnnotationClass().fullName()).isEqualTo(Convert.class.getName());
        assertThat(annotationUse.getAnnotationMembers()).hasSize(1);
        final String value = getAnnotationParamValue(annotationUse, "converter");
        assertThat(value).isNotNull()
                .isEqualTo("org.bonitasoft.engine.business.data.generator.DateAndTimeConverter.class");
    }

    @Test
    public void annotateSimpleField_should_generate_the_correct_annotation_for_OffsetDateAndTime_type()
            throws JClassAlreadyExistsException {

        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        SimpleField nameField = new SimpleField();
        nameField.setName("reunion");
        nameField.setType(FieldType.OFFSETDATETIME);
        nameField.setNullable(Boolean.FALSE);
        final JDefinedClass definedClass = codeGenerator.addClass(EMPLOYEE_QUALIFIED_NAME);
        entityCodeGenerator.addField(definedClass, nameField, "h2");
        final JFieldVar nameFieldVar = definedClass.fields().get("reunion");

        assertThat(nameFieldVar).isNotNull();
        assertThat(nameFieldVar.type()).isEqualTo(codeGenerator.getModel().ref(OffsetDateTime.class.getName()));
        assertThat(nameFieldVar.annotations().size()).isEqualTo(2);
        final Iterator<JAnnotationUse> iterator = nameFieldVar.annotations().iterator();
        JAnnotationUse annotationUse = iterator.next();
        assertThat(annotationUse.getAnnotationClass().fullName()).isEqualTo(Column.class.getName());

        final String name = getAnnotationParamValue(annotationUse, "name");
        assertThat(name).isNotNull().isEqualTo("REUNION");
        final String nullable = getAnnotationParamValue(annotationUse, "nullable");
        assertThat(nullable).isNotNull().isEqualTo("false");
        final int length = Integer.parseInt(getAnnotationParamValue(annotationUse, "length"));
        assertThat(length).isEqualTo(30);
        annotationUse = iterator.next();
        assertThat(annotationUse.getAnnotationClass().fullName()).isEqualTo(Convert.class.getName());
        assertThat(annotationUse.getAnnotationMembers()).hasSize(1);
        final String value = getAnnotationParamValue(annotationUse, "converter");
        assertThat(value).isNotNull()
                .isEqualTo("org.bonitasoft.engine.business.data.generator.OffsetDateTimeConverter.class");

    }
}
