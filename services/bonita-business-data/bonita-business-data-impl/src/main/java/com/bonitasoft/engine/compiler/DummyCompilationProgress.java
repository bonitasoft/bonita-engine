/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.compiler;

import org.eclipse.jdt.core.compiler.CompilationProgress;

/**
 * CompilationProgress which do nothing
 * Used by JdtCompiler
 * 
 * @author Colin PUY
 */
public class DummyCompilationProgress extends CompilationProgress {

    @Override
    public void worked(int workIncrement, int remainingWork) {

    }

    @Override
    public void setTaskName(String name) {

    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public void done() {

    }

    @Override
    public void begin(int remainingWork) {

    }
}
