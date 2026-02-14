package Admin.view;

import javax.swing.*;
import java.awt.*;

public class ManageUser extends JPanel {
    private String tpNumber, role, name;
    private MainDashboard parent;

    public ManageUser(String tpNumber, String role, String name, MainDashboard parent) {
        this.tpNumber = tpNumber;
        this.role = role;
        this.name = name;
        this.parent = parent;
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(255, 165, 0));
        header.setPreferredSize(new Dimension(0, 80));

        JLabel title = new JLabel("  Manage User");
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
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 30, 0, 30);

        JButton btnStudent = createBigButton("STUDENT");
        btnStudent.addActionListener(e -> parent.showPanel("MANAGE_STUDENT"));

        JButton btnStaff = createBigButton("STAFF");
        btnStaff.addActionListener(e -> parent.showPanel("MANAGE_STAFF"));

        content.add(btnStudent, gbc);
        content.add(btnStaff, gbc);

        return content;
    }

    private JButton createBigButton(String text) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(300, 200));
        btn.setBackground(new Color(255, 165, 0));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 32));
        btn.setFocusPainted(false);
        return btn;
    }
}