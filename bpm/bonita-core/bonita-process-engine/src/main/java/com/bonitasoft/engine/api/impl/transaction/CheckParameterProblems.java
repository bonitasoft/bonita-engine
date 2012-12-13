/*
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.api.impl.transaction;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.bpm.model.Problem;
import org.bonitasoft.engine.bpm.model.Problem.Level;
import org.bonitasoft.engine.bpm.model.ProblemImpl;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.parameter.OrderBy;
import org.bonitasoft.engine.parameter.ParameterService;
import org.bonitasoft.engine.parameter.SParameter;

/**
 * @author Matthieu Chaffotte
 */
public class CheckParameterProblems implements TransactionContent {

    private final ParameterService parameterService;

    private final long processId;

    private List<Problem> problems;

    public CheckParameterProblems(final ParameterService parameterService, final long processId) {
        this.parameterService = parameterService;
        this.processId = processId;
    }

    @Override
    public void execute() throws SBonitaException {
        List<SParameter> paramters;
        problems = new ArrayList<Problem>();
        int i = 0;
        do {
            paramters = parameterService.get(processId, i, 100, OrderBy.NAME_ASC);
            i += 100;
            for (final SParameter parameter : paramters) {
                if (parameter.getValue() == null) {
                    final Problem problem = new ProblemImpl(Level.ERROR, "parameter", "Parameter" + parameter.getName() + " is not set");
                    problems.add(problem);
                }
            }
        } while (paramters.size() == 100);
    }

    public List<Problem> getProblems() {
        return problems;
    }

}
