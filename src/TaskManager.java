import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class TaskManager {
    private Map<Integer, Task> taskMap = new HashMap<>();
    private Map<Integer, Epic> epicMap = new HashMap<>();
    private Map<Integer, Subtask> subtaskMap = new HashMap<>();
    private int counter = 1;


    public List<Task> getTasks() {
        return new ArrayList<>(taskMap.values());
    }

    public void removeTasks() {
        taskMap.clear();
    }

    public Task getTaskById(int id) {
        return taskMap.get(id);
    }

    public void createTask(Task task) {
        task.setId(counter++);
        taskMap.put(task.getId(), task);

    }

    public void updateTask(Task task) {
       taskMap.put(task.getId(),task);
    }

    public void removeTaskById(int id) {
        taskMap.remove(id);
    }

    public List<Epic> getEpics() {
        return new ArrayList<>(epicMap.values());
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

    public void updateEpic(Epic epic) {
        if (epicMap.containsKey(epic.getId())) {
            Epic existingEpic = epicMap.get(epic.getId());
            existingEpic.setName(epic.getName());
            existingEpic.setDescription(epic.getDescription());
            updateEpicStatus(existingEpic);
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

    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtaskMap.values());
    }

    public void removeSubtasks() {
        subtaskMap.clear();
        for (Epic epic : epicMap.values()) {
            epic.clearSubtaskIds();
            updateEpicStatus(epic);
        }
    }

    public Subtask getSubtaskById(int id) {
        return subtaskMap.get(id);
    }

    public void createSubtask(Subtask subtask) {
        Epic epic = epicMap.get(subtask.getEpicId());
        if (epic == null) {
            System.out.println("Ошибка: Эпик с ID " + subtask.getEpicId() + " не найден.");
            return;
        }
        subtask.setId(counter++);
        subtaskMap.put(subtask.getId(), subtask);
        epic.addSubtaskId(subtask.getId());
        updateEpicStatus(epic);
    }

    public void updateSubtask(Subtask subtask) {
        if (subtaskMap.containsKey(subtask.getId())) {
            subtaskMap.put(subtask.getId(), subtask);
            updateEpicStatus(epicMap.get(subtask.getEpicId()));
        }
    }


    public void removeSubtaskById(int id) {
        Subtask subtask = subtaskMap.remove(id);
        if (subtask == null) return;

        Epic epic = epicMap.get(subtask.getEpicId());
        if (epic != null) {
            epic.removeSubtaskId(id);
            updateEpicStatus(epic);
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

    public void updateEpicStatus(Epic epic) {
        boolean hasInProgress = false;
        boolean hasNew = false;
        boolean hasDone = false;

        for (Integer subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtaskMap.get(subtaskId);
            if (subtask == null) continue;

            switch (subtask.getStatus()) {
                case IN_PROGRESS:
                    epic.setStatus(Status.IN_PROGRESS);
                    return;
                case NEW:
                    hasNew = true;
                    break;
                case DONE:
                    hasDone = true;
                    break;
            }
        }

        if (hasNew && hasDone) {
            epic.setStatus(Status.IN_PROGRESS);
        } else if (hasDone) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.NEW);
        }
    }
}
