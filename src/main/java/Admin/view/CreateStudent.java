package Admin.view;

import util.FileManager;
import util.DatabaseFile;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;

public class CreateStudent extends JDialog {
    private FileManager fileManager = new FileManager();
    private ManageStudent parentPanel;
    
    private JTextField txtName, txtIC, txtRace, txtAddress, txtContact, txtEmail, txtPass, txtIntake;
    private JComboBox<String> cmbGender, cmbNationality;
    private JTextField txtDOB;
    private JLabel lblTpNumber;

    public CreateStudent(Frame parent, ManageStudent parentPanel) {
        super(parent, "Create New Student", true);
        this.parentPanel = parentPanel;
        setSize(1200, 800);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);
        generateNextID();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setBackground(new Color(255, 165, 0));
        header.setPreferredSize(new Dimension(0, 80));
        
        JLabel title = new JLabel("  CREATE NEW STUDENT");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        header.add(title);
        return header;
    }

    private JPanel createBody() {
        JPanel bodyPanel = new JPanel(new GridLayout(1, 2, 60, 0));
        bodyPanel.setBackground(Color.WHITE);
        bodyPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 50, 50));

        bodyPanel.add(createLeftPanel());
        bodyPanel.add(createRightPanel());
        return bodyPanel;
    }

    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 5, 12, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        addLabelAndField(leftPanel, gbc, 0, "Full Name (as per ID):", txtName = new JTextField());
        addLabelAndField(leftPanel, gbc, 1, "ID Number (Passport/NRIC):", txtIC = new JTextField());
        
        cmbGender = new JComboBox<>(new String[]{"Male", "Female"});
        cmbGender.setBackground(Color.WHITE);
        addLabelAndComponent(leftPanel, gbc, 2, "Gender:", cmbGender);
        
        txtDOB = new JTextField();
        txtDOB.setEditable(false);
        txtDOB.setText("YYYY-MM-DD");
        JButton btnDate = new JButton("📅");
        btnDate.setBackground(Color.LIGHT_GRAY);
        btnDate.addActionListener(e -> openDatePicker());
        
        JPanel dobPanel = new JPanel(new BorderLayout());
        dobPanel.add(txtDOB, BorderLayout.CENTER);
        dobPanel.add(btnDate, BorderLayout.EAST);
        addLabelAndComponent(leftPanel, gbc, 3, "Date of Birth:", dobPanel);

        String[] countries = {"Malaysian", "Indonesian", "Chinese", "Singaporean", "Indian", "Other"};
        cmbNationality = new JComboBox<>(countries);
        cmbNationality.setBackground(Color.WHITE);
        addLabelAndComponent(leftPanel, gbc, 4, "Nationality:", cmbNationality);
        
        addLabelAndField(leftPanel, gbc, 5, "Race:", txtRace = new JTextField());
        addLabelAndField(leftPanel, gbc, 6, "Correspondence Address:", txtAddress = new JTextField());
        addLabelAndField(leftPanel, gbc, 7, "Contact / Mobile:", txtContact = new JTextField());
        addLabelAndField(leftPanel, gbc, 8, "Email Address:", txtEmail = new JTextField());

        return leftPanel;
    }

    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        
        JLabel sysTitle = new JLabel("SYSTEM INFORMATION");
        sysTitle.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 40, 0);
        rightPanel.add(sysTitle, gbc);
        
        gbc.insets = new Insets(12, 5, 12, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = 1;
        
        lblTpNumber = new JLabel("TP000000");
        lblTpNumber.setFont(new Font("Arial", Font.BOLD, 16));
        lblTpNumber.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        lblTpNumber.setPreferredSize(new Dimension(200, 35));
        lblTpNumber.setOpaque(true);
        lblTpNumber.setBackground(new Color(240, 240, 240));
        addLabelAndComponent(rightPanel, gbc, 1, "TP Number:", lblTpNumber);
        
        addLabelAndField(rightPanel, gbc, 2, "Password:", txtPass = new JTextField());
        addLabelAndField(rightPanel, gbc, 3, "Intake Code:", txtIntake = new JTextField());

        gbc.weighty = 1.0;
        gbc.gridy = 4;
        rightPanel.add(Box.createGlue(), gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton btnSave = new JButton("SAVE");
        btnSave.setBackground(new Color(92, 184, 92));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Arial", Font.BOLD, 18));
        btnSave.setPreferredSize(new Dimension(150, 50));
        btnSave.setFocusPainted(false);
        btnSave.addActionListener(e -> handleSave());
        
        JButton btnDiscard = new JButton("DISCARD");
        btnDiscard.setBackground(new Color(217, 83, 79));
        btnDiscard.setForeground(Color.WHITE);
        btnDiscard.setFont(new Font("Arial", Font.BOLD, 18));
        btnDiscard.setPreferredSize(new Dimension(150, 50));
        btnDiscard.setFocusPainted(false);
        btnDiscard.addActionListener(e -> dispose());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnDiscard);
        
        gbc.gridy = 10;
        gbc.gridwidth = 2;
        gbc.weighty = 0;
        rightPanel.add(buttonPanel, gbc);

        return rightPanel;
    }

    private void generateNextID() {
        String nextID = "TP000001"; // Default start ID

        try (BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.USER.getFileName())))) {
            String line;
            int maxId = 0;
            
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length > 0) {
                    String currentId = data[0].trim();
                    // Check if ID starts with 'TP' (case-insensitive)
                    if (currentId.toLowerCase().startsWith("tp")) {
                        try {
                            // Extract numeric part (TP000015 -> 15)
                            int numericPart = Integer.parseInt(currentId.substring(2));
                            if (numericPart > maxId) {
                                maxId = numericPart;
                            }
                        } catch (NumberFormatException ignored) {
                            // Skip malformed IDs safely
                        }
                    }
                }
            }
            // Generate next ID (Max + 1)
            nextID = String.format("tp%06d", maxId + 1);
            
        } catch (IOException e) {
            System.out.println("User file not found, starting fresh with TP000001.");
        }

        lblTpNumber.setText(nextID);
    }

    private void handleSave() {
        if (txtName.getText().trim().isEmpty() || txtIC.getText().trim().isEmpty() || txtPass.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all essential fields!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate Contact (Digits only)
        if (!txtContact.getText().trim().matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Contact number must contain only numbers (0-9)!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate Email (Must end with @uni.edu)
        if (!txtEmail.getText().trim().endsWith("@uni.edu")) {
            JOptionPane.showMessageDialog(this, "Email must be a university email ending with '@uni.edu'!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String line = String.join(",",
            txtName.getText().trim(),
            txtIC.getText().trim(),
            (String) cmbGender.getSelectedItem(),
            txtDOB.getText().trim(),
            (String) cmbNationality.getSelectedItem(),
            txtRace.getText().trim(),
            txtAddress.getText().trim(),
            txtContact.getText().trim(),
            lblTpNumber.getText().trim(),
            txtPass.getText().trim(),
            txtIntake.getText().trim()
        );

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileManager.getFilePath(DatabaseFile.STUDENT.getFileName()), true))) {
            bw.newLine();
            bw.write(line);
            
            // Also write to user.txt
            String userLine = String.join(",", lblTpNumber.getText().trim(), "student", txtName.getText().trim(), txtEmail.getText().trim(), txtPass.getText().trim());
            try (BufferedWriter ubw = new BufferedWriter(new FileWriter(fileManager.getFilePath(DatabaseFile.USER.getFileName()), true))) {
                ubw.newLine();
                ubw.write(userLine);
            }
            
            JOptionPane.showMessageDialog(this, "Student Created Successfully!\nID: " + lblTpNumber.getText());
            parentPanel.refreshData();
            dispose();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openDatePicker() {
        JDialog d = new JDialog(this, "Select Date of Birth", true);
        d.setLayout(new FlowLayout());
        d.setSize(300, 100);
        d.setLocationRelativeTo(this);

        String[] years = new String[50];
        int currentYear = LocalDate.now().getYear();
        for (int i = 0; i < 50; i++) years[i] = String.valueOf(currentYear - 17 - i);
        
        String[] months = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};
        String[] days = new String[31];
        for (int i = 0; i < 31; i++) days[i] = String.format("%02d", i + 1);

        JComboBox<String> cbYear = new JComboBox<>(years);
        JComboBox<String> cbMonth = new JComboBox<>(months);
        JComboBox<String> cbDay = new JComboBox<>(days);
        
        JButton btnOk = new JButton("OK");
        btnOk.addActionListener(e -> {
            txtDOB.setText(cbYear.getSelectedItem() + "-" + cbMonth.getSelectedItem() + "-" + cbDay.getSelectedItem());
            d.dispose();
        });

        d.add(new JLabel("Year:")); d.add(cbYear);
        d.add(new JLabel("Month:")); d.add(cbMonth);
        d.add(new JLabel("Day:")); d.add(cbDay);
        d.add(btnOk);
        d.setVisible(true);
    }

    private void addLabelAndField(JPanel panel, GridBagConstraints gbc, int y, String label, JTextField field) {
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        field.setPreferredSize(new Dimension(250, 35));
        panel.add(field, gbc);
    }
    
    private void addLabelAndComponent(JPanel panel, GridBagConstraints gbc, int y, String label, JComponent comp) {
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        comp.setPreferredSize(new Dimension(250, 35));
        panel.add(comp, gbc);
    }
}