import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Integer, Node> nodeMap = new HashMap<>();
    private Node tail = null;
    private Node head = null;

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        } else if (nodeMap.containsKey(task.getId())) {
            Node oldNode = nodeMap.remove(task.getId());
            removeNode(oldNode);
        }
        Node newNode = linkLast(task);
        nodeMap.put(task.getId(), newNode);
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    @Override
    public void remove(int id) {
        if (!nodeMap.containsKey(id)) {
            return;
        } else {
            Node node = nodeMap.remove(id);
            removeNode(node);
        }
    }


    public Node linkLast(Task task) {
        if (task == null) {
            return null;
        }
        Node node = new Node(task);
        if (head == null) {
            head = node;
            tail = node;
        } else {
            node.prev = tail;
            tail.next = node;
            tail = node;
        }
        return node;
    }

    public List<Task> getTasks() {
        ArrayList<Task> tasksList = new ArrayList<>();
        Node current = head;
        while (current != null) {
            tasksList.add(current.task);
            current = current.next;
        }
        return tasksList;
    }

    public void removeNode(Node node) {
        if (node == null) {
            return;
        } else if (node == head && node == tail) {
            head = null;
            tail = null;
        } else if (node == head) {
            head = node.next;
            if (head != null) {
                head.prev = null;
            }
        } else if (node == tail) {
            tail = node.prev;
            if (tail != null) {
                tail.next = null;
            }
        } else {
            if (node.prev != null) {
                node.prev.next = node.next;
            }
            if (node.next != null) {
                node.next.prev = node.prev;
            }
        }
        node.prev = null;
        node.next = null;
        node.task = null;
    }

    private static class Node {
        Task task;
        Node next;
        Node prev;

        public Node(Task task) {
            this.task = task;
        }
    }


}
