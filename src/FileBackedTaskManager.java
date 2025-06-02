import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private File taskFile;

    public FileBackedTaskManager(File file) {
        this.taskFile = file;
    }

    public String toString(Task task) {
        String type;
        String epicId = "";
        if (task instanceof Subtask) {
            type = TaskType.SUBTASK.name();
            epicId = String.valueOf(((Subtask) task).getEpicId());
        } else if (task instanceof Epic) {
            type = TaskType.EPIC.name();
        } else {
            type = TaskType.TASK.name();
        }
        return String.format("%d,%s,%s,%s,%s,%s",
                task.getId(), type, task.getName(), task.getStatus().name(), task.getDescription(), epicId.isEmpty() ? "" : epicId);
    }

    public Task fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            System.out.println("Пропускаю пустую строку: " + value);
            return null;
        }
        String[] fields = value.split(",", -1); // -1 сохраняет пустые поля в конце
        System.out.println("Обрабатываю строку: " + value + ", найдено полей: " + fields.length);
        if (fields.length != 6) {
            System.err.println("Неверный формат строки: ожидается 6 полей, найдено " + fields.length + ", строка: " + value);
            return null;
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
            switch (type) {
                case TASK -> {
                    Task task = new Task(name, description, status);
                    task.setId(id);
                    return task;
                }
                case EPIC -> {
                    Epic epic = new Epic(name, description);
                    epic.setId(id);
                    return epic;
                }
                case SUBTASK -> {
                    Subtask subtask = new Subtask(name, description, status, epicId);
                    subtask.setId(id);
                    return subtask;
                }
                default -> {
                    return null;
                }
            }
        } catch (NumberFormatException e) {
            System.err.println("Ошибка парсинга строки (NumberFormatException): " + value + ", причина: " + e.getMessage());
            return null;
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка парсинга строки (IllegalArgumentException): " + value + ", причина: " + e.getMessage());
            return null;
        }
    }

    public void save() {
        ArrayList<String> listForFile = new ArrayList<>();
        listForFile.add("id,type,name,status,description,epic");

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
            throw new RuntimeException("ошибка при сохранении в файл: " + e.getMessage());
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) throws IOException {
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(file);
        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        if (lines.isEmpty()) {
            return fileBackedTaskManager;
        }
        List<String> dataLines = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (!line.isEmpty()) {
                dataLines.add(line);
            }
        }
        // Первый проход: EPIC
        for (String line : dataLines) {
            Task task = fileBackedTaskManager.fromString(line);
            if (task instanceof Epic) {
                fileBackedTaskManager.createEpic((Epic) task);
            }
        }
        // Второй проход: TASK и SUBTASK
        for (String line : dataLines) {
            Task task = fileBackedTaskManager.fromString(line);
            if (task != null) {
                if (task instanceof Task && !(task instanceof Epic) && !(task instanceof Subtask)) {
                    fileBackedTaskManager.createTask(task);
                } else if (task instanceof Subtask) {
                    fileBackedTaskManager.createSubtask((Subtask) task);
                }
            }
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


