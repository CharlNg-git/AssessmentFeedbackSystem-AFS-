package Admin.view;

import util.FileManager;
import util.DatabaseFile;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ManageClass extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private FileManager fileManager = new FileManager();
    private String tpNumber, role, name;
    private MainDashboard parentDashboard;
    
    // Store all classes data for refresh after search
    private List<Object[]> allClassesData = new ArrayList<>();

    public ManageClass(String tpNumber, String role, String name, MainDashboard parentDashboard) {
        this.tpNumber = tpNumber;
        this.role = role;
        this.name = name;
        this.parentDashboard = parentDashboard;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);
        
        loadClassData();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(255, 165, 0)); // Orange Theme
        header.setPreferredSize(new Dimension(0, 80));

        JLabel title = new JLabel("  Manage Classes");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        JLabel userInfo = new JLabel(tpNumber + " | ADMIN  ");
        userInfo.setFont(new Font("Arial", Font.BOLD, 18));
        userInfo.setForeground(Color.WHITE);
        header.add(userInfo, BorderLayout.EAST);

        return header;
    }

    private JPanel createBody() {
        JPanel bodyPanel = new JPanel(new BorderLayout(20, 20));
        bodyPanel.setBackground(Color.WHITE);
        bodyPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- TOP CONTROLS (Search & Buttons) ---
        JPanel controls = new JPanel(new BorderLayout());
        controls.setBackground(Color.WHITE);

        // Left: Search Bar + Buttons
        JPanel leftControl = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftControl.setBackground(Color.WHITE);

        txtSearch = new JTextField(20);
        txtSearch.setPreferredSize(new Dimension(200, 40));
        
        JButton btnSearch = new JButton("Search");
        btnSearch.setPreferredSize(new Dimension(100, 40));
        btnSearch.setBackground(new Color(230, 230, 230));
        btnSearch.setFocusPainted(false);
        btnSearch.addActionListener(e -> searchClass());

        // Refresh Button
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setPreferredSize(new Dimension(100, 40));
        btnRefresh.setBackground(new Color(52, 152, 219)); // Blue
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setFont(new Font("Arial", Font.BOLD, 12));
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadClassData(); // Reload all data
        });

        leftControl.add(txtSearch);
        leftControl.add(btnSearch);
        leftControl.add(btnRefresh); 

        // Right: New Class Button
        JButton btnNew = new JButton("New Class");
        btnNew.setPreferredSize(new Dimension(150, 40));
        btnNew.setBackground(new Color(46, 204, 113)); // Green
        btnNew.setForeground(Color.WHITE);
        btnNew.setFont(new Font("Arial", Font.BOLD, 14));
        btnNew.setFocusPainted(false);
        btnNew.addActionListener(e -> new CreateClass(parentDashboard, this).setVisible(true)); 

        controls.add(leftControl, BorderLayout.CENTER);
        controls.add(btnNew, BorderLayout.EAST);

        bodyPanel.add(controls, BorderLayout.NORTH);

        // --- CENTER TABLE ---
        String[] columns = {"No", "Class ID", "Module ID", "Intake", "Lecturer", "Duration", "Time", "Date", "Level"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        table = new JTable(model);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(255, 165, 0));
        table.getTableHeader().setForeground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(255, 165, 0), 2), "Class List"));
        scrollPane.setBackground(Color.WHITE);
        
        bodyPanel.add(scrollPane, BorderLayout.CENTER);

        // --- BOTTOM BUTTONS (Edit & Delete) ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(Color.WHITE);
        
        JButton btnEdit = new JButton("Edit Selected");
        btnEdit.setPreferredSize(new Dimension(150, 40));
        btnEdit.setBackground(new Color(52, 152, 219)); // Blue
        btnEdit.setForeground(Color.WHITE);
        btnEdit.setFont(new Font("Arial", Font.BOLD, 12));
        btnEdit.setFocusPainted(false);
        btnEdit.addActionListener(e -> editClass());
        
        JButton btnDelete = new JButton("Delete Selected");
        btnDelete.setPreferredSize(new Dimension(150, 40));
        btnDelete.setBackground(new Color(231, 76, 60)); // Red
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setFont(new Font("Arial", Font.BOLD, 12));
        btnDelete.setFocusPainted(false);
        btnDelete.addActionListener(e -> deleteClass());
        
        btnPanel.add(btnEdit);
        btnPanel.add(btnDelete);
        
        bodyPanel.add(btnPanel, BorderLayout.SOUTH);

        return bodyPanel;
    }

    public void loadClassData() {
        model.setRowCount(0);
        allClassesData.clear();
        int count = 1;
        
        System.out.println("=== Loading Class Data ===");
        
        try (BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.CLASSES.getFileName())))) {
            String line;
            String header = br.readLine(); // Skip header
            System.out.println("Header: " + header);
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] data = line.split(",");
                
                // Format: classID,moduleID,IntakeCode,tpNumber,studyLevel,Date,classStart,classEnd,day,locationOrCampus,classStatus
                if (data.length >= 11) {
                    String classID = data[0].trim();
                    String moduleID = data[1].trim();
                    String intakeCode = data[2].trim();
                    String tpNumber = data[3].trim();
                    String studyLevel = data[4].trim();
                    String date = data[5].trim();
                    String classStart = data[6].trim();
                    String classEnd = data[7].trim();
                    String day = data[8].trim();
                    String location = data[9].trim();
                    String status = data[10].trim();
                    
                    String duration = calculateDuration(classStart, classEnd);
                    String time = classStart + " - " + classEnd;

                    Object[] rowData = new Object[]{
                        count++, 
                        classID,
                        moduleID,
                        intakeCode,
                        tpNumber,
                        duration,
                        time,
                        date,
                        studyLevel
                    };
                    
                    allClassesData.add(rowData);
                    model.addRow(rowData);
                }
            }
            
            System.out.println("Total classes loaded: " + allClassesData.size());
            
        } catch (IOException e) {
            System.err.println("Error loading classes: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private String calculateDuration(String startTime, String endTime) {
        try {
            String[] start = startTime.split(":");
            String[] end = endTime.split(":");
            
            int startHour = Integer.parseInt(start[0]);
            int startMin = Integer.parseInt(start[1]);
            int endHour = Integer.parseInt(end[0]);
            int endMin = Integer.parseInt(end[1]);
            
            int totalMinutes = (endHour * 60 + endMin) - (startHour * 60 + startMin);
            double hours = totalMinutes / 60.0;
            
            if (hours == (int) hours) {
                return String.format("%d Hours", (int) hours);
            } else {
                return String.format("%.1f Hours", hours);
            }
        } catch (Exception e) {
            return "0 Hours";
        }
    }

    private void searchClass() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            // Reload all data
            model.setRowCount(0);
            int count = 1;
            for (Object[] rowData : allClassesData) {
                rowData[0] = count++; // Renumber
                model.addRow(rowData);
            }
            return;
        }

        model.setRowCount(0);
        int count = 1;
        
        for (Object[] rowData : allClassesData) {
            String classID = rowData[1].toString().toLowerCase();
            String moduleID = rowData[2].toString().toLowerCase();
            String intake = rowData[3].toString().toLowerCase();
            
            if (classID.contains(keyword) || moduleID.contains(keyword) || intake.contains(keyword)) {
                Object[] filteredRow = rowData.clone();
                filteredRow[0] = count++; // Renumber
                model.addRow(filteredRow);
            }
        }
    }
    
    private void editClass() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a class to edit!");
            return;
        }
        
        String classID = table.getValueAt(selectedRow, 1).toString();
        new EditClassDialog(parentDashboard, this, classID).setVisible(true);
    }
    
    private void deleteClass() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a class to delete!");
            return;
        }
        
        String classID = table.getValueAt(selectedRow, 1).toString();
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete class: " + classID + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                List<String> lines = new ArrayList<>();
                BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.CLASSES.getFileName())));
                String line;
                
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    // Keep line if it's the header OR if it's not the class we want to delete
                    if (line.startsWith("classID") || (data.length > 0 && !data[0].trim().equals(classID))) {
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
                
                JOptionPane.showMessageDialog(this, "Class deleted successfully!");
                loadClassData();
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error deleting class: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    // Method required by CreateClass.java
    public void refreshData() {
        loadClassData();
    }
}