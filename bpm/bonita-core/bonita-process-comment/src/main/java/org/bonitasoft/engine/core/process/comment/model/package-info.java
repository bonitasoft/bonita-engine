@FilterDef(name="tenantFilter",   parameters = {@ParamDef(name="tenantId", type="long")},
        defaultCondition = "tenantid = :tenantId")
package org.bonitasoft.engine.core.process.comment.model;

import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;