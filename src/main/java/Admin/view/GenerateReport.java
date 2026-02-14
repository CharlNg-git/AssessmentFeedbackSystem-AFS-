package Admin.view;

import util.FileManager;
import util.DatabaseFile;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class GenerateReport extends JPanel {
    private String tpNumber, role, name;
    private MainDashboard parent;
    private FileManager fileManager = new FileManager();
    
    private JComboBox<String> cmbIntake;
    private JComboBox<String> cmbReportType; 
    private PieChartPanel chartPanel;
    private JTextArea txtPreview;
    private JButton btnDownload;
    private Map<String, Integer> chartStats;
    
    // Grading schemes cache
    private Map<String, Integer> currentGradingScheme;

    public GenerateReport(String tpNumber, String role, String name, MainDashboard parent) {
        this.tpNumber = tpNumber;
        this.role = role;
        this.name = name;
        this.parent = parent;
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
        
        loadGradingScheme();
        loadIntakes();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(255, 165, 0));
        header.setPreferredSize(new Dimension(0, 80));
        JLabel title = new JLabel("  System Reports Generator");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);
        
        JLabel userInfo = new JLabel(tpNumber + " | ADMIN  ");
        userInfo.setFont(new Font("Arial", Font.BOLD, 18));
        userInfo.setForeground(Color.WHITE);
        header.add(userInfo, BorderLayout.EAST);
        
        return header;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout(20, 20));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        content.setBackground(Color.WHITE);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        controls.setBackground(Color.WHITE);
        controls.setBorder(BorderFactory.createTitledBorder("Report Configuration"));
        
        controls.add(new JLabel("Target Intake:"));
        cmbIntake = new JComboBox<>();
        cmbIntake.setPreferredSize(new Dimension(150, 30));
        controls.add(cmbIntake);
        
        controls.add(new JLabel("Report Type:"));
        cmbReportType = new JComboBox<>(new String[]{"Attendance Analysis", "Results Performance"});
        cmbReportType.setPreferredSize(new Dimension(180, 30));
        controls.add(cmbReportType);
        
        JButton btnPreview = new JButton("Generate Preview");
        btnPreview.setBackground(new Color(52, 152, 219));
        btnPreview.setForeground(Color.WHITE);
        btnPreview.setFocusPainted(false);
        btnPreview.addActionListener(e -> generatePreview());
        controls.add(btnPreview);
        
        content.add(controls, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerSize(5);
        
        chartPanel = new PieChartPanel();
        chartPanel.setBorder(BorderFactory.createTitledBorder("Visual Analytics"));
        chartPanel.setBackground(Color.WHITE);
        splitPane.setLeftComponent(chartPanel);
        
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createTitledBorder("Report Summary"));
        
        txtPreview = new JTextArea();
        txtPreview.setEditable(false);
        txtPreview.setFont(new Font("Monospaced", Font.PLAIN, 14));
        txtPreview.setMargin(new Insets(10, 10, 10, 10));
        rightPanel.add(new JScrollPane(txtPreview), BorderLayout.CENTER);
        
        btnDownload = new JButton("Download Report (Text File)");
        btnDownload.setBackground(new Color(46, 204, 113));
        btnDownload.setForeground(Color.WHITE);
        btnDownload.setFont(new Font("Arial", Font.BOLD, 16));
        btnDownload.setPreferredSize(new Dimension(0, 50));
        btnDownload.setEnabled(false);
        btnDownload.addActionListener(e -> downloadReport());
        rightPanel.add(btnDownload, BorderLayout.SOUTH);
        
        splitPane.setRightComponent(rightPanel);
        content.add(splitPane, BorderLayout.CENTER);

        return content;
    }

    private void loadGradingScheme() {
        currentGradingScheme = new LinkedHashMap<>();
        
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.GRADING.getFileName())));
            String line;
            String lastLine = "";
            
            // Get the last (most recent) grading scheme
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty() && !line.startsWith("gradingID")) {
                    lastLine = line;
                }
            }
            br.close();
            
            if (!lastLine.isEmpty()) {
                String[] data = lastLine.split(",");
                // Format: gradingID,date,A+_min,A+_max,A_min,A_max,B+_min,B+_max,...
                String[] grades = {"A+", "A", "B+", "B", "C+", "C", "C-", "D", "F+", "F", "F-"};
                
                int dataIndex = 2; // Start after ID and date
                for (String grade : grades) {
                    if (dataIndex < data.length) {
                        int minMark = Integer.parseInt(data[dataIndex].trim());
                        currentGradingScheme.put(grade, minMark);
                        dataIndex += 2; // Skip max, move to next min
                    }
                }
                
                System.out.println("Loaded grading scheme: " + currentGradingScheme);
            } else {
                // Default scheme if no grading.txt exists
                setDefaultGradingScheme();
            }
            
        } catch (Exception e) {
            System.err.println("Error loading grading scheme: " + e.getMessage());
            setDefaultGradingScheme();
        }
    }
    
    private void setDefaultGradingScheme() {
        currentGradingScheme.put("A+", 90);
        currentGradingScheme.put("A", 80);
        currentGradingScheme.put("B+", 75);
        currentGradingScheme.put("B", 70);
        currentGradingScheme.put("C+", 65);
        currentGradingScheme.put("C", 60);
        currentGradingScheme.put("C-", 55);
        currentGradingScheme.put("D", 50);
        currentGradingScheme.put("F+", 45);
        currentGradingScheme.put("F", 40);
        currentGradingScheme.put("F-", 0);
    }

    private void loadIntakes() {
        cmbIntake.removeAllItems();
        Set<String> intakes = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.STUDENT.getFileName())))) {
            String line = br.readLine(); // Header
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 11) {
                    intakes.add(data[10].trim());
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        for (String intake : intakes) cmbIntake.addItem(intake);
    }

    private void generatePreview() {
        String selectedIntake = (String) cmbIntake.getSelectedItem();
        String reportType = (String) cmbReportType.getSelectedItem();
        if (selectedIntake == null) return;

        if (reportType.equals("Attendance Analysis")) {
            generateAttendanceReport(selectedIntake);
        } else {
            generateResultsReport(selectedIntake);
        }
        btnDownload.setEnabled(true);
    }

    private void generateAttendanceReport(String intake) {
        chartStats = new LinkedHashMap<>();
        chartStats.put("Present", 0);
        chartStats.put("Absent", 0);
        chartStats.put("Late", 0);

        List<String> students = getStudentsInIntake(intake);
        try (BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.ATTENDANCE.getFileName())))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 4 && students.contains(data[0].trim())) {
                    String status = normalizeStatus(data[3].trim());
                    chartStats.put(status, chartStats.getOrDefault(status, 0) + 1);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        updateUI("Attendance Analysis", intake, students.size());
    }

    private void generateResultsReport(String intake) {
        chartStats = new LinkedHashMap<>();
        // Initialize all possible grades
        for (String grade : currentGradingScheme.keySet()) {
            String simpleGrade = grade.replaceAll("[+-]", ""); // A+, A, A- all become "A"
            if (!chartStats.containsKey(simpleGrade)) {
                chartStats.put(simpleGrade, 0);
            }
        }

        List<String> students = getStudentsInIntake(intake);
        
        // Calculate weighted average for each student per module
        Map<String, Map<String, Double>> studentModuleScores = new HashMap<>();
        
        try {
            // First, get assessment weights
            Map<String, Double> assessmentWeights = new HashMap<>();
            BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.ASSESSMENTS.getFileName())));
            String line = br.readLine(); // Skip header
            
            System.out.println("=== Loading Assessment Weights ===");
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] data = line.split(",");
                String assessmentID = data[0].trim();
                
                // Try to find weight - it should be a numeric value
                // Try from the end backwards to find the weight column
                double weight = 0.0;
                boolean found = false;
                
                for (int i = data.length - 1; i >= 2 && !found; i--) {
                    try {
                        weight = Double.parseDouble(data[i].trim());
                        // Make sure it's a reasonable weight (0-100)
                        if (weight >= 0 && weight <= 100) {
                            found = true;
                            System.out.println("  " + assessmentID + ": weight=" + weight + "% (column " + i + ")");
                        }
                    } catch (NumberFormatException e) {
                        // Not a number, continue to next column
                    }
                }
                
                if (found) {
                    assessmentWeights.put(assessmentID, weight);
                } else {
                    System.out.println("  " + assessmentID + ": WARNING - no valid weight found");
                }
            }
            br.close();
            
            // Then calculate weighted scores
            br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.MARKS.getFileName())));
            line = br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 4 && students.contains(data[0].trim())) {
                    String studentID = data[0].trim();
                    String moduleID = data[1].trim();
                    String assessmentID = data[2].trim();
                    double marks = Double.parseDouble(data[3].trim());
                    
                    String key = studentID + "_" + moduleID;
                    studentModuleScores.putIfAbsent(key, new HashMap<>());
                    
                    // Calculate weighted contribution
                    double weight = assessmentWeights.getOrDefault(assessmentID, 0.0);
                    double contribution = (marks / 100.0) * weight;
                    
                    studentModuleScores.get(key).put(assessmentID, contribution);
                }
            }
            br.close();
            
            // Calculate final grades
            for (Map<String, Double> assessments : studentModuleScores.values()) {
                double totalScore = assessments.values().stream().mapToDouble(Double::doubleValue).sum();
                String grade = calculateGradeFromScheme(totalScore);
                String simpleGrade = grade.replaceAll("[+-]", "");
                chartStats.put(simpleGrade, chartStats.getOrDefault(simpleGrade, 0) + 1);
            }
            
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        
        // Remove grades with 0 count for cleaner chart
        chartStats.entrySet().removeIf(entry -> entry.getValue() == 0);
        
        updateUI("Results Performance", intake, students.size());
    }

    private String calculateGradeFromScheme(double marks) {
        // Sort grades by minimum marks in descending order
        java.util.List<Map.Entry<String, Integer>> sortedGrades = new ArrayList<>(currentGradingScheme.entrySet());
        sortedGrades.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        
        for (Map.Entry<String, Integer> entry : sortedGrades) {
            if (marks >= entry.getValue()) {
                return entry.getKey();
            }
        }
        
        return "F-";
    }

    private void updateUI(String title, String intake, int count) {
        chartPanel.setStats(chartStats);
        chartPanel.repaint();

        StringBuilder sb = new StringBuilder();
        sb.append("=========================================\n");
        sb.append("       APU SYSTEM REPORT          \n");
        sb.append("=========================================\n\n");
        sb.append("Report Type:   ").append(title).append("\n");
        sb.append("Target Intake: ").append(intake).append("\n");
        sb.append("Total Students: ").append(count).append("\n");
        sb.append("Generated By:  ").append(name).append("\n");
        sb.append("Date:          ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date())).append("\n\n");
        
        int total = chartStats.values().stream().mapToInt(Integer::intValue).sum();
        chartStats.forEach((k, v) -> {
            sb.append(String.format("%-10s : %d (%.1f%%)\n", k, v, (total == 0) ? 0 : (v * 100.0 / total)));
        });

        txtPreview.setText(sb.toString());
    }

    private String normalizeStatus(String status) {
        if (status.equalsIgnoreCase("present")) return "Present";
        if (status.equalsIgnoreCase("late")) return "Late";
        return "Absent";
    }

    private List<String> getStudentsInIntake(String intake) {
        List<String> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.STUDENT.getFileName())))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 11 && data[10].trim().equalsIgnoreCase(intake)) {
                    list.add(data[8].trim());
                }
            }
        } catch (Exception ignored) {}
        return list;
    }

    private void downloadReport() {
        String selectedIntake = (String) cmbIntake.getSelectedItem();
        String reportType = (String) cmbReportType.getSelectedItem();
        
        if (selectedIntake == null || reportType == null) {
            JOptionPane.showMessageDialog(this, "Please generate a report first!");
            return;
        }
        
        // Format filename based on report type
        String typePrefix = reportType.equals("Attendance Analysis") ? "attendance_analysis" : "results_performance";
        String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String filename = String.format("%s_%s_%s.txt", typePrefix, selectedIntake, dateStr);
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(filename));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileChooser.getSelectedFile()))) {
                bw.write(txtPreview.getText());
                JOptionPane.showMessageDialog(this, 
                    "Report saved successfully!\n\nFile: " + fileChooser.getSelectedFile().getName(),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) { 
                JOptionPane.showMessageDialog(this, 
                    "Error saving report: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE); 
            }
        }
    }

    class PieChartPanel extends JPanel {
        private Map<String, Integer> stats = new HashMap<>();
        private final Color[] colors = { 
            new Color(46, 204, 113),  // Green
            new Color(52, 152, 219),  // Blue
            new Color(241, 196, 15),  // Yellow
            new Color(155, 89, 182),  // Purple
            new Color(231, 76, 60),   // Red
            new Color(149, 165, 166), // Gray
            new Color(26, 188, 156),  // Turquoise
            new Color(230, 126, 34)   // Orange
        };

        void setStats(Map<String, Integer> stats) { this.stats = stats; }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (stats == null || stats.isEmpty()) {
                g.setColor(Color.GRAY);
                g.drawString("No data to display", getWidth() / 2 - 50, getHeight() / 2);
                return;
            }
            
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Calculate sizes
            int legendHeight = stats.size() * 25 + 20;
            int availableHeight = getHeight() - legendHeight - 40;
            int size = Math.min(getWidth() - 100, availableHeight);
            int x = (getWidth() - size) / 2;
            int y = 20;
            
            int total = stats.values().stream().mapToInt(Integer::intValue).sum();
            if (total == 0) return;

            // Draw pie chart
            int startAngle = 90;
            int i = 0;
            for (Map.Entry<String, Integer> entry : stats.entrySet()) {
                int angle = (int) (entry.getValue() * 360.0 / total);
                g2.setColor(colors[i % colors.length]);
                g2.fillArc(x, y, size, size, startAngle, angle);
                startAngle += angle;
                i++;
            }

            // Draw legend BELOW the chart
            int legendY = y + size + 30;
            int legendX = 20;
            i = 0;
            for (Map.Entry<String, Integer> entry : stats.entrySet()) {
                g2.setColor(colors[i % colors.length]);
                g2.fillRect(legendX, legendY + (i * 25), 15, 15);
                
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Arial", Font.PLAIN, 12));
                String label = String.format("%s: %d (%.1f%%)", 
                    entry.getKey(), 
                    entry.getValue(), 
                    (entry.getValue() * 100.0 / total));
                g2.drawString(label, legendX + 25, legendY + (i * 25) + 12);
                i++;
            }
        }
    }
}