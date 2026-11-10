/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.api.bpm.flownode;

import static org.bonitasoft.console.common.server.utils.ContractTypeConverter.ISO_8601_DATE_PATTERNS;
import static org.bonitasoft.web.rest.server.api.AbstractRESTController.API_SPRING_INTERNAL;

import javax.servlet.http.HttpSession;

import org.bonitasoft.console.common.server.utils.ContractTypeConverter;
import org.bonitasoft.engine.bpm.contract.ContractDefinition;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.web.rest.server.api.AbstractRESTController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/" + API_SPRING_INTERNAL + "/bpm/userTask/{taskId}/contract")
public class UserTaskContractController extends AbstractRESTController {

    protected final ContractTypeConverter typeConverterUtil = new ContractTypeConverter(ISO_8601_DATE_PATTERNS);

    @GetMapping
    public ResponseEntity<ContractDefinition> getContract(@PathVariable final long taskId, HttpSession session)
            throws BonitaException {
        ContractDefinition contract = getProcessAPI(session).getUserTaskContract(taskId);
        ContractDefinition adaptedContract = typeConverterUtil.getAdaptedContractDefinition(contract);
        if (adaptedContract == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(adaptedContract);
    }

}
