package Lecture;

import javax.swing.*;
import java.awt.*;
import Login.LoginFrame;
import Student.FeedbackViewer;
import Student.ProfileViewer;

public class MainDashboard extends JFrame {

    private String tpNumber, role, name;
    private Lecturer lecturer;
    private CardLayout cardLayout;
    private JPanel contentPanel;

    public MainDashboard(String tpNumber, String name) {
        this.tpNumber = tpNumber;
        this.name = name;
        this.role = "LECTURER";
        this.lecturer = new Lecturer(tpNumber, name);

        setTitle("APSpace • LECTURER PORTAL");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // LEFT SIDEBAR
        add(createSidebar(), BorderLayout.WEST);

        // MAIN CONTENT AREA (CardLayout)
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Add pages
        contentPanel.add(createDashboardPanel(), "DASHBOARD");
        contentPanel.add(new ProfileViewer(tpNumber, "lecturer"), "PROFILE");
        contentPanel.add(new AssessmentManager(lecturer), "ASSESSMENTS");
        contentPanel.add(new GradingPanel(lecturer), "GRADING");
        contentPanel.add(new SubmissionTrackPanel(lecturer), "TRACKING");
        contentPanel.add(new FeedbackViewer(tpNumber, "lecturer"), "FEEDBACK");
        add(contentPanel, BorderLayout.CENTER);

        // Default page
        cardLayout.show(contentPanel, "DASHBOARD");

        setVisible(true);
    }

    /* ===================== SIDEBAR ===================== */
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(new Color(255, 165, 0));
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("  LECTURER MENU");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(title);

        addMenuButton(sidebar, "Dashboard", ()
                -> cardLayout.show(contentPanel, "DASHBOARD")
        );

        addMenuButton(sidebar, "Profile", ()
                -> cardLayout.show(contentPanel, "PROFILE")
        );

        addMenuButton(sidebar, "Assessments", ()
                -> cardLayout.show(contentPanel, "ASSESSMENTS")
        );

        addMenuButton(sidebar, "Grading", ()
                -> cardLayout.show(contentPanel, "GRADING")
        );

        addMenuButton(sidebar, "Submission Tracking", ()
                -> cardLayout.show(contentPanel, "TRACKING")
        );
        
        addMenuButton(sidebar, "Feedback", () ->
            cardLayout.show(contentPanel, "FEEDBACK")
        );
        sidebar.add(Box.createVerticalGlue());

        JButton logout = createButton("Logout", Color.RED);
        logout.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });

        sidebar.add(logout);
        sidebar.add(Box.createVerticalStrut(15));

        return sidebar;
    }

    /* ===================== DASHBOARD PAGE ===================== */
    private JPanel createDashboardPanel() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Color.WHITE);

        // HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(255, 165, 0));
        header.setPreferredSize(new Dimension(0, 80));

        JLabel pageTitle = new JLabel("Lecturer Dashboard");
        pageTitle.setForeground(Color.WHITE);
        pageTitle.setFont(new Font("Arial", Font.BOLD, 28));
        pageTitle.setHorizontalAlignment(SwingConstants.CENTER);
        header.add(pageTitle, BorderLayout.CENTER);

        JLabel userInfo = new JLabel(lecturer.getId() + " | " + lecturer.getName() + "  ");
        userInfo.setFont(new Font("Arial", Font.BOLD, 18));
        userInfo.setForeground(Color.WHITE);
        header.add(userInfo, BorderLayout.EAST);

        main.add(header, BorderLayout.NORTH);
        main.add(createLecturerGrid(), BorderLayout.CENTER);

        return main;
    }

    /* ===================== GRID ===================== */
    private JPanel createLecturerGrid() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 30, 30));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        addCard(panel, "Profile", "PROFILE");
        addCard(panel, "Assessments", "ASSESSMENTS");
        addCard(panel, "Grading", "GRADING");
        addCard(panel, "Submission Tracking", "TRACKING");
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

        if (cardName != null) {
            open.addActionListener(e
                    -> cardLayout.show(contentPanel, cardName)
            );
        } else {
            open.setEnabled(false);
            open.setText("Coming Soon");
        }

        card.add(open, BorderLayout.SOUTH);
        panel.add(card);
    }

    /* ===================== UTIL METHODS ===================== */
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainDashboard("tp000008", "Ms. Cheryl Lim");
        });
    }
}
