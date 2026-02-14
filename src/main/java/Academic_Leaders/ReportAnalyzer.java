package Academic_Leaders;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import util.FileManager;
import util.DatabaseFile;
import java.util.*;

public class ReportAnalyzer extends JPanel {
    private String tpNumber, role, name;
    private MainDashboard parent;
    private JTabbedPane tabbedPane;
    private JTable moduleTable, performanceTable, gradeTable;
    private DefaultTableModel moduleModel, performanceModel, gradeModel;
    private JPanel summaryPanel;
    
    private final FileManager fileManager = new FileManager();
    
    private Map<String, String> moduleMap = new HashMap<>();
    private Map<String, Map<String, Integer>> gradingSchemes = new HashMap<>(); // gradingID -> {grade -> minMarks}

    public ReportAnalyzer(String tpNumber, String role, String name, MainDashboard parent) {
        this.tpNumber = tpNumber;
        this.role = role;
        this.name = name;
        this.parent = parent;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // HEADER
        add(createHeader(), BorderLayout.NORTH);

        // TABBED PANE
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        
        tabbedPane.addTab("📊 Module Overview", createModulePanel());
        tabbedPane.addTab("📈 Student Performance", createPerformancePanel());
        tabbedPane.addTab("📉 Grade Distribution", createGradePanel());
        summaryPanel = createSummaryPanel();
        tabbedPane.addTab("📋 Summary Dashboard", summaryPanel);

        add(tabbedPane, BorderLayout.CENTER);

        refreshAllReports();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(255, 165, 0));
        header.setPreferredSize(new Dimension(0, 80));

        JLabel title = new JLabel("  Report Analyzer");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        header.add(title, BorderLayout.WEST);

        JLabel userInfoLabel = new JLabel(tpNumber + " | LEADER  ");
        userInfoLabel.setFont(new Font("Arial", Font.BOLD, 18));
        userInfoLabel.setForeground(Color.WHITE);
        header.add(userInfoLabel, BorderLayout.EAST);

        return header;
    }

    private JPanel createModulePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] columns = {"Module ID", "Module Name", "Credit Hours", "Session Type", 
                           "Assigned Lecturer", "Intake", "Students", "Avg Score"};
        moduleModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        moduleTable = new JTable(moduleModel);
        styleTable(moduleTable);

        JScrollPane scrollPane = new JScrollPane(moduleTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(255, 165, 0), 2),
            "Module Overview Report",
            0, 0, new Font("Arial", Font.BOLD, 16)));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(Color.WHITE);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setBackground(new Color(70, 130, 180));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFont(new Font("Arial", Font.BOLD, 14));
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> {
            loadGradingSchemes();
            loadModuleReport();
        });
        btnPanel.add(btnRefresh);

        JButton btnExport = new JButton("Export Report");
        btnExport.setBackground(new Color(34, 139, 34));
        btnExport.setForeground(Color.WHITE);
        btnExport.setFont(new Font("Arial", Font.BOLD, 14));
        btnExport.setFocusPainted(false);
        btnExport.addActionListener(e -> exportReport());
        btnPanel.add(btnExport);

        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createPerformancePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] columns = {"Student ID", "Student Name", "Module ID", "Module Name", 
                           "Assessment", "Marks", "Grade", "Status"};
        performanceModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        performanceTable = new JTable(performanceModel);
        styleTable(performanceTable);

        performanceTable.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    String status = value != null ? value.toString() : "";
                    if (status.equals("PASS")) {
                        c.setBackground(new Color(200, 255, 200));
                        c.setForeground(new Color(0, 128, 0));
                    } else if (status.equals("FAIL")) {
                        c.setBackground(new Color(255, 200, 200));
                        c.setForeground(new Color(200, 0, 0));
                    } else {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                    }
                }
                setHorizontalAlignment(CENTER);
                setFont(new Font("Arial", Font.BOLD, 12));
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(performanceTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(255, 165, 0), 2),
            "Student Performance Report",
            0, 0, new Font("Arial", Font.BOLD, 16)));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setBackground(Color.WHITE);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setBackground(new Color(70, 130, 180));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFont(new Font("Arial", Font.BOLD, 14));
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> {
            loadGradingSchemes();
            loadPerformanceReport();
        });
        btnPanel.add(btnRefresh);

        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createGradePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] columns = {"Module ID", "Module Name", "A", "B", "C", "D", "F", "Total", "Pass Rate %"};
        gradeModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        gradeTable = new JTable(gradeModel);
        styleTable(gradeTable);

        JScrollPane scrollPane = new JScrollPane(gradeTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(255, 165, 0), 2),
            "Grade Distribution by Module",
            0, 0, new Font("Arial", Font.BOLD, 16)));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setBackground(Color.WHITE);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setBackground(new Color(70, 130, 180));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFont(new Font("Arial", Font.BOLD, 14));
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> {
            loadGradingSchemes();
            loadGradeDistribution();
        });
        btnPanel.add(btnRefresh);

        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("Summary Dashboard");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(new Color(255, 165, 0));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(title, BorderLayout.NORTH);

        JPanel cardsPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        cardsPanel.setBackground(Color.WHITE);
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));

        cardsPanel.add(createStatCard("Total Modules", "0", new Color(52, 152, 219)));
        cardsPanel.add(createStatCard("Module Assignments", "0", new Color(155, 89, 182)));
        cardsPanel.add(createStatCard("Average Score", "0%", new Color(46, 204, 113)));
        cardsPanel.add(createStatCard("Total Students", "0", new Color(230, 126, 34)));
        cardsPanel.add(createStatCard("Pass Rate", "0%", new Color(26, 188, 156)));
        cardsPanel.add(createStatCard("Leader ID", tpNumber, new Color(255, 165, 0)));

        panel.add(cardsPanel, BorderLayout.CENTER);

        JLabel footer = new JLabel("Report generated for: " + name + " | " + new java.util.Date());
        footer.setFont(new Font("Arial", Font.ITALIC, 12));
        footer.setForeground(Color.GRAY);
        footer.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStatCard(String label, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 32));
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        valueLabel.setName("value");
        card.add(valueLabel, BorderLayout.CENTER);

        JLabel titleLabel = new JLabel(label);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(titleLabel, BorderLayout.SOUTH);

        return card;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(28);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.getTableHeader().setBackground(new Color(255, 165, 0));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public void refreshAllReports() {
        loadGradingSchemes();
        loadModuleMap();
        loadModuleReport();
        loadPerformanceReport();
        loadGradeDistribution();
        updateSummaryPanel();
    }

    private void loadGradingSchemes() {
        gradingSchemes.clear();
        
        System.out.println("=== Loading Grading Schemes from grading.txt ===");
        
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.GRADING.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine(); // Skip header
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] data = line.split(",");
                // Format: gradingID,date,A+_min,A+_max,A_min,A_max,B+_min,B+_max,...,F-_min,F-_max,CT
                if (data.length >= 24) {
                    String gradingID = data[0].trim();
                    
                    // Use LinkedHashMap to preserve insertion order, then we'll sort by values
                    Map<String, Integer> gradeMap = new LinkedHashMap<>();
                    
                    // Parse grade ranges (columns 2-23, pairs of min-max)
                    String[] grades = {"A+", "A", "B+", "B", "C+", "C", "C-", "D", "F+", "F", "F-"};
                    for (int i = 0; i < grades.length && (2 + i*2) < data.length - 1; i++) {
                        try {
                            int minMarks = Integer.parseInt(data[2 + i*2].trim());
                            gradeMap.put(grades[i], minMarks);
                        } catch (NumberFormatException e) {}
                    }
                    
                    gradingSchemes.put(gradingID, gradeMap);
                    System.out.println("  Loaded scheme: " + gradingID + " with grades: " + gradeMap);
                }
            }
            br.close();
            
            System.out.println("Total grading schemes loaded: " + gradingSchemes.size());
            
        } catch (Exception e) {
            System.err.println("Error loading grading schemes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String calculateGradeFromMarks(double marks, String gradingID) {
        if (!gradingSchemes.containsKey(gradingID)) {
            System.out.println("    Grading scheme " + gradingID + " not found, using default");
            // Fallback to default grading
            if (marks >= 90) return "A+";
            if (marks >= 80) return "A";
            if (marks >= 75) return "B+";
            if (marks >= 70) return "B";
            if (marks >= 65) return "C+";
            if (marks >= 60) return "C";
            if (marks >= 55) return "C-";
            if (marks >= 50) return "D";
            return "F";
        }
        
        Map<String, Integer> gradeMap = gradingSchemes.get(gradingID);
        
        // Sort grades by minimum marks in DESCENDING order to check from highest to lowest
        java.util.List<Map.Entry<String, Integer>> sortedGrades = new ArrayList<>(gradeMap.entrySet());
        sortedGrades.sort((a, b) -> Integer.compare(b.getValue(), a.getValue())); // Descending by value
        
        // Find the appropriate grade
        for (Map.Entry<String, Integer> entry : sortedGrades) {
            if (marks >= entry.getValue()) {
                System.out.println("    Marks " + marks + " >= " + entry.getValue() + " → Grade: " + entry.getKey());
                return entry.getKey();
            }
        }
        
        System.out.println("    Marks " + marks + " below all thresholds → Grade: F");
        return "F";
    }

    private void loadModuleMap() {
        moduleMap.clear();
        
        System.out.println("=== Loading Module Map for Leader: " + tpNumber + " ===");
        
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.MODULE.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine(); // Skip header
            
            int count = 0;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] data = line.split(",");
                
                // Format: moduleID,moduleName,creditHour,sessionType,Level,tpNumber,gradingID
                String leaderID = null;
                if (data.length >= 7) {
                    leaderID = data[5].trim();
                } else if (data.length >= 5) {
                    leaderID = data[4].trim();
                }
                
                if (leaderID != null && leaderID.equals(tpNumber)) {
                    moduleMap.put(data[0].trim(), data[1].trim());
                    count++;
                }
            }
            br.close();
            
            System.out.println("Total modules loaded: " + count);
            
        } catch (Exception ex) {
            System.err.println("Error loading module map: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void loadModuleReport() {
        moduleModel.setRowCount(0);

        System.out.println("=== Loading Module Report ===");

        Map<String, String[]> assignMap = new HashMap<>();
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.MODULE_ASSIGNMENTS.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine(); // Skip header
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] data = line.split(",");
                if (data.length >= 3 && moduleMap.containsKey(data[0].trim())) {
                    assignMap.put(data[0].trim(), new String[]{data[1].trim(), data[2].trim()});
                }
            }
            br.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            String filePath = fileManager.getFilePath(DatabaseFile.MODULE.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine(); // Skip header
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] data = line.split(",");
                
                String leaderID = null;
                if (data.length >= 7) {
                    leaderID = data[5].trim();
                } else if (data.length >= 5) {
                    leaderID = data[4].trim();
                }
                
                if (leaderID != null && leaderID.equals(tpNumber)) {
                    String moduleID = data[0].trim();
                    String moduleName = data[1].trim();
                    String credits = data[2].trim();
                    String sessionType = data[3].trim();

                    String lecturerID = "-";
                    String intakeID = "-";
                    if (assignMap.containsKey(moduleID)) {
                        lecturerID = assignMap.get(moduleID)[0];
                        intakeID = assignMap.get(moduleID)[1];
                    }

                    int studentCount = countStudents(moduleID);
                    double avgScore = calculateWeightedAverage(moduleID);

                    moduleModel.addRow(new Object[]{
                        moduleID, moduleName, credits, sessionType,
                        lecturerID, intakeID, studentCount,
                        avgScore > 0 ? String.format("%.1f", avgScore) : "-"
                    });
                }
            }
            br.close();
            
            System.out.println("Total modules in report: " + moduleModel.getRowCount());
            
        } catch (Exception ex) {
            System.err.println("Error loading module report: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private double calculateWeightedAverage(String moduleID) {
        System.out.println("  Calculating weighted average for: " + moduleID);
        
        // Get grading ID for this module
        String gradingID = getModuleGradingID(moduleID);
        
        // Get all assessments for this module
        Map<String, Double> assessmentWeights = new HashMap<>();
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.ASSESSMENTS.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine(); // Skip header
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] data = line.split(",");
                // Format: assessmentID,moduleID,name,type,weight,dueDate OR assessmentID,moduleID,name,weight,dueDate
                if (data.length >= 4 && data[1].trim().equals(moduleID)) {
                    String assessmentID = data[0].trim();
                    double weight;
                    try {
                        weight = Double.parseDouble(data[4].trim());
                    } catch (Exception e) {
                        weight = Double.parseDouble(data[3].trim());
                    }
                    assessmentWeights.put(assessmentID, weight);
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        System.out.println("    Assessment weights: " + assessmentWeights);
        
        // Calculate weighted average per student
        Map<String, Double> studentScores = new HashMap<>();
        
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.MARKS.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine(); // Skip header
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] data = line.split(",");
                // Format: tpNumber,moduleID,assessmentID,marks
                if (data.length >= 4 && data[1].trim().equals(moduleID)) {
                    String studentID = data[0].trim();
                    String assessmentID = data[2].trim();
                    double marks = Double.parseDouble(data[3].trim());
                    
                    if (assessmentWeights.containsKey(assessmentID)) {
                        double weight = assessmentWeights.get(assessmentID);
                        double contribution = (marks / 100.0) * weight;
                        
                        studentScores.put(studentID, studentScores.getOrDefault(studentID, 0.0) + contribution);
                    }
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        System.out.println("    Student scores: " + studentScores);
        
        // Calculate average of all student weighted scores
        if (studentScores.isEmpty()) return 0;
        
        double total = studentScores.values().stream().mapToDouble(Double::doubleValue).sum();
        double average = total / studentScores.size();
        
        System.out.println("    Module average: " + average);
        
        return average;
    }

    private String getModuleGradingID(String moduleID) {
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.MODULE.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine(); // Skip header
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] data = line.split(",");
                // Format: moduleID,moduleName,creditHour,sessionType,Level,tpNumber,gradingID
                if (data.length >= 7 && data[0].trim().equals(moduleID)) {
                    br.close();
                    return data[6].trim();
                }
            }
            br.close();
        } catch (Exception e) {}
        
        return "G001"; // Default
    }

    private void loadPerformanceReport() {
        performanceModel.setRowCount(0);

        System.out.println("=== Loading Performance Report ===");

        try {
            String filePath = fileManager.getFilePath(DatabaseFile.MARKS.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine(); // Skip header
            
            int count = 0;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] data = line.split(",");
                // Format: tpNumber,moduleID,assessmentID,marks
                if (data.length >= 4 && moduleMap.containsKey(data[1].trim())) {
                    String studentID = data[0].trim();
                    String moduleID = data[1].trim();
                    String moduleName = moduleMap.get(moduleID);
                    String assessmentID = data[2].trim();
                    String marks = data[3].trim();
                    
                    // Calculate grade based on INDIVIDUAL assessment marks (no weighting)
                    String gradingID = getModuleGradingID(moduleID);
                    double marksValue = Double.parseDouble(marks);
                    String grade = calculateGradeFromMarks(marksValue, gradingID);
                    String status = grade.equalsIgnoreCase("F") || grade.startsWith("F") ? "FAIL" : "PASS";
                    String studentName = getStudentName(studentID);

                    performanceModel.addRow(new Object[]{
                        studentID, studentName, moduleID, moduleName,
                        assessmentID, marks, grade, status
                    });
                    count++;
                }
            }
            br.close();
            
            System.out.println("Total performance records: " + count);
            
        } catch (Exception ex) {
            System.err.println("Error loading performance report: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void loadGradeDistribution() {
        gradeModel.setRowCount(0);

        System.out.println("=== Loading Grade Distribution ===");

        for (Map.Entry<String, String> entry : moduleMap.entrySet()) {
            String moduleID = entry.getKey();
            String moduleName = entry.getValue();
            String gradingID = getModuleGradingID(moduleID);

            int gradeA = 0, gradeB = 0, gradeC = 0, gradeD = 0, gradeF = 0;

            // Calculate weighted average per student first
            Map<String, Double> studentScores = new HashMap<>();
            Map<String, Double> assessmentWeights = new HashMap<>();
            
            // Get assessment weights
            try {
                String filePath = fileManager.getFilePath(DatabaseFile.ASSESSMENTS.getFileName());
                BufferedReader br = new BufferedReader(new FileReader(filePath));
                String line = br.readLine(); // Skip header
                
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    
                    String[] data = line.split(",");
                    if (data.length >= 4 && data[1].trim().equals(moduleID)) {
                        String assessmentID = data[0].trim();
                        double weight;
                        try {
                            weight = Double.parseDouble(data[4].trim());
                        } catch (Exception e) {
                            weight = Double.parseDouble(data[3].trim());
                        }
                        assessmentWeights.put(assessmentID, weight);
                    }
                }
                br.close();
            } catch (Exception ex) {}
            
            // Calculate student weighted scores
            try {
                String filePath = fileManager.getFilePath(DatabaseFile.MARKS.getFileName());
                BufferedReader br = new BufferedReader(new FileReader(filePath));
                String line = br.readLine(); // Skip header
                
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    
                    String[] data = line.split(",");
                    if (data.length >= 4 && data[1].trim().equals(moduleID)) {
                        String studentID = data[0].trim();
                        String assessmentID = data[2].trim();
                        double marks = Double.parseDouble(data[3].trim());
                        
                        if (assessmentWeights.containsKey(assessmentID)) {
                            double weight = assessmentWeights.get(assessmentID);
                            double contribution = (marks / 100.0) * weight;
                            studentScores.put(studentID, studentScores.getOrDefault(studentID, 0.0) + contribution);
                        }
                    }
                }
                br.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            
            // Convert weighted scores to grades
            for (double score : studentScores.values()) {
                String grade = calculateGradeFromMarks(score, gradingID);
                
                if (grade.startsWith("A")) gradeA++;
                else if (grade.startsWith("B")) gradeB++;
                else if (grade.startsWith("C")) gradeC++;
                else if (grade.startsWith("D")) gradeD++;
                else if (grade.startsWith("F")) gradeF++;
            }

            int total = gradeA + gradeB + gradeC + gradeD + gradeF;
            double passRate = total > 0 ? ((total - gradeF) * 100.0 / total) : 0;

            gradeModel.addRow(new Object[]{
                moduleID, moduleName, gradeA, gradeB, gradeC, gradeD, gradeF,
                total, String.format("%.1f", passRate)
            });
        }
        
        System.out.println("Total modules in grade distribution: " + gradeModel.getRowCount());
    }

    private void updateSummaryPanel() {
        int totalModules = moduleMap.size();
        int totalAssignments = countAssignments();
        double overallAvg = calculateOverallWeightedAverage();
        int totalStudents = countUniqueStudents();
        double passRate = calculateOverallPassRate();

        System.out.println("=== Summary Stats ===");
        System.out.println("Total Modules: " + totalModules);
        System.out.println("Total Assignments: " + totalAssignments);
        System.out.println("Overall Average: " + overallAvg);
        System.out.println("Total Students: " + totalStudents);
        System.out.println("Pass Rate: " + passRate);

        // Remove old summary and create new one
        tabbedPane.remove(3);
        
        JPanel newSummary = new JPanel(new BorderLayout());
        newSummary.setBackground(Color.WHITE);
        newSummary.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("Summary Dashboard");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(new Color(255, 165, 0));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        newSummary.add(title, BorderLayout.NORTH);

        JPanel cardsPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        cardsPanel.setBackground(Color.WHITE);
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));

        cardsPanel.add(createStatCard("Total Modules", String.valueOf(totalModules), new Color(52, 152, 219)));
        cardsPanel.add(createStatCard("Module Assignments", String.valueOf(totalAssignments), new Color(155, 89, 182)));
        cardsPanel.add(createStatCard("Average Score", String.format("%.1f%%", overallAvg), new Color(46, 204, 113)));
        cardsPanel.add(createStatCard("Total Students", String.valueOf(totalStudents), new Color(230, 126, 34)));
        cardsPanel.add(createStatCard("Pass Rate", String.format("%.1f%%", passRate), new Color(26, 188, 156)));
        cardsPanel.add(createStatCard("Leader ID", tpNumber, new Color(255, 165, 0)));

        newSummary.add(cardsPanel, BorderLayout.CENTER);

        JLabel footer = new JLabel("Report generated for: " + name + " | " + new java.util.Date());
        footer.setFont(new Font("Arial", Font.ITALIC, 12));
        footer.setForeground(Color.GRAY);
        footer.setHorizontalAlignment(SwingConstants.CENTER);
        newSummary.add(footer, BorderLayout.SOUTH);

        tabbedPane.addTab("📋 Summary Dashboard", newSummary);
    }

    private String getStudentName(String studentID) {
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.STUDENT.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine(); // Skip header
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] data = line.split(",");
                if (data.length >= 9 && data[8].trim().equals(studentID)) {
                    br.close();
                    return data[0].trim();
                }
            }
            br.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return studentID;
    }

    private int countStudents(String moduleID) {
        Set<String> students = new HashSet<>();
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.MARKS.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine(); // Skip header
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] data = line.split(",");
                if (data.length >= 2 && data[1].trim().equals(moduleID)) {
                    students.add(data[0].trim());
                }
            }
            br.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return students.size();
    }

    private int countAssignments() {
        int count = 0;
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.MODULE_ASSIGNMENTS.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine(); // Skip header
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] data = line.split(",");
                if (data.length >= 1 && moduleMap.containsKey(data[0].trim())) {
                    count++;
                }
            }
            br.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return count;
    }

    private double calculateOverallWeightedAverage() {
        double totalAverage = 0;
        int moduleCount = 0;
        
        for (String moduleID : moduleMap.keySet()) {
            double avg = calculateWeightedAverage(moduleID);
            if (avg > 0) {
                totalAverage += avg;
                moduleCount++;
            }
        }
        
        return moduleCount > 0 ? totalAverage / moduleCount : 0;
    }

    private int countUniqueStudents() {
        Set<String> students = new HashSet<>();
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.MARKS.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine(); // Skip header
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] data = line.split(",");
                if (data.length >= 2 && moduleMap.containsKey(data[1].trim())) {
                    students.add(data[0].trim());
                }
            }
            br.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return students.size();
    }

    private double calculateOverallPassRate() {
        int passed = 0, total = 0;
        
        for (String moduleID : moduleMap.keySet()) {
            String gradingID = getModuleGradingID(moduleID);
            Map<String, Double> studentScores = new HashMap<>();
            Map<String, Double> assessmentWeights = new HashMap<>();
            
            // Get assessment weights
            try {
                String filePath = fileManager.getFilePath(DatabaseFile.ASSESSMENTS.getFileName());
                BufferedReader br = new BufferedReader(new FileReader(filePath));
                String line = br.readLine(); // Skip header
                
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    
                    String[] data = line.split(",");
                    if (data.length >= 4 && data[1].trim().equals(moduleID)) {
                        String assessmentID = data[0].trim();
                        double weight;
                        try {
                            weight = Double.parseDouble(data[4].trim());
                        } catch (Exception e) {
                            weight = Double.parseDouble(data[3].trim());
                        }
                        assessmentWeights.put(assessmentID, weight);
                    }
                }
                br.close();
            } catch (Exception ex) {}
            
            // Calculate student weighted scores
            try {
                String filePath = fileManager.getFilePath(DatabaseFile.MARKS.getFileName());
                BufferedReader br = new BufferedReader(new FileReader(filePath));
                String line = br.readLine(); // Skip header
                
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    
                    String[] data = line.split(",");
                    if (data.length >= 4 && data[1].trim().equals(moduleID)) {
                        String studentID = data[0].trim();
                        String assessmentID = data[2].trim();
                        double marks = Double.parseDouble(data[3].trim());
                        
                        if (assessmentWeights.containsKey(assessmentID)) {
                            double weight = assessmentWeights.get(assessmentID);
                            double contribution = (marks / 100.0) * weight;
                            studentScores.put(studentID, studentScores.getOrDefault(studentID, 0.0) + contribution);
                        }
                    }
                }
                br.close();
            } catch (Exception ex) {}
            
            // Check pass/fail
            for (double score : studentScores.values()) {
                total++;
                String grade = calculateGradeFromMarks(score, gradingID);
                if (!grade.startsWith("F")) {
                    passed++;
                }
            }
        }
        
        return total > 0 ? (passed * 100.0 / total) : 0;
    }

    private void exportReport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Report");
        fileChooser.setSelectedFile(new java.io.File("report_" + tpNumber + ".txt"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try (java.io.PrintWriter pw = new java.io.PrintWriter(fileChooser.getSelectedFile())) {
                pw.println("========================================");
                pw.println("       APU ACADEMIC REPORT");
                pw.println("========================================");
                pw.println("Leader: " + name + " (" + tpNumber + ")");
                pw.println("Generated: " + new java.util.Date());
                pw.println();

                pw.println("--- MODULE OVERVIEW ---");
                for (int i = 0; i < moduleModel.getRowCount(); i++) {
                    pw.printf("%s | %s | Credits: %s | Avg: %s%n",
                        moduleModel.getValueAt(i, 0),
                        moduleModel.getValueAt(i, 1),
                        moduleModel.getValueAt(i, 2),
                        moduleModel.getValueAt(i, 7));
                }
                pw.println();

                pw.println("--- GRADE DISTRIBUTION ---");
                for (int i = 0; i < gradeModel.getRowCount(); i++) {
                    pw.printf("%s | A:%s B:%s C:%s D:%s F:%s | Pass Rate: %s%%%n",
                        gradeModel.getValueAt(i, 0),
                        gradeModel.getValueAt(i, 2),
                        gradeModel.getValueAt(i, 3),
                        gradeModel.getValueAt(i, 4),
                        gradeModel.getValueAt(i, 5),
                        gradeModel.getValueAt(i, 6),
                        gradeModel.getValueAt(i, 8));
                }

                JOptionPane.showMessageDialog(this, "Report exported successfully!");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error exporting report: " + ex.getMessage());
            }
        }
    }
}