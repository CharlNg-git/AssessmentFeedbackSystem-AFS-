package Admin.model;

public class User {
    private String id;
    private String role;
    private String name;
    private String email;

    public User(String id, String role, String name, String email) {
        this.id = id;
        this.role = role;
        this.name = name;
        this.email = email;
    }

    // Getters to access private data (Encapsulation)
    public String getId() { return id; }
    public String getRole() { return role; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    
    @Override
    public String toString() {
        return name + " (" + role + ")";
    }
}