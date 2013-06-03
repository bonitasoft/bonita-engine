package org.bonitasoft.engine.exceptions.impl;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * @author Baptiste Mesta
 */
public class MyTestException1 extends SBonitaException {

    public MyTestException1(final Object... arguments) {
        super(arguments);
    }

    public MyTestException1() {
        super();
    }

    private static final long serialVersionUID = 6593839764767605910L;

}
