package Academic_Leaders;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import util.FileManager;
import util.DatabaseFile;
import java.util.*;
import java.util.List;

public class LecturerWorkload extends JPanel {
    private String tpNumber, role, name;
    private MainDashboard parent;
    private JTable workloadTable, detailsTable;
    private DefaultTableModel workloadModel, detailsModel;
    private JLabel lblSelectedLecturer;
    private JPanel summaryCardsPanel;
    
    private final FileManager fileManager = new FileManager();
    
    private Map<String, String> lecturerNames = new HashMap<>();
    private Map<String, String> moduleNames = new HashMap<>();
    private Map<String, List<String[]>> lecturerModules = new HashMap<>();

    public LecturerWorkload(String tpNumber, String role, String name, MainDashboard parent) {
        this.tpNumber = tpNumber;
        this.role = role;
        this.name = name;
        this.parent = parent;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Header
        add(createHeader(), BorderLayout.NORTH);

        // Main Content
        add(createMainContent(), BorderLayout.CENTER);

        loadData();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(255, 165, 0));
        header.setPreferredSize(new Dimension(0, 80));

        JLabel title = new JLabel("  Lecturer Workload Dashboard");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        header.add(title, BorderLayout.WEST);

        JLabel userInfoLabel = new JLabel(tpNumber + " | LEADER  ");
        userInfoLabel.setFont(new Font("Arial", Font.BOLD, 18));
        userInfoLabel.setForeground(Color.WHITE);
        header.add(userInfoLabel, BorderLayout.EAST);

        return header;
    }

    private JPanel createMainContent() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Summary cards
        summaryCardsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        summaryCardsPanel.setBackground(Color.WHITE);
        summaryCardsPanel.setPreferredSize(new Dimension(0, 100));
        mainPanel.add(summaryCardsPanel, BorderLayout.NORTH);

        // Split pane for tables
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            createWorkloadTablePanel(),
            createDetailsPanel());
        splitPane.setDividerLocation(280);
        splitPane.setResizeWeight(0.5);

        mainPanel.add(splitPane, BorderLayout.CENTER);

        // Refresh button at bottom
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(Color.WHITE);

        JButton btnRefresh = new JButton("Refresh Data");
        btnRefresh.setBackground(new Color(70, 130, 180));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFont(new Font("Arial", Font.BOLD, 14));
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadData());
        btnPanel.add(btnRefresh);

        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    private JPanel createWorkloadTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);

        // REMOVED "Avg Performance" column
        String[] columns = {"Lecturer ID", "Lecturer Name", "Total Modules", "Total Intakes", "Workload Status"};
        workloadModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        workloadTable = new JTable(workloadModel);
        styleTable(workloadTable);

        // Renderer for workload status (now column 4 instead of 5)
        workloadTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    String status = value != null ? value.toString() : "";
                    if (status.equals("Heavy")) {
                        c.setBackground(new Color(255, 200, 200));
                        c.setForeground(new Color(200, 0, 0));
                    } else if (status.equals("Moderate")) {
                        c.setBackground(new Color(255, 240, 200));
                        c.setForeground(new Color(180, 140, 0));
                    } else if (status.equals("Light")) {
                        c.setBackground(new Color(200, 255, 200));
                        c.setForeground(new Color(0, 150, 0));
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

        workloadTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showLecturerDetails();
            }
        });

        JScrollPane scrollPane = new JScrollPane(workloadTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(255, 165, 0), 2),
            "Lecturer Workload Overview",
            0, 0, new Font("Arial", Font.BOLD, 16)));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);

        lblSelectedLecturer = new JLabel("Select a lecturer to view details");
        lblSelectedLecturer.setFont(new Font("Arial", Font.BOLD, 14));
        lblSelectedLecturer.setForeground(new Color(255, 165, 0));
        lblSelectedLecturer.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panel.add(lblSelectedLecturer, BorderLayout.NORTH);

        // REMOVED Avg Score and Pass Rate columns
        String[] columns = {"Module ID", "Module Name", "Intake", "Students"};
        detailsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        detailsTable = new JTable(detailsModel);
        styleTable(detailsTable);

        JScrollPane scrollPane = new JScrollPane(detailsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
            "Module Details",
            0, 0, new Font("Arial", Font.BOLD, 16)));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.getTableHeader().setBackground(new Color(255, 165, 0));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(new Color(220, 220, 220));
    }

    private void loadData() {
        System.out.println("=== Loading Lecturer Workload Data ===");
        
        lecturerNames.clear();
        moduleNames.clear();
        lecturerModules.clear();
        workloadModel.setRowCount(0);
        detailsModel.setRowCount(0);

        loadLecturers();
        loadModules();
        loadModuleAssignments();
        populateWorkloadTable();
        updateSummaryCards();
        
        System.out.println("Data loading complete!");
    }

    private void loadLecturers() {
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.LECTURER_ASSIGNMENTS.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine(); // Skip header
            
            System.out.println("Loading lecturers under leader: " + tpNumber);
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] data = line.split(",");
                if (data.length >= 2 && data[1].trim().equals(tpNumber)) {
                    String lecturerID = data[0].trim();
                    String lecturerName = getLecturerName(lecturerID);
                    lecturerNames.put(lecturerID, lecturerName);
                    lecturerModules.put(lecturerID, new ArrayList<>());
                    System.out.println("  ✓ " + lecturerID + " - " + lecturerName);
                }
            }
            br.close();
            
            System.out.println("Total lecturers loaded: " + lecturerNames.size());
        } catch (Exception e) {
            System.err.println("Error loading lecturers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getLecturerName(String lecturerID) {
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.USER.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine(); // Skip header
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] data = line.split(",");
                if (data.length >= 3 && data[0].trim().equals(lecturerID)) {
                    br.close();
                    return data[2].trim();
                }
            }
            br.close();
        } catch (Exception e) {}
        return lecturerID;
    }

    private void loadModules() {
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.MODULE.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine(); // Skip header
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] data = line.split(",");
                // Format: moduleID,moduleName,creditHour,sessionType,Level,tpNumber,gradingID
                if (data.length >= 2) {
                    moduleNames.put(data[0].trim(), data[1].trim());
                }
            }
            br.close();
            
            System.out.println("Total modules loaded: " + moduleNames.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadModuleAssignments() {
        System.out.println("Loading module assignments...");
        
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.MODULE_ASSIGNMENTS.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine(); // Skip header
            
            int count = 0;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] data = line.split(",");
                // Format: moduleID,lecturerID,intakeCode
                if (data.length >= 3) {
                    String moduleID = data[0].trim();
                    String lecturerID = data[1].trim();
                    String intakeID = data[2].trim();

                    // Only include if lecturer belongs to this leader
                    if (lecturerModules.containsKey(lecturerID) && moduleNames.containsKey(moduleID)) {
                        lecturerModules.get(lecturerID).add(new String[]{moduleID, intakeID});
                        count++;
                        System.out.println("  Assignment: " + lecturerID + " -> " + moduleID + " (" + intakeID + ")");
                    }
                }
            }
            br.close();
            
            System.out.println("Total assignments loaded: " + count);
        } catch (Exception e) {
            System.err.println("Error loading assignments: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void populateWorkloadTable() {
        System.out.println("Populating workload table...");
        
        for (Map.Entry<String, String> lecturer : lecturerNames.entrySet()) {
            String lecturerID = lecturer.getKey();
            String lecturerName = lecturer.getValue();

            List<String[]> modules = lecturerModules.get(lecturerID);
            
            // Count unique modules
            Set<String> uniqueModules = new HashSet<>();
            for (String[] m : modules) {
                uniqueModules.add(m[0]); // moduleID
            }
            int totalModules = uniqueModules.size();

            // Count unique intakes
            Set<String> uniqueIntakes = new HashSet<>();
            for (String[] m : modules) {
                uniqueIntakes.add(m[1]); // intakeCode
            }
            int totalIntakes = uniqueIntakes.size();

            String workloadStatus = getWorkloadStatus(totalModules);

            workloadModel.addRow(new Object[]{
                lecturerID,
                lecturerName,
                totalModules,
                totalIntakes,
                workloadStatus
            });
            
            System.out.println("  " + lecturerName + ": " + totalModules + " modules, " + totalIntakes + " intakes");
        }
    }

    private String getWorkloadStatus(int moduleCount) {
        if (moduleCount >= 4) return "Heavy";
        if (moduleCount >= 2) return "Moderate";
        if (moduleCount >= 1) return "Light";
        return "No Load";
    }

    private void showLecturerDetails() {
        int row = workloadTable.getSelectedRow();
        if (row < 0) return;

        String lecturerID = workloadModel.getValueAt(row, 0).toString();
        String lecturerName = workloadModel.getValueAt(row, 1).toString();

        lblSelectedLecturer.setText("Module Details for: " + lecturerName + " (" + lecturerID + ")");

        detailsModel.setRowCount(0);

        List<String[]> modules = lecturerModules.get(lecturerID);
        if (modules == null) return;

        for (String[] module : modules) {
            String moduleID = module[0];
            String intakeID = module[1];
            String moduleName = moduleNames.getOrDefault(moduleID, moduleID);

            int studentCount = countStudentsInIntake(intakeID);

            detailsModel.addRow(new Object[]{
                moduleID,
                moduleName,
                intakeID,
                studentCount
            });
        }
    }

    private int countStudentsInIntake(String intakeCode) {
        int count = 0;
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.STUDENT.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine(); // Skip header
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] data = line.split(",");
                // Format: fullName,ic,gender,dob,nation,race,addr,contact,tpNumber,password,intakeCode
                if (data.length >= 11 && data[10].trim().equals(intakeCode)) {
                    count++;
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    private void updateSummaryCards() {
        summaryCardsPanel.removeAll();

        int totalLecturers = lecturerNames.size();
        
        // Calculate TOTAL UNIQUE MODULES across all lecturers
        Set<String> allUniqueModules = new HashSet<>();
        for (List<String[]> modules : lecturerModules.values()) {
            for (String[] m : modules) {
                allUniqueModules.add(m[0]); // moduleID
            }
        }
        int totalModules = allUniqueModules.size();
        
        // Calculate TOTAL UNIQUE INTAKES across all lecturers
        Set<String> allUniqueIntakes = new HashSet<>();
        for (List<String[]> modules : lecturerModules.values()) {
            for (String[] m : modules) {
                allUniqueIntakes.add(m[1]); // intakeCode
            }
        }
        int totalIntakes = allUniqueIntakes.size();

        // Calculate workload distribution
        int heavyCount = 0, moderateCount = 0, lightCount = 0;
        for (List<String[]> modules : lecturerModules.values()) {
            Set<String> uniqueModules = new HashSet<>();
            for (String[] m : modules) {
                uniqueModules.add(m[0]);
            }
            int size = uniqueModules.size();
            
            if (size >= 4) heavyCount++;
            else if (size >= 2) moderateCount++;
            else if (size >= 1) lightCount++;
        }

        summaryCardsPanel.add(createSummaryCard("Total Lecturers", String.valueOf(totalLecturers), new Color(52, 152, 219)));
        summaryCardsPanel.add(createSummaryCard("Total Modules", String.valueOf(totalModules), new Color(155, 89, 182)));
        summaryCardsPanel.add(createSummaryCard("Total Intakes", String.valueOf(totalIntakes), new Color(46, 204, 113)));
        summaryCardsPanel.add(createSummaryCard("Heavy Workload", String.valueOf(heavyCount), new Color(231, 76, 60)));

        summaryCardsPanel.revalidate();
        summaryCardsPanel.repaint();
        
        System.out.println("Summary Cards Updated:");
        System.out.println("  Total Lecturers: " + totalLecturers);
        System.out.println("  Total Modules: " + totalModules);
        System.out.println("  Total Intakes: " + totalIntakes);
        System.out.println("  Heavy Workload: " + heavyCount);
    }

    private JPanel createSummaryCard(String label, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 28));
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(valueLabel, BorderLayout.CENTER);

        JLabel titleLabel = new JLabel(label);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(titleLabel, BorderLayout.SOUTH);

        return card;
    }
}