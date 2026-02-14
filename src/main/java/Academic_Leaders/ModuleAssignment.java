package Academic_Leaders;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import util.FileManager;
import util.DatabaseFile;
import java.nio.file.*;
import java.util.*;
import java.util.List;

public class ModuleAssignment extends JPanel {
    private String tpNumber, role, name;
    private MainDashboard parent;
    private JTable assignmentTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> cmbModule, cmbLecturer, cmbIntake;
    
    private final FileManager fileManager = new FileManager();

    public ModuleAssignment(String tpNumber, String role, String name, MainDashboard parent) {
        this.tpNumber = tpNumber;
        this.role = role;
        this.name = name;
        this.parent = parent;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // HEADER
        add(createHeader(), BorderLayout.NORTH);

        // MAIN CONTENT
        JPanel content = new JPanel(new BorderLayout(15, 15));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        content.add(createAssignmentForm(), BorderLayout.WEST);
        content.add(createTablePanel(), BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);

        refreshData();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(255, 165, 0));
        header.setPreferredSize(new Dimension(0, 80));

        JLabel title = new JLabel("  Assign Lecturers to Modules");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        header.add(title, BorderLayout.WEST);

        JLabel userInfoLabel = new JLabel(tpNumber + " | LEADER  ");
        userInfoLabel.setFont(new Font("Arial", Font.BOLD, 18));
        userInfoLabel.setForeground(Color.WHITE);
        header.add(userInfoLabel, BorderLayout.EAST);

        return header;
    }

    private JPanel createAssignmentForm() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 165, 0), 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        panel.setPreferredSize(new Dimension(350, 0));

        JLabel formTitle = new JLabel("New Assignment");
        formTitle.setFont(new Font("Arial", Font.BOLD, 18));
        formTitle.setForeground(new Color(255, 165, 0));
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(formTitle);
        panel.add(Box.createVerticalStrut(25));

        panel.add(createFormLabel("Select Module:"));
        panel.add(Box.createVerticalStrut(5));
        cmbModule = new JComboBox<>();
        cmbModule.setFont(new Font("Arial", Font.PLAIN, 14));
        cmbModule.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        cmbModule.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(cmbModule);
        panel.add(Box.createVerticalStrut(20));

        panel.add(createFormLabel("Select Lecturer:"));
        panel.add(Box.createVerticalStrut(5));
        cmbLecturer = new JComboBox<>();
        cmbLecturer.setFont(new Font("Arial", Font.PLAIN, 14));
        cmbLecturer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        cmbLecturer.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(cmbLecturer);
        panel.add(Box.createVerticalStrut(20));

        panel.add(createFormLabel("Select Intake:"));
        panel.add(Box.createVerticalStrut(5));
        cmbIntake = new JComboBox<>();
        cmbIntake.setEditable(true);
        cmbIntake.setFont(new Font("Arial", Font.PLAIN, 14));
        cmbIntake.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        cmbIntake.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(cmbIntake);
        panel.add(Box.createVerticalStrut(30));

        JButton btnAssign = createButton("Assign Lecturer", new Color(34, 139, 34));
        btnAssign.addActionListener(e -> assignLecturer());
        panel.add(btnAssign);
        panel.add(Box.createVerticalStrut(10));

        JButton btnRemove = createButton("Remove Selected", new Color(220, 20, 60));
        btnRemove.addActionListener(e -> removeAssignment());
        panel.add(btnRemove);
        panel.add(Box.createVerticalStrut(10));

        JButton btnRefresh = createButton("Refresh Data", new Color(70, 130, 180));
        btnRefresh.addActionListener(e -> refreshData());
        panel.add(btnRefresh);

        panel.add(Box.createVerticalStrut(30));
        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(15));

        JLabel infoTitle = new JLabel("Current Leader: " + name);
        infoTitle.setFont(new Font("Arial", Font.BOLD, 12));
        infoTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(infoTitle);

        JLabel infoID = new JLabel("Tp Number: " + tpNumber);
        infoID.setFont(new Font("Arial", Font.PLAIN, 12));
        infoID.setForeground(Color.GRAY);
        infoID.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(infoID);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);

        String[] columns = {"Module ID", "Module Name", "Lecturer ID", "Lecturer Name", "Intake ID"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        assignmentTable = new JTable(tableModel);
        assignmentTable.setRowHeight(30);
        assignmentTable.setFont(new Font("Arial", Font.PLAIN, 13));
        assignmentTable.getTableHeader().setBackground(new Color(255, 165, 0));
        assignmentTable.getTableHeader().setForeground(Color.WHITE);
        assignmentTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        assignmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(assignmentTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(255, 165, 0), 2),
            "Current Module Assignments",
            0, 0, new Font("Arial", Font.BOLD, 16)));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        return btn;
    }

    public void refreshData() {
        loadIntakeCodes();
        loadModules();
        loadLecturers();
        loadAssignments();
    }

    private void loadIntakeCodes() {
        cmbIntake.removeAllItems();
        
        Set<String> intakeCodes = new HashSet<>();
        
        System.out.println("=== Loading Intake Codes from studentinfo.txt ===");
        
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.STUDENT.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine(); // Skip header
            
            System.out.println("Header: " + line);
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] data = line.split(",");
                // Format: fullName,ic,gender,dob,nation,race,addr,contact,tpNumber,password,intakeCode
                if (data.length >= 11) {
                    String intakeCode = data[10].trim();
                    if (!intakeCode.isEmpty()) {
                        intakeCodes.add(intakeCode);
                    }
                }
            }
            br.close();
            
            System.out.println("Found intake codes: " + intakeCodes);
            System.out.println("Total: " + intakeCodes.size());
            
            // Sort and add to dropdown
            List<String> sortedIntakes = new ArrayList<>(intakeCodes);
            Collections.sort(sortedIntakes);
            
            for (String intake : sortedIntakes) {
                cmbIntake.addItem(intake);
            }
            
            System.out.println("=======================================");
            
        } catch (Exception ex) {
            System.err.println("Error loading intake codes: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void loadModules() {
        cmbModule.removeAllItems();
        
        System.out.println("=== Loading Modules for Leader: " + tpNumber + " ===");
        
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.MODULE.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine(); // Skip header
            
            System.out.println("module.txt header: " + line);
            
            int count = 0;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] data = line.split(",");
                // Format: moduleID,moduleName,creditHour,sessionType,Level,tpNumber,gradingID
                if (data.length >= 6) {
                    String moduleID = data[0].trim();
                    String moduleName = data[1].trim();
                    String creatorID = data[5].trim(); // Column 6
                    
                    // ONLY show modules created by this leader
                    if (creatorID.equals(tpNumber)) {
                        cmbModule.addItem(moduleID + " - " + moduleName);
                        count++;
                        System.out.println("  ✓ " + moduleID + " - " + moduleName);
                    } else {
                        System.out.println("  ✗ Skipping " + moduleID + " (owned by " + creatorID + ")");
                    }
                }
            }
            br.close();
            
            System.out.println("Total modules loaded: " + count);
            System.out.println("=======================================");
            
        } catch (Exception ex) {
            System.err.println("Error loading modules: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void loadLecturers() {
        cmbLecturer.removeAllItems();
        
        System.out.println("=== Loading Lecturers under Leader: " + tpNumber + " ===");
        
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.LECTURER_ASSIGNMENTS.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine(); // Skip header
            
            System.out.println("lecturer_assignments.txt header: " + line);
            
            int count = 0;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] data = line.split(",");
                // Format: lecturerID,leaderID
                if (data.length >= 2 && data[1].trim().equals(tpNumber)) {
                    String lecturerID = data[0].trim();
                    String lecturerName = getLecturerName(lecturerID);
                    cmbLecturer.addItem(lecturerID + " - " + lecturerName);
                    count++;
                    System.out.println("  ✓ " + lecturerID + " - " + lecturerName);
                }
            }
            br.close();
            
            System.out.println("Total lecturers loaded: " + count);
            System.out.println("=======================================");
            
        } catch (Exception ex) {
            System.err.println("Error loading lecturers: " + ex.getMessage());
            ex.printStackTrace();
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
                // Format: tpNumber,role,name,email,password
                if (data.length >= 3 && data[0].trim().equals(lecturerID)) {
                    br.close();
                    return data[2].trim();
                }
            }
            br.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return lecturerID;
    }

    private String getModuleName(String moduleID) {
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.MODULE.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine(); // Skip header
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] data = line.split(",");
                if (data.length >= 2 && data[0].trim().equals(moduleID)) {
                    br.close();
                    return data[1].trim();
                }
            }
            br.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return moduleID;
    }

    private void loadAssignments() {
        tableModel.setRowCount(0);

        System.out.println("=== Loading Module Assignments ===");

        try {
            String filePath = fileManager.getFilePath(DatabaseFile.MODULE_ASSIGNMENTS.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine(); // Skip header
            
            System.out.println("module_assignments.txt header: " + line);
            
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
                    
                    // Only show assignments for modules owned by this leader
                    if (isMyModule(moduleID)) {
                        String moduleName = getModuleName(moduleID);
                        String lecturerName = getLecturerName(lecturerID);

                        tableModel.addRow(new Object[]{
                            moduleID, moduleName, lecturerID, lecturerName, intakeID
                        });
                        count++;
                    }
                }
            }
            br.close();
            
            System.out.println("Total assignments loaded: " + count);
            System.out.println("=======================================");
            
        } catch (Exception ex) {
            System.err.println("Error loading assignments: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private boolean isMyModule(String moduleID) {
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.MODULE.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine(); // Skip header
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] data = line.split(",");
                // Format: moduleID,moduleName,creditHour,sessionType,Level,tpNumber,gradingID
                if (data.length >= 6 && data[0].trim().equals(moduleID)) {
                    String creatorID = data[5].trim();
                    br.close();
                    return creatorID.equals(tpNumber);
                }
            }
            br.close();
        } catch (Exception ex) {}
        
        return false;
    }

    private void assignLecturer() {
        if (cmbModule.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a module!");
            return;
        }
        if (cmbLecturer.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a lecturer!");
            return;
        }

        String moduleSelection = cmbModule.getSelectedItem().toString();
        String lecturerSelection = cmbLecturer.getSelectedItem().toString();
        String intakeID = cmbIntake.getSelectedItem().toString().trim();

        String moduleID = moduleSelection.split(" - ")[0];
        String lecturerID = lecturerSelection.split(" - ")[0];

        if (intakeID.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select or enter an Intake ID!");
            return;
        }

        if (assignmentExists(moduleID, lecturerID, intakeID)) {
            JOptionPane.showMessageDialog(this, "This assignment already exists!");
            return;
        }

        try {
            String filePath = fileManager.getFilePath(DatabaseFile.MODULE_ASSIGNMENTS.getFileName());
            List<String> lines = new ArrayList<>();
            File file = new File(filePath);
            
            if (file.exists()) {
                lines = Files.readAllLines(file.toPath());
            } else {
                lines.add("moduleID,lecturerID,intakeCode");
            }
            
            String newAssignment = String.format("%s,%s,%s", moduleID, lecturerID, intakeID);
            lines.add(newAssignment);
            Files.write(file.toPath(), lines);

            System.out.println("✓ Assignment added: " + newAssignment);

            JOptionPane.showMessageDialog(this, "Lecturer assigned successfully!");
            loadAssignments();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error assigning lecturer: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private boolean assignmentExists(String moduleID, String lecturerID, String intakeID) {
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.MODULE_ASSIGNMENTS.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine(); // Skip header
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] data = line.split(",");
                if (data.length >= 3 &&
                    data[0].trim().equals(moduleID) &&
                    data[1].trim().equals(lecturerID) &&
                    data[2].trim().equals(intakeID)) {
                    br.close();
                    return true;
                }
            }
            br.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private void removeAssignment() {
        int selectedRow = assignmentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an assignment to remove!");
            return;
        }

        String moduleID = tableModel.getValueAt(selectedRow, 0).toString();
        String lecturerID = tableModel.getValueAt(selectedRow, 2).toString();
        String intakeID = tableModel.getValueAt(selectedRow, 4).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
            "Remove this assignment?\n\n" +
            "Module: " + moduleID + "\n" +
            "Lecturer: " + lecturerID + "\n" +
            "Intake: " + intakeID,
            "Confirm Remove", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String filePath = fileManager.getFilePath(DatabaseFile.MODULE_ASSIGNMENTS.getFileName());
                List<String> lines = Files.readAllLines(Paths.get(filePath));
                
                lines.removeIf(line -> {
                    String[] data = line.split(",");
                    return data.length >= 3 &&
                           data[0].trim().equals(moduleID) &&
                           data[1].trim().equals(lecturerID) &&
                           data[2].trim().equals(intakeID);
                });
                
                Files.write(Paths.get(filePath), lines);
                
                System.out.println("✓ Assignment removed: " + moduleID + "," + lecturerID + "," + intakeID);
                
                JOptionPane.showMessageDialog(this, "Assignment removed successfully!");
                loadAssignments();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error removing assignment: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
}