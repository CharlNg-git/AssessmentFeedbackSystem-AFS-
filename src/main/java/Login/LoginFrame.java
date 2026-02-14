package Login;

import Student.*;
import Academic_Leaders.*;
import Admin.view.*;

import util.FileManager;
import util.DatabaseFile;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class LoginFrame extends JFrame {

    private JTextField txtTpNumber;  
    private JPasswordField txtPassword;

    // OODJ: File manager object
    private final FileManager fileManager = new FileManager();

    public LoginFrame() {
        setTitle("APSpace - Login");
        setSize(500, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(Color.WHITE);

        // ===== Header =====
        JPanel header = new JPanel();
        header.setBackground(new Color(255, 165, 0));
        header.setBounds(0, 0, 500, 100);
        header.setLayout(new BorderLayout());
        add(header);

        JLabel title = new JLabel("LOGIN", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.CENTER);

        JLabel uniIcon = new JLabel("Assessment Feedback System (AFS) APU", SwingConstants.CENTER);
        uniIcon.setForeground(Color.WHITE);
        header.add(uniIcon, BorderLayout.SOUTH);

        // ===== Content =====
        JPanel content = new JPanel();
        content.setBounds(0, 100, 500, 550);
        content.setLayout(null);
        content.setBackground(Color.WHITE);
        add(content);

        JLabel lblTpNumber = new JLabel("TP Number");
        lblTpNumber.setBounds(50, 100, 100, 30);
        content.add(lblTpNumber);

        txtTpNumber = new JTextField();  // Changed from tpNumber
        txtTpNumber.setBounds(50, 130, 400, 40);
        content.add(txtTpNumber);

        JLabel lblPassword = new JLabel("PASSWORD");
        lblPassword.setBounds(50, 190, 100, 30);
        content.add(lblPassword);

        txtPassword = new JPasswordField();
        txtPassword.setBounds(50, 220, 400, 40);
        content.add(txtPassword);

        JButton btnLogin = new JButton("CONTINUE");
        btnLogin.setBounds(50, 300, 400, 50);
        btnLogin.setBackground(new Color(255, 165, 0));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 16));
        btnLogin.setFocusPainted(false);
        btnLogin.addActionListener(e -> authenticate());
        content.add(btnLogin);

        setVisible(true);
    }

    // ===== Authentication Logic =====
    private void authenticate() {
        String tpNumber = txtTpNumber.getText().trim();  // Get text from txtTpNumber
        String password = new String(txtPassword.getPassword()).trim();

        // OODJ: get file path via FileManager
        String filePath = fileManager.getFilePath(
                DatabaseFile.USER.getFileName()
        );

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            br.readLine(); // skip header
            String line;

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");

                if (data.length < 5) continue;

                String fileID = data[0].trim();
                String role = data[1].trim();
                String name = data[2].trim();
                String filePassword = data[4].trim();

                if (fileID.equals(tpNumber) && filePassword.equals(password)) {
                    dispose();
                    openDashboard(role, tpNumber, name);
                    return;
                }
            }

            JOptionPane.showMessageDialog(this, "Invalid TP Number or Password");

        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Cannot read user.txt:\n" + e.getMessage()
            );
        }
    }

    // ===== Role Routing =====
    private void openDashboard(String role, String tpNumber, String name) {
        switch (role.toLowerCase()) {
            case "student":
                new Student.MainDashboard(tpNumber, name);
                break;
                
            case "leader":
                new Academic_Leaders.MainDashboard(role, tpNumber, name);
                break;

            case "lecturer":
                new Lecture.MainDashboard(tpNumber, name);
                break;

            case "admin":
                new Admin.view.MainDashboard(tpNumber, name);
                break;

            default:
                JOptionPane.showMessageDialog(this, "Unknown role: " + role);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}