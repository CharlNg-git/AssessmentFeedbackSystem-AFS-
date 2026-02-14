package Student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import util.FileManager;
import util.DatabaseFile;

public class ClassRegister extends JPanel {
    private String tpNumber;
    private String intakeCode;
    private JTable availableTable, registeredTable;
    private DefaultTableModel availableModel, registeredModel;
    private List<ClassInfo> availableClasses, registeredClasses;
    
    // Use FileManager instead of hardcoded path
    private final FileManager fileManager = new FileManager();

    public ClassRegister(String tpNumber) {
        this.tpNumber = tpNumber;
        
        System.out.println("=== ClassRegister Started ===");
        System.out.println("tpNumber received: " + tpNumber);
        
        this.intakeCode = getStudentIntake(tpNumber);
        
        System.out.println("Intake Code retrieved: " + intakeCode);
        System.out.println("========================");
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Header
        add(createHeader(), BorderLayout.NORTH);

        // Main Content
        JPanel mainPanel = new JPanel(new GridLayout(2, 1, 0, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // Available Classes Panel (Top)
        mainPanel.add(createAvailableClassesPanel());

        // Registered Classes Panel (Bottom)
        mainPanel.add(createRegisteredClassesPanel());

        add(mainPanel, BorderLayout.CENTER);

        // Load Data
        loadAvailableClasses();
        loadRegisteredClasses();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(255, 165, 0));
        header.setPreferredSize(new Dimension(0, 80));
        
        JLabel title = new JLabel("Class Registration System");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        header.add(title, BorderLayout.CENTER);
        
        JLabel userInfoLabel = new JLabel(tpNumber + " | Intake: " + intakeCode + "  ");
        userInfoLabel.setFont(new Font("Arial", Font.BOLD, 18));
        userInfoLabel.setForeground(Color.WHITE);
        header.add(userInfoLabel, BorderLayout.EAST);

        return header;

    }

    private JPanel createAvailableClassesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(255, 165, 0), 2),
            "Available Classes",
            0, 0, new Font("Arial", Font.BOLD, 16)));

        String[] columns = {"Class ID", "Module Name", "Intake", "Study Level", "Date", "Day", "Time", "Location", "Credits", "Status"};
        availableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        availableTable = new JTable(availableModel);
        availableTable.setRowHeight(35);
        availableTable.setFont(new Font("Arial", Font.PLAIN, 13));
        availableTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        availableTable.getTableHeader().setBackground(new Color(255, 165, 0));
        availableTable.getTableHeader().setForeground(Color.WHITE);
        availableTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        // Set column widths for better display
        availableTable.getColumnModel().getColumn(0).setPreferredWidth(90);   // Class ID
        availableTable.getColumnModel().getColumn(1).setPreferredWidth(220);  // Module Name
        availableTable.getColumnModel().getColumn(2).setPreferredWidth(90);   // Intake
        availableTable.getColumnModel().getColumn(3).setPreferredWidth(110);  // Study Level
        availableTable.getColumnModel().getColumn(4).setPreferredWidth(110);  // Date
        availableTable.getColumnModel().getColumn(5).setPreferredWidth(90);   // Day
        availableTable.getColumnModel().getColumn(6).setPreferredWidth(130);  // Time
        availableTable.getColumnModel().getColumn(7).setPreferredWidth(90);   // Location
        availableTable.getColumnModel().getColumn(8).setPreferredWidth(70);   // Credits
        availableTable.getColumnModel().getColumn(9).setPreferredWidth(90);   // Status
        
        JScrollPane scroll = new JScrollPane(availableTable);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(scroll, BorderLayout.CENTER);

        // Button Panel - Only Register button
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setBackground(Color.WHITE);
        
        JButton registerBtn = new JButton("Register Selected Class");
        registerBtn.setBackground(new Color(255, 165, 0));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setFont(new Font("Arial", Font.BOLD, 14));
        registerBtn.setFocusPainted(false);
        registerBtn.addActionListener(e -> registerClass());

        btnPanel.add(registerBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createRegisteredClassesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(34, 139, 34), 2),
            "My Registered Classes",
            0, 0, new Font("Arial", Font.BOLD, 16)));

        String[] columns = {"Class ID", "Module Name", "Intake", "Date", "Day", "Time", "Location", "Status"};
        registeredModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        registeredTable = new JTable(registeredModel);
        registeredTable.setRowHeight(35);
        registeredTable.setFont(new Font("Arial", Font.PLAIN, 13));
        registeredTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        registeredTable.getTableHeader().setBackground(new Color(34, 139, 34));
        registeredTable.getTableHeader().setForeground(Color.WHITE);
        registeredTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        // Set column widths for better display
        registeredTable.getColumnModel().getColumn(0).setPreferredWidth(100);  // Class ID
        registeredTable.getColumnModel().getColumn(1).setPreferredWidth(250); // Module Name
        registeredTable.getColumnModel().getColumn(2).setPreferredWidth(100);  // Intake
        registeredTable.getColumnModel().getColumn(3).setPreferredWidth(120); // Date
        registeredTable.getColumnModel().getColumn(4).setPreferredWidth(100);  // Day
        registeredTable.getColumnModel().getColumn(5).setPreferredWidth(150); // Time
        registeredTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Location
        registeredTable.getColumnModel().getColumn(7).setPreferredWidth(120);  // Status
        
        JScrollPane scroll = new JScrollPane(registeredTable);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(scroll, BorderLayout.CENTER);

        // Button Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setBackground(Color.WHITE);
        
        JButton dropBtn = new JButton("Drop Selected Class");
        dropBtn.setBackground(new Color(220, 20, 60));
        dropBtn.setForeground(Color.WHITE);
        dropBtn.setFont(new Font("Arial", Font.BOLD, 14));
        dropBtn.setFocusPainted(false);
        dropBtn.addActionListener(e -> dropClass());

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBackground(new Color(128, 128, 128));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFont(new Font("Arial", Font.BOLD, 14));
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> {
            loadAvailableClasses();
            loadRegisteredClasses();
        });

        btnPanel.add(dropBtn);
        btnPanel.add(refreshBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadAvailableClasses() {
        availableModel.setRowCount(0);
        availableClasses = new ArrayList<>();

        try {
            String filePath = fileManager.getFilePath(DatabaseFile.CLASSES.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine(); // Skip header
            
            System.out.println("Student ID: " + tpNumber);
            System.out.println("Intake Code: " + intakeCode);
            System.out.println("Reading classes from: " + filePath);

            // Get current date for filtering
            java.time.LocalDate currentDate = java.time.LocalDate.now();

            int count = 0;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] data = line.split(",");
                System.out.println("Line " + count++ + ": " + Arrays.toString(data));
                
                // classID,moduleID,IntakeCode,tpNumber,studyLevel,Date,classStart,classEnd,day,locationOrCampus,classStatus
                if (data.length >= 11) {
                    String classID = data[0].trim();
                    String moduleID = data[1].trim();
                    String intake = data[2].trim();
                    String studyLevel = data[4].trim();
                    String classDate = data[5].trim();
                    String startTime = data[6].trim();
                    String endTime = data[7].trim();
                    String day = data[8].trim();
                    String location = data[9].trim();
                    String classStatus = data[10].trim();

                    System.out.println("Comparing: '" + intake + "' with '" + intakeCode + "'");
                    
                    // Parse class date and compare with current date
                    try {
                        java.time.LocalDate classLocalDate = parseFlexibleDate(classDate);
                        
                        // Only show classes on or after current date
                        if (!classLocalDate.isBefore(currentDate)) {
                            // Check if intake matches (case-insensitive)
                            if (intake.equalsIgnoreCase(intakeCode)) {
                                // Check if already registered
                                if (!isAlreadyRegistered(classID)) {
                                    String moduleName = getModuleName(moduleID);
                                    int credits = getModuleCredits(moduleID);
                                    String timeSlot = startTime + " - " + endTime;

                                    ClassInfo classInfo = new ClassInfo(classID, moduleID, moduleName, intake, 
                                                                       studyLevel, day, startTime, endTime, location, 
                                                                       credits, classStatus, classDate);
                                    availableClasses.add(classInfo);

                                    availableModel.addRow(new Object[]{
                                        classID, moduleName, intake, studyLevel, classDate, day, timeSlot, location, credits, classStatus
                                    });
                                    
                                    System.out.println("Added class: " + classID + " - " + moduleName + " (Date: " + classDate + ")");
                                } else {
                                    System.out.println("Already registered: " + classID);
                                }
                            }
                        } else {
                            System.out.println("Skipping past class: " + classID + " (Date: " + classDate + " is before " + currentDate + ")");
                        }
                    } catch (Exception e) {
                        System.out.println("Error parsing date: " + classDate + " for class " + classID + " - " + e.getMessage());
                    }
                }
            }
            br.close();
            
            System.out.println("Total available classes loaded: " + availableClasses.size());
            
            
        } catch (FileNotFoundException e) {
            String filePath = fileManager.getFilePath(DatabaseFile.CLASSES.getFileName());
            JOptionPane.showMessageDialog(this, 
                "File not found: " + filePath + "\n" +
                "Please ensure the file exists in the correct location.",
                "File Not Found", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading available classes: " + e.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Parse date string that can be in various formats:
     * - 2026-02-12 (standard format)
     * - 2026-2-12 (single digit month/day)
     * - 2025-11-10 (mixed)
     */
    private java.time.LocalDate parseFlexibleDate(String dateStr) throws Exception {
        try {
            // Try standard format first
            return java.time.LocalDate.parse(dateStr, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e1) {
            try {
                // Try flexible format (handles single or double digits)
                String[] parts = dateStr.split("-");
                if (parts.length == 3) {
                    int year = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]);
                    int day = Integer.parseInt(parts[2]);
                    return java.time.LocalDate.of(year, month, day);
                }
                throw new Exception("Invalid date format: " + dateStr);
            } catch (Exception e2) {
                throw new Exception("Cannot parse date: " + dateStr);
            }
        }
    }

    private void loadRegisteredClasses() {
        registeredModel.setRowCount(0);
        registeredClasses = new ArrayList<>();

        try {
            String filePath = fileManager.getFilePath(DatabaseFile.CLASS_REGISTER.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            br.readLine(); // skip header
            String line;
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // classID,moduleID,intakeCode,tpNumber,date,classStart,classEnd,day,location,status
                String[] data = line.split(",");
                if (data.length >= 10 && data[3].trim().equals(tpNumber)) {
                    String classID = data[0].trim();
                    String moduleID = data[1].trim();
                    String intake = data[2].trim();
                    String date = data[4].trim();
                    String startTime = data[5].trim();
                    String endTime = data[6].trim();
                    String day = data[7].trim();
                    String location = data[8].trim();
                    String status = data[9].trim();

                    String moduleName = getModuleName(moduleID);
                    String timeSlot = startTime + " - " + endTime;

                    ClassInfo classInfo = new ClassInfo(classID, moduleID, moduleName, intake, 
                                                       "", day, startTime, endTime, location, 
                                                       0, status, date);
                    registeredClasses.add(classInfo);

                    registeredModel.addRow(new Object[]{
                        classID, moduleName, intake, date, day, timeSlot, location, status
                    });
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerClass() {
        int selectedRow = availableTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a class to register!");
            return;
        }

        ClassInfo selectedClass = availableClasses.get(selectedRow);

        try {
            String filePath = fileManager.getFilePath(DatabaseFile.CLASS_REGISTER.getFileName());
            BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true));
            String newEntry = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,Registered\n",
                selectedClass.classID, selectedClass.moduleID, intakeCode, tpNumber,
                selectedClass.date, selectedClass.startTime, selectedClass.endTime,
                selectedClass.day, selectedClass.location);
            bw.write(newEntry);
            bw.close();

            JOptionPane.showMessageDialog(this, "Class registered successfully!");
            loadAvailableClasses();
            loadRegisteredClasses();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error registering class: " + e.getMessage());
        }
    }

    private void dropClass() {
        int selectedRow = registeredTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a class to drop!");
            return;
        }

        ClassInfo selectedClass = registeredClasses.get(selectedRow);

        // Check if class date is in the past
        try {
            java.time.LocalDate currentDate = java.time.LocalDate.now();
            java.time.LocalDate classDate = parseFlexibleDate(selectedClass.date);
            
            if (classDate.isBefore(currentDate)) {
                JOptionPane.showMessageDialog(this, 
                    "Cannot drop this class!\n" +
                    "Class date (" + selectedClass.date + ") has already passed.\n" +
                    "You can only drop classes that are scheduled for today or future dates.",
                    "Cannot Drop Past Class", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (Exception e) {
            System.out.println("Error parsing date for drop validation: " + selectedClass.date);
            JOptionPane.showMessageDialog(this, 
                "Error validating class date: " + e.getMessage(),
                "Date Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to drop this class?\n" +
            "Class: " + selectedClass.moduleName + "\n" +
            "Date: " + selectedClass.date + " (" + selectedClass.day + ")\n" +
            "Time: " + selectedClass.startTime + " - " + selectedClass.endTime,
            "Confirm Drop", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String filePath = fileManager.getFilePath(DatabaseFile.CLASS_REGISTER.getFileName());
                List<String> lines = new ArrayList<>();
                BufferedReader br = new BufferedReader(new FileReader(filePath));
                String line;
                while ((line = br.readLine()) != null) {
                    lines.add(line);
                }
                br.close();

                BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
                for (String l : lines) {
                    if (!l.contains(selectedClass.classID + "," + selectedClass.moduleID) || !l.contains(tpNumber)) {
                        bw.write(l + "\n");
                    }
                }
                bw.close();

                JOptionPane.showMessageDialog(this, "Class dropped successfully!");
                loadAvailableClasses();
                loadRegisteredClasses();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error dropping class: " + e.getMessage());
            }
        }
    }

    private boolean timesOverlap(ClassInfo c1, ClassInfo c2) {
        int start1 = Integer.parseInt(c1.startTime.replace(":", ""));
        int end1 = Integer.parseInt(c1.endTime.replace(":", ""));
        int start2 = Integer.parseInt(c2.startTime.replace(":", ""));
        int end2 = Integer.parseInt(c2.endTime.replace(":", ""));

        return (start1 < end2 && start2 < end1);
    }

    private boolean isAlreadyRegistered(String classID) {
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.CLASS_REGISTER.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 4 && data[0].trim().equals(classID) && data[3].trim().equals(tpNumber)) {
                    br.close();
                    return true;
                }
            }
            br.close();
        } catch (Exception e) {
            // File might not exist yet
        }
        return false;
    }

    private String getStudentIntake(String tpNumber) {
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.STUDENT.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine(); // Skip header
            
            System.out.println("Looking for student: " + tpNumber);
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] data = line.split(",");
                System.out.println("Checking student line: " + Arrays.toString(data));
                
                if (data.length >= 11) {
                    String filetpNumber = data[8].trim();
                    String intake = data[10].trim();
                    
                    System.out.println("Comparing: '" + filetpNumber + "' with '" + tpNumber + "'");
                    
                    if (filetpNumber.equals(tpNumber)) {
                        System.out.println("FOUND! Intake: " + intake);
                        br.close();
                        return intake;
                    }
                }
            }
            br.close();
            
            System.out.println("Student not found in studentinfo.txt!");
        } catch (Exception e) {
            System.out.println("Error reading studentinfo.txt: " + e.getMessage());
            e.printStackTrace();
        }
        return "Unknown";
    }

    private String getModuleName(String moduleID) {
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.MODULE.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 2 && data[0].trim().equals(moduleID)) {
                    br.close();
                    return data[1].trim();
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return moduleID;
    }

    private int getModuleCredits(String moduleID) {
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.MODULE.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 3 && data[0].trim().equals(moduleID)) {
                    br.close();
                    return Integer.parseInt(data[2].trim());
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 3;
    }

    class ClassInfo {
        String classID, moduleID, moduleName, day, startTime, endTime, location, intake, studyLevel, status, date;
        int credits;

        ClassInfo(String classID, String moduleID, String moduleName, String intake, String studyLevel,
                 String day, String startTime, String endTime, String location, int credits, String status, String date) {
            this.classID = classID;
            this.moduleID = moduleID;
            this.moduleName = moduleName;
            this.intake = intake;
            this.studyLevel = studyLevel;
            this.day = day;
            this.startTime = startTime;
            this.endTime = endTime;
            this.location = location;
            this.credits = credits;
            this.status = status;
            this.date = date;
        }
    }
}