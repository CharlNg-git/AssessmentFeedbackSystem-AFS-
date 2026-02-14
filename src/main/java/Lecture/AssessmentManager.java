package Lecture;

import util.FileManager;
import util.DatabaseFile;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class AssessmentManager extends JPanel {
    private FileManager fileManager = new FileManager();
    private Lecturer lecturer;

    private JComboBox<Module> cmbModule;
    private JTable assessmentTable;
    private DefaultTableModel tableModel;
    private JLabel lblTotalCount, lblAvgWeight;

    public AssessmentManager(Lecturer lecturer) {
        this.lecturer = lecturer;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);

        loadModules();
        if (cmbModule.getItemCount() > 0) {
            cmbModule.setSelectedIndex(0);
            loadAssessments();
        }
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(255, 165, 0));
        header.setPreferredSize(new Dimension(0, 80));

        JLabel title = new JLabel("  Assessment Management");
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

        // Top: Module Selection + Stats
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(Color.WHITE);

        // Module Selector
        JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        selectorPanel.setBackground(Color.WHITE);

        JLabel lblModule = new JLabel("Select Module:");
        lblModule.setFont(new Font("Arial", Font.BOLD, 14));
        selectorPanel.add(lblModule);

        cmbModule = new JComboBox<>();
        cmbModule.setPreferredSize(new Dimension(300, 35));
        cmbModule.setFont(new Font("Arial", Font.PLAIN, 13));
        cmbModule.addActionListener(e -> loadAssessments());
        selectorPanel.add(cmbModule);

        topPanel.add(selectorPanel, BorderLayout.NORTH);

        // Statistics Cards
        topPanel.add(createStatsPanel(), BorderLayout.CENTER);

        content.add(topPanel, BorderLayout.NORTH);

        // Center: Assessment Table
        String[] columns = { "Assessment ID", "Name", "Type", "Weight (%)", "Due Date" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        assessmentTable = new JTable(tableModel);
        assessmentTable.setRowHeight(35);
        assessmentTable.setFont(new Font("Arial", Font.PLAIN, 14));
        assessmentTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        assessmentTable.getTableHeader().setBackground(new Color(255, 165, 0));
        assessmentTable.getTableHeader().setForeground(Color.WHITE);
        assessmentTable.getTableHeader().setPreferredSize(new Dimension(0, 40));
        assessmentTable.setSelectionBackground(new Color(255, 235, 205));
        assessmentTable.setGridColor(new Color(220, 220, 220));

        JScrollPane scrollPane = new JScrollPane(assessmentTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(255, 165, 0), 2));
        content.add(scrollPane, BorderLayout.CENTER);

        // Bottom: Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton btnViewStudents = new JButton("View Students");
        btnViewStudents.setBackground(new Color(70, 130, 180));
        btnViewStudents.setForeground(Color.WHITE);
        btnViewStudents.setFont(new Font("Arial", Font.BOLD, 14));
        btnViewStudents.setPreferredSize(new Dimension(160, 40));
        btnViewStudents.setFocusPainted(false);
        btnViewStudents.addActionListener(e -> showStudentsDialog());
        buttonPanel.add(btnViewStudents);

        JButton btnAdd = new JButton("+ Add Assessment");
        btnAdd.setBackground(new Color(46, 204, 113));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Arial", Font.BOLD, 14));
        btnAdd.setPreferredSize(new Dimension(180, 40));
        btnAdd.setFocusPainted(false);
        btnAdd.addActionListener(e -> showAddDialog());
        buttonPanel.add(btnAdd);

        JButton btnDelete = new JButton("Delete Assessment");
        btnDelete.setBackground(new Color(220, 53, 69));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setFont(new Font("Arial", Font.BOLD, 14));
        btnDelete.setPreferredSize(new Dimension(180, 40));
        btnDelete.setFocusPainted(false);
        btnDelete.addActionListener(e -> deleteSelectedAssessment());
        buttonPanel.add(btnDelete);

        content.add(buttonPanel, BorderLayout.SOUTH);

        return content;
    }

    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        JPanel totalCard = createStatCard("Total Assessments", "0", new Color(70, 130, 180));
        lblTotalCount = (JLabel) ((JPanel) totalCard.getComponent(0)).getComponent(1);
        statsPanel.add(totalCard);

        JPanel weightCard = createStatCard("Total Weight", "0%", new Color(34, 139, 34));
        lblAvgWeight = (JLabel) ((JPanel) weightCard.getComponent(0)).getComponent(1);
        statsPanel.add(weightCard);

        return statsPanel;
    }

    private JPanel createStatCard(String title, String value, Color borderColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 3),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)));

        JPanel contentPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        contentPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        titleLabel.setForeground(Color.GRAY);

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 28));
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
        tableModel.setRowCount(0);
        Module selected = (Module) cmbModule.getSelectedItem();
        if (selected == null)
            return;

        // Get due dates from file
        Map<String, String> dueDateMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(
                new FileReader(fileManager.getFilePath(DatabaseFile.ASSESSMENTS.getFileName())))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) {
                    first = false;
                    continue;
                }
                if (line.trim().isEmpty())
                    continue;

                String[] parts = line.split(",");
                if (parts.length >= 5 && parts[1].trim().equals(selected.getId())) {
                    String id = parts[0].trim();
                    String dueDate = parts.length > 5 ? parts[5].trim() : (parts.length > 4 ? parts[4].trim() : "-");
                    dueDateMap.put(id, dueDate);
                }
            }
        } catch (IOException e) {
        }

        List<Assessment> assessments = AssessmentService.getAssessmentsForModule(selected.getId());

        int totalWeight = 0;
        for (Assessment a : assessments) {
            String dueDate = dueDateMap.getOrDefault(a.getId(), "-");
            tableModel.addRow(new Object[] {
                    a.getId(), a.getName(), a.getType(), a.getMaxMarks() + "%", dueDate
            });
            totalWeight += a.getMaxMarks();
        }

        // Update statistics
        lblTotalCount.setText(String.valueOf(assessments.size()));

        // Update Total Weight with color coding
        lblAvgWeight.setText(totalWeight + "%");

        // Color code based on total weight
        if (totalWeight == 100) {
            lblAvgWeight.setForeground(new Color(34, 139, 34)); // Green if exactly 100%
        } else if (totalWeight > 100) {
            lblAvgWeight.setForeground(new Color(220, 53, 69)); // Red if over 100%
        } else {
            lblAvgWeight.setForeground(new Color(255, 165, 0)); // Orange if under 100%
        }
    }

    private void showStudentsDialog() {
        Module selected = (Module) cmbModule.getSelectedItem();
        if (selected == null)
            return;

        List<String> studentIds = StudentService.getStudentsForModule(selected.getId());
        Map<String, String> names = StudentService.getStudentNames();

        StringBuilder sb = new StringBuilder(String.format("Enrolled Students: %d\n\n", studentIds.size()));
        for (String sid : studentIds) {
            sb.append(sid).append(" - ").append(names.getOrDefault(sid, "Unknown")).append("\n");
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Arial", Font.PLAIN, 13));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        JOptionPane.showMessageDialog(this, scrollPane, "Enrolled Students", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAddDialog() {
        Module selected = (Module) cmbModule.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a module first.");
            return;
        }

        JTextField nameField = new JTextField();
        String[] types = { "Assignment", "Test", "Exam", "Lab", "Presentation" };
        JComboBox<String> typeCombo = new JComboBox<>(types);
        JTextField weightField = new JTextField();
        JTextField dateField = new JTextField("YYYY-MM-DD");

        Object[] message = {
                "Assessment Name:", nameField,
                "Type:", typeCombo,
                "Weight/Marks:", weightField,
                "Due Date:", dateField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add New Assessment", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText();
                String type = (String) typeCombo.getSelectedItem();
                int weight = Integer.parseInt(weightField.getText());
                String date = dateField.getText();

                // Validate date
                if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    throw new Exception("Invalid date format. Use YYYY-MM-DD");
                }
                int year = Integer.parseInt(date.substring(0, 4));
                int month = Integer.parseInt(date.substring(5, 7));
                int day = Integer.parseInt(date.substring(8, 10));

                if (year < 2020 || year > 2035)
                    throw new Exception("Year must be between 2020 and 2035");
                if (month < 1 || month > 12)
                    throw new Exception("Month must be between 1 and 12");
                if (day < 1 || day > 31)
                    throw new Exception("Day must be between 1 and 31");

                // Validate total weight
                List<Assessment> existingAssessments = AssessmentService.getAssessmentsForModule(selected.getId());
                int currentTotalWeight = 0;
                for (Assessment a : existingAssessments) {
                    currentTotalWeight += a.getMaxMarks();
                }
                int newTotalWeight = currentTotalWeight + weight;
                if (newTotalWeight > 100) {
                    throw new Exception(String.format(
                            "Total weight would be %d%%. Cannot exceed 100%%.\nCurrent total: %d%%",
                            newTotalWeight, currentTotalWeight));
                }

                String id = generateNextAssessmentId();
                Assessment newAssessment = new Assessment(id, selected.getId(), name, type, weight);
                AssessmentService.addAssessment(newAssessment, date);

                loadAssessments();
                JOptionPane.showMessageDialog(this, "Assessment added successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid Input: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String generateNextAssessmentId() {
        try (BufferedReader br = new BufferedReader(
                new FileReader(fileManager.getFilePath(DatabaseFile.ASSESSMENTS.getFileName())))) {
            String line, lastID = "A000";
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) {
                    first = false;
                    continue;
                }
                String[] data = line.split(",");
                if (data.length > 0 && data[0].trim().startsWith("A")) {
                    lastID = data[0].trim();
                }
            }
            int id = Integer.parseInt(lastID.substring(1));
            return String.format("A%03d", id + 1);
        } catch (Exception e) {
            return "A001";
        }
    }

    private void deleteSelectedAssessment() {
        int selectedRow = assessmentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an assessment to delete.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String assessmentId = (String) tableModel.getValueAt(selectedRow, 0);
        String assessmentName = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete \"" + assessmentName + "\" (" + assessmentId + ")?\n" +
                        "This action cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            List<String> lines = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(
                    new FileReader(fileManager.getFilePath(DatabaseFile.ASSESSMENTS.getFileName())))) {
                String line;
                boolean isFirstLine = true;
                while ((line = br.readLine()) != null) {
                    // Always keep the header (first line)
                    if (isFirstLine) {
                        lines.add(line);
                        isFirstLine = false;
                        continue;
                    }

                    // Keep all lines EXCEPT the one matching the assessment ID
                    String[] parts = line.split(",");
                    if (parts.length > 0 && !parts[0].trim().equals(assessmentId)) {
                        lines.add(line);
                    }
                }
            } catch (IOException e) {
            }

            try (BufferedWriter bw = new BufferedWriter(
                    new FileWriter(fileManager.getFilePath(DatabaseFile.ASSESSMENTS.getFileName())))) {
                for (String l : lines) {
                    bw.write(l);
                    bw.newLine();
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error deleting: " + e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Reload after file is fully written and closed
            loadAssessments();
            JOptionPane.showMessageDialog(this, "Assessment deleted successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
}