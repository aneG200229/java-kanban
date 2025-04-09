import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private final List<Task> history = new ArrayList<>();
    @Override
    public void add(Task task) {
        addToHistory(task);
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }

    @Override
    public void remove(Task task) {
        removeHistory(task);
    }

    private void addToHistory(Task task) {
        if (task == null) {
            return;
        }
        history.add(task);
        if (history.size()>10){
            history.remove(0);
        }
    }

    public void removeHistory(Task task){
        if (task == null) {
            return;
        }
        history.remove(task);
    }
}
