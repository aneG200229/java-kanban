import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> taskMap = new HashMap<>();
    private final Map<Integer, Epic> epicMap = new HashMap<>();
    private final Map<Integer, Subtask> subtaskMap = new HashMap<>();
    private final HistoryManager historyManager;
    private int counter = 1;


    public InMemoryTaskManager() {
        this.historyManager = Managers.getDefaultHistory();
    }

    Comparator<Task> comparator = (t1, t2) -> {
        if (t1.getStartTime() == null && t2.getStartTime() == null) return Integer.compare(t1.getId(), t2.getId());
        if (t1.getStartTime() == null) return 1;
        if (t2.getStartTime() == null) return -1;
        int result = t1.getStartTime().compareTo(t2.getStartTime());
        return result != 0 ? result : Integer.compare(t1.getId(), t2.getId());
    };
    private final Set<Task> prioritizedTasks = new TreeSet<>(comparator);


    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(taskMap.values());
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    @Override
    public void removeTasks() {
        taskMap.clear();
    }

    @Override
    public Task getTaskById(int id) {
        historyManager.add(taskMap.get(id));
        return taskMap.get(id);
    }

    @Override
    public void createTask(Task task) {
        if (isTaskTimeOverlapping(task)) {
            System.out.println("Ошибка: задача пересекается по времени с другой задачей.");
            return;
        }
        task.setId(counter++);
        taskMap.put(task.getId(), task);
        prioritizedTasks.add(task);
    }


    @Override
    public void updateTask(Task task) {
        Task oldTask = taskMap.get(task.getId());
        if (oldTask != null) {
            prioritizedTasks.remove(oldTask);
        }

        if (isTaskTimeOverlapping(task)) {
            System.out.println("Ошибка: задача пересекается по времени с другой задачей.");
            if (oldTask != null) prioritizedTasks.add(oldTask);
            return;
        }

        taskMap.put(task.getId(), task);
        prioritizedTasks.add(task);
    }


    @Override
    public void removeTaskById(int id) {
        Task task = taskMap.remove(id);
        historyManager.remove(id);
        prioritizedTasks.remove(task);
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epicMap.values());
    }

    @Override
    public void removeEpics() {
        epicMap.clear();
        subtaskMap.clear();
    }

    @Override
    public Epic getEpicById(int id) {
        historyManager.add(epicMap.get(id));
        return epicMap.get(id);
    }

    @Override
    public void createEpic(Epic epic) {
        if (epic.getId() == 0) { // Предполагаем, что 0 — значение по умолчанию для нового объекта
            epic.setId(counter++);
        }
        epicMap.put(epic.getId(), epic);
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epicMap.containsKey(epic.getId())) {
            Epic existingEpic = epicMap.get(epic.getId());
            existingEpic.setName(epic.getName());
            existingEpic.setDescription(epic.getDescription());
            updateEpicStatus(existingEpic);
        }
    }


    @Override
    public void removeEpicById(int id) {
        Epic epic = epicMap.remove(id);
        if (epic != null) {
            for (Integer subtaskId : epic.getSubtaskIds()) {
                subtaskMap.remove(subtaskId);
            }
        }
        historyManager.remove(id);
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtaskMap.values());
    }

    @Override
    public void removeSubtasks() {
        subtaskMap.clear();
        for (Epic epic : epicMap.values()) {
            epic.clearSubtaskIds();
            updateEpicStatus(epic);
            updateEpicTime(epic);
        }
    }

    @Override
    public Subtask getSubtaskById(int id) {
        historyManager.add(subtaskMap.get(id));
        return subtaskMap.get(id);
    }

    @Override
    public void createSubtask(Subtask subtask) {
        Epic epic = epicMap.get(subtask.getEpicId());
        if (epic == null) {
            System.out.println("Ошибка: Эпик с ID " + subtask.getEpicId() + " не найден.");
            return;
        }

        if (isTaskTimeOverlapping(subtask)) {
            System.out.println("Ошибка: сабтаск пересекается по времени с другой задачей.");
            return;
        }

        subtask.setId(counter++);
        subtaskMap.put(subtask.getId(), subtask);
        prioritizedTasks.add(subtask);
        epic.addSubtaskId(subtask.getId());
        updateEpicStatus(epic);
        updateEpicTime(epic);
    }


    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtaskMap.containsKey(subtask.getId())) {
            Subtask oldSubtask = subtaskMap.get(subtask.getId());
            if (oldSubtask != null) {
                prioritizedTasks.remove(oldSubtask);
            }

            if (isTaskTimeOverlapping(subtask)) {
                System.out.println("Ошибка: сабтаск пересекается по времени с другой задачей.");
                if (oldSubtask != null) prioritizedTasks.add(oldSubtask);
                return;
            }

            subtaskMap.put(subtask.getId(), subtask);
            prioritizedTasks.add(subtask);

            Epic epic = epicMap.get(subtask.getEpicId());
            if (epic != null) {
                updateEpicStatus(epic);
                updateEpicTime(epic);
            }
        }
    }


    @Override
    public void removeSubtaskById(int id) {
        Subtask subtask = subtaskMap.remove(id);
        if (subtask == null) return;
        prioritizedTasks.remove(subtask);

        Epic epic = epicMap.get(subtask.getEpicId());
        if (epic != null) {
            epic.removeSubtaskId(id);
            updateEpicStatus(epic);
            updateEpicTime(epic);
        }
        historyManager.remove(id);

    }

    @Override
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

    @Override
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

    @Override
    public void updateEpicTime(Epic epic) {
        List<Integer> subtaskIds = epic.getSubtaskIds();
        if (subtaskIds.isEmpty()) {
            epic.setStartTime(null);
            epic.setEndTime(null);
            epic.setDuration(Duration.ZERO);
            return;
        }

        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        Duration totalDuration = Duration.ZERO;

        for (int subtaskId : subtaskIds) {
            Subtask subtask = subtaskMap.get(subtaskId);
            if (subtask == null) continue;

            LocalDateTime subStart = subtask.getStartTime();
            LocalDateTime subEnd = subtask.getEndTime();
            Duration subDuration = subtask.getDuration();

            if (subStart != null) {
                if (startTime == null || subStart.isBefore(startTime)) {
                    startTime = subStart;
                }
            }

            if (subEnd != null) {
                if (endTime == null || subEnd.isAfter(endTime)) {
                    endTime = subEnd;
                }
            }

            if (subDuration != null) {
                totalDuration = totalDuration.plus(subDuration);
            }
        }

        epic.setStartTime(startTime);
        epic.setEndTime(endTime);
        epic.setDuration(totalDuration);
    }

    private boolean isTaskTimeOverlapping(Task newTask) {
        if (newTask.getStartTime() == null || newTask.getDuration() == null) {
            return false;
        }

        LocalDateTime newStart = newTask.getStartTime();
        LocalDateTime newEnd = newTask.getEndTime();

        for (Task existingTask : prioritizedTasks) {
            if (existingTask.getId() == newTask.getId()) continue;
            if (existingTask.getStartTime() == null || existingTask.getDuration() == null) continue;

            LocalDateTime existingStart = existingTask.getStartTime();
            LocalDateTime existingEnd = existingTask.getEndTime();


            boolean isOverlapping = newStart.isBefore(existingEnd) && existingStart.isBefore(newEnd);
            if (isOverlapping) {
                return true;
            }
        }
        return false;
    }


    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

}
