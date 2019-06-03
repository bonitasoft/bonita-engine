package org.bonitasoft.engine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatasourceConfiguration {

    /**
     * Maximum number of connections in the pool
     * Must be more than 0
     * Default value is 7
     */
    private int maxPoolSize;


}
