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

import java.lang.management.ManagementFactory;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import javax.persistence.*;

import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import org.bonitasoft.engine.bdm.BDMQueryUtil;
import org.bonitasoft.engine.bdm.lazy.LazyLoaded;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.Index;
import org.bonitasoft.engine.bdm.model.Query;
import org.bonitasoft.engine.bdm.model.UniqueConstraint;
import org.bonitasoft.engine.bdm.model.field.Field;
import org.bonitasoft.engine.bdm.model.field.FieldType;
import org.bonitasoft.engine.bdm.model.field.RelationField;
import org.bonitasoft.engine.bdm.model.field.SimpleField;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Colin PUY,
 * @author Matthieu Chaffotte
 */
public class EntityCodeGenerator {

    private final CodeGenerator codeGenerator;

    private final RelationFieldAnnotator relationFieldAnnotator;

    private final BusinessObjectModel bom;

    protected static final Logger LOGGER = LoggerFactory.getLogger(EntityCodeGenerator.class);

    public EntityCodeGenerator(final CodeGenerator codeGenerator, final BusinessObjectModel bom) {
        this.codeGenerator = codeGenerator;
        this.bom = bom;
        relationFieldAnnotator = new RelationFieldAnnotator(codeGenerator);
    }

    public JDefinedClass addEntity(final BusinessObject bo) throws JClassAlreadyExistsException {
        final String qualifiedName = bo.getQualifiedName();
        validateClassNotExistsInRuntime(qualifiedName);

        JDefinedClass entityClass = codeGenerator.addClass(qualifiedName);
        entityClass = codeGenerator.addInterface(entityClass, org.bonitasoft.engine.bdm.Entity.class.getName());
        entityClass.javadoc().add(bo.getDescription());

        final JAnnotationUse entityAnnotation = codeGenerator.addAnnotation(entityClass, Entity.class);
        entityAnnotation.param("name", entityClass.name());

        addIndexAnnotations(bo, entityClass);
        addUniqueConstraintAnnotations(bo, entityClass);
        addQueriesAnnotation(bo, entityClass);

        String dbVendor = determineDbVendor();
        addFieldsAndMethods(bo, entityClass, dbVendor);

        codeGenerator.addDefaultConstructor(entityClass);

        return entityClass;
    }

    private void addFieldsAndMethods(final BusinessObject bo, final JDefinedClass entityClass, String dbVendor) {
        addPersistenceIdFieldAndAccessors(entityClass, dbVendor);
        addPersistenceVersionFieldAndAccessors(entityClass);

        for (final Field field : bo.getFields()) {
            final JFieldVar fieldVar = addField(entityClass, field);
            addAccessors(entityClass, fieldVar, field);
            addModifiers(entityClass, field);
        }
    }

    private void addQueriesAnnotation(final BusinessObject bo, final JDefinedClass entityClass) {
        final JAnnotationUse namedQueriesAnnotation = codeGenerator.addAnnotation(entityClass, NamedQueries.class);
        final JAnnotationArrayMember valueArray = namedQueriesAnnotation.paramArray("value");

        // Add provided queries
        for (final Query providedQuery : BDMQueryUtil.createProvidedQueriesForBusinessObject(bo)) {
            addNamedQuery(entityClass, valueArray, providedQuery.getName(), providedQuery.getContent());
        }

        // Add method for lazy fields
        for (final Query query : BDMQueryUtil.createProvidedQueriesForLazyField(bom, bo)) {
            addNamedQuery(entityClass, valueArray, query.getName(), query.getContent());
        }

        // Add custom queries
        for (final Query query : bo.getQueries()) {
            addNamedQuery(entityClass, valueArray, query.getName(), query.getContent());
        }
    }

    private void addUniqueConstraintAnnotations(final BusinessObject businessObject, final JDefinedClass entityClass) {
        final JAnnotationUse tableAnnotation = codeGenerator.addAnnotation(entityClass, Table.class);
        tableAnnotation.param("name", entityClass.name().toUpperCase());

        final List<UniqueConstraint> uniqueConstraints = businessObject.getUniqueConstraints();
        if (!uniqueConstraints.isEmpty()) {
            final JAnnotationArrayMember uniqueConstraintsArray = tableAnnotation.paramArray("uniqueConstraints");
            for (final UniqueConstraint uniqueConstraint : uniqueConstraints) {
                final JAnnotationUse uniqueConstraintAnnotation = uniqueConstraintsArray
                        .annotate(javax.persistence.UniqueConstraint.class);
                uniqueConstraintAnnotation.param("name", uniqueConstraint.getName().toUpperCase());
                final JAnnotationArrayMember columnNamesParamArray = uniqueConstraintAnnotation
                        .paramArray("columnNames");
                for (final String fieldName : uniqueConstraint.getFieldNames()) {
                    columnNamesParamArray.param(getFieldRealColumnName(businessObject, fieldName));
                }
            }
        }
    }

    private void addIndexAnnotations(final BusinessObject businessObject, final JDefinedClass entityClass) {
        final List<Index> indexes = businessObject.getIndexes();
        if (indexes != null && !indexes.isEmpty()) {
            final JAnnotationUse hibTabAnnotation = codeGenerator.addAnnotation(entityClass,
                    org.hibernate.annotations.Table.class);
            hibTabAnnotation.param("appliesTo", entityClass.name().toUpperCase());
            final JAnnotationArrayMember indexesArray = hibTabAnnotation.paramArray("indexes");
            for (final Index index : indexes) {
                final JAnnotationUse indexAnnotation = indexesArray.annotate(org.hibernate.annotations.Index.class);
                indexAnnotation.param("name", index.getName().toUpperCase());
                final JAnnotationArrayMember columnParamArray = indexAnnotation.paramArray("columnNames");
                for (final String fieldName : index.getFieldNames()) {
                    columnParamArray.param(getFieldRealColumnName(businessObject, fieldName).toUpperCase());
                }
            }
        }
    }

    /**
     * get real column name used in database
     *
     * @return fieldName for simple fields or reduced name suffix by "_PID" when we have an entity relationship
     */
    private String getFieldRealColumnName(BusinessObject businessObject, String fieldName) {
        String columnName;
        if (businessObject.isARelationField(fieldName)) {
            columnName = relationFieldAnnotator.getJoinColumnName(fieldName);
        } else {
            columnName = fieldName;
        }
        return columnName;
    }

    private void addNamedQuery(final JDefinedClass entityClass, final JAnnotationArrayMember valueArray,
            final String name, final String content) {
        final JAnnotationUse nameQueryAnnotation = valueArray.annotate(NamedQuery.class);
        nameQueryAnnotation.param("name", entityClass.name() + "." + name);
        nameQueryAnnotation.param("query", content);
    }

    private void validateClassNotExistsInRuntime(final String qualifiedName) {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Class clazz = contextClassLoader.loadClass(qualifiedName);
            // Here the class is found, which is NOT normal! Let's investigate:
            final StringBuilder message = new StringBuilder(
                    "Class " + qualifiedName + " already exists in target runtime environment");
            final ClassLoader classLoader = clazz.getClassLoader();
            if (classLoader != null) {
                if (classLoader instanceof URLClassLoader) {
                    for (URL url : ((URLClassLoader) classLoader).getURLs()) {
                        message.append("\n").append(url.toString());
                    }
                } else {
                    message.append("\nCurrent classloader is NOT an URLClassLoader: ").append(classLoader.toString());
                }
            }
            message.append("\nCurrent JVM Id where the class is found: ")
                    .append(ManagementFactory.getRuntimeMXBean().getName());
            message.append(
                    "\nMake sure you did not manually add the jar files bdm-model.jar / bdm-dao.jar somewhere on the classpath.");
            message.append(
                    "\nThose jar files are handled by Bonita internally and should not be manipulated outside Bonita.");
            throw new IllegalArgumentException(message.toString());
        } catch (final ClassNotFoundException ignored) {
            // here is the normal behaviour
        }
    }

    public void addPersistenceIdFieldAndAccessors(final JDefinedClass entityClass, String dbVendor) {
        final JFieldVar idFieldVar = codeGenerator.addField(entityClass, Field.PERSISTENCE_ID,
                codeGenerator.toJavaClass(FieldType.LONG));
        codeGenerator.addAnnotation(idFieldVar, Id.class);
        JAnnotationUse generateValue = codeGenerator.addAnnotation(idFieldVar, GeneratedValue.class);
        switch (dbVendor) {
            case "h2":
            case "postgres":
            case "oracle":
                // This generates the following annotation:
                // @GeneratedValue(generator = "default_bonita_seq_generator")
                // @GenericGenerator(
                //      name = "default_bonita_seq_generator",
                //      strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                //      parameters = {
                //          @Parameter(name = "sequence_name", value = "hibernate_sequence")
                //      })
                // maps to default Hibernate sequence name in database 'hibernate_sequence'
                generateValue.param("generator", "default_bonita_seq_generator");
                JAnnotationUse genericGenerator = codeGenerator.addAnnotation(idFieldVar, GenericGenerator.class);
                genericGenerator.param("name", "default_bonita_seq_generator");
                genericGenerator.param("strategy", SequenceStyleGenerator.class.getName());
                final JAnnotationArrayMember parametersAnnotation = genericGenerator.paramArray("parameters");
                final JAnnotationUse paramAnnotation = parametersAnnotation.annotate(Parameter.class);
                paramAnnotation.param("name", "sequence_name");
                paramAnnotation.param("value", "hibernate_sequence");
                break;
            case "mysql":
            case "sqlserver":
                generateValue.param("strategy", GenerationType.IDENTITY);
                break;
        }
        addAccessors(entityClass, idFieldVar);
    }

    public void addPersistenceVersionFieldAndAccessors(final JDefinedClass entityClass) {
        final JFieldVar versionField = codeGenerator.addField(entityClass, Field.PERSISTENCE_VERSION,
                codeGenerator.toJavaClass(FieldType.LONG));
        codeGenerator.addAnnotation(versionField, Version.class);
        addAccessors(entityClass, versionField);
    }

    public JFieldVar addField(final JDefinedClass entityClass, final Field field) {
        JFieldVar fieldVar;
        if (field.isCollection()) {
            fieldVar = codeGenerator.addListField(entityClass, field);
        } else {
            fieldVar = codeGenerator.addField(entityClass, field.getName(), codeGenerator.toJavaClass(field));
        }
        annotateField(entityClass, field, fieldVar);
        return fieldVar;
    }

    private void annotateField(final JDefinedClass entityClass, final Field field, final JFieldVar fieldVar) {
        if (field instanceof SimpleField) {
            annotateSimpleField((SimpleField) field, fieldVar);
        } else if (field instanceof RelationField) {
            annotateRelationField(entityClass, (RelationField) field, fieldVar);
        }
    }

    private void annotateRelationField(final JDefinedClass entityClass, final RelationField rfield,
            final JFieldVar fieldVar) {
        relationFieldAnnotator.annotateRelationField(entityClass, rfield, fieldVar);
    }

    private void annotateSimpleField(final SimpleField sfield, final JFieldVar fieldVar) {
        if (sfield.isCollection()) {
            final JAnnotationUse collectionAnnotation = codeGenerator.addAnnotation(fieldVar, ElementCollection.class);
            collectionAnnotation.param("fetch", FetchType.EAGER);
            codeGenerator.addAnnotation(fieldVar, OrderColumn.class);
        }
        final JAnnotationUse columnAnnotation = codeGenerator.addAnnotation(fieldVar, Column.class);
        columnAnnotation.param("name", sfield.getName().toUpperCase());
        columnAnnotation.param("nullable", sfield.isNullable());

        if (sfield.getType() == FieldType.DATE) {
            final JAnnotationUse temporalAnnotation = codeGenerator.addAnnotation(fieldVar, Temporal.class);
            temporalAnnotation.param("value", TemporalType.TIMESTAMP);
        } else if (FieldType.TEXT == sfield.getType()) {
            codeGenerator.addAnnotation(fieldVar, Lob.class);
        } else if (FieldType.STRING == sfield.getType() && sfield.getLength() != null && sfield.getLength() > 0) {
            columnAnnotation.param("length", sfield.getLength());
        } else if (FieldType.LOCALDATE == sfield.getType()) {
            // 10 = to support ISO-8801 date format:
            columnAnnotation.param("length", 10);
            final JAnnotationUse converterAnnotation = codeGenerator.addAnnotation(fieldVar, Convert.class);
            converterAnnotation.param("converter", DateConverter.class);
        } else if (FieldType.LOCALDATETIME == sfield.getType()) {
            columnAnnotation.param("length", 30);
            final JAnnotationUse converterAnnotation = codeGenerator.addAnnotation(fieldVar, Convert.class);
            converterAnnotation.param("converter", DateAndTimeConverter.class);
        } else if (FieldType.OFFSETDATETIME == sfield.getType()) {
            columnAnnotation.param("length", 30);
            final JAnnotationUse converterAnnotation = codeGenerator.addAnnotation(fieldVar, Convert.class);
            converterAnnotation.param("converter", OffsetDateTimeConverter.class);
        }
    }

    public void addAccessors(final JDefinedClass entityClass, final JFieldVar fieldVar) {
        addAccessors(entityClass, fieldVar, null);
    }

    public void addAccessors(final JDefinedClass entityClass, final JFieldVar fieldVar, final Field field) {
        if (isCollectionField(field)) {
            codeGenerator.addListSetter(entityClass, fieldVar);
        } else {
            codeGenerator.addSetter(entityClass, fieldVar);
        }
        final JMethod getter = codeGenerator.addGetter(entityClass, fieldVar);
        if (field instanceof RelationField && ((RelationField) field).isLazy()) {
            getter.annotate(LazyLoaded.class);
        }
    }

    protected void addModifiers(final JDefinedClass entityClass, final Field field) {
        if (isCollectionField(field)) {
            codeGenerator.addAddMethod(entityClass, field);
            codeGenerator.addRemoveMethod(entityClass, field);
        }
    }

    private boolean isCollectionField(final Field field) {
        if (field == null) {
            return false;
        }
        final Boolean collection = field.isCollection();
        return collection != null && collection;
    }

    private String determineDbVendor() {
        String dbVendor = System.getProperty("sysprop.bonita.bdm.db.vendor");
        if (dbVendor != null) {
            return dbVendor;
        } else {
            // The situation is not normally possible at runtime.
            // here to allow testing without too much code change
            LOGGER.error(
                    "sysprop.bonita.bdm.db.vendor is not set. This should not happen at runtime. Defaulting to h2.");
            return "h2";
        }
    }

}
