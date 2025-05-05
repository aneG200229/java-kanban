import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    @Test
    void shouldCreateAndFindDifferentTasksById() {
        InMemoryTaskManager manager = new InMemoryTaskManager();

        Task task = new Task("Задача", "Описание", Status.NEW);
        manager.createTask(task);

        Epic epic = new Epic("Эпик", "Описание");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Подзадача", "Описание", Status.NEW, epic.getId());
        manager.createSubtask(subtask);

        assertEquals(task, manager.getTaskById(task.getId()));
        assertEquals(epic, manager.getEpicById(epic.getId()));
        assertEquals(subtask, manager.getSubtaskById(subtask.getId()));
    }

    @Test
    void manuallyAndAutoAssignedIdsShouldNotConflict() {
        InMemoryTaskManager manager = new InMemoryTaskManager();

        Task manualTask = new Task("Ручная задача", "Описание", Status.NEW);
        manualTask.setId(99);
        manager.createTask(manualTask);

        Task autoTask = new Task("Автоматическая задача", "Описание", Status.NEW);
        manager.createTask(autoTask);

        assertNotEquals(manualTask.getId(), autoTask.getId(), "ID не должны конфликтовать");
    }

    @Test
    void taskShouldRemainUnchangedAfterAddition() {
        InMemoryTaskManager manager = new InMemoryTaskManager();
        Task task = new Task("Оригинал", "Описание", Status.NEW);
        manager.createTask(task);

        Task stored = manager.getTaskById(task.getId());

        assertEquals(task.getName(), stored.getName());
        assertEquals(task.getDescription(), stored.getDescription());
        assertEquals(task.getStatus(), stored.getStatus());
    }

    @Test
    void subtaskWithSameIdAsEpicShouldNotBeAdded() {
        InMemoryTaskManager manager = new InMemoryTaskManager();

        Epic epic = new Epic("Эпик", "Описание");
        epic.setId(100);
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Ошибка", "Описание", Status.NEW, 100);
        subtask.setId(100);

        manager.createSubtask(subtask);


        assertNull(manager.getSubtaskById(100), "Подзадача с ID, равным Epic, не должна быть добавлена.");
    }
}