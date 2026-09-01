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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.SourceVersion;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.CodeWriter;
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
import com.sun.codemodel.writer.FileCodeWriter;
import com.sun.codemodel.writer.ProgressCodeWriter;
import org.apache.commons.text.WordUtils;
import org.bonitasoft.engine.bdm.model.field.Field;
import org.bonitasoft.engine.bdm.model.field.FieldType;
import org.bonitasoft.engine.bdm.model.field.RelationField;
import org.bonitasoft.engine.bdm.model.field.SimpleField;

/**
 * @author Romain Bioteau
 * @author Matthieu Chaffotte
 */
public class CodeGenerator {

    private final JCodeModel model;
    protected boolean shouldValidateRuntimeClasses = true;

    public CodeGenerator() {
        model = new JCodeModel();
    }

    public CodeGenerator disableRuntimeClassesValidation() {
        this.shouldValidateRuntimeClasses = false;
        return this;
    }

    public void generate(final File destDir) throws IOException {
        Charset encoding = StandardCharsets.UTF_8;
        try (PrintStream statusStream = new PrintStream(new NullStream(), false, encoding)) {
            CodeWriter src = new ProgressCodeWriter(new FileCodeWriter(destDir, encoding.name()),
                    statusStream);
            CodeWriter res = new ProgressCodeWriter(new FileCodeWriter(destDir, encoding.name()),
                    statusStream);
            model.build(src, res);
        }
    }

    public JDefinedClass addClass(final String fullyQualifiedName) throws JClassAlreadyExistsException {
        if (fullyQualifiedName == null || fullyQualifiedName.isEmpty()) {
            throw new IllegalArgumentException("Classname cannot be null or empty");
        }
        if (!SourceVersion.isName(fullyQualifiedName)) {
            throw new IllegalArgumentException("Classname " + fullyQualifiedName + " is not a valid qualified name");
        }
        if (shouldValidateRuntimeClasses) {
            validateClassNotExistsInRuntime(fullyQualifiedName);
        }
        return model._class(fullyQualifiedName);
    }

    private void validateClassNotExistsInRuntime(final String qualifiedName) {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            var clazz = contextClassLoader.loadClass(qualifiedName);
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

    public JDefinedClass addInterface(final JDefinedClass definedClass, final String fullyQualifiedName) {
        return definedClass._implements(model.ref(fullyQualifiedName));
    }

    public JDefinedClass addInterface(final String fullyQualifiedName) throws JClassAlreadyExistsException {
        if (!fullyQualifiedName.contains(".")) {
            return model.rootPackage()._class(JMod.PUBLIC, fullyQualifiedName, ClassType.INTERFACE);
        }
        return model._class(fullyQualifiedName, ClassType.INTERFACE);
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
    private JClass narrowClass(final Class<? extends Collection> collectionClass, final JClass narrowClass) {
        final JClass collectionJClass = getModel().ref(collectionClass);
        return collectionJClass.narrow(narrowClass);
    }

    JFieldVar addListField(final JDefinedClass entityClass, final Field field) {
        final JClass fieldClass = toJavaClass(field);
        final JClass fieldListClass = narrowClass(List.class, fieldClass);
        final JClass arrayListFieldClazz = narrowClass(ArrayList.class, fieldClass);

        final JFieldVar listFieldVar = entityClass.field(JMod.PRIVATE, fieldListClass, field.getName());

        final JExpression newInstance = JExpr._new(arrayListFieldClazz).arg(JExpr.lit(10));
        listFieldVar.init(newInstance);
        return listFieldVar;
    }

    JClass toJavaClass(final Field field) {
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

    void addDefaultConstructor(final JDefinedClass definedClass) {
        definedClass.constructor(JMod.PUBLIC);
    }

    public JMethod addSetter(final JDefinedClass definedClass, final JFieldVar field) {
        final JMethod method = definedClass.method(JMod.PUBLIC, Void.TYPE, getSetterName(field));
        method.param(field.type(), field.name());
        method.body().assign(JExpr._this().ref(field.name()), JExpr.ref(field.name()));
        return method;
    }

    JMethod addListSetter(final JDefinedClass definedClass, final JFieldVar field) {
        final JMethod method = definedClass.method(JMod.PUBLIC, Void.TYPE, getSetterName(field));
        method.param(field.type(), field.name());
        final JFieldRef thisField = JExpr._this().ref(field.name());
        final JConditional ifListIsNull = method.body()._if(thisField.eq(JExpr._null()));

        ifListIsNull._then().assign(JExpr._this().ref(field.name()), JExpr.ref(field.name()));

        final JBlock elseBlock = ifListIsNull._else();
        final JVar copyVar = elseBlock.decl(field.type(), "copy",
                JExpr._new(getModel().ref(ArrayList.class)).arg(field));
        elseBlock.invoke(JExpr._this().ref(field.name()), "clear");
        elseBlock.invoke(JExpr._this().ref(field.name()), "addAll").arg(copyVar);

        return method;
    }

    /**
     * Adds the read accessor named after the field type: <code>getX()</code>, or <code>isX()</code> for a wrapper
     * <code>Boolean</code>.
     * <p>
     * Annotations applied to the returned method do not reach the <code>getX()</code> alias that
     * {@link #addBooleanGetterAlias(JDefinedClass, JFieldVar)} adds in a later pass, so that a wrapper
     * <code>Boolean</code> stays readable as a Groovy property. None does today, {@code LazyLoaded} on lazy
     * relation getters being the only annotation any caller applies.
     */
    public JMethod addGetter(final JDefinedClass definedClass, final JFieldVar field) {
        final JMethod method = definedClass.method(JMod.PUBLIC, field.type(), getGetterName(field));
        final JBlock block = method.body();
        block._return(field);
        return method;
    }

    /**
     * Adds a <code>getX()</code> alias next to the <code>isX()</code> accessor of a wrapper <code>Boolean</code>
     * field, so that the <code>entity.x</code> shorthand used in process expressions resolves under Groovy 4, which
     * accepts <code>isX()</code> as a property getter only for a primitive <code>boolean</code> (BPA-833). The
     * <code>isX()</code> accessor is left untouched.
     * <p>
     * Call it only after every other accessor of the class exists, and only when
     * {@link #canAddBooleanGetterAlias(JDefinedClass, JFieldVar)} holds. For example, a BOOLEAN
     * <code>hungry</code> and any other attribute named <code>Hungry</code> both yield <code>getHungry()</code>,
     * and the alias is the accessor that must give way.
     */
    JMethod addBooleanGetterAlias(final JDefinedClass definedClass, final JFieldVar field) {
        final JMethod alias = definedClass.method(JMod.PUBLIC, field.type(), getGetterName(false, field.name()));
        alias.body()._return(field);
        return alias;
    }

    /**
     * Whether the <code>getX()</code> alias name is free on the given class. Asked separately from
     * {@link #addBooleanGetterAlias(JDefinedClass, JFieldVar)} so that the caller can tell this skip apart from its
     * other one, a name clashing with a lazy relation only in case, and give each its own remedy.
     * <p>
     * No type check is needed: for any other field type the alias name is the field's own accessor name, already
     * declared by the time the alias pass runs.
     */
    boolean canAddBooleanGetterAlias(final JDefinedClass definedClass, final JFieldVar field) {
        final String aliasName = getGetterName(false, field.name());
        // getClass() is the only inherited method a get-prefixed alias can collide with, and it is final: emitting it
        // would fail to compile. A BOOLEAN attribute named Class passes the BDM validator, so the alias gives way.
        return !declaresMethod(definedClass, aliasName) && !"getClass".equals(aliasName);
    }

    private boolean declaresMethod(final JDefinedClass definedClass, final String methodName) {
        return definedClass.methods().stream().anyMatch(method -> methodName.equals(method.name()));
    }

    public JMethod addMethodSignature(final JDefinedClass definedClass, final String methodName,
            final JType returnType) {
        return definedClass.method(JMod.PUBLIC, returnType, methodName);
    }

    public JMethod addAddMethod(final JDefinedClass definedClass, final Field field) {
        return addListMethod(definedClass, field, "add", "addTo");
    }

    public JMethod addRemoveMethod(final JDefinedClass definedClass, final Field field) {
        return addListMethod(definedClass, field, "remove", "removeFrom");
    }

    private JMethod addListMethod(final JDefinedClass definedClass, final Field field, final String listMethodName,
            final String parameterName) {
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

    private String getGetterName(final JVar field) {
        return getGetterName(usesIsAccessor(field), field.name());
    }

    private boolean usesIsAccessor(final JVar field) {
        final JType type = field.type();
        return Boolean.class.getName().equals(type.fullName());
    }

    private String getGetterName(final Field field) {
        return getGetterName(usesIsAccessor(field), field.getName());
    }

    /**
     * Whether the model field is generated with an <code>isX()</code> accessor rather than <code>getX()</code>: a
     * non-collection BOOLEAN, and nothing else. Package-private because the alias pass needs the same predicate.
     * <p>
     * {@code Field.isCollection()} returns a {@code Boolean}, so the comparison avoids unboxing it.
     */
    boolean usesIsAccessor(final Field field) {
        return field instanceof SimpleField simpleField
                && FieldType.BOOLEAN == simpleField.getType()
                && !Boolean.TRUE.equals(field.isCollection());
    }

    /**
     * Builds the accessor name for a field: <code>isX()</code> when {@code bool}, <code>getX()</code> otherwise.
     * <p>
     * Package-private so that {@code EntityCodeGenerator} builds the accessor names it reports to administrators
     * from the same place they are generated.
     */
    String getGetterName(final boolean bool, final String fieldName) {
        final StringBuilder builder = new StringBuilder();
        if (bool) {
            builder.append("is");
        } else {
            builder.append("get");
        }
        builder.append(WordUtils.capitalize(fieldName));
        return builder.toString();
    }

    String getSetterName(final JVar field) {
        return "set" + WordUtils.capitalize(field.name());
    }

    public JCodeModel getModel() {
        return model;
    }

    protected JAnnotationUse addAnnotation(final JAnnotatable annotable,
            final Class<? extends Annotation> annotationType) {
        final Set<ElementType> supportedElementTypes = getSupportedElementTypes(annotationType);
        checkAnnotationTarget(annotable, annotationType, supportedElementTypes);
        return annotable.annotate(model.ref(annotationType));
    }

    private void checkAnnotationTarget(final JAnnotatable annotable, final Class<? extends Annotation> annotationType,
            final Set<ElementType> supportedElementTypes) {
        if (annotable instanceof JClass && !supportedElementTypes.isEmpty()
                && !supportedElementTypes.contains(ElementType.TYPE)) {
            throw new IllegalArgumentException(annotationType.getName() + " is not supported for " + annotable);
        }
        if (annotable instanceof JFieldVar && !supportedElementTypes.isEmpty()
                && !supportedElementTypes.contains(ElementType.FIELD)) {
            throw new IllegalArgumentException(annotationType.getName() + " is not supported for " + annotable);
        }
        if (annotable instanceof JMethod && !supportedElementTypes.isEmpty()
                && !supportedElementTypes.contains(ElementType.METHOD)) {
            throw new IllegalArgumentException(annotationType.getName() + " is not supported for " + annotable);
        }
    }

    private Set<ElementType> getSupportedElementTypes(final Class<? extends Annotation> annotationType) {
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

    private static class NullStream extends OutputStream {

        NullStream() {
        }

        public void write(int b) throws IOException {
        }

        public void close() throws IOException {
        }

        public void flush() throws IOException {
        }

        public void write(byte[] b, int off, int len) throws IOException {
        }

        public void write(byte[] b) throws IOException {
        }
    }

}
