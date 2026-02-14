package Admin.view;

import util.FileManager;
import util.DatabaseFile;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class StudentDetails extends JDialog {
    private FileManager fileManager = new FileManager();
    private ManageStudent parentPanel;
    private String studentID;
    private String mode; // "VIEW" or "EDIT"
    
    private JTextField txtName, txtIC, txtRace, txtAddress, txtContact, txtEmail, txtPass, txtIntake;
    private JComboBox<String> cmbGender, cmbNationality;
    private JTextField txtDOB;
    private JLabel lbltpNumber;
    private JButton btnSave, btnDiscard, btnDate;

    public StudentDetails(Frame parent, ManageStudent parentPanel, String studentID, String mode) {
        super(parent, "Student Details - " + mode, true);
        this.parentPanel = parentPanel;
        this.studentID = studentID;
        this.mode = mode;
        
        setSize(1200, 800);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);
        
        loadStudentData();
        applyModeSettings();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(255, 165, 0));
        header.setPreferredSize(new Dimension(0, 80));

        JLabel title = new JLabel("  STUDENT DETAILS (" + mode + ")");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

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
        
        addLabelAndField(leftPanel, gbc, 0, "Full Name:", txtName = new JTextField());
        addLabelAndField(leftPanel, gbc, 1, "IC Number:", txtIC = new JTextField());
        
        cmbGender = new JComboBox<>(new String[]{"Male", "Female"});
        cmbGender.setBackground(Color.WHITE);
        addLabelAndComponent(leftPanel, gbc, 2, "Gender:", cmbGender);
        
        txtDOB = new JTextField();
        txtDOB.setEditable(false);
        btnDate = new JButton("📅");
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
        addLabelAndField(leftPanel, gbc, 6, "Address:", txtAddress = new JTextField());
        addLabelAndField(leftPanel, gbc, 7, "Contact:", txtContact = new JTextField());
        addLabelAndField(leftPanel, gbc, 8, "Email:", txtEmail = new JTextField());

        return leftPanel;
    }

    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        
        JLabel sysTitle = new JLabel("SYSTEM INFO");
        sysTitle.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 40, 0);
        rightPanel.add(sysTitle, gbc);
        
        gbc.insets = new Insets(12, 5, 12, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = 1;
        
        lbltpNumber = new JLabel(studentID);
        lbltpNumber.setFont(new Font("Arial", Font.BOLD, 16));
        lbltpNumber.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        lbltpNumber.setPreferredSize(new Dimension(250, 35));
        lbltpNumber.setOpaque(true);
        lbltpNumber.setBackground(new Color(240, 240, 240));
        addLabelAndComponent(rightPanel, gbc, 1, "Tp Number:", lbltpNumber);
        
        addLabelAndField(rightPanel, gbc, 2, "Password:", txtPass = new JTextField());
        addLabelAndField(rightPanel, gbc, 3, "Intake Code:", txtIntake = new JTextField());
        
        gbc.weighty = 1.0;
        rightPanel.add(Box.createGlue(), gbc);
        
        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnP.setBackground(Color.WHITE);
        
        btnSave = new JButton("SAVE");
        btnSave.setBackground(new Color(92, 184, 92));
        btnSave.setForeground(Color.WHITE);
        btnSave.setPreferredSize(new Dimension(150, 50));
        btnSave.addActionListener(e -> saveChanges());
        
        btnDiscard = new JButton("CLOSE");
        btnDiscard.setBackground(new Color(217, 83, 79));
        btnDiscard.setForeground(Color.WHITE);
        btnDiscard.setPreferredSize(new Dimension(150, 50));
        btnDiscard.addActionListener(e -> dispose());
        
        btnP.add(btnSave);
        btnP.add(btnDiscard);
        
        gbc.gridy = 10;
        gbc.gridwidth = 2;
        gbc.weighty = 0;
        rightPanel.add(btnP, gbc);

        return rightPanel;
    }

    private void loadStudentData() {
        try (BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.STUDENT.getFileName())))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.toLowerCase().startsWith("fullname")) continue;
                String[] data = line.split(",");
                // tpNumber is index 8
                if (data.length >= 11 && data[8].trim().equalsIgnoreCase(studentID)) {
                    txtName.setText(data[0].trim());
                    txtIC.setText(data[1].trim());
                    cmbGender.setSelectedItem(data[2].trim());
                    txtDOB.setText(data[3].trim());
                    cmbNationality.setSelectedItem(data[4].trim());
                    txtRace.setText(data[5].trim());
                    txtAddress.setText(data[6].trim());
                    txtContact.setText(data[7].trim());
                    lbltpNumber.setText(data[8].trim());
                    txtPass.setText(data[9].trim());
                    txtIntake.setText(data[10].trim());
                    break;
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        
        // Load Email from user.txt
        txtEmail.setText(getEmailFromUserFile(studentID));
    }

    private String getEmailFromUserFile(String id) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.USER.getFileName())))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 4 && data[0].trim().equalsIgnoreCase(id)) {
                    return data[3].trim(); 
                }
            }
        } catch (Exception e) {}
        return ""; 
    }

    private void saveChanges() {
        if (txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty!");
            return;
        }

        // --- NEW VALIDATION START ---
        
        // Validate Contact (Must be digits only)
        if (!txtContact.getText().trim().matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Contact number must contain only numbers (0-9)!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate Email (Must end with @uni.edu09)
        if (!txtEmail.getText().trim().endsWith("@uni.edu")) {
            JOptionPane.showMessageDialog(this, "Email must be a university email ending with '@uni.edu'!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // --- NEW VALIDATION END ---

        List<String> lines = new ArrayList<>();
        // Update studentinfo.txt
        try (BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.STUDENT.getFileName())))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 11 && data[8].trim().equalsIgnoreCase(studentID)) {
                    String updated = String.join(",", 
                        txtName.getText().trim(), txtIC.getText().trim(), (String)cmbGender.getSelectedItem(),
                        txtDOB.getText().trim(), (String)cmbNationality.getSelectedItem(), txtRace.getText().trim(),
                        txtAddress.getText().trim(), txtContact.getText().trim(), 
                        studentID, txtPass.getText().trim(), txtIntake.getText().trim()
                    );
                    lines.add(updated);
                } else { lines.add(line); }
            }
        } catch (IOException e) {}

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileManager.getFilePath(DatabaseFile.STUDENT.getFileName())))) {
            for (String l : lines) { bw.write(l); bw.newLine(); }
        } catch (IOException e) {}

        // Update user.txt (Email, Name, Password)
        List<String> userLines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.USER.getFileName())))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 4 && data[0].trim().equalsIgnoreCase(studentID)) {
                    // Preserve ID, Role, Update Name, Email, Password
                    String updated = String.join(",", 
                        data[0].trim(), data[1].trim(), txtName.getText().trim(), 
                        txtEmail.getText().trim(), txtPass.getText().trim()
                    );
                    userLines.add(updated);
                } else { userLines.add(line); }
            }
        } catch (IOException e) {}

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileManager.getFilePath(DatabaseFile.USER.getFileName())))) {
            for (String l : userLines) { bw.write(l); bw.newLine(); }
            
            parentPanel.refreshData();
            JOptionPane.showMessageDialog(this, "Student Details Updated!");
            dispose();
        } catch (IOException e) {}
    }

    private void applyModeSettings() {
        if (mode.equalsIgnoreCase("VIEW")) {
            txtName.setEditable(false); txtIC.setEditable(false); txtRace.setEditable(false);
            txtAddress.setEditable(false); txtContact.setEditable(false); txtEmail.setEditable(false);
            txtPass.setEditable(false); txtIntake.setEditable(false);
            cmbGender.setEnabled(false); cmbNationality.setEnabled(false); btnDate.setEnabled(false);
            btnSave.setVisible(false); btnDiscard.setText("CLOSE");
        } else { btnDiscard.setText("DISCARD"); }
    }

    private void openDatePicker() {
        JDialog d = new JDialog(this, "Select Date", true);
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

        d.add(cbYear); d.add(cbMonth); d.add(cbDay); d.add(btnOk);
        d.setVisible(true);
    }

    private void addLabelAndField(JPanel p, GridBagConstraints g, int y, String l, JTextField f) {
        g.gridx = 0; g.gridy = y; g.weightx = 0.3;
        JLabel lbl = new JLabel(l);
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        p.add(lbl, g);
        g.gridx = 1; g.weightx = 0.7;
        f.setPreferredSize(new Dimension(250, 35));
        p.add(f, g);
    }
    
    private void addLabelAndComponent(JPanel p, GridBagConstraints g, int y, String l, JComponent c) {
        g.gridx = 0; g.gridy = y; g.weightx = 0.3;
        JLabel lbl = new JLabel(l);
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        p.add(lbl, g);
        g.gridx = 1; g.weightx = 0.7;
        c.setPreferredSize(new Dimension(250, 35));
        p.add(c, g);
    }
}