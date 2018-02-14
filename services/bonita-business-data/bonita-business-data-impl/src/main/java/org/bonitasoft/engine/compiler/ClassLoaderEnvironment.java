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

package org.bonitasoft.engine.compiler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;

/**
 *
 * Environment provided to the JDT compiler that load class from a classloader instead of a classpath
 *
 * /!\ it extends FileSystem only because the JDT compiler use the subtype instead of Environment
 *
 * @author Baptiste Mesta
 */
public class ClassLoaderEnvironment extends FileSystem {

    private final ClassLoader classLoader;
    private final Map<String, IBinaryType> loadedClassFiles;

    public ClassLoaderEnvironment(ClassLoader classLoader) {
        super(new String[] {}, null, "UTF-8");
        this.classLoader = classLoader;
        loadedClassFiles = new HashMap<>();
    }

    @Override
    public NameEnvironmentAnswer findType(char[][] compoundTypeName) {
        return findType(toPointedNotation(compoundTypeName));
    }

    @Override
    public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName) {
        return findType(getClassName(toPointedNotation(packageName), new String(typeName)));
    }


    private NameEnvironmentAnswer findType(String className) {
        //load from cache
        if(loadedClassFiles.containsKey(className)){
            return new NameEnvironmentAnswer(loadedClassFiles.get(className),null);
        }
        //load from the classloader
        try {
            String resourceName = className.replace('.', '/') + ".class";
            InputStream is = classLoader.getResourceAsStream(resourceName);
            if (is != null) {
                byte[] classBytes;
                byte[] buf = new byte[8192];
                ByteArrayOutputStream baos = new ByteArrayOutputStream(buf.length);
                int count;
                while ((count = is.read(buf, 0, buf.length)) > 0) {
                    baos.write(buf, 0, count);
                }
                baos.flush();
                classBytes = baos.toByteArray();
                char[] fileName = className.toCharArray();
                ClassFileReader classFileReader = new ClassFileReader(classBytes, fileName, true);
                loadedClassFiles.put(className, classFileReader);
                return new NameEnvironmentAnswer(classFileReader, null);
            }
        } catch (IOException | ClassFormatException exc) {
            System.err.println("Compilation error");
            exc.printStackTrace();
        }
        return null;
    }

    private boolean isPackage(String result) {
        if(result.isEmpty()){
            return true;
        }
        try {
            return classLoader.loadClass(result) == null;
        } catch (ClassNotFoundException e) {
            return true;
        }
    }

    @Override
    public boolean isPackage(char[][] parentPackageName,
            char[] className) {
        return !Character.isUpperCase(className[0]) && isPackage(getClassName(toPointedNotation(parentPackageName), new String(className)));
    }

    String getClassName(String parentPackage, String className) {
        if(parentPackage.isEmpty()){
            return className;
        }else{
            return parentPackage+"."+className;
        }
    }

    String toPointedNotation(char[][] parentPackageName) {
        StringBuilder result = new StringBuilder();
        String sep = "";
        if (parentPackageName != null) {
            for (char[] aParentPackageName : parentPackageName) {
                result.append(sep).append(aParentPackageName);
                sep = ".";
            }
        }
        return result.toString();
    }

    @Override
    public void cleanup() {
        loadedClassFiles.clear();
    }
}
