import java.util.List;

public interface TaskManager {
    List<Task> getTasks();

    void removeTasks();

    Task getTaskById(int id);

    void createTask(Task task);

    void updateTask(Task task);

    void removeTaskById(int id);

    List<Epic> getEpics();

    void removeEpics();

    Epic getEpicById(int id);

    void createEpic(Epic epic);

    void updateEpic(Epic epic);

    void removeEpicById(int id);

    List<Subtask> getSubtasks();

    void removeSubtasks();

    Subtask getSubtaskById(int id);

    void createSubtask(Subtask subtask);

    void updateSubtask(Subtask subtask);

    void removeSubtaskById(int id);

    List<Subtask> getSubtasksByEpicId(int epicId);

    void updateEpicStatus(Epic epic);

    void updateEpicTime(Epic epic);

    List<Task> getHistory();


}
