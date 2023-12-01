package org.opengroup.osdu.partition.logging;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

public class AuditEventsTest {

    @Test
    public void shouldInitializeAuditEvents_whenUserIsValid() {
        AuditEvents auditEvents = new AuditEvents("testUser");
        
        assertNotNull(auditEvents);
    }

    @Test
    public void shouldThrowIllegalArgumentException_whenAuditEvents_initializedWithEmptyUser() {
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> new AuditEvents(""));

        assertEquals("User not provided for audit events.", illegalArgumentException.getMessage());
    }

    @Test
    public void shouldThrowIllegalArgumentException_whenAuditEvents_initializedWithNullUser() {
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> new AuditEvents(null));

        assertEquals("User not provided for audit events.", illegalArgumentException.getMessage());
    }
}
