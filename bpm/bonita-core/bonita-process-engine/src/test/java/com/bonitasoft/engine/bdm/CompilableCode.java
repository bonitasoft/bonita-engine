/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.bonitasoft.engine.bdm;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * @author Romain Bioteau
 *
 */
public abstract class CompilableCode {
	
	protected void assertCompilationSuccessful(File sourceFileToCompile) {
		final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
		final Iterable<? extends JavaFileObject> compUnits = fileManager.getJavaFileObjects(sourceFileToCompile);
		final Boolean compiled = compiler.getTask(null, fileManager, null, null, null, compUnits).call();
		assertThat(compiled).isTrue();
	}
	
}
