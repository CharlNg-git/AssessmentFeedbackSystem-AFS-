package Admin.view;

import util.FileManager;
import util.DatabaseFile;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class ManageStaff extends JPanel {
    private FileManager fileManager = new FileManager();
    private String tpNumber, role, name;
    private MainDashboard parent;
    
    private JTable staffTable;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;

    public ManageStaff(String tpNumber, String role, String name, MainDashboard parent) {
        this.tpNumber = tpNumber;
        this.role = role;
        this.name = name;
        this.parent = parent;
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
        
        loadStaffData("");
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(255, 165, 0));
        header.setPreferredSize(new Dimension(0, 80));

        JLabel title = new JLabel("  Manage Staffs");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        JLabel userInfo = new JLabel(tpNumber + " | ADMIN  ");
        userInfo.setFont(new Font("Arial", Font.BOLD, 18));
        userInfo.setForeground(Color.WHITE);
        header.add(userInfo, BorderLayout.EAST);

        return header;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top controls
        JPanel topControls = new JPanel(new BorderLayout(10, 0));
        topControls.setBackground(Color.WHITE);

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setBackground(Color.WHITE);

        txtSearch = new JTextField(25);
        txtSearch.setFont(new Font("Arial", Font.PLAIN, 14));
        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    loadStaffData(txtSearch.getText().trim());
                }
            }
        });

        JButton btnSearch = new JButton("Search");
        btnSearch.setBackground(new Color(52, 152, 219));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFont(new Font("Arial", Font.BOLD, 14));
        btnSearch.setPreferredSize(new Dimension(120, 40));
        btnSearch.addActionListener(e -> loadStaffData(txtSearch.getText().trim()));

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setBackground(new Color(52, 152, 219));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFont(new Font("Arial", Font.BOLD, 14));
        btnRefresh.setPreferredSize(new Dimension(120, 40));
        btnRefresh.addActionListener(e -> { txtSearch.setText(""); loadStaffData(""); });

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnRefresh);
        topControls.add(searchPanel, BorderLayout.CENTER);

        JButton btnNew = new JButton("New Staff");
        btnNew.setBackground(new Color(46, 204, 113));
        btnNew.setForeground(Color.WHITE);
        btnNew.setFont(new Font("Arial", Font.BOLD, 14));
        btnNew.setPreferredSize(new Dimension(150, 40));
        btnNew.setFocusPainted(false);
        btnNew.addActionListener(e -> showCreateStaff());
        topControls.add(btnNew, BorderLayout.EAST);

        content.add(topControls, BorderLayout.NORTH);

        // Table
        String[] columns = {"No", "Full Name", "tpNumber", "Job Title", "Info", "Edit", "Delete"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { 
                return column >= 4; // Make action columns editable
            }
        };

        staffTable = new JTable(tableModel);
        staffTable.setRowHeight(40);
        staffTable.setFont(new Font("Arial", Font.PLAIN, 14));
        staffTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        staffTable.getTableHeader().setBackground(new Color(255, 165, 0));
        staffTable.getTableHeader().setForeground(Color.WHITE);

        // Set button renderers and editors for action columns
        staffTable.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer("Info", new Color(52, 152, 219)));
        staffTable.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new JCheckBox(), "Info"));
        
        staffTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer("Edit", new Color(241, 196, 15)));
        staffTable.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox(), "Edit"));
        
        staffTable.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer("Delete", new Color(231, 76, 60)));
        staffTable.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(new JCheckBox(), "Delete"));

        // Set column widths
        staffTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        staffTable.getColumnModel().getColumn(1).setPreferredWidth(250);
        staffTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        staffTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        staffTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        staffTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        staffTable.getColumnModel().getColumn(6).setPreferredWidth(80);

        JScrollPane scrollPane = new JScrollPane(staffTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(255, 165, 0), 2),
            "Staff List", 0, 0, new Font("Arial", Font.BOLD, 16)));
        content.add(scrollPane, BorderLayout.CENTER);

        return content;
    }

    private void loadStaffData(String searchQuery) {
        tableModel.setRowCount(0);
        int count = 1;

        try (BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.STAFF.getFileName())))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.toLowerCase().startsWith("fullname")) continue;
                
                String[] data = line.split(",");
                if (data.length >= 11) {
                    String fullName = data[0].trim();
                    String jobTitle = data[8].trim();
                    String id = data[10].trim();

                    if (searchQuery.isEmpty() || 
                        fullName.toLowerCase().contains(searchQuery.toLowerCase()) ||
                        id.toLowerCase().contains(searchQuery.toLowerCase())) {
                        
                        tableModel.addRow(new Object[]{count++, fullName, id, jobTitle, "Info", "Edit", "Delete"});
                    }
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }

    private void deleteStaff(String staffID) {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Delete staff " + staffID + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            // Delete from staffinfo.txt
            List<String> staffLines = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.STAFF.getFileName())))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    if (data.length >= 11 && data[10].trim().equalsIgnoreCase(staffID)) continue;
                    staffLines.add(line);
                }
            } catch (IOException e) {}

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileManager.getFilePath(DatabaseFile.STAFF.getFileName())))) {
                for (String l : staffLines) { bw.write(l); bw.newLine(); }
            } catch (IOException e) {}
            
            // Delete from user.txt
            List<String> userLines = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.USER.getFileName())))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    if (data.length >= 1 && data[0].trim().equalsIgnoreCase(staffID)) continue;
                    userLines.add(line);
                }
            } catch (IOException e) {}

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileManager.getFilePath(DatabaseFile.USER.getFileName())))) {
                for (String l : userLines) { bw.write(l); bw.newLine(); }
            } catch (IOException e) {}
            
            JOptionPane.showMessageDialog(this, "Staff deleted successfully!");
            loadStaffData("");
        }
    }

    private void showCreateStaff() {
        new CreateStaff((Frame) SwingUtilities.getWindowAncestor(this), this).setVisible(true);
    }

    public void refreshData() {
        loadStaffData("");
    }

    private void showStaffDetails(String tpNumber, String mode) {
        new StaffDetails((Frame) SwingUtilities.getWindowAncestor(this), this, tpNumber, mode).setVisible(true);
    }

    // Button Renderer
    class ButtonRenderer extends JButton implements TableCellRenderer {
        private Color buttonColor;

        public ButtonRenderer(String text, Color color) {
            setText(text);
            this.buttonColor = color;
            setOpaque(true);
            setFocusPainted(false);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(buttonColor);
            setForeground(Color.WHITE);
            setFont(new Font("Arial", Font.BOLD, 12));
            return this;
        }
    }

    // Button Editor
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean clicked;
        private int row;
        private JTable table;

        public ButtonEditor(JCheckBox checkBox, String buttonLabel) {
            super(checkBox);
            this.label = buttonLabel;
            button = new JButton(label);
            button.setOpaque(true);
            button.setFocusPainted(false);
            button.setFont(new Font("Arial", Font.BOLD, 12));
            button.setForeground(Color.WHITE);

            if (label.equals("Info")) button.setBackground(new Color(52, 152, 219));
            else if (label.equals("Edit")) button.setBackground(new Color(241, 196, 15));
            else if (label.equals("Delete")) button.setBackground(new Color(231, 76, 60));

            button.addActionListener(e -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            this.table = table;
            this.row = row;
            clicked = true;
            return button;
        }

        public Object getCellEditorValue() {
            if (clicked) {
                String tpNumber = (String) table.getValueAt(row, 2);
                
                SwingUtilities.invokeLater(() -> {
                    if (label.equals("Info")) {
                        showStaffDetails(tpNumber, "VIEW");
                    } else if (label.equals("Edit")) {
                        showStaffDetails(tpNumber, "EDIT");
                    } else if (label.equals("Delete")) {
                        deleteStaff(tpNumber);
                    }
                });
            }
            clicked = false;
            return label;
        }

        public boolean stopCellEditing() {
            clicked = false;
            return super.stopCellEditing();
        }
    }
}