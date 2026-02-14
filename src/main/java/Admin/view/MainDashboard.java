package Admin.view;

import javax.swing.*;
import java.awt.*;
import Login.LoginFrame;
import Student.FeedbackViewer;

public class MainDashboard extends JFrame {
    private String tpNumber, name;
    private CardLayout cardLayout;
    private JPanel contentPanel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainDashboard("tp000001", "Ali Bin Ahmad"));
    }

    public MainDashboard(String tpNumber, String name) {
        this.tpNumber = tpNumber;
        this.name = name;

        setTitle("APSpace • ADMIN PORTAL");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createSidebar(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Add pages
        contentPanel.add(createDashboardPanel(), "DASHBOARD");
        contentPanel.add(new ManageUser(tpNumber, "admin", name, this), "MANAGE_USER");
        contentPanel.add(new ManageStudent(tpNumber, "admin", name, this), "MANAGE_STUDENT");
        contentPanel.add(new ManageStaff(tpNumber, "admin", name, this), "MANAGE_STAFF");
        contentPanel.add(new ManageClass(tpNumber, "admin", name, this), "MANAGE_CLASS");
        contentPanel.add(new GradingAnalyticsPanel(tpNumber, "admin", name, this), "GRADING");
        contentPanel.add(new GenerateReport(tpNumber, "admin", name, this), "REPORT");
        contentPanel.add(new FeedbackViewer(tpNumber, "admin"), "FEEDBACK");

        add(contentPanel, BorderLayout.CENTER);
        cardLayout.show(contentPanel, "DASHBOARD");
        setVisible(true);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(new Color(255, 165, 0));
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("  ADMIN MENU");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(title);

        addMenuButton(sidebar, "Manage User", () -> cardLayout.show(contentPanel, "MANAGE_USER"));
        addMenuButton(sidebar, "Manage Class", () -> cardLayout.show(contentPanel, "MANAGE_CLASS"));
        addMenuButton(sidebar, "Grading System", () -> cardLayout.show(contentPanel, "GRADING"));
        addMenuButton(sidebar, "Reports", () -> cardLayout.show(contentPanel, "REPORT"));
        addMenuButton(sidebar, "Feedback", () -> cardLayout.show(contentPanel, "FEEDBACK"));
        addMenuButton(sidebar, "Back to Dashboard", () -> cardLayout.show(contentPanel, "DASHBOARD"));

        sidebar.add(Box.createVerticalGlue());
        
        JButton logout = createButton("Logout", Color.RED);
        logout.addActionListener(e -> { dispose(); new LoginFrame(); });
        sidebar.add(logout);
        sidebar.add(Box.createVerticalStrut(15));

        return sidebar;
    }

    private JPanel createDashboardPanel() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Color.WHITE);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(255, 165, 0));
        header.setPreferredSize(new Dimension(0, 80));

        JLabel pageTitle = new JLabel("Admin Dashboard");
        pageTitle.setForeground(Color.WHITE);
        pageTitle.setFont(new Font("Arial", Font.BOLD, 28));
        pageTitle.setHorizontalAlignment(SwingConstants.CENTER);
        header.add(pageTitle, BorderLayout.CENTER);

        JLabel userInfo = new JLabel(tpNumber + " | ADMIN  ");
        userInfo.setFont(new Font("Arial", Font.BOLD, 18));
        userInfo.setForeground(Color.WHITE);
        header.add(userInfo, BorderLayout.EAST);

        main.add(header, BorderLayout.NORTH);
        main.add(createDashboardGrid(), BorderLayout.CENTER);

        return main;
    }

    private JPanel createDashboardGrid() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 30, 30));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        addCard(panel, "Manage User", "MANAGE_USER");
        addCard(panel, "Manage Class", "MANAGE_CLASS");
        addCard(panel, "Grading System", "GRADING");
        addCard(panel, "Reports", "REPORT");
        addCard(panel, "Feedback", "FEEDBACK");

        return panel;
    }

    private void addCard(JPanel panel, String titleText, String cardName) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(255, 165, 0), 2));

        JLabel title = new JLabel(titleText);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(title, BorderLayout.CENTER);

        JButton open = new JButton("Access " + titleText);
        open.setBackground(new Color(255, 165, 0));
        open.setForeground(Color.WHITE);
        open.setFocusPainted(false);
        open.addActionListener(e -> cardLayout.show(contentPanel, cardName));
        card.add(open, BorderLayout.SOUTH);
        
        panel.add(card);
    }

    private void addMenuButton(JPanel sidebar, String text, Runnable action) {
        JButton btn = createButton(text, new Color(255, 165, 0));
        btn.addActionListener(e -> action.run());
        sidebar.add(btn);
        sidebar.add(Box.createVerticalStrut(10));
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton("  " + text);
        btn.setMaximumSize(new Dimension(230, 45));
        btn.setBackground(Color.WHITE);
        btn.setForeground(color);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }

    public void showPanel(String panelName) {
        cardLayout.show(contentPanel, panelName);
    }
}