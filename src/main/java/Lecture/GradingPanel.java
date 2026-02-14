package Lecture;

import util.FileManager;
import util.DatabaseFile;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class GradingPanel extends JPanel {
    private FileManager fileManager = new FileManager();
    private Lecturer lecturer;

    private JComboBox<Module> cmbModule;
    private JComboBox<Assessment> cmbAssessment;
    private JTable gradingTable;
    private DefaultTableModel tableModel;
    private JLabel lblTotal, lblAverage, lblMarked;
    private String currentGradingID;
    private List<GradeRule> gradeRules;

    public GradingPanel(Lecturer lecturer) {
        this.lecturer = lecturer;
        this.gradeRules = new ArrayList<>();

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);

        loadModules();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(255, 165, 0));
        header.setPreferredSize(new Dimension(0, 80));

        JLabel title = new JLabel("  Grading");
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
        cmbAssessment.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Assessment) {
                    Assessment a = (Assessment) value;
                    setText(a.getName() + " (" + a.getType() + ")");
                }
                return this;
            }
        });
        cmbAssessment.addActionListener(e -> loadGradingData());
        selectionPanel.add(cmbAssessment);

        content.add(selectionPanel, BorderLayout.NORTH);

        // Middle: Stats Cards + Table
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(Color.WHITE);

        // Stats Cards
        centerPanel.add(createStatsPanel(), BorderLayout.NORTH);

        // Grading Table
        String[] columns = { "Student ID", "Name", "Marks", "Grade", "GPA", "Feedback" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2 || column == 5; // Marks and Feedback editable
            }
        };

        gradingTable = new JTable(tableModel);
        gradingTable.setRowHeight(35);
        gradingTable.setFont(new Font("Arial", Font.PLAIN, 14));
        gradingTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        gradingTable.getTableHeader().setBackground(new Color(255, 165, 0));
        gradingTable.getTableHeader().setForeground(Color.WHITE);
        gradingTable.getTableHeader().setPreferredSize(new Dimension(0, 40));

        // Auto-update Grade and GPA when Marks are entered
        tableModel.addTableModelListener(e -> {
            if (e.getColumn() == 2 && e.getType() == javax.swing.event.TableModelEvent.UPDATE) { // Marks column
                int row = e.getFirstRow();
                if (row >= 0 && row < tableModel.getRowCount()) {
                    try {
                        Object marksObj = tableModel.getValueAt(row, 2);
                        if (marksObj != null) {
                            String marksStr = marksObj.toString();
                            double marks = Double.parseDouble(marksStr);

                            // Validate marks range
                            if (marks < 0 || marks > 100) {
                                JOptionPane.showMessageDialog(this,
                                        "Marks must be between 0 and 100",
                                        "Invalid Marks",
                                        JOptionPane.WARNING_MESSAGE);
                                tableModel.setValueAt(0.0, row, 2);
                                return;
                            }

                            GradeInfo gradeInfo = calculateGradeAndGPA(marks);

                            // Update grade and GPA columns
                            tableModel.setValueAt(gradeInfo.grade, row, 3);
                            tableModel.setValueAt(String.format("%.1f", gradeInfo.gpa), row, 4);

                            updateStats();
                        }
                    } catch (NumberFormatException ex) {
                        tableModel.setValueAt("-", row, 3);
                        tableModel.setValueAt("0.0", row, 4);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(gradingTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(255, 165, 0), 2));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        content.add(centerPanel, BorderLayout.CENTER);

        // Bottom: Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton btnSave = new JButton("Save Grades");
        btnSave.setBackground(new Color(46, 204, 113));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Arial", Font.BOLD, 14));
        btnSave.setPreferredSize(new Dimension(150, 40));
        btnSave.setFocusPainted(false);
        btnSave.addActionListener(e -> saveGrades());
        buttonPanel.add(btnSave);

        JButton btnCGPA = new JButton("View Total CGPA");
        btnCGPA.setBackground(new Color(70, 130, 180));
        btnCGPA.setForeground(Color.WHITE);
        btnCGPA.setFont(new Font("Arial", Font.BOLD, 14));
        btnCGPA.setPreferredSize(new Dimension(180, 40));
        btnCGPA.setFocusPainted(false);
        btnCGPA.addActionListener(e -> viewTotalCGPA());
        buttonPanel.add(btnCGPA);

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

        JPanel avgCard = createStatCard("Average Marks", "0.0", new Color(255, 165, 0));
        lblAverage = (JLabel) ((JPanel) avgCard.getComponent(0)).getComponent(1);
        statsPanel.add(avgCard);

        JPanel markedCard = createStatCard("Marked Students", "0", new Color(34, 139, 34));
        lblMarked = (JLabel) ((JPanel) markedCard.getComponent(0)).getComponent(1);
        statsPanel.add(markedCard);

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

        List<String[]> modulesData = ModuleService.getModulesForLecturer(lecturer.getId());

        for (String[] row : modulesData) {
            cmbModule.addItem(new Module(row[0], row[1]));
        }
    }

    private void loadAssessments() {
        cmbAssessment.removeAllItems();

        Module selected = (Module) cmbModule.getSelectedItem();
        if (selected == null)
            return;

        // Load grading scheme for this module
        loadGradingScheme(selected.getId());

        List<Assessment> assessments = AssessmentService.getAssessmentsForModule(selected.getId());
        for (Assessment a : assessments) {
            cmbAssessment.addItem(a);
        }
    }

    private void loadGradingScheme(String moduleID) {
        gradeRules.clear();
        currentGradingID = null;

        // Get gradingID from module.txt
        try (BufferedReader br = new BufferedReader(
                new FileReader(fileManager.getFilePath(DatabaseFile.MODULE.getFileName())))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) {
                    first = false;
                    continue;
                }
                if (line.trim().isEmpty())
                    continue;

                String[] data = line.split(",");
                // Format: moduleID,moduleName,creditHour,sessionType,Level,tpNumber,gradingID
                if (data.length >= 7 && data[0].trim().equals(moduleID)) {
                    currentGradingID = data[6].trim();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Load grading rules from grading.txt
        if (currentGradingID != null) {
            try (BufferedReader br = new BufferedReader(
                    new FileReader(fileManager.getFilePath(DatabaseFile.GRADING.getFileName())))) {
                String line;
                boolean first = true;
                while ((line = br.readLine()) != null) {
                    if (first) {
                        first = false;
                        continue;
                    }
                    if (line.trim().isEmpty())
                        continue;

                    String[] data = line.split(",");

                    if (data.length >= 24 && data[0].trim().equals(currentGradingID)) {
                        addGradeRule(data, 2, 3, "A+", 4.0);
                        addGradeRule(data, 4, 5, "A", 4.0);
                        addGradeRule(data, 6, 7, "B+", 3.0);
                        addGradeRule(data, 8, 9, "B", 3.0);
                        addGradeRule(data, 10, 11, "C+", 2.0);
                        addGradeRule(data, 12, 13, "C", 2.0);
                        addGradeRule(data, 14, 15, "C-", 2.0);
                        addGradeRule(data, 16, 17, "D", 1.7);
                        addGradeRule(data, 18, 19, "F+", 0.0);
                        addGradeRule(data, 20, 21, "F", 0.0);

                        if (data.length >= 24) {
                            addGradeRule(data, 22, 23, "F-", 0.0);
                        }

                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Sort rules by minMarks descending (check highest first)
        gradeRules.sort((a, b) -> Double.compare(b.minMarks, a.minMarks));
    }

    private void addGradeRule(String[] data, int minIndex, int maxIndex, String grade, double gpa) {
        try {
            if (data.length > maxIndex) {
                double min = Double.parseDouble(data[minIndex].trim());
                double max = Double.parseDouble(data[maxIndex].trim());
                gradeRules.add(new GradeRule(grade, min, max, gpa));
            }
        } catch (NumberFormatException e) {
        }
    }

    private void loadGradingData() {
        tableModel.setRowCount(0);

        Module selectedModule = (Module) cmbModule.getSelectedItem();
        Assessment selectedAssessment = (Assessment) cmbAssessment.getSelectedItem();
        if (selectedModule == null || selectedAssessment == null) {
            return;
        }

        String moduleID = selectedModule.getId();
        String assessmentID = selectedAssessment.getId();

        // Get students for this module
        List<String> studentIDs = getStudentsForModule(moduleID);
        Map<String, String> studentNames = getStudentNames();

        // Load existing marks from marks.txt
        Map<String, Double> marksMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(
                new FileReader(fileManager.getFilePath(DatabaseFile.MARKS.getFileName())))) {
            String line;
            boolean first = true;

            while ((line = br.readLine()) != null) {
                if (first) {
                    first = false;
                    continue;
                }
                if (line.trim().isEmpty())
                    continue;

                String[] data = line.split(",");
                // Format: tpNumber,moduleID,assessmentID,marks
                if (data.length >= 4) {
                    String lineModuleID = data[1].trim();
                    String lineAssessmentID = data[2].trim();
                    String sid = data[0].trim();

                    if (lineModuleID.equals(moduleID) && lineAssessmentID.equals(assessmentID)) {
                        double marks = Double.parseDouble(data[3].trim());
                        marksMap.put(sid, marks);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Load existing feedback from feedback.txt
        Map<String, String> feedbackMap = new HashMap<>();
        String moduleName = selectedModule.getName();
        String assessmentName = selectedAssessment.getName();
        String categoryMatch = moduleName + " - " + assessmentName;

        try (BufferedReader br = new BufferedReader(
                new FileReader(fileManager.getFilePath(DatabaseFile.FEEDBACK.getFileName())))) {
            String line;
            boolean first = true;

            while ((line = br.readLine()) != null) {
                if (first) {
                    first = false;
                    continue;
                }
                if (line.trim().isEmpty())
                    continue;

                String[] data = line.split(",");
                // Format: feedbackID,senderID,senderRole,sendToRole,receiverID,category,feedback,timestamp,status,response
                if (data.length >= 7) {
                    String senderID = data[1].trim();
                    String receiverID = data[4].trim();
                    String category = data[5].trim();
                    String feedback = data[6].trim().replace(";", ","); // Unescape commas

                    // Check if this feedback is from this lecturer, to this student, for this assessment
                    if (senderID.equals(lecturer.getId()) && category.equals(categoryMatch)) {
                        feedbackMap.put(receiverID, feedback);
                    }
                }
            }
        } catch (IOException e) {
            // Feedback file might not exist yet
        }

        // Populate table - CALCULATE grade and GPA for loaded marks
        for (String sid : studentIDs) {
            String name = studentNames.getOrDefault(sid, "Unknown");
            double marks = marksMap.getOrDefault(sid, 0.0);
            String feedback = feedbackMap.getOrDefault(sid, ""); // Load existing feedback

            // Calculate grade and GPA based on marks
            GradeInfo gradeInfo = calculateGradeAndGPA(marks);

            tableModel.addRow(new Object[] {
                    sid,
                    name,
                    marks,
                    gradeInfo.grade,
                    String.format("%.1f", gradeInfo.gpa),
                    feedback // Pre-populate feedback column
            });
        }

        updateStats();
    }

    private List<String> getStudentsForModule(String moduleID) {
        List<String> students = new ArrayList<>();
        Set<String> uniqueStudents = new HashSet<>();

        // Step 1: Get intake codes from module_assignments.txt
        Set<String> intakeCodes = new HashSet<>();

        try (BufferedReader br = new BufferedReader(
                new FileReader(fileManager.getFilePath(DatabaseFile.MODULE_ASSIGNMENTS.getFileName())))) {
            String line;
            boolean first = true;

            while ((line = br.readLine()) != null) {
                if (first) {
                    first = false;
                    continue;
                }
                if (line.trim().isEmpty())
                    continue;

                String[] data = line.split(",");
                // Format: moduleID,lecturerID,intakeCode
                if (data.length >= 3 && data[0].trim().equals(moduleID)) {
                    String intakeCode = data[2].trim();
                    intakeCodes.add(intakeCode);
                }
            }
        } catch (IOException e) {
        }

        // Step 2: Get students from studentinfo.txt matching those intake codes
        if (!intakeCodes.isEmpty()) {
            try (BufferedReader br = new BufferedReader(
                    new FileReader(fileManager.getFilePath(DatabaseFile.STUDENT.getFileName())))) {
                String line;
                boolean first = true;

                while ((line = br.readLine()) != null) {
                    if (first) {
                        first = false;
                        continue;
                    }
                    if (line.trim().isEmpty())
                        continue;

                    String[] data = line.split(",");
                    // Format:
                    // fullName,ic,gender,dob,nation,race,addr,contact,tpNumber,password,intakeCode
                    if (data.length >= 11) {
                        String studentIntake = data[10].trim();
                        String sid = data[8].trim();

                        if (intakeCodes.contains(studentIntake)) {
                            if (!uniqueStudents.contains(sid)) {
                                uniqueStudents.add(sid);
                                students.add(sid);
                            }
                        }
                    }
                }
            } catch (IOException e) {
            }
        }

        return students;
    }

    private Map<String, String> getStudentNames() {
        Map<String, String> names = new HashMap<>();
        try (BufferedReader br = new BufferedReader(
                new FileReader(fileManager.getFilePath(DatabaseFile.STUDENT.getFileName())))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) {
                    first = false;
                    continue;
                }
                if (line.trim().isEmpty())
                    continue;

                String[] data = line.split(",");
                if (data.length >= 9) {
                    names.put(data[8].trim(), data[0].trim());
                }
            }
        } catch (IOException e) {
        }
        return names;
    }

    private GradeInfo calculateGradeAndGPA(double marks) {
        for (GradeRule rule : gradeRules) {
            if (marks >= rule.minMarks && marks <= rule.maxMarks) {
                return new GradeInfo(rule.grade, rule.gpa);
            }
        }
        return new GradeInfo("F", 0.0); // Default
    }

    private void updateStats() {
        int total = tableModel.getRowCount();
        int marked = 0;
        double sum = 0;

        for (int i = 0; i < total; i++) {
            try {
                double marks = Double.parseDouble(tableModel.getValueAt(i, 2).toString());
                if (marks > 0) {
                    marked++;
                    sum += marks;
                }
            } catch (NumberFormatException e) {
            }
        }

        double average = marked > 0 ? sum / marked : 0.0;

        lblTotal.setText(String.valueOf(total));
        lblAverage.setText(String.format("%.1f", average));
        lblMarked.setText(String.valueOf(marked));
    }

    private void saveGrades() {
        Module selectedModule = (Module) cmbModule.getSelectedItem();
        Assessment selectedAssessment = (Assessment) cmbAssessment.getSelectedItem();
        if (selectedModule == null || selectedAssessment == null) {
            JOptionPane.showMessageDialog(this, "Please select module and assessment!");
            return;
        }

        String moduleID = selectedModule.getId();
        String assessmentID = selectedAssessment.getId();

        // Read all existing marks
        List<String> allLines = new ArrayList<>();
        allLines.add("tpNumber,moduleID,assessmentID,marks");

        try (BufferedReader br = new BufferedReader(
                new FileReader(fileManager.getFilePath(DatabaseFile.MARKS.getFileName())))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) {
                    first = false;
                    continue;
                }
                if (line.trim().isEmpty())
                    continue;

                String[] data = line.split(",");
                // Keep lines that are NOT for this module+assessment
                if (!(data.length >= 3 && data[1].trim().equals(moduleID) && data[2].trim().equals(assessmentID))) {
                    allLines.add(line);
                }
            }
        } catch (IOException e) {
        }

        // Add new marks from table
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String sid = tableModel.getValueAt(i, 0).toString();
            String marksStr = tableModel.getValueAt(i, 2).toString();

            try {
                double marks = Double.parseDouble(marksStr);
                if (marks >= 0 && marks <= 100) {
                    String line = String.format("%s,%s,%s,%.1f", sid, moduleID, assessmentID, marks);
                    allLines.add(line);
                }
            } catch (NumberFormatException e) {
            }
        }

        // Write marks back
        try (BufferedWriter bw = new BufferedWriter(
                new FileWriter(fileManager.getFilePath(DatabaseFile.MARKS.getFileName())))) {
            for (String l : allLines) {
                bw.write(l);
                bw.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving marks: " + e.getMessage());
            return;
        }

        // Save feedback to feedback.txt (only if feedback is provided)
        int feedbackCount = saveFeedbackToFile(moduleID, assessmentID);

        if (feedbackCount > 0) {
            JOptionPane.showMessageDialog(this, 
                String.format("Grades saved successfully!\n%d feedback(s) sent to students.", feedbackCount));
        } else {
            JOptionPane.showMessageDialog(this, "Grades saved successfully!");
        }
    }

    private int saveFeedbackToFile(String moduleID, String assessmentID) {
        int feedbackCount = 0;
        
        try {
            String feedbackFilePath = fileManager.getFilePath(DatabaseFile.FEEDBACK.getFileName());
            File feedbackFile = new File(feedbackFilePath);
            
            // Get module and assessment names for category matching
            String moduleName = ((Module) cmbModule.getSelectedItem()).getName();
            Assessment assessment = (Assessment) cmbAssessment.getSelectedItem();
            String assessmentName = assessment.getName();
            String categoryMatch = moduleName + " - " + assessmentName;
            
            // Read existing feedback and track what's already saved
            List<String> existingLines = new ArrayList<>();
            existingLines.add("feedbackID,senderID,senderRole,sendToRole,receiverID,category,feedback,timestamp,status,response");
            
            Map<String, String> existingFeedbackMap = new HashMap<>(); // studentID -> existing feedback text
            Map<String, String> existingFeedbackLines = new HashMap<>(); // studentID -> full line
            int maxFeedbackNum = 0;
            
            if (feedbackFile.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(feedbackFilePath))) {
                    String line;
                    boolean first = true;
                    
                    while ((line = br.readLine()) != null) {
                        if (first) {
                            first = false;
                            continue;
                        }
                        if (line.trim().isEmpty()) continue;
                        
                        String[] data = line.split(",");
                        
                        // Track max feedback ID
                        if (data.length > 0 && data[0].startsWith("FB")) {
                            try {
                                int num = Integer.parseInt(data[0].substring(2));
                                maxFeedbackNum = Math.max(maxFeedbackNum, num);
                            } catch (NumberFormatException e) {
                            }
                        }
                        
                        // Check if this feedback is from this lecturer for this assessment
                        if (data.length >= 7) {
                            String senderID = data[1].trim();
                            String receiverID = data[4].trim();
                            String category = data[5].trim();
                            String feedback = data[6].trim().replace(";", ",");
                            
                            if (senderID.equals(lecturer.getId()) && category.equals(categoryMatch)) {
                                // This is feedback from this lecturer for this assessment
                                existingFeedbackMap.put(receiverID, feedback);
                                existingFeedbackLines.put(receiverID, line);
                                // Don't add to existingLines yet - we'll process later
                                continue;
                            }
                        }
                        
                        // Keep all other feedback (different lecturer, different assessment, etc.)
                        existingLines.add(line);
                    }
                }
            }
            
            // Get current timestamp
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String timestamp = now.format(formatter);
            
            // Process feedback from table
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String studentID = tableModel.getValueAt(i, 0).toString();
                Object feedbackObj = tableModel.getValueAt(i, 5); // Feedback column (index 5)
                
                String currentFeedback = "";
                if (feedbackObj != null) {
                    currentFeedback = feedbackObj.toString().trim();
                }
                
                String existingFeedback = existingFeedbackMap.getOrDefault(studentID, "");
                
                if (currentFeedback.isEmpty()) {
                    // Case 1: Feedback is empty
                    if (!existingFeedback.isEmpty()) {
                        // Had feedback before, now deleted - don't add back
                        // (already excluded from existingLines)
                    }
                    // No feedback before, still no feedback - nothing to do
                } else {
                    // Case 2: Feedback exists
                    if (currentFeedback.equals(existingFeedback)) {
                        // Unchanged - keep the existing line as-is
                        existingLines.add(existingFeedbackLines.get(studentID));
                    } else {
                        // New or modified - create new record
                        maxFeedbackNum++;
                        String feedbackID = String.format("FB%03d", maxFeedbackNum);
                        
                        // Format: feedbackID,senderID,senderRole,sendToRole,receiverID,category,feedback,timestamp,status,response
                        String category = moduleName + " - " + assessmentName;
                        String feedbackLine = String.format("%s,%s,lecturer,Student,%s,%s,%s,%s,Pending,",
                            feedbackID,
                            lecturer.getId(),
                            studentID,
                            category,
                            currentFeedback.replace(",", ";"), // Escape commas in feedback
                            timestamp
                        );
                        
                        existingLines.add(feedbackLine);
                        feedbackCount++;
                    }
                }
            }
            
            // Write all feedback back
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(feedbackFilePath))) {
                for (String line : existingLines) {
                    bw.write(line);
                    bw.newLine();
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error saving feedback: " + e.getMessage());
            e.printStackTrace();
        }
        
        return feedbackCount;
    }

    private void viewTotalCGPA() {
        Module selectedModule = (Module) cmbModule.getSelectedItem();
        if (selectedModule == null) {
            JOptionPane.showMessageDialog(this, "Please select a module first.");
            return;
        }

        // Calculate CGPA for all students
        List<String> studentIDs = getStudentsForModule(selectedModule.getId());
        Map<String, String> studentNames = getStudentNames();

        // Get all marks for this module
        Map<String, Map<String, Double>> studentMarks = new HashMap<>();

        try (BufferedReader br = new BufferedReader(
                new FileReader(fileManager.getFilePath(DatabaseFile.MARKS.getFileName())))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) {
                    first = false;
                    continue;
                }
                if (line.trim().isEmpty())
                    continue;

                String[] data = line.split(",");
                if (data.length >= 4 && data[1].trim().equals(selectedModule.getId())) {
                    String sid = data[0].trim();
                    String aid = data[2].trim();
                    double marks = Double.parseDouble(data[3].trim());

                    studentMarks.computeIfAbsent(sid, k -> new HashMap<>()).put(aid, marks);
                }
            }
        } catch (IOException e) {
        }

        // Create dialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Total CGPA - " + selectedModule.getName(), true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        // Table
        String[] columns = { "Student ID", "Name", "Assessments Completed", "Total CGPA" };
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (String sid : studentIDs) {
            String name = studentNames.getOrDefault(sid, "Unknown");
            Map<String, Double> marks = studentMarks.get(sid);
            int count = (marks != null) ? marks.size() : 0;
            double totalCGPA = 0.0;

            if (count > 0) {
                double sum = 0;
                for (double m : marks.values()) {
                    GradeInfo info = calculateGradeAndGPA(m);
                    sum += info.gpa;
                }
                totalCGPA = sum / count;
            }

            model.addRow(new Object[] { sid, name, count, String.format("%.2f", totalCGPA) });
        }

        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.getTableHeader().setBackground(new Color(255, 165, 0));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));

        dialog.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> dialog.dispose());
        JPanel btnPanel = new JPanel();
        btnPanel.add(btnClose);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    // Helper classes
    class GradeRule {
        String grade;
        double minMarks, maxMarks, gpa;

        GradeRule(String grade, double minMarks, double maxMarks, double gpa) {
            this.grade = grade;
            this.minMarks = minMarks;
            this.maxMarks = maxMarks;
            this.gpa = gpa;
        }
    }

    class GradeInfo {
        String grade;
        double gpa;

        GradeInfo(String grade, double gpa) {
            this.grade = grade;
            this.gpa = gpa;
        }
    }
}