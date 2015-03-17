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

import org.apache.commons.lang3.text.WordUtils;

import org.bonitasoft.engine.bdm.model.field.Field;
import org.bonitasoft.engine.bdm.model.field.FieldType;
import org.bonitasoft.engine.bdm.model.field.RelationField;
import org.bonitasoft.engine.bdm.model.field.SimpleField;
import com.sun.codemodel.ClassType;
import com.sun.codemodel.JAnnotatable;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.util.NullStream;

/**
 * @author Romain Bioteau
 * @author Matthieu Chaffotte
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

    public void generate(final File destDir) throws IOException {
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
        if (fullyqualifiedName.indexOf(".") == -1) {
            return model.rootPackage()._class(JMod.PUBLIC, fullyqualifiedName, ClassType.INTERFACE);
        }
        return model._class(fullyqualifiedName, ClassType.INTERFACE);
    }

    public JFieldVar addField(final JDefinedClass definedClass, final String fieldName, final Class<?> type) {
        validateFieldName(fieldName);
        if (type == null) {
            throw new IllegalArgumentException("Field type cannot be null");
        }
        return definedClass.field(JMod.PRIVATE, type, fieldName);
    }

    public JFieldVar addField(final JDefinedClass definedClass, final Field field) {
        return addField(definedClass, field.getName(), toJavaClass(field));
    }

    public JFieldVar addField(final JDefinedClass definedClass, final String fieldName, final JClass type) {
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

    protected JFieldVar addListField(final JDefinedClass entityClass, final Field field) {
        final JClass fieldClass = toJavaClass(field);
        final JClass fieldListClass = narrowClass(List.class, fieldClass);
        final JClass arrayListFieldClazz = narrowClass(ArrayList.class, fieldClass);

        final JFieldVar listFieldVar = entityClass.field(JMod.PRIVATE, fieldListClass, field.getName());

        final JExpression newInstance = JExpr._new(arrayListFieldClazz).arg(JExpr.lit(10));
        listFieldVar.init(newInstance);
        return listFieldVar;
    }

    public JClass toJavaClass(final Field field) {
        if (field instanceof SimpleField) {
            final Class<?> fieldClass = ((SimpleField) field).getType().getClazz();
                return getModel().ref(fieldClass);
            }
        final String qualifiedName = ((RelationField) field).getReference().getQualifiedName();
        return getModel().ref(qualifiedName);
    }

    public JClass toJavaClass(final FieldType type) {
        return getModel().ref(type.getClazz());
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

    public JMethod addListSetter(final JDefinedClass definedClass, final JFieldVar field) {
        final JMethod method = definedClass.method(JMod.PUBLIC, Void.TYPE, getSetterName(field));
        method.param(field.type(), field.name());
        final JFieldRef thisField = JExpr._this().ref(field.name());
        final JConditional ifListIsNull = method.body()._if(thisField.eq(JExpr._null()));

        ifListIsNull._then().assign(JExpr._this().ref(field.name()), JExpr.ref(field.name()));

        final JBlock elseBlock = ifListIsNull._else();
        elseBlock.invoke(JExpr._this().ref(field.name()), "clear");
        elseBlock.invoke(JExpr._this().ref(field.name()), "addAll").arg(field);

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
        final JClass fieldClass = toJavaClass(field);
        final StringBuilder builder = new StringBuilder(parameterName);
        builder.append(WordUtils.capitalize(field.getName()));
        final JMethod method = definedClass.method(JMod.PUBLIC, void.class, builder.toString());
        final JVar adderParam = method.param(fieldClass, parameterName);
        final JBlock body = method.body();
        final JVar decl = body.decl(getModel().ref(List.class), field.getName(), JExpr.invoke(getGetterName(field)));
        body.add(decl.invoke(listMethodName).arg(adderParam));
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

    public String getGetterName(final JVar field) {
        final JType type = field.type();
        final boolean bool = Boolean.class.getName().equals(type.fullName());
        return getGetterName(bool, field.name());
    }

    public String getGetterName(final Field field) {
        final boolean bool = field instanceof SimpleField && FieldType.BOOLEAN.equals(((SimpleField) field).getType()) && !field.isCollection();
        return getGetterName(bool, field.getName());
    }

    private String getGetterName(final boolean bool, final String fieldName) {
        final StringBuilder builder = new StringBuilder();
        if (bool) {
            builder.append("is");
        } else {
            builder.append("get");
        }
        builder.append(WordUtils.capitalize(fieldName));
        return builder.toString();
    }

    public String getSetterName(final JVar field) {
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
