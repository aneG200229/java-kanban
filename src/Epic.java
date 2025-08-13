import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtaskIds;


    public Epic(String name, String description) {
        super(name, description, Status.NEW);
        this.subtaskIds = new ArrayList<>();
    }

    public void addSubtaskId(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void removeSubtaskId(int subtaskId) {
        subtaskIds.remove((Integer) subtaskId);
    }

    public void clearSubtaskIds() {
        subtaskIds.clear();
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }


    @Override
    public String toString() {
        return "Epic{" + "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", subtaskIds=" + subtaskIds + '}';
    }
}