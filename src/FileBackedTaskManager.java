import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private File taskFile;

    public FileBackedTaskManager(File file) {
        this.taskFile = file;
    }

    public String toString(Task task) {
        String epicId = task.getType() == TaskType.SUBTASK ? String.valueOf(((Subtask) task).getEpicId()) : "";
        String duration = task.getDuration() != null ? task.getDuration().toString() : "";
        String startTime = task.getStartTime() != null ? task.getStartTime().toString() : "";

        return String.format("%d,%s,%s,%s,%s,%s,%s,%s",
                task.getId(), task.getType().name(), task.getName(), task.getStatus().name(), task.getDescription(),
                epicId, duration, startTime);
    }

    public Task fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            System.out.println("Пропускаю пустую строку: " + value);
            return null;
        }
        String[] fields = value.split(",", -1); // -1 сохраняет пустые поля в конце
        System.out.println("Обрабатываю строку: " + value + ", найдено полей: " + fields.length);
        if (fields.length != 8) {
            throw new ManagerSaveException("Неверный формат строки: ожидается 8 полей, найдено " + fields.length + ", строка: " + value);
        }
        try {
            int id = Integer.parseInt(fields[0].trim());
            TaskType type = TaskType.valueOf(fields[1].trim());
            String name = fields[2].trim();
            Status status = Status.valueOf(fields[3].trim());
            String description = fields[4].trim();
            int epicId = 0;
            if (type == TaskType.SUBTASK && !fields[5].trim().isEmpty()) {
                epicId = Integer.parseInt(fields[5].trim());
            }
            Duration duration = null;
            if (!fields[6].trim().isEmpty()) {
                duration = Duration.parse(fields[6].trim());
            }
            LocalDateTime startTime = null;
            if (!fields[7].trim().isEmpty()) {
                startTime = LocalDateTime.parse(fields[7].trim());
            }
            switch (type) {
                case TASK -> {
                    if (duration != null || startTime != null) {
                        Task task = new Task(name, description, status, duration, startTime);
                        task.setId(id);
                        return task;
                    } else {
                        Task task = new Task(name, description, status);
                        task.setId(id);
                        return task;
                    }
                }
                case EPIC -> {
                    Epic epic = new Epic(name, description);
                    epic.setId(id);
                    return epic;
                }
                case SUBTASK -> {
                    if (duration != null || startTime != null) {
                        Subtask subtask = new Subtask(name, description, status, duration, startTime, epicId);
                        subtask.setId(id);
                        return subtask;
                    } else {
                        Subtask subtask = new Subtask(name, description, status, epicId);
                        subtask.setId(id);
                        return subtask;
                    }
                }
                default -> {
                    return null;
                }
            }
        } catch (NumberFormatException e) {
            throw new ManagerSaveException("Ошибка парсинга строки (NumberFormatException): " + value + ", причина: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new ManagerSaveException("Ошибка парсинга строки (IllegalArgumentException): " + value + ", причина: " + e.getMessage(), e);
        }
    }

    public void save() {
        ArrayList<String> listForFile = new ArrayList<>();
        listForFile.add("id,type,name,status,description,epic,duration,startTime");

        for (Task task : getTasks()) {
            listForFile.add(toString(task));
        }
        for (Epic epic : getEpics()) {
            listForFile.add(toString(epic));
        }
        for (Subtask subtask : getSubtasks()) {
            listForFile.add(toString(subtask));
        }

        try {
            Files.write(taskFile.toPath(), listForFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении в файл: " + e.getMessage(), e);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(file);
        List<String> lines;
        try {
            lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при чтении файла: " + e.getMessage(), e);
        }
        if (lines.isEmpty()) {
            return fileBackedTaskManager;
        }
        int maxId = 0;
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("id,type,name,status,description,epic,duration,startTime")) { // Пропускаем заголовок
                try {
                    String[] fields = line.split(",", -1);
                    if (fields.length > 0) {
                        int id = Integer.parseInt(fields[0].trim());
                        maxId = Math.max(maxId, id);
                    }
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }
        fileBackedTaskManager.setCounter(maxId + 1);
        List<String> dataLines = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (!line.isEmpty()) {
                dataLines.add(line);
            }
        }
        try {
            for (String line : dataLines) {
                Task task = fileBackedTaskManager.fromString(line);
                if (task != null && task.getType() == TaskType.EPIC) {
                    fileBackedTaskManager.createEpic((Epic) task);
                }
            }
            for (String line : dataLines) {
                Task task = fileBackedTaskManager.fromString(line);
                if (task != null) {
                    if (task.getType() == TaskType.TASK) {
                        fileBackedTaskManager.createTask(task);
                    } else if (task.getType() == TaskType.SUBTASK) {
                        fileBackedTaskManager.createSubtask((Subtask) task);
                    }
                }
            }
        } catch (ManagerSaveException e) {
            throw new ManagerSaveException("Ошибка при загрузке задач из файла: " + e.getMessage(), e);
        }
        List<Epic> epics = fileBackedTaskManager.getEpics();
        for (Epic epic : epics) {
            fileBackedTaskManager.updateEpicStatus(epic);
        }
        return fileBackedTaskManager;
    }

    @Override
    public void createEpic(Epic epic) {
        super.createEpic(epic);
        save();
    }

    @Override
    public void createSubtask(Subtask subtask) {
        super.createSubtask(subtask);
        save();
    }

    @Override
    public void createTask(Task task) {
        super.createTask(task);
        save();
    }
}