// ModuleManagement.java - JPanel for internal navigation
// Format: moduleID,moduleName,creditHour,sessionType,Level,tpNumber,gradingID
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

public class ModuleManagement extends JPanel {
    private String tpNumber, role, name;
    private MainDashboard parent;
    private JTable moduleTable;
    private DefaultTableModel tableModel;
    
    private final FileManager fileManager = new FileManager();

    public ModuleManagement(String tpNumber, String role, String name, MainDashboard parent) {
        this.tpNumber = tpNumber;
        this.role = role;
        this.name = name;
        this.parent = parent;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // HEADER
        add(createHeader(), BorderLayout.NORTH);

        // MAIN CONTENT
        add(createContent(), BorderLayout.CENTER);

        loadModules();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(255, 165, 0));
        header.setPreferredSize(new Dimension(0, 80));

        JLabel title = new JLabel("  Module Management");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        header.add(title, BorderLayout.WEST);

        JLabel userInfoLabel = new JLabel(tpNumber + " | LEADER  ");
        userInfoLabel.setFont(new Font("Arial", Font.BOLD, 18));
        userInfoLabel.setForeground(Color.WHITE);
        header.add(userInfoLabel, BorderLayout.EAST);

        return header;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        content.setBackground(Color.WHITE);

        // TABLE - Updated columns to include Level and Grading ID
        String[] columns = {"Module ID", "Module Name", "Credits", "Session Type", "Level", "Grading ID", "Leader ID"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        moduleTable = new JTable(tableModel);
        moduleTable.setRowHeight(30);
        moduleTable.getTableHeader().setBackground(new Color(255, 165, 0));
        moduleTable.getTableHeader().setForeground(Color.WHITE);
        moduleTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        moduleTable.setFont(new Font("Arial", Font.PLAIN, 13));
        moduleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(moduleTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(255, 165, 0), 2),
            "All Modules",
            0, 0, new Font("Arial", Font.BOLD, 16)));
        content.add(scrollPane, BorderLayout.CENTER);

        // BUTTON PANEL
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton btnAdd = createButton("Add New Module", new Color(34, 139, 34));
        btnAdd.addActionListener(e -> addModule());
        buttonPanel.add(btnAdd);

        JButton btnEdit = createButton("Edit Selected", new Color(255, 165, 0));
        btnEdit.addActionListener(e -> editModule());
        buttonPanel.add(btnEdit);

        JButton btnDelete = createButton("Delete Selected", new Color(220, 20, 60));
        btnDelete.addActionListener(e -> deleteModule());
        buttonPanel.add(btnDelete);

        JButton btnRefresh = createButton("Refresh", new Color(70, 130, 180));
        btnRefresh.addActionListener(e -> loadModules());
        buttonPanel.add(btnRefresh);

        content.add(buttonPanel, BorderLayout.SOUTH);

        return content;
    }

    private JButton createButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(160, 40));
        return btn;
    }

    private void loadModules() {
        tableModel.setRowCount(0);

        System.out.println("=== Loading Modules ===");

        try {
            String filePath = fileManager.getFilePath(DatabaseFile.MODULE.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine(); // Skip header
            
            System.out.println("Header: " + line);

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] data = line.split(",");
                // Format: moduleID,moduleName,creditHour,sessionType,Level,tpNumber,gradingID
                if (data.length >= 7) {
                    tableModel.addRow(new Object[]{
                        data[0].trim(),  // Module ID
                        data[1].trim(),  // Module Name
                        data[2].trim(),  // Credits
                        data[3].trim(),  // Session Type
                        data[4].trim(),  // Level
                        data[6].trim(),  // Grading ID
                        data[5].trim()   // Leader ID
                    });
                } else if (data.length >= 5) {
                    // Old format without Level and GradingID
                    tableModel.addRow(new Object[]{
                        data[0].trim(),
                        data[1].trim(),
                        data[2].trim(),
                        data[3].trim(),
                        "N/A",
                        "N/A",
                        data[4].trim()
                    });
                }
            }
            br.close();
            
            System.out.println("Total modules loaded: " + tableModel.getRowCount());

        } catch (FileNotFoundException e) {
            System.err.println("module.txt not found, will create on first save");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addModule() {
        JDialog dialog = createModuleDialog("Add New Module", null);
        dialog.setVisible(true);
    }

    private void editModule() {
        int selectedRow = moduleTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a module to edit!");
            return;
        }

        // Get all 7 columns
        String[] moduleData = new String[7];
        for (int i = 0; i < 7; i++) {
            moduleData[i] = moduleTable.getValueAt(selectedRow, i).toString();
        }

        JDialog dialog = createModuleDialog("Edit Module", moduleData);
        dialog.setVisible(true);
    }

    private JDialog createModuleDialog(String title, String[] existingData) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(600, 650);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        form.setBackground(Color.WHITE);

        // Module ID - Auto-generated (read-only)
        String moduleID;
        if (existingData != null) {
            moduleID = existingData[0];  // Keep existing ID when editing
        } else {
            moduleID = generateModuleID();  // Auto-generate for new module
        }
        
        form.add(createFormLabel("Module ID:"));
        JTextField txtModuleID = new JTextField(moduleID);
        txtModuleID.setMaximumSize(new Dimension(540, 35));
        txtModuleID.setFont(new Font("Arial", Font.PLAIN, 14));
        txtModuleID.setEnabled(false);  // Always read-only
        txtModuleID.setBackground(new Color(240, 240, 240));
        form.add(txtModuleID);
        form.add(Box.createVerticalStrut(10));

        // Module Name
        form.add(createFormLabel("Module Name:*"));
        JTextField txtModuleName = new JTextField(existingData != null ? existingData[1] : "");
        txtModuleName.setMaximumSize(new Dimension(540, 35));
        txtModuleName.setFont(new Font("Arial", Font.PLAIN, 14));
        form.add(txtModuleName);
        form.add(Box.createVerticalStrut(10));

        // Credit Hours
        form.add(createFormLabel("Credit Hours:*"));
        JTextField txtCreditHours = new JTextField(existingData != null ? existingData[2] : "");
        txtCreditHours.setMaximumSize(new Dimension(540, 35));
        txtCreditHours.setFont(new Font("Arial", Font.PLAIN, 14));
        form.add(txtCreditHours);
        form.add(Box.createVerticalStrut(10));

        // Session Type
        form.add(createFormLabel("Session Type:*"));
        JComboBox<String> cmbSessionType = new JComboBox<>(new String[]{"Core", "Elective"});
        if (existingData != null) cmbSessionType.setSelectedItem(existingData[3]);
        cmbSessionType.setMaximumSize(new Dimension(540, 35));
        cmbSessionType.setFont(new Font("Arial", Font.PLAIN, 14));
        form.add(cmbSessionType);
        form.add(Box.createVerticalStrut(10));

        // Level
        form.add(createFormLabel("Level:*"));
        JComboBox<String> cmbLevel = new JComboBox<>(new String[]{"Diploma", "Degree", "Foundation"});
        if (existingData != null) cmbLevel.setSelectedItem(existingData[4]);
        cmbLevel.setMaximumSize(new Dimension(540, 35));
        cmbLevel.setFont(new Font("Arial", Font.PLAIN, 14));
        form.add(cmbLevel);
        form.add(Box.createVerticalStrut(10));

        // Grading Scheme Selection
        form.add(createFormLabel("Grading Scheme:*"));
        JComboBox<String> cmbGrading = new JComboBox<>();
        loadGradingSchemes(cmbGrading);
        if (existingData != null) {
            cmbGrading.setSelectedItem(existingData[5]);
        }
        cmbGrading.setMaximumSize(new Dimension(540, 35));
        cmbGrading.setFont(new Font("Arial", Font.PLAIN, 14));
        form.add(cmbGrading);
        form.add(Box.createVerticalStrut(10));

        // Academic Leader - Auto-filled with current user, read-only
        form.add(createFormLabel("Academic Leader:"));
        JTextField txtLeaderID = new JTextField(tpNumber + " (" + name + ")");
        txtLeaderID.setMaximumSize(new Dimension(540, 35));
        txtLeaderID.setFont(new Font("Arial", Font.PLAIN, 14));
        txtLeaderID.setEnabled(false);  // Read-only
        txtLeaderID.setBackground(new Color(240, 240, 240));
        form.add(txtLeaderID);
        form.add(Box.createVerticalStrut(20));

        // Save Button
        JPanel buttonWrapper = new JPanel(new BorderLayout());
        buttonWrapper.setBackground(Color.WHITE);
        buttonWrapper.setMaximumSize(new Dimension(540, 45));
        
        JButton btnSave = new JButton(existingData != null ? "UPDATE MODULE" : "CREATE MODULE");
        btnSave.setBackground(new Color(255, 165, 0));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Arial", Font.BOLD, 14));
        btnSave.setFocusPainted(false);
        btnSave.setPreferredSize(new Dimension(540, 45));
        
        btnSave.addActionListener(e -> {
            String selectedGrading = (String) cmbGrading.getSelectedItem();
            String gradingID = selectedGrading != null ? selectedGrading.split(" - ")[0] : "G001";
            
            if (saveModuleData(txtModuleID.getText().trim(), txtModuleName.getText().trim(),
                    txtCreditHours.getText().trim(), cmbSessionType.getSelectedItem().toString(),
                    cmbLevel.getSelectedItem().toString(),
                    tpNumber,  // Always use current user's tpNumber
                    gradingID,
                    existingData != null ? existingData[0] : null)) {
                dialog.dispose();
                loadModules();
            }
        });
        
        buttonWrapper.add(btnSave, BorderLayout.WEST);
        form.add(buttonWrapper);

        JScrollPane scrollPane = new JScrollPane(form);
        scrollPane.setBorder(null);
        dialog.add(scrollPane);

        return dialog;
    }

    private void loadGradingSchemes(JComboBox<String> combo) {
        combo.removeAllItems();
        
        System.out.println("=== Loading Grading Schemes ===");
        
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.GRADING.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine(); // Skip header
            
            System.out.println("grading.txt header: " + line);
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] data = line.split(",");
                // Format: gradingID,date,A+_min,A+_max,...
                if (data.length >= 2) {
                    String gradingID = data[0].trim();
                    String date = data[1].trim();
                    combo.addItem(gradingID + " - " + date);
                    System.out.println(gradingID + " - " + date);
                }
            }
            br.close();
            
            System.out.println("Total grading schemes loaded: " + combo.getItemCount());
            
        } catch (FileNotFoundException e) {
            System.err.println("grading.txt not found");
            combo.addItem("G001 - Default");
        } catch (Exception e) {
            e.printStackTrace();
            combo.addItem("G001 - Default");
        }
        
        System.out.println("==================================");
    }

    /**
     * Generate a new Module ID in format M### (e.g., M001, M002, M003)
     */
    private String generateModuleID() {
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.MODULE.getFileName());
            File file = new File(filePath);
            
            if (!file.exists()) {
                return "M001";  // First module
            }

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine(); // Skip header
            
            int maxNum = 0;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] data = line.split(",");
                if (data.length > 0) {
                    String moduleID = data[0].trim();
                    if (moduleID.startsWith("M")) {
                        try {
                            int num = Integer.parseInt(moduleID.substring(1));
                            maxNum = Math.max(maxNum, num);
                        } catch (NumberFormatException e) {}
                    }
                }
            }
            br.close();
            
            // Generate next ID
            return String.format("M%03d", maxNum + 1);

        } catch (Exception e) {
            e.printStackTrace();
            return "M001";
        }
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private boolean saveModuleData(String moduleID, String moduleName, String creditHours,
                                    String sessionType, String level, String leaderID, 
                                    String gradingID, String originalID) {
        if (moduleID.isEmpty() || moduleName.isEmpty() || creditHours.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields!");
            return false;
        }

        // Validate credit hours is a number
        try {
            int credits = Integer.parseInt(creditHours);
            if (credits < 1 || credits > 10) {
                JOptionPane.showMessageDialog(this, "Credit hours must be between 1 and 10!");
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Credit hours must be a valid number!");
            return false;
        }

        try {
            String filePath = fileManager.getFilePath(DatabaseFile.MODULE.getFileName());
            List<String> lines = new ArrayList<>();
            File file = new File(filePath);
            
            if (file.exists()) {
                lines = Files.readAllLines(file.toPath());
            } else {
                // Create with header if doesn't exist
                lines.add("moduleID,moduleName,creditHour,sessionType,Level,tpNumber,gradingID");
            }

            boolean updated = false;
            if (originalID != null) {
                // Edit existing - keep the same moduleID
                for (int i = 0; i < lines.size(); i++) {
                    if (lines.get(i).startsWith(originalID + ",")) {
                        lines.set(i, String.format("%s,%s,%s,%s,%s,%s,%s",
                                moduleID, moduleName, creditHours, sessionType, level, leaderID, gradingID));
                        updated = true;
                        System.out.println("Updated module: " + moduleID);
                        break;
                    }
                }
            } else {
                // Add new module
                String newLine = String.format("%s,%s,%s,%s,%s,%s,%s",
                        moduleID, moduleName, creditHours, sessionType, level, leaderID, gradingID);
                lines.add(newLine);
                updated = true;
                System.out.println("Created module: " + moduleID);
            }

            if (updated) {
                Files.write(file.toPath(), lines);
                JOptionPane.showMessageDialog(this, 
                    originalID != null ? "Module updated successfully!" : "Module created successfully!\n\nModule ID: " + moduleID);
                return true;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving module: " + ex.getMessage());
            ex.printStackTrace();
        }

        return false;
    }

    private void deleteModule() {
        int selectedRow = moduleTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a module to delete!");
            return;
        }

        String moduleID = tableModel.getValueAt(selectedRow, 0).toString();
        String moduleName = tableModel.getValueAt(selectedRow, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this module?\n\n" +
            "Module ID: " + moduleID + "\n" +
            "Module Name: " + moduleName + "\n\n" +
            "This action cannot be undone!",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String filePath = fileManager.getFilePath(DatabaseFile.MODULE.getFileName());
                List<String> lines = Files.readAllLines(Paths.get(filePath));
                
                lines.removeIf(line -> line.startsWith(moduleID + ","));
                
                Files.write(Paths.get(filePath), lines);
                
                System.out.println("Deleted module: " + moduleID);
                
                JOptionPane.showMessageDialog(this, "Module deleted successfully!");
                loadModules();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting module: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
}