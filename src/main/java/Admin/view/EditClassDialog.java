package Admin.view;

import util.FileManager;
import util.DatabaseFile;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EditClassDialog extends JDialog {
    private MainDashboard parentDashboard;
    private ManageClass manageClassPanel;
    private FileManager fileManager = new FileManager();
    private String classID;
    
    private JTextField txtDate, txtStartTime, txtEndTime, txtLocation;
    private JLabel txtDay; // Read-only, auto-updated from date
    
    // Store full class data
    private String moduleID, intakeCode, lecturerID, studyLevel, status;

    public EditClassDialog(MainDashboard parentDashboard, ManageClass manageClassPanel, String classID) {
        super(parentDashboard, "Edit Class", true);
        this.parentDashboard = parentDashboard;
        this.manageClassPanel = manageClassPanel;
        this.classID = classID;
        
        setSize(600, 500);
        setLocationRelativeTo(parentDashboard);
        setLayout(new BorderLayout());
        
        // IMPORTANT: Create UI components FIRST
        JPanel header = createHeader();
        JPanel form = createForm();
        JPanel buttons = createButtons();
        
        add(header, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        
        // THEN load data into those components
        loadClassData();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setBackground(new Color(255, 165, 0));
        header.setPreferredSize(new Dimension(0, 60));
        
        JLabel title = new JLabel("  Edit Class: " + classID);
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
        gbc.insets = new Insets(10, 10, 10, 10);
        
        int row = 0;
        
        // Date
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel lblDate = new JLabel("Date (yyyy-MM-dd):");
        lblDate.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(lblDate, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtDate = new JTextField();
        txtDate.setPreferredSize(new Dimension(300, 35));
        txtDate.setFont(new Font("Arial", Font.PLAIN, 13));
        txtDate.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                updateDayFromDate();
            }
        });
        formPanel.add(txtDate, gbc);
        row++;
        
        // Day (Auto-updated, read-only)
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lblDay = new JLabel("Day (Auto):");
        lblDay.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(lblDay, gbc);
        
        gbc.gridx = 1;
        txtDay = new JLabel();
        txtDay.setFont(new Font("Arial", Font.PLAIN, 13));
        txtDay.setOpaque(true);
        txtDay.setBackground(new Color(240, 240, 240));
        txtDay.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        formPanel.add(txtDay, gbc);
        row++;
        
        // Start Time
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lblStartTime = new JLabel("Start Time (HH:mm):");
        lblStartTime.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(lblStartTime, gbc);
        
        gbc.gridx = 1;
        txtStartTime = new JTextField();
        txtStartTime.setPreferredSize(new Dimension(300, 35));
        txtStartTime.setFont(new Font("Arial", Font.PLAIN, 13));
        formPanel.add(txtStartTime, gbc);
        row++;
        
        // End Time
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lblEndTime = new JLabel("End Time (HH:mm):");
        lblEndTime.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(lblEndTime, gbc);
        
        gbc.gridx = 1;
        txtEndTime = new JTextField();
        txtEndTime.setPreferredSize(new Dimension(300, 35));
        txtEndTime.setFont(new Font("Arial", Font.PLAIN, 13));
        formPanel.add(txtEndTime, gbc);
        row++;
        
        // Location
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lblLocation = new JLabel("Location:");
        lblLocation.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(lblLocation, gbc);
        
        gbc.gridx = 1;
        txtLocation = new JTextField();
        txtLocation.setPreferredSize(new Dimension(300, 35));
        txtLocation.setFont(new Font("Arial", Font.PLAIN, 13));
        formPanel.add(txtLocation, gbc);
        row++;
        
        // Info Label
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        JLabel infoLabel = new JLabel("<html><i>Note: You can only edit date, time, and location.</i></html>");
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        infoLabel.setForeground(Color.GRAY);
        formPanel.add(infoLabel, gbc);
        
        return formPanel;
    }

    private JPanel createButtons() {
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        btnPanel.setBackground(Color.WHITE);
        
        JButton btnSave = new JButton("Save Changes");
        btnSave.setPreferredSize(new Dimension(150, 40));
        btnSave.setBackground(new Color(46, 204, 113)); // Green
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Arial", Font.BOLD, 14));
        btnSave.setFocusPainted(false);
        btnSave.addActionListener(e -> saveChanges());
        
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

    private void loadClassData() {
        System.out.println("=== Loading Class Data for Edit: " + classID + " ===");
        System.out.println("txtDate null? " + (txtDate == null));
        System.out.println("txtDay null? " + (txtDay == null));
        System.out.println("txtStartTime null? " + (txtStartTime == null));
        System.out.println("txtEndTime null? " + (txtEndTime == null));
        System.out.println("txtLocation null? " + (txtLocation == null));
        
        if (txtDate == null || txtDay == null || txtStartTime == null || txtEndTime == null || txtLocation == null) {
            System.err.println("ERROR: UI components not initialized before loadClassData() was called!");
            JOptionPane.showMessageDialog(this, 
                "Internal Error: UI not initialized.\nPlease recompile the project.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.CLASSES.getFileName())));
            String line = br.readLine(); // Skip header
            
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                // Format: classID,moduleID,IntakeCode,tpNumber,studyLevel,Date,classStart,classEnd,day,locationOrCampus,classStatus
                if (data.length >= 11 && data[0].trim().equals(classID)) {
                    moduleID = data[1].trim();
                    intakeCode = data[2].trim();
                    lecturerID = data[3].trim();
                    studyLevel = data[4].trim();
                    String date = data[5].trim();
                    String startTime = data[6].trim();
                    String endTime = data[7].trim();
                    String day = data[8].trim();
                    String location = data[9].trim();
                    status = data[10].trim();
                    
                    System.out.println("  Found class: " + classID);
                    System.out.println("  Date: " + date);
                    System.out.println("  Time: " + startTime + " - " + endTime);
                    System.out.println("  Day: " + day);
                    System.out.println("  Location: " + location);
                    
                    // Populate fields
                    txtDate.setText(date);
                    txtDay.setText(day);
                    txtStartTime.setText(startTime);
                    txtEndTime.setText(endTime);
                    txtLocation.setText(location);
                    
                    break;
                }
            }
            br.close();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading class data: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
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

    private void saveChanges() {
        // Validate date
        String dateStr = txtDate.getText().trim();
        try {
            LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid date format! Use yyyy-MM-dd");
            return;
        }
        
        // Validate times
        String startTime = txtStartTime.getText().trim();
        String endTime = txtEndTime.getText().trim();
        if (!startTime.matches("\\d{2}:\\d{2}") || !endTime.matches("\\d{2}:\\d{2}")) {
            JOptionPane.showMessageDialog(this, "Invalid time format! Use HH:mm");
            return;
        }
        
        // Validate location
        String location = txtLocation.getText().trim();
        if (location.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a location!");
            return;
        }
        
        String day = txtDay.getText();
        
        // Update the file
        try {
            List<String> lines = new ArrayList<>();
            BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.CLASSES.getFileName())));
            String line;
            
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 11 && data[0].trim().equals(classID)) {
                    // Reconstruct line with new values
                    // Format: classID,moduleID,IntakeCode,tpNumber,studyLevel,Date,classStart,classEnd,day,locationOrCampus,classStatus
                    String newLine = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                        classID, moduleID, intakeCode, lecturerID, studyLevel,
                        dateStr, startTime, endTime, day, location, status);
                    lines.add(newLine);
                } else {
                    lines.add(line);
                }
            }
            br.close();
            
            // Write back
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileManager.getFilePath(DatabaseFile.CLASSES.getFileName())));
            for (String l : lines) {
                bw.write(l);
                bw.newLine();
            }
            bw.close();
            
            JOptionPane.showMessageDialog(this, 
                "Class updated successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
            manageClassPanel.refreshData();
            dispose();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error saving changes: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}