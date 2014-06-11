package com.bonitasoft.engine.bdm.validator.rule;

import com.bonitasoft.engine.bdm.model.Index;
import com.bonitasoft.engine.bdm.validator.SQLNameValidator;
import com.bonitasoft.engine.bdm.validator.ValidationStatus;

public class IndexValidationRule extends ValidationRule<Index> {

    private static final int MAX_CONSTRAINTNAME_LENGTH = 25;

    private final SQLNameValidator sqlNameValidator;

    public IndexValidationRule() {
        super(Index.class);
        sqlNameValidator = new SQLNameValidator(MAX_CONSTRAINTNAME_LENGTH);
    }

    @Override
    protected ValidationStatus validate(Index index) {
        final ValidationStatus status = new ValidationStatus();
        final String name = index.getName();
        if (name == null || name.isEmpty()) {
            status.addError("An index must have name");
            return status;
        }
        final boolean isValid = sqlNameValidator.isValid(name);
        if (!isValid) {
            status.addError(name + " is not a valid SQL identifier");
        }

        if (index.getFieldNames().isEmpty()) {
            status.addError(name + " index must have at least one field declared");
        }

        return status;
    }

}
