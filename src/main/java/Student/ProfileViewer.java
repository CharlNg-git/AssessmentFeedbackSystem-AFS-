package Student;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import util.FileManager;
import util.DatabaseFile;

public class ProfileViewer extends JPanel {
    private String tpNumber;
    private String role;
    
    // Profile fields
    private JTextField nameField, emailField, phoneField, addressField;
    private JPasswordField currentPasswordField, newPasswordField, confirmPasswordField;
    
    private final FileManager fileManager = new FileManager();

    public ProfileViewer(String tpNumber, String role) {
        this.tpNumber = tpNumber;
        this.role = role;
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Header
        JPanel header = createHeader();
        add(header, BorderLayout.NORTH);

        // Main Content
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // Profile Info Panel (Left)
        mainPanel.add(createProfileInfoPanel(), BorderLayout.CENTER);

        // Password Change Panel (Right)
        mainPanel.add(createPasswordPanel(), BorderLayout.EAST);

        add(mainPanel, BorderLayout.CENTER);

        // Load profile data
        loadProfile();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(255, 165, 0));
        header.setPreferredSize(new Dimension(0, 80));

        JLabel title = new JLabel("  My Profile");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        // For students: show intake, for staff: show role
        String userInfo;
        if (role.equalsIgnoreCase("student")) {
            String intakeCode = getStudentIntake(tpNumber);
            userInfo = tpNumber + " | Intake: " + intakeCode + "  ";
        } else {
            userInfo = tpNumber + " | " + role.toUpperCase() + "  ";
        }
        
        JLabel userInfoLabel = new JLabel(userInfo);
        userInfoLabel.setFont(new Font("Arial", Font.BOLD, 18));
        userInfoLabel.setForeground(Color.WHITE);
        header.add(userInfoLabel, BorderLayout.EAST);

        return header;
    }

    private JPanel createProfileInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(255, 165, 0), 2),
            "Profile Information",
            0, 0, new Font("Arial", Font.BOLD, 16)));

        // Info Fields Panel (Center)
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBackground(Color.WHITE);
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Tp Number (Read-only)
        addField(fieldsPanel, gbc, 0, "Tp Number:", tpNumber, false);

        // Name
        nameField = new JTextField(20);
        addFieldWithComponent(fieldsPanel, gbc, 1, "Full Name:", nameField);

        // Email
        emailField = new JTextField(20);
        addFieldWithComponent(fieldsPanel, gbc, 2, "Email:", emailField);

        // Phone
        phoneField = new JTextField(20);
        addFieldWithComponent(fieldsPanel, gbc, 3, "Phone:", phoneField);

        // Address
        addressField = new JTextField(20);
        addFieldWithComponent(fieldsPanel, gbc, 4, "Address:", addressField);

        panel.add(fieldsPanel, BorderLayout.CENTER);

        // Button Panel (Bottom)
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(Color.WHITE);

        JButton updateBtn = new JButton("Update Profile");
        updateBtn.setBackground(new Color(255, 165, 0));
        updateBtn.setForeground(Color.WHITE);
        updateBtn.setFont(new Font("Arial", Font.BOLD, 14));
        updateBtn.setFocusPainted(false);
        updateBtn.addActionListener(e -> updateProfile());

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBackground(new Color(70, 130, 180));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFont(new Font("Arial", Font.BOLD, 14));
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> loadProfile());

        btnPanel.add(updateBtn);
        btnPanel.add(refreshBtn);

        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createPasswordPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(400, 0));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
            "Change Password",
            0, 0, new Font("Arial", Font.BOLD, 16)));

        // Fields Panel
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBackground(Color.WHITE);
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        currentPasswordField = new JPasswordField(20);
        addFieldWithComponent(fieldsPanel, gbc, 0, "Current Password:", currentPasswordField);

        newPasswordField = new JPasswordField(20);
        addFieldWithComponent(fieldsPanel, gbc, 1, "New Password:", newPasswordField);

        confirmPasswordField = new JPasswordField(20);
        addFieldWithComponent(fieldsPanel, gbc, 2, "Confirm Password:", confirmPasswordField);

        panel.add(fieldsPanel, BorderLayout.CENTER);

        // Button Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setBackground(Color.WHITE);

        JButton changePasswordBtn = new JButton("Change Password");
        changePasswordBtn.setBackground(new Color(34, 139, 34));
        changePasswordBtn.setForeground(Color.WHITE);
        changePasswordBtn.setFont(new Font("Arial", Font.BOLD, 14));
        changePasswordBtn.setFocusPainted(false);
        changePasswordBtn.addActionListener(e -> changePassword());

        btnPanel.add(changePasswordBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String label, String value, boolean editable) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        JTextField field = new JTextField(value);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setEditable(editable);
        if (!editable) {
            field.setBackground(new Color(240, 240, 240));
        }
        panel.add(field, gbc);
    }

    private void addFieldWithComponent(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent component) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        if (component instanceof JTextField) {
            ((JTextField) component).setFont(new Font("Arial", Font.PLAIN, 14));
        } else if (component instanceof JPasswordField) {
            ((JPasswordField) component).setFont(new Font("Arial", Font.PLAIN, 14));
        }
        panel.add(component, gbc);
    }

    private void loadProfile() {
        try {
            String filePath;
            
            // Determine which file to read based on role
            if (role.equalsIgnoreCase("student")) {
                filePath = fileManager.getFilePath(DatabaseFile.STUDENT.getFileName());
            } else {
                filePath = fileManager.getFilePath(DatabaseFile.STAFF.getFileName());
            }

            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] data = line.split(",");
                
                // For students: fullName,icNumber/passport,gender,dateOfBirth,nationality,race,address,contactNumber,tpNumber,password,intakeCode
                // Indices:      0         1                 2      3           4           5     6       7             8        9        10
                
                // For staff: fullName,icNumber/passport,gender,dateOfBirth,nationality,race,address,contactNumber,jobTitle,yearOfService,tpNumber,password,role,academicLeader
                // Indices:   0         1                 2      3           4           5     6       7             8        9             10       11       12   13
                // NOTE: Need at least 11 elements to have tpNumber at index 10
                
                String filetpNumber = "";
                if (role.equalsIgnoreCase("student") && data.length >= 11) {
                    filetpNumber = data[8].trim();
                } else if (!role.equalsIgnoreCase("student") && data.length >= 11) {  // Staff needs at least index 10
                    filetpNumber = data[10].trim();
                }

                if (filetpNumber.equals(tpNumber)) {
                    // Load data based on role
                    if (role.equalsIgnoreCase("student")) {
                        nameField.setText(data[0].trim());        // fullName
                        phoneField.setText(data[7].trim());       // contactNumber
                        addressField.setText(data[6].trim());     // address
                        
                        // Get email from user.txt
                        String email = getEmailFromUserFile();
                        emailField.setText(email);
                    } else {
                        // Staff
                        nameField.setText(data[0].trim());        // fullName
                        phoneField.setText(data[7].trim());       // contactNumber
                        addressField.setText(data[6].trim());     // address
                        
                        // Get email from user.txt
                        String email = getEmailFromUserFile();
                        emailField.setText(email);
                    }
                    
                    br.close();
                    return;
                }
            }
            br.close();

            JOptionPane.showMessageDialog(this, "Profile not found!");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get email from user.txt file
     */
    private String getEmailFromUserFile() {
        try {
            String userFilePath = fileManager.getFilePath(DatabaseFile.USER.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(userFilePath));
            String line = br.readLine(); // Skip header
            
            // tpNumber,role,name,email,password
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] data = line.split(",");
                if (data.length >= 4 && data[0].trim().equals(tpNumber)) {
                    br.close();
                    return data[3].trim(); // email
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private void updateProfile() {
        try {
            String filePath;
            
            if (role.equalsIgnoreCase("student")) {
                filePath = fileManager.getFilePath(DatabaseFile.STUDENT.getFileName());
            } else {
                filePath = fileManager.getFilePath(DatabaseFile.STAFF.getFileName());
            }

            java.util.List<String> lines = new java.util.ArrayList<>();
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line;
            
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
            br.close();

            // Update the matching line
            BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
            for (int i = 0; i < lines.size(); i++) {
                if (i == 0) {
                    bw.write(lines.get(i) + "\n");
                    continue;
                }

                String[] data = lines.get(i).split(",");
                String filetpNumber = "";
                
                // For students: fullName,icNumber/passport,gender,dateOfBirth,nationality,race,address,contactNumber,tpNumber,password,intakeCode
                // For staff: fullName,icNumber/passport,gender,dateOfBirth,nationality,race,address,contactNumber,jobTitle,yearOfService,tpNumber,password,role,academicLeader
                // NOTE: Need at least 11 elements to have tpNumber at index 10 for staff
                
                if (role.equalsIgnoreCase("student") && data.length >= 11) {
                    filetpNumber = data[8].trim();
                } else if (!role.equalsIgnoreCase("student") && data.length >= 11) {
                    filetpNumber = data[10].trim();
                }

                if (filetpNumber.equals(tpNumber)) {
                    // Update fields
                    data[0] = nameField.getText().trim();        // fullName
                    data[7] = phoneField.getText().trim();       // contactNumber
                    data[6] = addressField.getText().trim();     // address
                    
                    bw.write(String.join(",", data) + "\n");
                } else {
                    bw.write(lines.get(i) + "\n");
                }
            }
            bw.close();

            // Update email in user.txt
            updateEmailInUserFile(emailField.getText().trim());
            
            // Update name in user.txt
            updateNameInUserFile(nameField.getText().trim());

            JOptionPane.showMessageDialog(this, "Profile updated successfully!");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update email in user.txt file
     */
    private void updateEmailInUserFile(String newEmail) {
        try {
            String userFilePath = fileManager.getFilePath(DatabaseFile.USER.getFileName());
            java.util.List<String> lines = new java.util.ArrayList<>();
            
            BufferedReader br = new BufferedReader(new FileReader(userFilePath));
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
            br.close();

            BufferedWriter bw = new BufferedWriter(new FileWriter(userFilePath));
            for (int i = 0; i < lines.size(); i++) {
                if (i == 0) {
                    bw.write(lines.get(i) + "\n");
                    continue;
                }

                String[] data = lines.get(i).split(",");
                // tpNumber,role,name,email,password
                if (data.length >= 5 && data[0].trim().equals(tpNumber)) {
                    data[3] = newEmail;  // Update email
                    bw.write(String.join(",", data) + "\n");
                } else {
                    bw.write(lines.get(i) + "\n");
                }
            }
            bw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Update name in user.txt file
     */
    private void updateNameInUserFile(String newName) {
        try {
            String userFilePath = fileManager.getFilePath(DatabaseFile.USER.getFileName());
            java.util.List<String> lines = new java.util.ArrayList<>();
            
            BufferedReader br = new BufferedReader(new FileReader(userFilePath));
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
            br.close();

            BufferedWriter bw = new BufferedWriter(new FileWriter(userFilePath));
            for (int i = 0; i < lines.size(); i++) {
                if (i == 0) {
                    bw.write(lines.get(i) + "\n");
                    continue;
                }

                String[] data = lines.get(i).split(",");
                // tpNumber,role,name,email,password
                if (data.length >= 5 && data[0].trim().equals(tpNumber)) {
                    data[2] = newName;  // Update name
                    bw.write(String.join(",", data) + "\n");
                } else {
                    bw.write(lines.get(i) + "\n");
                }
            }
            bw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void changePassword() {
        String currentPassword = new String(currentPasswordField.getPassword()).trim();
        String newPassword = new String(newPasswordField.getPassword()).trim();
        String confirmPassword = new String(confirmPasswordField.getPassword()).trim();

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all password fields!");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "New password and confirm password do not match!");
            return;
        }

        if (newPassword.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters long!");
            return;
        }

        try {
            // Verify current password in user.txt
            String userFilePath = fileManager.getFilePath(DatabaseFile.USER.getFileName());
            boolean passwordVerified = false;

            BufferedReader br = new BufferedReader(new FileReader(userFilePath));
            String line = br.readLine(); // Skip header
            
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 5 && data[0].trim().equals(tpNumber) && data[4].trim().equals(currentPassword)) {
                    passwordVerified = true;
                    break;
                }
            }
            br.close();

            if (!passwordVerified) {
                JOptionPane.showMessageDialog(this, "Current password is incorrect!");
                return;
            }

            // Update password in user.txt
            java.util.List<String> userLines = new java.util.ArrayList<>();
            br = new BufferedReader(new FileReader(userFilePath));
            while ((line = br.readLine()) != null) {
                userLines.add(line);
            }
            br.close();

            BufferedWriter bw = new BufferedWriter(new FileWriter(userFilePath));
            for (int i = 0; i < userLines.size(); i++) {
                if (i == 0) {
                    bw.write(userLines.get(i) + "\n");
                    continue;
                }

                String[] data = userLines.get(i).split(",");
                if (data.length >= 5 && data[0].trim().equals(tpNumber)) {
                    data[4] = newPassword;
                    bw.write(String.join(",", data) + "\n");
                } else {
                    bw.write(userLines.get(i) + "\n");
                }
            }
            bw.close();

            // Update password in studentinfo.txt or staffinfo.txt
            String dataFilePath;
            if (role.equalsIgnoreCase("student")) {
                dataFilePath = fileManager.getFilePath(DatabaseFile.STUDENT.getFileName());
            } else {
                dataFilePath = fileManager.getFilePath(DatabaseFile.STAFF.getFileName());
            }

            java.util.List<String> dataLines = new java.util.ArrayList<>();
            br = new BufferedReader(new FileReader(dataFilePath));
            while ((line = br.readLine()) != null) {
                dataLines.add(line);
            }
            br.close();

            bw = new BufferedWriter(new FileWriter(dataFilePath));
            for (int i = 0; i < dataLines.size(); i++) {
                if (i == 0) {
                    bw.write(dataLines.get(i) + "\n");
                    continue;
                }

                String[] data = dataLines.get(i).split(",");
                String filetpNumber = "";
                
                // For students: tpNumber is at index 8
                // For staff: tpNumber is at index 10
                if (role.equalsIgnoreCase("student") && data.length >= 11) {
                    filetpNumber = data[8].trim();
                } else if (data.length >= 14) {
                    filetpNumber = data[10].trim();
                }
                
                if (filetpNumber.equals(tpNumber)) {
                    // Update password at correct index
                    if (role.equalsIgnoreCase("student")) {
                        data[9] = newPassword;  // password at index 9 for students
                    } else {
                        data[11] = newPassword; // password at index 11 for staff
                    }
                    bw.write(String.join(",", data) + "\n");
                } else {
                    bw.write(dataLines.get(i) + "\n");
                }
            }
            bw.close();

            JOptionPane.showMessageDialog(this, "Password changed successfully!");
            
            // Clear password fields
            currentPasswordField.setText("");
            newPasswordField.setText("");
            confirmPasswordField.setText("");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error changing password: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Public method to reload profile data
     * Called when switching to Profile view in MainDashboard
     */
    public void loadUserData() {
        loadProfile();
    }

    private String getStudentIntake(String tpNumber) {
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.STUDENT.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine(); // Skip header
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] data = line.split(",");
                // fullName,icNumber/passport,gender,dateOfBirth,nationality,race,address,contactNumber,tpNumber,password,intakeCode
                if (data.length >= 11) {
                    String filetpNumber = data[8].trim();
                    String intake = data[10].trim();
                    
                    if (filetpNumber.equals(tpNumber)) {
                        br.close();
                        return intake;
                    }
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "N/A";
    }
}