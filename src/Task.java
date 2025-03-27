public class Task {
    private String name;
    private String description;
    private  int id;
    private static int idCounter = 1;
    private Status status;

    public Task(String name, String description, Status status) {
        this.description = description;
        this.id = idCounter++;
        this.name = name;
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
    @Override
    public String toString() {
        return "Task{id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status + '}';
    }
}
