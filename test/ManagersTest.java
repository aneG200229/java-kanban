import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {
    @Test
    void managersShouldReturnInitializedInstances() {
        TaskManager manager = Managers.getDefault();
        HistoryManager history = Managers.getDefaultHistory();

        assertNotNull(manager, "TaskManager должен быть инициализирован");
        assertNotNull(history, "HistoryManager должен быть инициализирован");
    }
}