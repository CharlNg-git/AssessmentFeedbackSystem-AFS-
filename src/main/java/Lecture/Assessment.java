package Lecture;

public class Assessment {
    private String id;
    private String moduleId;
    private String name;
    private String type;
    private int maxMarks;

    public Assessment(String id, String moduleId, String name, String type, int maxMarks) {
        this.id = id;
        this.moduleId = moduleId;
        this.name = name;
        this.type = type;
        this.maxMarks = maxMarks;
    }

    public String getId() { return id; }
    public String getModuleId() { return moduleId; }
    public String getName() { return name; }
    public String getType() { return type; }
    public int getMaxMarks() { return maxMarks; }

    @Override
    public String toString() {
        return name + " (" + type + ")";
    }
}