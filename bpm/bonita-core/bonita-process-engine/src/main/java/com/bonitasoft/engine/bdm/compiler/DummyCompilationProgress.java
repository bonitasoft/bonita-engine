package com.bonitasoft.engine.bdm.compiler;

import org.eclipse.jdt.core.compiler.CompilationProgress;

/**
 * CompilationProgress which do nothing
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
