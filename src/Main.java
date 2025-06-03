import java.io.File;
import java.io.IOException;


public class Main {

    public static void main(String[] args) {
        try {

            File tempFile = File.createTempFile("test", ".txt");
            System.out.println("Временный файл создан: " + tempFile.getAbsolutePath());


            FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);


            Task task1 = new Task("Task1", "Description1", Status.NEW);
            Task task2 = new Task("Task2", "Description2", Status.IN_PROGRESS);
            Epic epic1 = new Epic("Epic1", "Epic Description1");


            System.out.println("Добавляем задачи, эпики и подзадачи...");
            manager.createTask(task1);
            manager.createTask(task2);
            manager.createEpic(epic1);


            Subtask subtask1 = new Subtask("Subtask1", "Subtask Description1", Status.DONE, epic1.getId());
            Subtask subtask2 = new Subtask("Subtask2", "Subtask Description2", Status.NEW, epic1.getId());
            manager.createSubtask(subtask1);
            manager.createSubtask(subtask2);


            manager.save();
            System.out.println("Данные сохранены в файл: " + tempFile.getAbsolutePath());


        } catch (IOException e) {
            System.out.println("Ошибка при выполнении теста: " + e.getMessage());
        }
    }
}

