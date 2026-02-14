package Student;

import util.FileManager;
import util.DatabaseFile;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class ViewResult extends JPanel {
    private FileManager fileManager = new FileManager();
    private String tpNumber;
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private JLabel cgpaLabel, totalCreditsLabel, statusLabel, modulesLabel;
    private List<ModuleResult> moduleResults;

    public ViewResult(String tpNumber) {
        this.tpNumber = tpNumber;
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
        
        loadResults();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(255, 165, 0));
        header.setPreferredSize(new Dimension(0, 80));

        JLabel title = new JLabel("  Academic Results & CGPA");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);
        
        String intakeCode = getStudentIntake(tpNumber);
        JLabel userInfoLabel = new JLabel(tpNumber + " | Intake: " + intakeCode + "  ");
        userInfoLabel.setFont(new Font("Arial", Font.BOLD, 18));
        userInfoLabel.setForeground(Color.WHITE);
        header.add(userInfoLabel, BorderLayout.EAST);

        return header;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        content.setBackground(Color.WHITE);

        // CGPA Summary Panel
        content.add(createCGPASummaryPanel(), BorderLayout.NORTH);

        // Results Table
        content.add(createResultsTablePanel(), BorderLayout.CENTER);

        // Grade Legend
        content.add(createGradeLegendPanel(), BorderLayout.SOUTH);

        return content;
    }

    private JPanel createCGPASummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 20, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));

        // CGPA Card
        cgpaLabel = new JLabel("0.00");
        JPanel cgpaCard = createSummaryCard("CGPA", cgpaLabel, new Color(255, 165, 0));
        panel.add(cgpaCard);

        // Total Credits Card
        totalCreditsLabel = new JLabel("0");
        JPanel creditsCard = createSummaryCard("Total Credits", totalCreditsLabel, new Color(70, 130, 180));
        panel.add(creditsCard);

        // Status Card
        statusLabel = new JLabel("Good Standing");
        JPanel statusCard = createSummaryCard("Academic Status", statusLabel, new Color(34, 139, 34));
        panel.add(statusCard);

        // Modules Completed Card
        modulesLabel = new JLabel("0");
        JPanel modulesCard = createSummaryCard("Modules Taken", modulesLabel, new Color(186, 85, 211));
        panel.add(modulesCard);

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

    private JPanel createResultsTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);

        String[] columns = {"Module Code", "Module Name", "Credits", "Assessments", "Final Grade", "Grade Point", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        resultsTable = new JTable(tableModel);
        resultsTable.setRowHeight(30);
        resultsTable.setFont(new Font("Arial", Font.PLAIN, 13));
        resultsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        resultsTable.getTableHeader().setBackground(new Color(255, 165, 0));
        resultsTable.getTableHeader().setForeground(Color.WHITE);

        // Set column widths
        resultsTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        resultsTable.getColumnModel().getColumn(1).setPreferredWidth(250);
        resultsTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        resultsTable.getColumnModel().getColumn(3).setPreferredWidth(300);
        resultsTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        resultsTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        resultsTable.getColumnModel().getColumn(6).setPreferredWidth(80);

        JScrollPane scroll = new JScrollPane(resultsTable);
        scroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(255, 165, 0), 2),
            "Module Results",
            0, 0, new Font("Arial", Font.BOLD, 16)));

        panel.add(scroll, BorderLayout.CENTER);

        // Button Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(Color.WHITE);

        JButton detailsBtn = new JButton("View Assessment Details");
        detailsBtn.setBackground(new Color(70, 130, 180));
        detailsBtn.setForeground(Color.WHITE);
        detailsBtn.setFont(new Font("Arial", Font.BOLD, 14));
        detailsBtn.setFocusPainted(false);
        detailsBtn.addActionListener(e -> showAssessmentDetails());

        JButton exportBtn = new JButton("Export Transcript");
        exportBtn.setBackground(new Color(34, 139, 34));
        exportBtn.setForeground(Color.WHITE);
        exportBtn.setFont(new Font("Arial", Font.BOLD, 14));
        exportBtn.setFocusPainted(false);
        exportBtn.addActionListener(e -> exportTranscript());

        btnPanel.add(detailsBtn);
        btnPanel.add(exportBtn);

        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createGradeLegendPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createTitledBorder("Grade Point Scale"));

        String[] grades = {"A+/A (4.0)", "B+/B (3.0)", "C+/C/C- (2.0)", "D (1.7)", "F (0.0)"};
        
        for (String grade : grades) {
            JLabel label = new JLabel(grade);
            label.setFont(new Font("Arial", Font.BOLD, 12));
            label.setOpaque(true);
            label.setBackground(Color.WHITE);
            label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 165, 0), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
            panel.add(label);
        }

        return panel;
    }

    private void loadResults() {
        tableModel.setRowCount(0);
        moduleResults = new ArrayList<>();

        System.out.println("=== Loading Results for Student: " + tpNumber + " ===");

        try {
            // Step 1: Group marks by module
            Map<String, List<AssessmentMark>> moduleMarks = new HashMap<>();
            
            try (BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.MARKS.getFileName())))) {
                String line;
                boolean first = true;
                
                while ((line = br.readLine()) != null) {
                    if (first) { first = false; continue; }
                    if (line.trim().isEmpty()) continue;
                    
                    String[] data = line.split(",");
                    // Format: tpNumber,moduleID,assessmentID,marks
                    if (data.length >= 4 && data[0].trim().equals(tpNumber)) {
                        String moduleID = data[1].trim();
                        String assessmentID = data[2].trim();
                        double marks = Double.parseDouble(data[3].trim());
                        
                        if (!moduleMarks.containsKey(moduleID)) {
                            moduleMarks.put(moduleID, new ArrayList<>());
                        }
                        
                        moduleMarks.get(moduleID).add(new AssessmentMark(assessmentID, marks));
                        System.out.println("Loaded: " + moduleID + " | " + assessmentID + " | " + marks);
                    }
                }
            }
            
            System.out.println("Total modules found: " + moduleMarks.size());

            double totalGradePoints = 0;
            int totalCredits = 0;
            int moduleCount = 0;

            // Step 2: Process each module
            for (Map.Entry<String, List<AssessmentMark>> entry : moduleMarks.entrySet()) {
                String moduleID = entry.getKey();
                List<AssessmentMark> marks = entry.getValue();

                System.out.println("\n--- Processing Module: " + moduleID + " ---");

                // Get module info
                String moduleName = getModuleName(moduleID);
                int credits = getModuleCredits(moduleID);
                String gradingID = getGradingID(moduleID);
                
                System.out.println("Module: " + moduleName + " | Credits: " + credits + " | GradingID: " + gradingID);

                // Get assessment weights
                Map<String, Double> assessmentWeights = getAssessmentWeights(moduleID);

                // Calculate weighted average
                double weightedSum = 0;
                double totalWeight = 0;
                StringBuilder assessmentDetails = new StringBuilder();

                for (AssessmentMark am : marks) {
                    double weight = assessmentWeights.getOrDefault(am.assessmentID, 0.0);
                    
                    // Weighted contribution: (marks / 100) * weight
                    double contribution = (am.marks / 100.0) * weight;
                    weightedSum += contribution;
                    totalWeight += weight;
                    
                    String assessmentName = getAssessmentName(am.assessmentID);
                    assessmentDetails.append(String.format("%s: %.1f%% (weight: %.0f%%), ", 
                        assessmentName, am.marks, weight));
                    
                    System.out.println("  " + assessmentName + ": " + am.marks + "% × " + weight + "% = " + contribution);
                }

                if (assessmentDetails.length() > 0) {
                    assessmentDetails.setLength(assessmentDetails.length() - 2);
                }

                // Final mark is the weighted sum (already in percentage)
                double finalMark = weightedSum;
                System.out.println("  Weighted Sum: " + finalMark + "%");

                // Get grade and GPA from grading scheme
                GradeInfo gradeInfo = calculateGradeFromScheme(finalMark, gradingID);
                
                System.out.println("  Final: " + finalMark + "% → Grade: " + gradeInfo.grade + " | GPA: " + gradeInfo.gpa);

                String status = gradeInfo.gpa >= 1.7 ? "Pass" : "Fail";

                // Format assessment details for display
                String assessmentDetailsDisplay = assessmentDetails.toString();

                ModuleResult result = new ModuleResult(
                    moduleID, moduleName, credits, 
                    assessmentDetailsDisplay,  // Show all assessments
                    gradeInfo.grade, gradeInfo.gpa, status
                );
                moduleResults.add(result);

                tableModel.addRow(new Object[]{
                    moduleID, 
                    moduleName, 
                    credits, 
                    assessmentDetailsDisplay,  // Assessments column
                    gradeInfo.grade,           // Final Grade column
                    String.format("%.2f", gradeInfo.gpa),  // Grade Point column
                    status
                });

                totalGradePoints += gradeInfo.gpa * credits;
                totalCredits += credits;
                moduleCount++;
            }

            // Update summary
            double cgpa = totalCredits > 0 ? totalGradePoints / totalCredits : 0.0;
            cgpaLabel.setText(String.format("%.2f", cgpa));
            totalCreditsLabel.setText(String.valueOf(totalCredits));
            
            String academicStatus = cgpa >= 3.0 ? "Good Standing" : cgpa >= 2.0 ? "Probation" : "At Risk";
            statusLabel.setText(academicStatus);
            modulesLabel.setText(String.valueOf(moduleCount));
            
            System.out.println("\n=== Summary ===");
            System.out.println("CGPA: " + cgpa);
            System.out.println("Total Credits: " + totalCredits);
            System.out.println("Modules: " + moduleCount);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading results: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Map<String, Double> getAssessmentWeights(String moduleID) {
        Map<String, Double> weights = new HashMap<>();
        
        System.out.println("=== Getting Assessment Weights for Module: " + moduleID + " ===");
        
        try (BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.ASSESSMENTS.getFileName())))) {
            String line;
            boolean first = true;
            
            while ((line = br.readLine()) != null) {
                if (first) { 
                    System.out.println("assessments.txt header: " + line);
                    first = false; 
                    continue; 
                }
                if (line.trim().isEmpty()) continue;
                
                String[] data = line.split(",");
                System.out.println("Assessment line: " + java.util.Arrays.toString(data));
                
                // Try different formats:
                // Format 1: assessmentID,moduleID,name,type,weight,dueDate (6 columns)
                // Format 2: assessmentID,moduleID,name,weight,dueDate (5 columns) 
                
                if (data.length >= 5) {
                    String assessmentID = data[0].trim();
                    String fileModuleID = data[1].trim();
                    
                    if (fileModuleID.equals(moduleID)) {
                        try {
                            double weight;
                            
                            // Try to find weight column (it's a number, not a date)
                            if (data.length >= 6) {
                                // Try column 4 (index 4) - might be weight
                                try {
                                    weight = Double.parseDouble(data[4].trim());
                                    System.out.println("  Weight found at column 4: " + weight);
                                } catch (NumberFormatException e) {
                                    // Column 4 is not a number, try column 3
                                    weight = Double.parseDouble(data[3].trim());
                                    System.out.println("  Weight found at column 3: " + weight);
                                }
                            } else {
                                // Only 5 columns, weight is likely at column 3
                                weight = Double.parseDouble(data[3].trim());
                                System.out.println("  Weight found at column 3: " + weight);
                            }
                            
                            weights.put(assessmentID, weight);
                            System.out.println("  ✓ " + assessmentID + " weight: " + weight + "%");
                            
                        } catch (NumberFormatException e) {
                            System.err.println("  ✗ Could not parse weight for " + assessmentID + ": " + e.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading assessments.txt: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("Total weights loaded: " + weights.size());
        System.out.println("=========================================");
        
        return weights;
    }

    private String getGradingID(String moduleID) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.MODULE.getFileName())))) {
            String line;
            boolean first = true;
            
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; }
                if (line.trim().isEmpty()) continue;
                
                String[] data = line.split(",");
                // Format: moduleID,moduleName,creditHour,sessionType,Level,tpNumber,gradingID
                if (data.length >= 7 && data[0].trim().equals(moduleID)) {
                    return data[6].trim();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "G001"; // Default
    }

    private GradeInfo calculateGradeFromScheme(double finalMark, String gradingID) {
        List<GradeRule> gradeRules = new ArrayList<>();
        
        // Load grading scheme
        try (BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.GRADING.getFileName())))) {
            String line;
            boolean first = true;
            
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; }
                if (line.trim().isEmpty()) continue;
                
                String[] data = line.split(",");
                // Format: gradingID,date,A+_min,A+_max,A_min,A_max,...
                if (data.length >= 24 && data[0].trim().equals(gradingID)) {
                    // Parse all grade ranges
                    addGradeRule(gradeRules, data, 2, 3, "A+", 4.0);
                    addGradeRule(gradeRules, data, 4, 5, "A", 4.0);
                    addGradeRule(gradeRules, data, 6, 7, "B+", 3.0);
                    addGradeRule(gradeRules, data, 8, 9, "B", 3.0);
                    addGradeRule(gradeRules, data, 10, 11, "C+", 2.0);
                    addGradeRule(gradeRules, data, 12, 13, "C", 2.0);
                    addGradeRule(gradeRules, data, 14, 15, "C-", 2.0);
                    addGradeRule(gradeRules, data, 16, 17, "D", 1.7);
                    addGradeRule(gradeRules, data, 18, 19, "F+", 0.0);
                    addGradeRule(gradeRules, data, 20, 21, "F", 0.0);
                    if (data.length >= 24) {
                        addGradeRule(gradeRules, data, 22, 23, "F-", 0.0);
                    }
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Sort by minMarks descending
        gradeRules.sort((a, b) -> Double.compare(b.minMarks, a.minMarks));
        
        // Find matching grade
        for (GradeRule rule : gradeRules) {
            if (finalMark >= rule.minMarks && finalMark <= rule.maxMarks) {
                return new GradeInfo(rule.grade, rule.gpa);
            }
        }
        
        return new GradeInfo("F", 0.0); // Default
    }

    private void addGradeRule(List<GradeRule> rules, String[] data, int minIndex, int maxIndex, String grade, double gpa) {
        try {
            if (data.length > maxIndex) {
                double min = Double.parseDouble(data[minIndex].trim());
                double max = Double.parseDouble(data[maxIndex].trim());
                rules.add(new GradeRule(grade, min, max, gpa));
            }
        } catch (NumberFormatException e) {}
    }

    private String getAssessmentName(String assessmentID) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.ASSESSMENTS.getFileName())))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; }
                String[] data = line.split(",");
                if (data.length >= 3 && data[0].trim().equals(assessmentID)) {
                    return data[2].trim();
                }
            }
        } catch (Exception e) {}
        return assessmentID;
    }

    private String getModuleName(String moduleID) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.MODULE.getFileName())))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; }
                String[] data = line.split(",");
                if (data.length >= 2 && data[0].trim().equals(moduleID)) {
                    return data[1].trim();
                }
            }
        } catch (Exception e) {}
        return moduleID;
    }

    private int getModuleCredits(String moduleID) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.MODULE.getFileName())))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; }
                String[] data = line.split(",");
                if (data.length >= 3 && data[0].trim().equals(moduleID)) {
                    return Integer.parseInt(data[2].trim());
                }
            }
        } catch (Exception e) {}
        return 3;
    }

    private void showAssessmentDetails() {
        int selectedRow = resultsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a module to view details!");
            return;
        }

        ModuleResult result = moduleResults.get(selectedRow);
        
        StringBuilder details = new StringBuilder();
        details.append("Module: ").append(result.moduleName).append(" (").append(result.moduleCode).append(")\n");
        details.append("Credits: ").append(result.credits).append("\n");
        details.append("Final Grade: ").append(result.finalGrade).append(" (").append(String.format("%.2f", result.gradePoint)).append(" GP)\n");
        details.append("Status: ").append(result.status).append("\n\n");
        details.append("Assessment Breakdown:\n");
        details.append(result.assessments);

        JOptionPane.showMessageDialog(this, details.toString(), 
            "Assessment Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void exportTranscript() {
        StringBuilder transcript = new StringBuilder();
        transcript.append("ACADEMIC TRANSCRIPT\n");
        transcript.append("Student ID: ").append(tpNumber).append("\n");
        transcript.append("CGPA: ").append(cgpaLabel.getText()).append("\n");
        transcript.append("Total Credits: ").append(totalCreditsLabel.getText()).append("\n");
        transcript.append("Status: ").append(statusLabel.getText()).append("\n\n");
        transcript.append("=".repeat(80)).append("\n\n");

        for (ModuleResult result : moduleResults) {
            transcript.append(String.format("%-10s %-30s Credits: %d Grade: %s (%.2f GP) %s\n",
                result.moduleCode, result.moduleName, result.credits, 
                result.finalGrade, result.gradePoint, result.status));
            transcript.append("  Assessments: ").append(result.assessments).append("\n\n");
        }

        JTextArea textArea = new JTextArea(transcript.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(800, 600));

        JOptionPane.showMessageDialog(this, scrollPane, "Academic Transcript", JOptionPane.PLAIN_MESSAGE);
    }

    // Helper classes
    class AssessmentMark {
        String assessmentID;
        double marks;
        
        AssessmentMark(String assessmentID, double marks) {
            this.assessmentID = assessmentID;
            this.marks = marks;
        }
    }

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

    class ModuleResult {
        String moduleCode, moduleName, assessments, finalGrade, status;
        int credits;
        double gradePoint;

        ModuleResult(String moduleCode, String moduleName, int credits, 
                    String assessments, String finalGrade, double gradePoint, String status) {
            this.moduleCode = moduleCode;
            this.moduleName = moduleName;
            this.credits = credits;
            this.assessments = assessments;
            this.finalGrade = finalGrade;
            this.gradePoint = gradePoint;
            this.status = status;
        }
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