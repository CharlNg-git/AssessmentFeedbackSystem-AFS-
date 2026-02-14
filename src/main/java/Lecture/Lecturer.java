package Lecture;

public class Lecturer {
    private String id;
    private String name;
    private String email;
    private String password;

    public Lecturer(String id, String name) {
        this.id = id;
        this.name = name;
        this.email = "";
        this.password = "";
    }

    public Lecturer(String id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
}