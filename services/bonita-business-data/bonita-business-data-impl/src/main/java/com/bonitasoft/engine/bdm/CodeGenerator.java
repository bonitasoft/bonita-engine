/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.SourceVersion;
import javax.persistence.ElementCollection;
import javax.persistence.FetchType;

import org.apache.commons.lang3.text.WordUtils;

import com.bonitasoft.engine.bdm.model.Field;
import com.bonitasoft.engine.bdm.model.FieldType;
import com.bonitasoft.engine.bdm.model.SimpleField;
import com.sun.codemodel.ClassType;
import com.sun.codemodel.JAnnotatable;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.util.NullStream;

/**
 * @author Romain Bioteau
 */
public class CodeGenerator {

    private final JCodeModel model;

    private final EqualsBuilder equalsBuilder;

    private final HashCodeBuilder hashCodeBuilder;

    public CodeGenerator() {
        model = new JCodeModel();
        equalsBuilder = new EqualsBuilder();
        hashCodeBuilder = new HashCodeBuilder();
    }

    public void generate(final File destDir) throws IOException, JClassAlreadyExistsException, BusinessObjectModelValidationException, ClassNotFoundException {
        final PrintStream stream = new PrintStream(new NullStream());
        try {
            model.build(destDir, stream);
        } finally {
            stream.close();
        }
    }

    public JDefinedClass addClass(final String fullyqualifiedName) throws JClassAlreadyExistsException {
        if (fullyqualifiedName == null || fullyqualifiedName.isEmpty()) {
            throw new IllegalArgumentException("Classname cannot cannot be null or empty");
        }
        if (!SourceVersion.isName(fullyqualifiedName)) {
            throw new IllegalArgumentException("Classname " + fullyqualifiedName + " is not a valid qualified name");
        }
        return model._class(fullyqualifiedName);
    }

    public JDefinedClass addInterface(final JDefinedClass definedClass, final String fullyqualifiedName) {
        return definedClass._implements(model.ref(fullyqualifiedName));
    }

    public JDefinedClass addInterface(final String fullyqualifiedName) throws JClassAlreadyExistsException {
        return model._class(fullyqualifiedName, ClassType.INTERFACE);
    }

    public JFieldVar addField(final JDefinedClass definedClass, final String fieldName, final Class<?> type) {
        validateFieldName(fieldName);
        if (type == null) {
            throw new IllegalArgumentException("Field type cannot be null");
        }
        return definedClass.field(JMod.PRIVATE, type, fieldName);
    }

    public JFieldVar addField(final JDefinedClass definedClass, final String fieldName, final JType type) {
        validateFieldName(fieldName);
        if (type == null) {
            throw new IllegalArgumentException("Field type cannot be null");
        }
        return definedClass.field(JMod.PRIVATE, type, fieldName);
    }

    private void validateFieldName(final String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            throw new IllegalArgumentException("Field name cannot be null or empty");
        }
        if (SourceVersion.isKeyword(fieldName)) {
            throw new IllegalArgumentException("Field " + fieldName + " is a resered keyword");
        }
        if (!SourceVersion.isIdentifier(fieldName)) {
            throw new IllegalArgumentException("Field " + fieldName + " is not a valid Java identifier");
        }
    }

    @SuppressWarnings("rawtypes")
    protected JClass narrowClass(final Class<? extends Collection> collectionClass, final JClass narrowClass) {
        final JClass collectionJClass = getModel().ref(collectionClass);
        return collectionJClass.narrow(narrowClass);
    }

    protected JFieldVar addListField(final JDefinedClass entityClass, final Field field) throws JClassAlreadyExistsException {
        JClass fieldClass = null;
        if (field instanceof SimpleField) {
            fieldClass = getModel().ref(((SimpleField)field).getType().getClazz());
        }
        final JClass fieldListClass = narrowClass(List.class, fieldClass);
        final JClass arrayListFieldClazz = narrowClass(ArrayList.class, fieldClass);

        final JFieldVar listFieldVar = entityClass.field(JMod.PRIVATE, fieldListClass, field.getName());

        final JExpression newInstance = JExpr._new(arrayListFieldClazz).arg(JExpr.lit(10));
        listFieldVar.init(newInstance);

        final JAnnotationUse collectionAnnotation = addAnnotation(listFieldVar, ElementCollection.class);
        collectionAnnotation.param("fetch", FetchType.EAGER);

        return listFieldVar;
    }

    public void addDefaultConstructor(final JDefinedClass definedClass) {
        definedClass.constructor(JMod.PUBLIC);
    }

    public JMethod addSetter(final JDefinedClass definedClass, final JFieldVar field) {
        final JMethod method = definedClass.method(JMod.PUBLIC, Void.TYPE, getSetterName(field));
        method.param(field.type(), field.name());
        method.body().assign(JExpr._this().ref(field.name()), JExpr.ref(field.name()));
        return method;
    }

    public JMethod addGetter(final JDefinedClass definedClass, final JFieldVar field) {
        final JMethod method = definedClass.method(JMod.PUBLIC, field.type(), getGetterName(field));
        final JBlock block = method.body();
        block._return(field);
        return method;
    }

    public JMethod addMethodSignature(final JDefinedClass definedClass, final String methodName, final JType returnType) {
        return definedClass.method(JMod.PUBLIC, returnType, methodName);
    }

    public JMethod addAddMethod(final JDefinedClass definedClass, final Field field) {
        return addListMethod(definedClass, field, "add", "addTo");
    }

    public JMethod addRemoveMethod(final JDefinedClass definedClass, final Field field) {
        return addListMethod(definedClass, field, "remove", "removeFrom");
    }

    private JMethod addListMethod(final JDefinedClass definedClass, final Field field, final String listMethodName, final String parameterName) {
        JClass fieldClass = null;
        if (field instanceof SimpleField) {
            fieldClass = getModel().ref(((SimpleField)field).getType().getClazz());
        }
        
        final StringBuilder builder = new StringBuilder(parameterName);
        builder.append(WordUtils.capitalize(field.getName()));

        final JMethod method = definedClass.method(JMod.PUBLIC, void.class, builder.toString());
        final JVar adderParam = method.param(fieldClass, parameterName);
        method.body().invoke(JExpr.ref(field.getName()), listMethodName).arg(adderParam);
        return method;
    }

    public JMethod addEqualsMethod(final JDefinedClass definedClass) {
        final JMethod equalsMethod = equalsBuilder.generate(definedClass);
        addAnnotation(equalsMethod, Override.class);
        return equalsMethod;
    }

    public JMethod addHashCodeMethod(final JDefinedClass definedClass) {
        final JMethod hashCodeMethod = hashCodeBuilder.generate(definedClass);
        addAnnotation(hashCodeMethod, Override.class);
        return hashCodeMethod;
    }

    public String getGetterName(final JFieldVar field) {
        final JType type = field.type();
        final StringBuilder builder = new StringBuilder();
        if (Boolean.class.getName().equals(type.fullName())) {
            builder.append("is");
        } else {
            builder.append("get");
        }
        builder.append(WordUtils.capitalize(field.name()));
        return builder.toString();
    }

    public String getGetterName(final Field field) {
        final StringBuilder builder = new StringBuilder();
        if (field instanceof SimpleField && FieldType.BOOLEAN.equals(((SimpleField) field).getType())) {
            builder.append("is");
        } else {
            builder.append("get");
        }
        builder.append(WordUtils.capitalize(field.getName()));
        return builder.toString();
    }

    public String getSetterName(final JFieldVar field) {
        return "set" + WordUtils.capitalize(field.name());
    }

    public JCodeModel getModel() {
        return model;
    }

    protected JAnnotationUse addAnnotation(final JAnnotatable annotable, final Class<? extends Annotation> annotationType) {
        final Set<ElementType> supportedElementTypes = getSupportedElementTypes(annotationType);
        checkAnnotationTarget(annotable, annotationType, supportedElementTypes);
        return annotable.annotate(model.ref(annotationType));
    }

    protected void checkAnnotationTarget(final JAnnotatable annotable, final Class<? extends Annotation> annotationType,
            final Set<ElementType> supportedElementTypes) {
        if (annotable instanceof JClass && !supportedElementTypes.isEmpty() && !supportedElementTypes.contains(ElementType.TYPE)) {
            throw new IllegalArgumentException(annotationType.getName() + " is not supported for " + annotable);
        }
        if (annotable instanceof JFieldVar && !supportedElementTypes.isEmpty() && !supportedElementTypes.contains(ElementType.FIELD)) {
            throw new IllegalArgumentException(annotationType.getName() + " is not supported for " + annotable);
        }
        if (annotable instanceof JMethod && !supportedElementTypes.isEmpty() && !supportedElementTypes.contains(ElementType.METHOD)) {
            throw new IllegalArgumentException(annotationType.getName() + " is not supported for " + annotable);
        }
    }

    protected Set<ElementType> getSupportedElementTypes(final Class<? extends Annotation> annotationType) {
        final Set<ElementType> elementTypes = new HashSet<ElementType>();
        final Target targetAnnotation = annotationType.getAnnotation(Target.class);
        if (targetAnnotation != null) {
            final ElementType[] value = targetAnnotation.value();
            if (value != null) {
                for (final ElementType et : value) {
                    elementTypes.add(et);
                }
            }
        }
        return elementTypes;
    }

}
