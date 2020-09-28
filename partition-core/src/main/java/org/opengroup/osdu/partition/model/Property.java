package org.opengroup.osdu.partition.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Property {
    @Builder.Default
    private boolean sensitive = false;
    private Object value;
}
