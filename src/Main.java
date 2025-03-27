public class Main {

    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        // 1. Тестируем создание обычной задачи
        Task task1 = new Task("Задача 1", "Описание задачи 1", Status.NEW);
        manager.createTask(task1);

        System.out.println("Создана задача: " + manager.getTaskById(task1.getId()));

        // 2. Тестируем обновление задачи
        task1.setDescription("Обновленное описание");
        task1.setStatus(Status.IN_PROGRESS);
        manager.updateTask(task1.getId(), task1);

        System.out.println("Обновленная задача: " + manager.getTaskById(task1.getId()));

        // 3. Тестируем создание эпика и подзадач
        Epic epic1 = new Epic("Эпик 1", "Описание эпика", Status.NEW);
        manager.createEpic(epic1);
        System.out.println("Создан эпик: " + manager.getEpicById(epic1.getId()));

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи", Status.NEW, epic1.getId());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи", Status.NEW, epic1.getId());

        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        System.out.println("Подзадачи эпика: " + manager.getSubtasksByEpicId(epic1.getId()));
        System.out.println("Статус эпика после добавления подзадач: " + manager.getEpicById(epic1.getId()).getStatus());

        // 4. Меняем статус подзадач и проверяем обновление эпика
        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.DONE);
        manager.updateSubtask(subtask1);
        manager.updateSubtask(subtask2);

        System.out.println("Подзадачи эпика после обновления статуса: " + manager.getSubtasksByEpicId(epic1.getId()));
        System.out.println("Статус эпика после завершения подзадач: " + manager.getEpicById(epic1.getId()).getStatus());

        // 5. Удаление подзадачи и проверка статуса эпика
        manager.removeSubtaskById(subtask1.getId());
        System.out.println("Подзадачи эпика после удаления одной: " + manager.getSubtasksByEpicId(epic1.getId()));
        System.out.println("Статус эпика после удаления подзадачи: " + manager.getEpicById(epic1.getId()).getStatus());

        // 6. Удаляем эпик и проверяем, что подзадачи тоже удалены
        manager.removeEpicById(epic1.getId());
        System.out.println("Эпик после удаления: " + manager.getEpicById(epic1.getId()));
        System.out.println("Подзадачи после удаления эпика: " + manager.getSubtasksByEpicId(epic1.getId()));

        // 7. Удаление задачи
        manager.removeTaskById(task1.getId());
        System.out.println("Задача после удаления: " + manager.getTaskById(task1.getId()));
    }
}


