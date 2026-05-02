package kartingRM.Backend;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class BackendApplicationTest {

    @Test
    void constructor_createsApplicationInstance() {
        assertNotNull(new BackendApplication());
    }
}
