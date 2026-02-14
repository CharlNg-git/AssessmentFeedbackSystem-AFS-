package Student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import util.FileManager;
import util.DatabaseFile;

public class AttendanceViewer extends JPanel {
    private String tpNumber;
    private String intakeCode;
    private JTable attendanceTable;
    private DefaultTableModel tableModel;
    private JLabel overallPercentLabel, presentCountLabel, absentCountLabel, totalClassesLabel;
    private List<AttendanceRecord> attendanceRecords;
    private Map<String, ModuleAttendance> moduleAttendanceMap;
    
    // Use FileManager instead of hardcoded path
    private final FileManager fileManager = new FileManager();

    public AttendanceViewer(String tpNumber) {
        this.tpNumber = tpNumber;
        this.intakeCode = getStudentIntake(tpNumber);
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Header
        add(createHeader(), BorderLayout.NORTH);

        // Main Content
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // Summary Panel
        mainPanel.add(createSummaryPanel(), BorderLayout.NORTH);

        // Attendance Table
        mainPanel.add(createAttendanceTablePanel(), BorderLayout.CENTER);

        // Module Statistics
        mainPanel.add(createModuleStatsPanel(), BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        // Load Data
        loadAttendance();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(255, 165, 0));
        header.setPreferredSize(new Dimension(0, 80));

        JLabel title = new JLabel("  Attendance Tracker & Statistics");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        JLabel userInfoLabel = new JLabel(tpNumber + " | Intake: " + intakeCode + "  ");
        userInfoLabel.setFont(new Font("Arial", Font.BOLD, 18));
        userInfoLabel.setForeground(Color.WHITE);
        header.add(userInfoLabel, BorderLayout.EAST);

        return header;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 20, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));

        // Overall Attendance Percentage
        overallPercentLabel = new JLabel("0%");
        JPanel percentCard = createSummaryCard("Overall Attendance", overallPercentLabel, new Color(255, 165, 0));
        panel.add(percentCard);

        // Total Classes
        totalClassesLabel = new JLabel("0");
        JPanel totalCard = createSummaryCard("Total Classes", totalClassesLabel, new Color(70, 130, 180));
        panel.add(totalCard);

        // Present Count
        presentCountLabel = new JLabel("0");
        JPanel presentCard = createSummaryCard("Present", presentCountLabel, new Color(34, 139, 34));
        panel.add(presentCard);

        // Absent Count
        absentCountLabel = new JLabel("0");
        JPanel absentCard = createSummaryCard("Absent", absentCountLabel, new Color(220, 20, 60));
        panel.add(absentCard);

        return panel;
    }

    private JPanel createSummaryCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 3),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        titleLabel.setForeground(Color.GRAY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        valueLabel.setFont(new Font("Arial", Font.BOLD, 32));
        valueLabel.setForeground(color);

        JPanel valuePanel = new JPanel();
        valuePanel.setBackground(Color.WHITE);
        valuePanel.add(valueLabel);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(valuePanel);

        return card;
    }

    private JPanel createAttendanceTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);

        String[] columns = {"Date", "Class ID", "Module", "Day", "Time", "Location", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        attendanceTable = new JTable(tableModel);
        attendanceTable.setRowHeight(30);
        attendanceTable.setFont(new Font("Arial", Font.PLAIN, 13));
        attendanceTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        attendanceTable.getTableHeader().setBackground(new Color(255, 165, 0));
        attendanceTable.getTableHeader().setForeground(Color.WHITE);

        // Custom cell renderer for status column
        attendanceTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = value.toString();
                
                if (!isSelected) {
                    if (status.equalsIgnoreCase("present")) {
                        c.setBackground(new Color(144, 238, 144));
                        c.setForeground(Color.BLACK);
                    } else if (status.equalsIgnoreCase("absent")) {
                        c.setBackground(new Color(255, 182, 193));
                        c.setForeground(Color.BLACK);
                    } else {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                    }
                }
                
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Arial", Font.BOLD, 12));
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(attendanceTable);
        scroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(255, 165, 0), 2),
            "Attendance Records",
            0, 0, new Font("Arial", Font.BOLD, 16)));

        panel.add(scroll, BorderLayout.CENTER);

        // Button Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(Color.WHITE);

        JButton filterBtn = new JButton("Filter by Module");
        filterBtn.setBackground(new Color(70, 130, 180));
        filterBtn.setForeground(Color.WHITE);
        filterBtn.setFont(new Font("Arial", Font.BOLD, 14));
        filterBtn.setFocusPainted(false);
        filterBtn.addActionListener(e -> filterByModule());

        JButton exportBtn = new JButton("Export Report");
        exportBtn.setBackground(new Color(34, 139, 34));
        exportBtn.setForeground(Color.WHITE);
        exportBtn.setFont(new Font("Arial", Font.BOLD, 14));
        exportBtn.setFocusPainted(false);
        exportBtn.addActionListener(e -> exportReport());

        JButton showAllBtn = new JButton("Show All");
        showAllBtn.setBackground(new Color(128, 128, 128));
        showAllBtn.setForeground(Color.WHITE);
        showAllBtn.setFont(new Font("Arial", Font.BOLD, 14));
        showAllBtn.setFocusPainted(false);
        showAllBtn.addActionListener(e -> loadAttendance());

        btnPanel.add(filterBtn);
        btnPanel.add(exportBtn);
        btnPanel.add(showAllBtn);

        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createModuleStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 0, 15, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
            "Attendance by Module",
            0, 0, new Font("Arial", Font.BOLD, 16)));
        panel.setPreferredSize(new Dimension(0, 150));

        return panel;
    }

    private void loadAttendance() {
        tableModel.setRowCount(0);
        attendanceRecords = new ArrayList<>();
        moduleAttendanceMap = new HashMap<>();

        int totalPresent = 0;
        int totalAbsent = 0;

        try {
            String filePath = fileManager.getFilePath(DatabaseFile.ATTENDANCE.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine();
            
            System.out.println("=== Loading Attendance for tpNumber: " + tpNumber + " ===");

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] data = line.split(",");
                System.out.println("Attendance line: " + Arrays.toString(data));
                
                if (data.length >= 4 && data[0].trim().equals(tpNumber)) {
                    String moduleID = data[1].trim();
                    String classID = data[2].trim();
                    String status = data[3].trim();

                    System.out.println("✓ MATCHED - Processing attendance: tpNumber=" + tpNumber + ", classID=" + classID + ", moduleID=" + moduleID + ", status=" + status);

                    ClassDetails details = getClassDetails(classID);
                    
                    AttendanceRecord record = new AttendanceRecord(
                        details.date, classID, moduleID, details.moduleName,
                        details.day, details.time, details.location, status
                    );
                    attendanceRecords.add(record);

                    tableModel.addRow(new Object[]{
                        details.date, classID, details.moduleName, details.day,
                        details.time, details.location, status.toUpperCase()
                    });

                    if (status.equalsIgnoreCase("present")) {
                        totalPresent++;
                    } else {
                        totalAbsent++;
                    }

                    if (!moduleAttendanceMap.containsKey(moduleID)) {
                        moduleAttendanceMap.put(moduleID, new ModuleAttendance(details.moduleName));
                        System.out.println("Added module to map: " + moduleID + " -> " + details.moduleName);
                    }
                    ModuleAttendance modAtt = moduleAttendanceMap.get(moduleID);
                    if (status.equalsIgnoreCase("present")) {
                        modAtt.presentCount++;
                    } else {
                        modAtt.absentCount++;
                    }
                }
            }
            br.close();

            System.out.println("Total modules in map: " + moduleAttendanceMap.size());
            for (Map.Entry<String, ModuleAttendance> entry : moduleAttendanceMap.entrySet()) {
                System.out.println("  Module ID: " + entry.getKey() + " -> " + entry.getValue().moduleName);
            }

            int totalClasses = totalPresent + totalAbsent;
            double percentage = totalClasses > 0 ? (totalPresent * 100.0 / totalClasses) : 0;

            overallPercentLabel.setText(String.format("%.1f%%", percentage));
            totalClassesLabel.setText(String.valueOf(totalClasses));
            presentCountLabel.setText(String.valueOf(totalPresent));
            absentCountLabel.setText(String.valueOf(totalAbsent));

            Color percentColor = percentage >= 75 ? new Color(34, 139, 34) :
                               percentage >= 60 ? new Color(255, 165, 0) :
                               new Color(220, 20, 60);
            overallPercentLabel.setForeground(percentColor);

            updateModuleStatsPanel();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading attendance: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateModuleStatsPanel() {
        Component[] components = getComponents();
        JPanel mainPanel = null;
        for (Component c : components) {
            if (c instanceof JPanel && c != getComponent(0)) {
                mainPanel = (JPanel) c;
                break;
            }
        }
        
        if (mainPanel == null) return;
        
        JPanel statsPanel = (JPanel) mainPanel.getComponent(2);
        statsPanel.removeAll();

        for (Map.Entry<String, ModuleAttendance> entry : moduleAttendanceMap.entrySet()) {
            ModuleAttendance ma = entry.getValue();
            int total = ma.presentCount + ma.absentCount;
            double percent = total > 0 ? (ma.presentCount * 100.0 / total) : 0;

            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

            JLabel moduleLabel = new JLabel(ma.moduleName);
            moduleLabel.setFont(new Font("Arial", Font.BOLD, 13));
            moduleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel percentLabel = new JLabel(String.format("%.1f%%", percent));
            percentLabel.setFont(new Font("Arial", Font.BOLD, 24));
            percentLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            Color color = percent >= 75 ? new Color(34, 139, 34) :
                         percent >= 60 ? new Color(255, 165, 0) :
                         new Color(220, 20, 60);
            percentLabel.setForeground(color);

            JLabel detailLabel = new JLabel(String.format("Present: %d | Absent: %d", 
                                                          ma.presentCount, ma.absentCount));
            detailLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            detailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            card.add(moduleLabel);
            card.add(Box.createVerticalStrut(5));
            card.add(percentLabel);
            card.add(Box.createVerticalStrut(5));
            card.add(detailLabel);

            statsPanel.add(card);
        }

        statsPanel.revalidate();
        statsPanel.repaint();
    }

    private void filterByModule() {
        if (moduleAttendanceMap.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No attendance records found!");
            return;
        }

        // Get unique module names using a Set to avoid duplicates
        Set<String> uniqueModules = new HashSet<>();
        for (ModuleAttendance ma : moduleAttendanceMap.values()) {
            uniqueModules.add(ma.moduleName);
        }
        
        String[] modules = uniqueModules.toArray(new String[0]);
        Arrays.sort(modules); // Sort alphabetically for better UX
        
        System.out.println("Available modules for filter: " + Arrays.toString(modules));


        String selected = (String) JOptionPane.showInputDialog(
            this, "Select Module to Filter:", "Filter by Module",
            JOptionPane.QUESTION_MESSAGE, null, modules, modules[0]);

        if (selected != null && !selected.isEmpty()) {
            tableModel.setRowCount(0);
            int count = 0;
            
            for (AttendanceRecord record : attendanceRecords) {
                if (record.moduleName.equals(selected)) {
                    tableModel.addRow(new Object[]{
                        record.date, record.classID, record.moduleName, record.day,
                        record.time, record.location, record.status.toUpperCase()
                    });
                    count++;
                }
            }
            
            System.out.println("Filtered " + count + " records for module: " + selected);
            
            if (count == 0) {
                JOptionPane.showMessageDialog(this, 
                    "No attendance records found for module: " + selected);
            }
        }
    }

    private void exportReport() {
        StringBuilder report = new StringBuilder();
        report.append("ATTENDANCE REPORT\n");
        report.append("Student ID: ").append(tpNumber).append("\n");
        report.append("Overall Attendance: ").append(overallPercentLabel.getText()).append("\n");
        report.append("Total Classes: ").append(totalClassesLabel.getText()).append("\n");
        report.append("Present: ").append(presentCountLabel.getText()).append("\n");
        report.append("Absent: ").append(absentCountLabel.getText()).append("\n\n");
        report.append("=".repeat(80)).append("\n\n");

        report.append("MODULE BREAKDOWN:\n");
        for (Map.Entry<String, ModuleAttendance> entry : moduleAttendanceMap.entrySet()) {
            ModuleAttendance ma = entry.getValue();
            int total = ma.presentCount + ma.absentCount;
            double percent = total > 0 ? (ma.presentCount * 100.0 / total) : 0;
            report.append(String.format("%-30s: %.1f%% (Present: %d, Absent: %d)\n",
                ma.moduleName, percent, ma.presentCount, ma.absentCount));
        }

        report.append("\n").append("=".repeat(80)).append("\n\n");
        report.append("DETAILED RECORDS:\n");

        for (AttendanceRecord record : attendanceRecords) {
            report.append(String.format("%s | %s | %s | %s | %s | %s\n",
                record.date, record.moduleName, record.day, record.time, 
                record.location, record.status.toUpperCase()));
        }

        JTextArea textArea = new JTextArea(report.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(800, 600));

        JOptionPane.showMessageDialog(this, scrollPane, "Attendance Report", JOptionPane.PLAIN_MESSAGE);
    }

    private ClassDetails getClassDetails(String classID) {
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.CLASS_REGISTER.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine(); // Skip header
            
            System.out.println("Looking for classID in class_register: " + classID);
            
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                // classID,moduleID,intakeCode,tpNumber,date,classStart,classEnd,day,location,status
                if (data.length >= 10) {
                    String fileClassID = data[0].trim();
                    
                    System.out.println("Comparing: '" + classID + "' with '" + fileClassID + "'");
                    
                    if (fileClassID.equals(classID)) {
                        String moduleID = data[1].trim();
                        String moduleName = getModuleName(moduleID);
                        String date = data[4].trim();
                        String startTime = data[5].trim();
                        String endTime = data[6].trim();
                        String day = data[7].trim();
                        String location = data[8].trim();
                        String time = startTime + " - " + endTime;
                        
                        System.out.println("FOUND: date=" + date + ", day=" + day + ", time=" + time + ", location=" + location);
                        
                        br.close();
                        return new ClassDetails(date, moduleName, day, time, location);
                    }
                }
            }
            br.close();
            System.out.println("NOT FOUND: classID " + classID);
        } catch (Exception e) {
            System.out.println("ERROR in getClassDetails: " + e.getMessage());
            e.printStackTrace();
        }
        return new ClassDetails("N/A", "Unknown", "N/A", "N/A", "N/A");
    }

    private String getModuleName(String moduleID) {
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.MODULE.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 2 && data[0].equals(moduleID)) {
                    br.close();
                    return data[1];
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return moduleID;
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

    class AttendanceRecord {
        String date, classID, moduleID, moduleName, day, time, location, status;

        AttendanceRecord(String date, String classID, String moduleID, String moduleName,
                        String day, String time, String location, String status) {
            this.date = date;
            this.classID = classID;
            this.moduleID = moduleID;
            this.moduleName = moduleName;
            this.day = day;
            this.time = time;
            this.location = location;
            this.status = status;
        }
    }

    class ClassDetails {
        String date, moduleName, day, time, location;

        ClassDetails(String date, String moduleName, String day, String time, String location) {
            this.date = date;
            this.moduleName = moduleName;
            this.day = day;
            this.time = time;
            this.location = location;
        }
    }

    class ModuleAttendance {
        String moduleName;
        int presentCount = 0;
        int absentCount = 0;

        ModuleAttendance(String moduleName) {
            this.moduleName = moduleName;
        }
    }
}