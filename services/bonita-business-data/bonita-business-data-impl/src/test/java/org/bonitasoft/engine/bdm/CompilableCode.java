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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * @author Romain Bioteau
 */
public abstract class CompilableCode {

    protected void assertCompilationSuccessful(final File sourceFileToCompile) {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        final Iterable<? extends JavaFileObject> compUnits = fileManager.getJavaFileObjects(sourceFileToCompile);
        final List<String> optionList = new ArrayList<String>();

        String javaxPersistencefilePath = findJarPath(javax.persistence.Basic.class);
        String bdmEntityfilePath = findJarPath(Entity.class);
        optionList.addAll(Arrays.asList("-classpath", javaxPersistencefilePath + File.pathSeparator + bdmEntityfilePath));
        final Boolean compiled = compiler.getTask(null, fileManager, null, optionList, null, compUnits).call();
        assertThat(compiled).isTrue();
    }

    private String findJarPath(final Class<?> clazzToFind) {
        URL jarURL = clazzToFind.getResource(clazzToFind.getSimpleName() + ".class");
        String jarPath = jarURL.getFile();
        if (jarPath.indexOf("!") == -1) {
            jarPath = getDotClassParentPath(jarPath);
        } else {
            jarPath = jarPath.split("!")[0];
        }
        return jarPath;
    }

    private String getDotClassParentPath(final String completeClassUrl) {
        int indexOf = completeClassUrl.indexOf(Entity.class.getName().replace('.', File.separatorChar));
        if (indexOf != -1) {
            return completeClassUrl.substring(0, indexOf);
        }
        File f = new File(completeClassUrl);
        if (f.exists()) {
            return f.getParent();
        }
        return completeClassUrl;
    }

}
