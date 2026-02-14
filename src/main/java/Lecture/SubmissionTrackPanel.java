package Lecture;

import util.FileManager;
import util.DatabaseFile;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class SubmissionTrackPanel extends JPanel {
    private FileManager fileManager = new FileManager();
    private Lecturer lecturer;
    
    private JComboBox<String> cmbModule, cmbAssessment;
    private JTable submissionTable;
    private DefaultTableModel tableModel;
    private JLabel lblTotal, lblSubmitted, lblPending;
    private Map<String, String> moduleMap;
    private Map<String, String> assessmentMap;
    private Map<String, String> studentNames;

    public SubmissionTrackPanel(Lecturer lecturer) {
        this.lecturer = lecturer;
        this.moduleMap = new HashMap<>();
        this.assessmentMap = new HashMap<>();
        this.studentNames = new HashMap<>();
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
        
        // CRITICAL: Load student names FIRST before anything else
        loadStudentNames();
        
        // Then load modules (which might trigger dropdown events)
        loadModules();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(255, 165, 0));
        header.setPreferredSize(new Dimension(0, 80));

        JLabel title = new JLabel("  Submission Tracking");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        JLabel userInfo = new JLabel(lecturer.getId() + " | " + lecturer.getName() + "  ");
        userInfo.setFont(new Font("Arial", Font.BOLD, 18));
        userInfo.setForeground(Color.WHITE);
        header.add(userInfo, BorderLayout.EAST);

        return header;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top: Selection Panel
        JPanel selectionPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        selectionPanel.setBackground(Color.WHITE);
        selectionPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel lblModule = new JLabel("Select Module:");
        lblModule.setFont(new Font("Arial", Font.BOLD, 14));
        selectionPanel.add(lblModule);

        cmbModule = new JComboBox<>();
        cmbModule.setFont(new Font("Arial", Font.PLAIN, 13));
        cmbModule.addActionListener(e -> loadAssessments());
        selectionPanel.add(cmbModule);

        JLabel lblAssessment = new JLabel("Select Assessment:");
        lblAssessment.setFont(new Font("Arial", Font.BOLD, 14));
        selectionPanel.add(lblAssessment);

        cmbAssessment = new JComboBox<>();
        cmbAssessment.setFont(new Font("Arial", Font.PLAIN, 13));
        cmbAssessment.addActionListener(e -> loadSubmissions());
        selectionPanel.add(cmbAssessment);

        content.add(selectionPanel, BorderLayout.NORTH);

        // Middle: Stats Cards + Table
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(Color.WHITE);

        // Stats Cards
        centerPanel.add(createStatsPanel(), BorderLayout.NORTH);

        // Submission Table
        String[] columns = {"Student ID", "Name", "Submission Date", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        submissionTable = new JTable(tableModel);
        submissionTable.setRowHeight(35);
        submissionTable.setFont(new Font("Arial", Font.PLAIN, 14));
        submissionTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        submissionTable.getTableHeader().setBackground(new Color(255, 165, 0));
        submissionTable.getTableHeader().setForeground(Color.WHITE);
        submissionTable.getTableHeader().setPreferredSize(new Dimension(0, 40));

        // Color code status column
        submissionTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = value.toString();
                
                if (!isSelected) {
                    if (status.equalsIgnoreCase("Submitted")) {
                        c.setBackground(new Color(144, 238, 144));
                    } else if (status.equalsIgnoreCase("Not Submitted")) {
                        c.setBackground(new Color(255, 182, 193));
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(submissionTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(255, 165, 0), 2));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        content.add(centerPanel, BorderLayout.CENTER);

        // Bottom: Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton btnShowAll = new JButton("Show All Submissions");
        btnShowAll.setBackground(new Color(70, 130, 180));
        btnShowAll.setForeground(Color.WHITE);
        btnShowAll.setFont(new Font("Arial", Font.BOLD, 14));
        btnShowAll.setPreferredSize(new Dimension(200, 40));
        btnShowAll.setFocusPainted(false);
        btnShowAll.addActionListener(e -> showAllSubmissionsDialog());
        buttonPanel.add(btnShowAll);

        JButton btnExport = new JButton("Export Report");
        btnExport.setBackground(new Color(34, 139, 34));
        btnExport.setForeground(Color.WHITE);
        btnExport.setFont(new Font("Arial", Font.BOLD, 14));
        btnExport.setPreferredSize(new Dimension(180, 40));
        btnExport.setFocusPainted(false);
        btnExport.addActionListener(e -> exportSubmissionReport());
        buttonPanel.add(btnExport);

        content.add(buttonPanel, BorderLayout.SOUTH);

        return content;
    }

    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));

        JPanel totalCard = createStatCard("Total Students", "0", new Color(70, 130, 180));
        lblTotal = (JLabel) ((JPanel) totalCard.getComponent(0)).getComponent(1);
        statsPanel.add(totalCard);

        JPanel submittedCard = createStatCard("Submitted", "0", new Color(34, 139, 34));
        lblSubmitted = (JLabel) ((JPanel) submittedCard.getComponent(0)).getComponent(1);
        statsPanel.add(submittedCard);

        JPanel pendingCard = createStatCard("Not Submitted", "0", new Color(220, 53, 69));
        lblPending = (JLabel) ((JPanel) pendingCard.getComponent(0)).getComponent(1);
        statsPanel.add(pendingCard);

        return statsPanel;
    }

    private JPanel createStatCard(String title, String value, Color borderColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor, 3),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JPanel contentPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        contentPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        titleLabel.setForeground(Color.GRAY);

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 32));
        valueLabel.setForeground(borderColor);

        contentPanel.add(titleLabel);
        contentPanel.add(valueLabel);
        card.add(contentPanel);

        return card;
    }

    private void loadModules() {
        cmbModule.removeAllItems();
        moduleMap.clear();
        
        // Use ModuleService
        List<String[]> modulesData = ModuleService.getModulesForLecturer(lecturer.getId());
        
        for (String[] row : modulesData) {
            String moduleID = row[0];
            String moduleName = row[1];
            moduleMap.put(moduleID, moduleName);
            cmbModule.addItem(moduleID + " - " + moduleName);
        }
    }

    private void loadAssessments() {
        cmbAssessment.removeAllItems();
        assessmentMap.clear();
        
        String selected = (String) cmbModule.getSelectedItem();
        if (selected == null) return;
        
        String moduleID = selected.split(" - ")[0];
        
        try (BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.ASSESSMENTS.getFileName())))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; }
                if (line.trim().isEmpty()) continue;
                
                String[] data = line.split(",");
                if (data.length >= 4 && data[1].trim().equalsIgnoreCase(moduleID)) {
                    String assessmentID = data[0].trim();
                    String assessmentName = data[2].trim();
                    String type = data[3].trim();
                    assessmentMap.put(assessmentID, assessmentName);
                    cmbAssessment.addItem(assessmentName + " (" + type + ")");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadStudentNames() {
        System.out.println("=== Loading Student Names ===");
        
        try (BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.STUDENT.getFileName())))) {
            String line;
            boolean first = true;
            int count = 0;
            
            while ((line = br.readLine()) != null) {
                if (first) { 
                    System.out.println("studentinfo.txt header: " + line);
                    first = false; 
                    continue; 
                }
                if (line.trim().isEmpty()) continue;
                
                String[] data = line.split(",");
                
                if (count < 3) { // Debug first 3 lines
                    System.out.println("Student line: " + java.util.Arrays.toString(data));
                }
                
                // Format: fullName,ic,gender,dob,nation,race,addr,contact,tpNumber,password,intakeCode
                if (data.length >= 9) {
                    String tpNumber = data[8].trim();
                    String name = data[0].trim();
                    studentNames.put(tpNumber, name);
                    
                    if (count < 3) {
                        System.out.println("  Stored: " + tpNumber + " -> " + name);
                    }
                    count++;
                }
            }
            
            System.out.println("Total student names loaded: " + studentNames.size());
            System.out.println("Sample entries:");
            int shown = 0;
            for (Map.Entry<String, String> entry : studentNames.entrySet()) {
                if (shown++ < 5) {
                    System.out.println("  " + entry.getKey() + " -> " + entry.getValue());
                }
            }
            System.out.println("================================");
            
        } catch (IOException e) {
            System.err.println("Error reading studentinfo.txt: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadSubmissions() {
        tableModel.setRowCount(0);
        
        String selectedModule = (String) cmbModule.getSelectedItem();
        String selectedAssessment = (String) cmbAssessment.getSelectedItem();
        if (selectedModule == null || selectedAssessment == null) return;
        
        String moduleID = selectedModule.split(" - ")[0];
        String assessmentName = selectedAssessment.split(" \\(")[0];
        
        // Find assessment ID
        String assessmentID = "";
        for (Map.Entry<String, String> entry : assessmentMap.entrySet()) {
            if (entry.getValue().equals(assessmentName)) {
                assessmentID = entry.getKey();
                break;
            }
        }
        
        if (assessmentID.isEmpty()) return;
        
        // Get students for this module
        List<String> studentIDs = getStudentsForModule(moduleID);
        
        // Load submissions from submission.txt
        Map<String, String[]> submissionMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.SUBMISSION.getFileName())))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; }
                if (line.trim().isEmpty()) continue;
                
                String[] data = line.split(",");
                // Format: tpNumber,assessmentID,submissionDate,status
                if (data.length >= 4 && data[1].trim().equalsIgnoreCase(assessmentID)) {
                    String sid = data[0].trim();
                    String date = data[2].trim();
                    String status = data[3].trim();
                    submissionMap.put(sid, new String[]{date, status});
                }
            }
        } catch (IOException e) {}
        
        // Populate table
        int submitted = 0, notSubmitted = 0;
        
        System.out.println("\n=== Populating Submission Table ===");
        System.out.println("Total students to display: " + studentIDs.size());
        
        for (String sid : studentIDs) {
            String name = studentNames.getOrDefault(sid, "Unknown");
            
            if (name.equals("Unknown")) {
                System.out.println("  ⚠ Student " + sid + " -> Name NOT FOUND in studentNames map");
            } else {
                System.out.println("  ✓ Student " + sid + " -> " + name);
            }
            
            String date = "-";
            String status = "Not Submitted";
            
            if (submissionMap.containsKey(sid)) {
                String[] data = submissionMap.get(sid);
                date = data[0];
                status = data[1];
                submitted++;
            } else {
                notSubmitted++;
            }
            
            tableModel.addRow(new Object[]{sid, name, date, status});
        }
        
        System.out.println("Table populated with " + studentIDs.size() + " rows");
        System.out.println("Submitted: " + submitted + " | Not Submitted: " + notSubmitted);
        System.out.println("====================================\n");
        
        // Update stats
        lblTotal.setText(String.valueOf(studentIDs.size()));
        lblSubmitted.setText(String.valueOf(submitted));
        lblPending.setText(String.valueOf(notSubmitted));
    }

    private List<String> getStudentsForModule(String moduleID) {
        List<String> students = new ArrayList<>();
        Set<String> uniqueStudents = new HashSet<>();
        
        System.out.println("=== SubmissionTrack: Getting Students for Module: " + moduleID + " ===");
        
        // Step 1: Get intake codes from module_assignments.txt
        Set<String> intakeCodes = new HashSet<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.MODULE_ASSIGNMENTS.getFileName())))) {
            String line;
            boolean first = true;
            
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; }
                if (line.trim().isEmpty()) continue;
                
                String[] data = line.split(",");
                // Format: moduleID,lecturerID,intakeCode
                if (data.length >= 3 && data[0].trim().equals(moduleID)) {
                    String intakeCode = data[2].trim();
                    intakeCodes.add(intakeCode);
                    System.out.println("Found intake: " + intakeCode);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading module_assignments.txt: " + e.getMessage());
        }
        
        System.out.println("Total intakes found: " + intakeCodes.size());
        
        // Step 2: Get students from studentinfo.txt matching those intake codes
        if (!intakeCodes.isEmpty()) {
            try (BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.STUDENT.getFileName())))) {
                String line;
                boolean first = true;
                
                while ((line = br.readLine()) != null) {
                    if (first) { first = false; continue; }
                    if (line.trim().isEmpty()) continue;
                    
                    String[] data = line.split(",");
                    // Format: fullName,ic,gender,dob,nation,race,addr,contact,tpNumber,password,intakeCode
                    if (data.length >= 11) {
                        String studentIntake = data[10].trim();
                        String sid = data[8].trim();
                        
                        if (intakeCodes.contains(studentIntake)) {
                            if (!uniqueStudents.contains(sid)) {
                                uniqueStudents.add(sid);
                                students.add(sid);
                                System.out.println("  ✓ Added student: " + sid + " (Intake: " + studentIntake + ")");
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading studentinfo.txt: " + e.getMessage());
            }
        }
        
        System.out.println("Total students found: " + students.size());
        System.out.println("========================================");
        
        return students;
    }

    private void showAllSubmissionsDialog() {
        System.out.println("=== Show All Submissions Dialog ===");
        
        // Get all modules for this lecturer
        List<String[]> lecturerModules = ModuleService.getModulesForLecturer(lecturer.getId());
        
        // Create dialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "All Submissions - " + lecturer.getName(), true);
        dialog.setSize(1000, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        // Table for all submissions
        String[] columns = {"Module", "Assessment", "Student ID", "Student Name", "Submission Date", "Status"};
        DefaultTableModel allModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable allTable = new JTable(allModel);
        allTable.setRowHeight(30);
        allTable.setFont(new Font("Arial", Font.PLAIN, 13));
        allTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        allTable.getTableHeader().setBackground(new Color(255, 165, 0));
        allTable.getTableHeader().setForeground(Color.WHITE);
        
        // Custom cell renderer for status column
        allTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = value.toString();
                
                if (!isSelected) {
                    if (status.equalsIgnoreCase("Submitted")) {
                        c.setBackground(new Color(144, 238, 144));
                    } else {
                        c.setBackground(new Color(255, 182, 193));
                    }
                }
                
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Arial", Font.BOLD, 12));
                return c;
            }
        });
        
        // Load all submissions across all modules
        int totalSubmitted = 0, totalPending = 0;
        
        for (String[] moduleData : lecturerModules) {
            String moduleID = moduleData[0];
            String moduleName = moduleData[1];
            
            // Get all assessments for this module
            Map<String, String> moduleAssessments = new HashMap<>();
            try (BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.ASSESSMENTS.getFileName())))) {
                String line;
                boolean first = true;
                while ((line = br.readLine()) != null) {
                    if (first) { first = false; continue; }
                    if (line.trim().isEmpty()) continue;
                    
                    String[] data = line.split(",");
                    if (data.length >= 3 && data[1].trim().equals(moduleID)) {
                        moduleAssessments.put(data[0].trim(), data[2].trim());
                    }
                }
            } catch (IOException e) {}
            
            // For each assessment, get submissions
            for (Map.Entry<String, String> assessment : moduleAssessments.entrySet()) {
                String assessmentID = assessment.getKey();
                String assessmentName = assessment.getValue();
                
                // Get all students for this module
                List<String> students = getStudentsForModule(moduleID);
                
                // Load submissions
                Map<String, String[]> submissionMap = new HashMap<>();
                try (BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.SUBMISSION.getFileName())))) {
                    String line;
                    boolean first = true;
                    while ((line = br.readLine()) != null) {
                        if (first) { first = false; continue; }
                        if (line.trim().isEmpty()) continue;
                        
                        String[] data = line.split(",");
                        if (data.length >= 4 && data[1].trim().equals(assessmentID)) {
                            submissionMap.put(data[0].trim(), new String[]{data[2].trim(), data[3].trim()});
                        }
                    }
                } catch (IOException e) {}
                
                // Add rows for each student
                for (String sid : students) {
                    String name = studentNames.getOrDefault(sid, "Unknown");
                    String date = "-";
                    String status = "Not Submitted";
                    
                    if (submissionMap.containsKey(sid)) {
                        String[] subData = submissionMap.get(sid);
                        date = subData[0];
                        status = subData[1];
                        totalSubmitted++;
                    } else {
                        totalPending++;
                    }
                    
                    allModel.addRow(new Object[]{
                        moduleName, assessmentName, sid, name, date, status
                    });
                }
            }
        }
        
        JScrollPane scrollPane = new JScrollPane(allTable);
        dialog.add(scrollPane, BorderLayout.CENTER);
        
        // Summary panel
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        summaryPanel.setBackground(Color.WHITE);
        
        JLabel lblSummary = new JLabel(String.format(
            "Total Rows: %d  |  Submitted: %d  |  Not Submitted: %d",
            allModel.getRowCount(), totalSubmitted, totalPending
        ));
        lblSummary.setFont(new Font("Arial", Font.BOLD, 14));
        summaryPanel.add(lblSummary);
        
        dialog.add(summaryPanel, BorderLayout.NORTH);
        
        // Close button
        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> dialog.dispose());
        JPanel btnPanel = new JPanel();
        btnPanel.add(btnClose);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }

    private void exportSubmissionReport() {
        String selectedModule = (String) cmbModule.getSelectedItem();
        String selectedAssessment = (String) cmbAssessment.getSelectedItem();
        
        if (selectedModule == null || selectedAssessment == null) {
            JOptionPane.showMessageDialog(this, "Please select a module and assessment first.");
            return;
        }
        
        StringBuilder report = new StringBuilder();
        report.append("SUBMISSION TRACKING REPORT\n");
        report.append("Lecturer: ").append(lecturer.getName()).append(" (").append(lecturer.getId()).append(")\n");
        report.append("Module: ").append(selectedModule).append("\n");
        report.append("Assessment: ").append(selectedAssessment).append("\n");
        report.append("Generated: ").append(new java.util.Date()).append("\n\n");
        report.append("=".repeat(80)).append("\n\n");
        
        report.append("SUMMARY:\n");
        report.append("Total Students: ").append(lblTotal.getText()).append("\n");
        report.append("Submitted: ").append(lblSubmitted.getText()).append("\n");
        report.append("Not Submitted: ").append(lblPending.getText()).append("\n\n");
        report.append("=".repeat(80)).append("\n\n");
        
        report.append("DETAILED LIST:\n");
        report.append(String.format("%-12s %-25s %-15s %-15s\n", 
            "Student ID", "Name", "Submission Date", "Status"));
        report.append("-".repeat(80)).append("\n");
        
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            report.append(String.format("%-12s %-25s %-15s %-15s\n",
                tableModel.getValueAt(i, 0),
                tableModel.getValueAt(i, 1),
                tableModel.getValueAt(i, 2),
                tableModel.getValueAt(i, 3)
            ));
        }
        
        JTextArea textArea = new JTextArea(report.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(800, 600));
        
        JOptionPane.showMessageDialog(this, scrollPane, 
            "Submission Report", JOptionPane.PLAIN_MESSAGE);
    }
}