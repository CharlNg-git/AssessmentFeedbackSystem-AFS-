package Admin.view;

import util.FileManager;
import util.DatabaseFile;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CreateClass extends JDialog {
    private MainDashboard parentDashboard;
    private ManageClass manageClassPanel;
    private FileManager fileManager = new FileManager();
    
    private JComboBox<String> cmbModuleAssignment;
    private JTextField txtModuleID, txtModuleName, txtIntakeCode, txtLecturerID;
    private JTextField txtStudyLevel, txtDate, txtStartTime, txtEndTime, txtDay, txtLocation;
    private JComboBox<String> cmbStatus;
    
    private Map<String, ModuleAssignment> assignmentMap = new LinkedHashMap<>();
    private java.util.List<String> assignmentKeys = new ArrayList<>();

    public CreateClass(MainDashboard parentDashboard, ManageClass manageClassPanel) {
        super(parentDashboard, "Create New Class", true);
        this.parentDashboard = parentDashboard;
        this.manageClassPanel = manageClassPanel;
        
        setSize(650, 750);
        setLocationRelativeTo(parentDashboard);
        setLayout(new BorderLayout());
        
        loadModuleAssignments();
        
        add(createHeader(), BorderLayout.NORTH);
        add(createForm(), BorderLayout.CENTER);
        add(createButtons(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setBackground(new Color(255, 165, 0));
        header.setPreferredSize(new Dimension(0, 60));
        
        JLabel title = new JLabel("  Create New Class");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        header.add(title);
        
        return header;
    }

    private JPanel createForm() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        
        int row = 0;
        
        // Module Assignment Selection (THIS IS THE ONLY FIELD ADMIN CHANGES)
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel lblAssignment = new JLabel("Select Module Assignment:");
        lblAssignment.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(lblAssignment, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.7;
        cmbModuleAssignment = new JComboBox<>();
        cmbModuleAssignment.setPreferredSize(new Dimension(300, 35));
        cmbModuleAssignment.setFont(new Font("Arial", Font.PLAIN, 13));
        
        for (String key : assignmentKeys) {
            cmbModuleAssignment.addItem(key);
        }
        
        cmbModuleAssignment.addActionListener(e -> updateFormFields());
        formPanel.add(cmbModuleAssignment, gbc);
        
        row++;
        
        // Separator
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        JSeparator sep1 = new JSeparator();
        formPanel.add(sep1, gbc);
        gbc.gridwidth = 1;
        row++;
        
        // AUTO-FILLED FIELDS (Read-only, greyed out)
        
        // Module ID
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(new JLabel("Module ID:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtModuleID = createReadOnlyField();
        formPanel.add(txtModuleID, gbc);
        row++;
        
        // Module Name
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(new JLabel("Module Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtModuleName = createReadOnlyField();
        formPanel.add(txtModuleName, gbc);
        row++;
        
        // Intake Code
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Intake Code:"), gbc);
        gbc.gridx = 1;
        txtIntakeCode = createReadOnlyField();
        formPanel.add(txtIntakeCode, gbc);
        row++;
        
        // Lecturer ID
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Lecturer ID:"), gbc);
        gbc.gridx = 1;
        txtLecturerID = createReadOnlyField();
        formPanel.add(txtLecturerID, gbc);
        row++;
        
        // Study Level
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Study Level:"), gbc);
        gbc.gridx = 1;
        txtStudyLevel = createReadOnlyField();
        formPanel.add(txtStudyLevel, gbc);
        row++;
        
        // Separator
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        JSeparator sep2 = new JSeparator();
        formPanel.add(sep2, gbc);
        gbc.gridwidth = 1;
        row++;
        
        // EDITABLE CLASS DETAILS
        
        // Date (Editable)
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Date (yyyy-MM-dd):"), gbc);
        gbc.gridx = 1;
        txtDate = new JTextField();
        txtDate.setPreferredSize(new Dimension(300, 35));
        txtDate.setFont(new Font("Arial", Font.PLAIN, 13));
        txtDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        // Add listener to auto-update day when date changes
        txtDate.addActionListener(e -> updateDayFromDate());
        txtDate.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                updateDayFromDate();
            }
        });
        formPanel.add(txtDate, gbc);
        row++;
        
        // Day (Auto-updated from date, read-only)
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Day (Auto):"), gbc);
        gbc.gridx = 1;
        txtDay = createReadOnlyField();
        txtDay.setText(getDayFromDate(LocalDate.now()));
        formPanel.add(txtDay, gbc);
        row++;
        
        // Start Time (Editable)
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Start Time (HH:mm):"), gbc);
        gbc.gridx = 1;
        txtStartTime = new JTextField();
        txtStartTime.setPreferredSize(new Dimension(300, 35));
        txtStartTime.setFont(new Font("Arial", Font.PLAIN, 13));
        txtStartTime.setText("09:00");
        formPanel.add(txtStartTime, gbc);
        row++;
        
        // End Time (Editable)
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("End Time (HH:mm):"), gbc);
        gbc.gridx = 1;
        txtEndTime = new JTextField();
        txtEndTime.setPreferredSize(new Dimension(300, 35));
        txtEndTime.setFont(new Font("Arial", Font.PLAIN, 13));
        txtEndTime.setText("11:00");
        formPanel.add(txtEndTime, gbc);
        row++;
        
        // Location (Editable)
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Location:"), gbc);
        gbc.gridx = 1;
        txtLocation = new JTextField();
        txtLocation.setPreferredSize(new Dimension(300, 35));
        txtLocation.setFont(new Font("Arial", Font.PLAIN, 13));
        txtLocation.setText("A-1-1");
        formPanel.add(txtLocation, gbc);
        row++;
        
        // Status
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Class Status:"), gbc);
        gbc.gridx = 1;
        cmbStatus = new JComboBox<>(new String[]{"Active", "Inactive", "Completed", "Cancelled"});
        cmbStatus.setPreferredSize(new Dimension(300, 35));
        cmbStatus.setFont(new Font("Arial", Font.PLAIN, 13));
        formPanel.add(cmbStatus, gbc);
        row++;
        
        // Info Label
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        JLabel infoLabel = new JLabel("<html><i>Note: Module info is auto-filled. Date, time, and location can be edited.</i></html>");
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        infoLabel.setForeground(Color.GRAY);
        formPanel.add(infoLabel, gbc);
        
        return formPanel;
    }

    private JTextField createReadOnlyField() {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(300, 35));
        field.setFont(new Font("Arial", Font.PLAIN, 13));
        field.setEditable(false);
        field.setBackground(new Color(240, 240, 240));
        field.setForeground(Color.DARK_GRAY);
        return field;
    }

    private JPanel createButtons() {
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        btnPanel.setBackground(Color.WHITE);
        
        JButton btnSave = new JButton("Create Class");
        btnSave.setPreferredSize(new Dimension(150, 40));
        btnSave.setBackground(new Color(46, 204, 113)); // Green
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Arial", Font.BOLD, 14));
        btnSave.setFocusPainted(false);
        btnSave.addActionListener(e -> saveClass());
        
        JButton btnCancel = new JButton("Cancel");
        btnCancel.setPreferredSize(new Dimension(150, 40));
        btnCancel.setBackground(new Color(231, 76, 60)); // Red
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFont(new Font("Arial", Font.BOLD, 14));
        btnCancel.setFocusPainted(false);
        btnCancel.addActionListener(e -> dispose());
        
        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);
        
        return btnPanel;
    }

    private void loadModuleAssignments() {
        System.out.println("=== Loading Module Assignments ===");
        
        // First, load module details
        Map<String, ModuleInfo> moduleInfoMap = new HashMap<>();
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.MODULE.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine(); // Skip header
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] data = line.split(",");
                // Format: moduleID,moduleName,creditHour,sessionType,Level,tpNumber,gradingID (7 cols)
                // OR OLD: moduleID,moduleName,creditHour,sessionType,tpNumber (5 cols)
                if (data.length >= 5) {
                    String moduleID = data[0].trim();
                    String moduleName = data[1].trim();
                    String level = data.length >= 7 ? data[4].trim() : "Degree"; // Default to Degree
                    
                    moduleInfoMap.put(moduleID, new ModuleInfo(moduleID, moduleName, level));
                    System.out.println("  Module: " + moduleID + " - " + moduleName + " (" + level + ")");
                }
            }
            br.close();
        } catch (Exception e) {
            System.err.println("Error loading modules: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Now load module assignments
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.MODULE_ASSIGNMENTS.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine(); // Skip header
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] data = line.split(",");
                // Format: moduleID,lecturerID,intakeCode
                if (data.length >= 3) {
                    String moduleID = data[0].trim();
                    String lecturerID = data[1].trim();
                    String intakeCode = data[2].trim();
                    
                    if (moduleInfoMap.containsKey(moduleID)) {
                        ModuleInfo info = moduleInfoMap.get(moduleID);
                        String key = moduleID + " - " + info.moduleName + " [" + intakeCode + " | " + lecturerID + "]";
                        
                        ModuleAssignment assignment = new ModuleAssignment(
                            moduleID, info.moduleName, lecturerID, intakeCode, info.level
                        );
                        
                        assignmentMap.put(key, assignment);
                        assignmentKeys.add(key);
                        
                        System.out.println("  Assignment: " + key);
                    }
                }
            }
            br.close();
            
            System.out.println("Total assignments loaded: " + assignmentMap.size());
            
        } catch (Exception e) {
            System.err.println("Error loading module assignments: " + e.getMessage());
            e.printStackTrace();
        }
        
        if (assignmentKeys.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No module assignments found!\nPlease create module assignments first.",
                "No Assignments", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void updateFormFields() {
        String selected = (String) cmbModuleAssignment.getSelectedItem();
        if (selected == null || !assignmentMap.containsKey(selected)) {
            return;
        }
        
        ModuleAssignment assignment = assignmentMap.get(selected);
        
        txtModuleID.setText(assignment.moduleID);
        txtModuleName.setText(assignment.moduleName);
        txtIntakeCode.setText(assignment.intakeCode);
        txtLecturerID.setText(assignment.lecturerID);
        txtStudyLevel.setText(assignment.level);
    }

    private void saveClass() {
        // Validate selection
        if (cmbModuleAssignment.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a module assignment!");
            return;
        }
        
        String selected = (String) cmbModuleAssignment.getSelectedItem();
        ModuleAssignment assignment = assignmentMap.get(selected);
        
        if (assignment == null) {
            JOptionPane.showMessageDialog(this, "Invalid module assignment!");
            return;
        }
        
        // Validate date format
        String dateStr = txtDate.getText().trim();
        try {
            LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid date format! Use yyyy-MM-dd (e.g., 2026-02-11)");
            return;
        }
        
        // Validate time format
        String startTime = txtStartTime.getText().trim();
        String endTime = txtEndTime.getText().trim();
        if (!startTime.matches("\\d{2}:\\d{2}") || !endTime.matches("\\d{2}:\\d{2}")) {
            JOptionPane.showMessageDialog(this, "Invalid time format! Use HH:mm (e.g., 09:00)");
            return;
        }
        
        // Validate location
        String location = txtLocation.getText().trim();
        if (location.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a location!");
            return;
        }
        
        // Generate Class ID
        String classID = generateClassID();
        
        // Get values
        String moduleID = assignment.moduleID;
        String intakeCode = assignment.intakeCode;
        String lecturerID = assignment.lecturerID;
        String studyLevel = assignment.level;
        String day = txtDay.getText().trim();
        String status = (String) cmbStatus.getSelectedItem();
        
        // Format: classID,moduleID,IntakeCode,tpNumber,studyLevel,Date,classStart,classEnd,day,locationOrCampus,classStatus
        String newLine = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
            classID, moduleID, intakeCode, lecturerID, studyLevel,
            dateStr, startTime, endTime, day, location, status
        );
        
        try {
            // Check if file exists, if not create with header
            File file = new File(fileManager.getFilePath(DatabaseFile.CLASSES.getFileName()));
            boolean fileExists = file.exists();
            
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            
            if (!fileExists) {
                // Write header
                bw.write("classID,moduleID,intakeCode,tpNumber,studyLevel,date,classStart,classEnd,day,location,status");
                bw.newLine();
            }
            
            bw.write(newLine);
            bw.newLine();
            bw.close();
            
            JOptionPane.showMessageDialog(this, 
                "Class created successfully!\nClass ID: " + classID,
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
            manageClassPanel.refreshData();
            dispose();
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Error saving class: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void updateDayFromDate() {
        String dateStr = txtDate.getText().trim();
        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String dayName = getDayFromDate(date);
            txtDay.setText(dayName);
        } catch (Exception e) {
            // Invalid date format, keep current day
        }
    }
    
    private String getDayFromDate(LocalDate date) {
        switch (date.getDayOfWeek()) {
            case MONDAY: return "Monday";
            case TUESDAY: return "Tuesday";
            case WEDNESDAY: return "Wednesday";
            case THURSDAY: return "Thursday";
            case FRIDAY: return "Friday";
            case SATURDAY: return "Saturday";
            case SUNDAY: return "Sunday";
            default: return "Monday";
        }
    }

    private String generateClassID() {
        String prefix = "C";
        int maxNum = 0;
        
        try {
            BufferedReader br = new BufferedReader(
                new FileReader(fileManager.getFilePath(DatabaseFile.CLASSES.getFileName()))
            );
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length > 0 && data[0].startsWith(prefix)) {
                    try {
                        int num = Integer.parseInt(data[0].substring(prefix.length()));
                        if (num > maxNum) maxNum = num;
                    } catch (NumberFormatException e) {}
                }
            }
            br.close();
        } catch (IOException e) {
            // File doesn't exist yet, start from 1
        }
        
        return String.format("%s%04d", prefix, maxNum + 1);
    }

    // Inner classes for data structure
    class ModuleInfo {
        String moduleID, moduleName, level;
        
        ModuleInfo(String moduleID, String moduleName, String level) {
            this.moduleID = moduleID;
            this.moduleName = moduleName;
            this.level = level;
        }
    }

    class ModuleAssignment {
        String moduleID, moduleName, lecturerID, intakeCode, level;
        
        ModuleAssignment(String moduleID, String moduleName, String lecturerID, 
                        String intakeCode, String level) {
            this.moduleID = moduleID;
            this.moduleName = moduleName;
            this.lecturerID = lecturerID;
            this.intakeCode = intakeCode;
            this.level = level;
        }
    }
}