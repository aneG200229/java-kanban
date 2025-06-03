import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void tasksWithSameIdShouldBeEqual() {
        Task task1 = new Task("Задача", "Описание", Status.NEW);
        Task task2 = new Task("Другая задача", "Другое описание", Status.IN_PROGRESS);

        task1.setId(1);
        task2.setId(1);

        assertEquals(task1, task2, "Задачи с одинаковым ID должны быть равны");
    }

    @Test
    void subtasksWithSameIdShouldBeEqual() {
        Subtask subtask1 = new Subtask("Подзадача", "Описание", Status.NEW, 5);
        Subtask subtask2 = new Subtask("Подзадача2", "Другое", Status.DONE, 5);

        subtask1.setId(100);
        subtask2.setId(100);

        assertEquals(subtask1, subtask2, "Subtask с одинаковым ID должны быть равны");
    }


    @Test
    void subtaskCannotHaveItselfAsEpic() {
        Subtask subtask = new Subtask("Ошибка", "Описание", Status.NEW, 42);
        subtask.setId(42); // ID совпадает с epicId

        InMemoryTaskManager manager = new InMemoryTaskManager();
        manager.createSubtask(subtask);

        assertNull(manager.getSubtaskById(42), "Subtask не должен ссылаться сам на себя как на Epic");
    }


}