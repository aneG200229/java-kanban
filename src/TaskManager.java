import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class TaskManager {
    private Map<Integer, Task> taskMap = new HashMap<>();
    private Map<Integer, Epic> epicMap = new HashMap<>();
    private Map<Integer, Subtask> subtaskMap = new HashMap<>();
    private int counter = 1;


    public Map<Integer, Task> getTasks() {
        return taskMap;
    }

    public void removeTasks() {
        taskMap.clear();
    }

    public Task getTaskById(int id) {
        return taskMap.get(id);
    }

    public void createTask(Task task) {
        taskMap.put(task.getId(), task);

    }

    public void updateTask(int id, Task task) {
        if (taskMap.containsKey(id)) {
            Task existingTask = taskMap.get(id);
            existingTask.setName(task.getName());
            existingTask.setDescription(task.getDescription());
            existingTask.setStatus(task.getStatus());
        } else {
            System.out.println("Задача с ID " + id + " не найдена.");
        }
    }

    public void removeTaskById(int id) {
        taskMap.remove(id);
    }

    public Map<Integer, Epic> getEpics() {
        return epicMap;
    }

    public void removeEpics() {
        epicMap.clear();
        subtaskMap.clear();
    }

    public Epic getEpicById(int id) {
        return epicMap.get(id);
    }

    public void createEpic(Epic epic) {
        epic.setId(counter++);
        epicMap.put(epic.getId(), epic);
    }

    public void updateEpic(int id, Epic epic) {
        if (epicMap.containsKey(id)) {
            Epic existingEpic = epicMap.get(id);
            existingEpic.setName(epic.getName());
            existingEpic.setDescription(epic.getDescription());
        } else {
            System.out.println("Эпик с ID " + id + " не найден.");
        }
    }


    public void removeEpicById(int id) {
        Epic epic = epicMap.remove(id);
        if (epic != null) {
            for (Integer subtaskId : epic.getSubtaskIds()) {
                subtaskMap.remove(subtaskId);
            }
        }
    }

    public Map<Integer, Subtask> getSubtasks() {
        return subtaskMap;
    }

    public void removeSubtasks() {
        subtaskMap.clear();
    }

    public Subtask getSubtaskById(int id) {
        return subtaskMap.get(id);
    }

    public void createSubtask(Subtask subtask) {
        subtask.setId(counter++);
        subtaskMap.put(subtask.getId(), subtask);
        Epic epic = epicMap.get(subtask.getEpicId());
        epic.addSubtaskId(subtask.getId());
        updateEpicStatus(epic.getId());
    }

    public void updateSubtask(Subtask subtask) {
        if (subtaskMap.containsKey(subtask.getId())) {
            subtaskMap.put(subtask.getId(), subtask);
            updateEpicStatus(subtask.getEpicId());
        } else {
            System.out.println("Подзадача с ID " + subtask.getId() + " не найдена.");
        }
    }

    public void removeSubtaskById(int id) {
        Subtask subtask = subtaskMap.remove(id);
        if (subtask != null) {
            Epic epic = epicMap.get(subtask.getEpicId());
            epic.removeSubtaskId(id);
            updateEpicStatus(epic.getId());  // Пересчитываем статус эпика
        }
    }

    public List<Subtask> getSubtasksByEpicId(int epicId) {
        List<Subtask> result = new ArrayList<>();
        Epic epic = epicMap.get(epicId);
        if (epic != null) {
            for (Integer subtaskId : epic.getSubtaskIds()) {
                result.add(subtaskMap.get(subtaskId));
            }
        }
        return result;
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epicMap.get(epicId);
        if (epic == null) return;

        List<Integer> subtaskIds = epic.getSubtaskIds();
        if (subtaskIds.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allDone = true;
        boolean allNew = true;

        for (Integer subtaskId : subtaskIds) {
            Subtask subtask = subtaskMap.get(subtaskId);
            if (subtask.getStatus() != Status.DONE) {
                allDone = false;
            }
            if (subtask.getStatus() != Status.NEW) {
                allNew = false;
            }
        }

        if (allDone) {
            epic.setStatus(Status.DONE);
        } else if (allNew) {
            epic.setStatus(Status.NEW);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }
}
