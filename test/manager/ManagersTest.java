package manager;

import managers.HistoryManager;
import managers.Managers;
import managers.TaskManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {
    @Test
    void managersShouldReturnInitializedInstances() {
        TaskManager manager = Managers.getDefault();
        HistoryManager history = Managers.getDefaultHistory();

        assertNotNull(manager, "managers.TaskManager должен быть инициализирован");
        assertNotNull(history, "managers.HistoryManager должен быть инициализирован");
    }
}