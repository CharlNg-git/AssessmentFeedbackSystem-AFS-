package Student;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import Login.LoginFrame;
import util.FileManager;
import util.DatabaseFile;

public class MainDashboard extends JFrame {

    private String tpNumber, name;
    private String intakeCode;

    // OODJ: CardLayout for panel switching
    private CardLayout cardLayout;
    private JPanel contentPanel;
    
    // Use FileManager
    private final FileManager fileManager = new FileManager();

    /**
     * MAIN METHOD
     * Run Student Dashboard directly without login
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainDashboard("tp000015", "John Lim");
        });
    }

    public MainDashboard(String tpNumber, String name) {
        this.tpNumber = tpNumber;
        this.name = name;
        this.intakeCode = getStudentIntake(tpNumber);

        setTitle("APSpace • STUDENT PORTAL");
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
        contentPanel.add(new ClassRegister(tpNumber), "CLASS");
        contentPanel.add(new TimetableViewer(tpNumber), "TIMETABLE");
        contentPanel.add(new AttendanceViewer(tpNumber), "ATTENDANCE");
        contentPanel.add(new ViewResult(tpNumber), "RESULT");
        contentPanel.add(new ProfileViewer(tpNumber, "student"), "PROFILE");
        contentPanel.add(new FeedbackViewer(tpNumber, "student"), "FEEDBACK");

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

        JLabel title = new JLabel("  STUDENT MENU");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(title);

        addMenuButton(sidebar, "Profile", () ->
            cardLayout.show(contentPanel, "PROFILE")
        );

        addMenuButton(sidebar, "Class Register", () ->
                cardLayout.show(contentPanel, "CLASS")
        );

        addMenuButton(sidebar, "Timetable", () ->
                cardLayout.show(contentPanel, "TIMETABLE")
        );

        addMenuButton(sidebar, "Attendance", () ->
                cardLayout.show(contentPanel, "ATTENDANCE")
        );

        addMenuButton(sidebar, "View Result", () ->
                cardLayout.show(contentPanel, "RESULT")
        );

        addMenuButton(sidebar, "Feedback", () ->
            cardLayout.show(contentPanel, "FEEDBACK")
        );

        // Back to Dashboard button
        addMenuButton(sidebar, "Back to Dashboard", () ->
                cardLayout.show(contentPanel, "DASHBOARD")
        );

        // Add vertical glue to push logout to bottom
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

        JLabel pageTitle = new JLabel("Student Dashboard");
        pageTitle.setForeground(Color.WHITE);
        pageTitle.setFont(new Font("Arial", Font.BOLD, 28));
        pageTitle.setHorizontalAlignment(SwingConstants.CENTER);
        header.add(pageTitle, BorderLayout.CENTER);

        JLabel userInfoLabel = new JLabel(tpNumber + " | Intake: " + intakeCode + "  ");
        userInfoLabel.setFont(new Font("Arial", Font.BOLD, 18));
        userInfoLabel.setForeground(Color.WHITE);
        header.add(userInfoLabel, BorderLayout.EAST);

        main.add(header, BorderLayout.NORTH);
        main.add(createStudentGrid(), BorderLayout.CENTER);

        return main;
    }

    /* ===================== GRID ===================== */
    private JPanel createStudentGrid() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 30, 30));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        addCard(panel, "Profile", "PROFILE");
        addCard(panel, "Class Register", "CLASS");
        addCard(panel, "Timetable", "TIMETABLE");
        addCard(panel, "Attendance", "ATTENDANCE");
        addCard(panel, "View Result", "RESULT");
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
            open.addActionListener(e ->
                    cardLayout.show(contentPanel, cardName)
            );
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

    private String getStudentIntake(String tpNumber) {
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.STUDENT.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine(); // Skip header
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] data = line.split(",");
                // fullName,icNumber/passport,gender,dateOfBirth,nationality,race,address,contactNumber,tpNumber,password,intakeCode
                if (data.length >= 11) {
                    String filetpNumber = data[8].trim();
                    String intake = data[10].trim();
                    
                    if (filetpNumber.equals(tpNumber)) {
                        br.close();
                        return intake;
                    }
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "N/A";
    }
}