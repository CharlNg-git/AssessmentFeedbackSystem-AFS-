package Student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import util.FileManager;
import util.DatabaseFile;

public class FeedbackViewer extends JPanel {
    private String tpNumber;
    private String role;
    private JTable sentTable, receivedTable;
    private DefaultTableModel sentTableModel, receivedTableModel;
    private JTextArea feedbackTextArea;
    private JComboBox<String> categoryComboBox;
    private JComboBox<String> sendToComboBox;
    private JComboBox<String> receiverComboBox;
    
    private final FileManager fileManager = new FileManager();
    
    private Map<String, String> receiverMap = new HashMap<>();

    public FeedbackViewer(String tpNumber, String role) {
        this.tpNumber = tpNumber;
        this.role = role;
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        add(createHeader(), BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        mainPanel.add(createSubmitFeedbackPanel(), BorderLayout.NORTH);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(220); // Smaller sent feedback section
        splitPane.setResizeWeight(0.35);   // Less space for sent, more for received
        splitPane.setTopComponent(createSentFeedbackPanel());
        splitPane.setBottomComponent(createReceivedFeedbackPanel());
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        loadSentFeedback();
        loadReceivedFeedback();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(255, 165, 0));
        header.setPreferredSize(new Dimension(0, 80));

        JLabel title = new JLabel("  Feedback System");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        String userInfo;
        if (role.equalsIgnoreCase("student")) {
            String intakeCode = getStudentIntake(tpNumber);
            userInfo = tpNumber + " | Intake: " + intakeCode + "  ";
        } else {
            userInfo = tpNumber + " | " + role.toUpperCase() + "  ";
        }
        
        JLabel userInfoLabel = new JLabel(userInfo);
        userInfoLabel.setFont(new Font("Arial", Font.BOLD, 18));
        userInfoLabel.setForeground(Color.WHITE);
        header.add(userInfoLabel, BorderLayout.EAST);

        return header;
    }

    private JPanel createSubmitFeedbackPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 5, 5, 5),
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(255, 165, 0), 2),
                "Submit New Feedback",
                0, 0, new Font("Arial", Font.BOLD, 14))));
        panel.setPreferredSize(new Dimension(0, 200));

        JPanel topRow = new JPanel(new GridLayout(1, 3, 15, 0));
        topRow.setBackground(Color.WHITE);
        topRow.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // LEFT: Send To
        JPanel sendToPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        sendToPanel.setBackground(Color.WHITE);
        
        JLabel sendToLabel = new JLabel("Send To:");
        sendToLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        String[] sendToOptions = getSendToOptions();
        sendToComboBox = new JComboBox<>(sendToOptions);
        sendToComboBox.setFont(new Font("Arial", Font.PLAIN, 12));
        sendToComboBox.setPreferredSize(new Dimension(150, 28));
        sendToComboBox.addActionListener(e -> updateReceiverDropdown());
        
        sendToPanel.add(sendToLabel);
        sendToPanel.add(sendToComboBox);

        // CENTER: Category
        JPanel categoryPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        categoryPanel.setBackground(Color.WHITE);
        
        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        String[] categories = {"General", "Academic", "Facilities", "Technical Support", 
                               "Course Content", "Teaching Quality", "Assignment", "Complaint", "Suggestion"};
        categoryComboBox = new JComboBox<>(categories);
        categoryComboBox.setFont(new Font("Arial", Font.PLAIN, 12));
        categoryComboBox.setPreferredSize(new Dimension(150, 28));
        
        categoryPanel.add(categoryLabel);
        categoryPanel.add(categoryComboBox);

        // RIGHT: Receiver
        JPanel receiverPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        receiverPanel.setBackground(Color.WHITE);
        
        JLabel receiverLabel = new JLabel("Receiver:");
        receiverLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        receiverComboBox = new JComboBox<>();
        receiverComboBox.setFont(new Font("Arial", Font.PLAIN, 12));
        receiverComboBox.setPreferredSize(new Dimension(180, 28));
        
        receiverPanel.add(receiverLabel);
        receiverPanel.add(receiverComboBox);

        topRow.add(sendToPanel);
        topRow.add(categoryPanel);
        topRow.add(receiverPanel);

        panel.add(topRow, BorderLayout.NORTH);

        JPanel textPanel = new JPanel(new BorderLayout(5, 5));
        textPanel.setBackground(Color.WHITE);
        textPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JLabel textLabel = new JLabel("Your Feedback:");
        textLabel.setFont(new Font("Arial", Font.BOLD, 12));
        textPanel.add(textLabel, BorderLayout.NORTH);

        feedbackTextArea = new JTextArea(3, 40);
        feedbackTextArea.setFont(new Font("Arial", Font.PLAIN, 13));
        feedbackTextArea.setLineWrap(true);
        feedbackTextArea.setWrapStyleWord(true);
        feedbackTextArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        JScrollPane textScroll = new JScrollPane(feedbackTextArea);
        textPanel.add(textScroll, BorderLayout.CENTER);

        panel.add(textPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnPanel.setBackground(Color.WHITE);

        JButton submitBtn = new JButton("Submit Feedback");
        submitBtn.setBackground(new Color(255, 165, 0));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFont(new Font("Arial", Font.BOLD, 13));
        submitBtn.setFocusPainted(false);
        submitBtn.addActionListener(e -> submitFeedback());

        JButton clearBtn = new JButton("Clear");
        clearBtn.setBackground(new Color(128, 128, 128));
        clearBtn.setForeground(Color.WHITE);
        clearBtn.setFont(new Font("Arial", Font.BOLD, 13));
        clearBtn.setFocusPainted(false);
        clearBtn.addActionListener(e -> {
            feedbackTextArea.setText("");
            categoryComboBox.setSelectedIndex(0);
            sendToComboBox.setSelectedIndex(0);
            updateReceiverDropdown();
        });

        btnPanel.add(submitBtn);
        btnPanel.add(clearBtn);

        panel.add(btnPanel, BorderLayout.SOUTH);

        updateReceiverDropdown();

        return panel;
    }

    private JPanel createSentFeedbackPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
            "My Sent Feedback",
            0, 0, new Font("Arial", Font.BOLD, 14)));

        String[] columns = {"Feedback ID", "Date & Time", "To", "Receiver", "Category", "Feedback", "Status"};
        sentTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        sentTable = new JTable(sentTableModel);
        sentTable.setRowHeight(28);
        sentTable.setFont(new Font("Arial", Font.PLAIN, 12));
        sentTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        sentTable.getTableHeader().setBackground(new Color(70, 130, 180));
        sentTable.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(sentTable);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnPanel.setBackground(Color.WHITE);

        JButton viewBtn = new JButton("View Details");
        viewBtn.setBackground(new Color(70, 130, 180));
        viewBtn.setForeground(Color.WHITE);
        viewBtn.setFont(new Font("Arial", Font.BOLD, 12));
        viewBtn.setFocusPainted(false);
        viewBtn.addActionListener(e -> viewSentDetails());

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBackground(new Color(34, 139, 34));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFont(new Font("Arial", Font.BOLD, 12));
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> loadSentFeedback());

        btnPanel.add(viewBtn);
        btnPanel.add(refreshBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createReceivedFeedbackPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(34, 139, 34), 2),
            "Feedback Received (Sent to Me)",
            0, 0, new Font("Arial", Font.BOLD, 14)));

        String[] columns = {"Feedback ID", "Date & Time", "From", "Sender", "Category", "Feedback", "Status"};
        receivedTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        receivedTable = new JTable(receivedTableModel);
        receivedTable.setRowHeight(28);
        receivedTable.setFont(new Font("Arial", Font.PLAIN, 12));
        receivedTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        receivedTable.getTableHeader().setBackground(new Color(34, 139, 34));
        receivedTable.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(receivedTable);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnPanel.setBackground(Color.WHITE);

        JButton respondBtn = new JButton("Respond");
        respondBtn.setBackground(new Color(255, 165, 0));
        respondBtn.setForeground(Color.WHITE);
        respondBtn.setFont(new Font("Arial", Font.BOLD, 12));
        respondBtn.setFocusPainted(false);
        respondBtn.addActionListener(e -> respondToFeedback());

        JButton viewBtn = new JButton("View Details");
        viewBtn.setBackground(new Color(34, 139, 34));
        viewBtn.setForeground(Color.WHITE);
        viewBtn.setFont(new Font("Arial", Font.BOLD, 12));
        viewBtn.setFocusPainted(false);
        viewBtn.addActionListener(e -> viewReceivedDetails());

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBackground(new Color(52, 152, 219));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFont(new Font("Arial", Font.BOLD, 12));
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> loadReceivedFeedback());

        btnPanel.add(respondBtn);
        btnPanel.add(viewBtn);
        btnPanel.add(refreshBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void updateReceiverDropdown() {
        String sendTo = (String) sendToComboBox.getSelectedItem();
        if (sendTo == null) return;

        receiverComboBox.removeAllItems();
        receiverMap.clear();


        if (sendTo.equalsIgnoreCase("Admin")) {
            receiverComboBox.addItem("Admin");
            receiverMap.put("Admin", "S000001");
            return;
        }
        
        if (role.equalsIgnoreCase("admin") || role.equalsIgnoreCase("lecturer") || role.equalsIgnoreCase("leader")) {
            receiverComboBox.addItem("All " + sendTo + "s");
            receiverMap.put("All " + sendTo + "s", "ALL");
        }

        if (role.equalsIgnoreCase("student")) {
            if (sendTo.equalsIgnoreCase("Lecturer")) {
                loadLecturersForStudent();
            } else if (sendTo.equalsIgnoreCase("Academic Leader")) {
                loadLeadersForStudent();
            }
        } else if (role.equalsIgnoreCase("lecturer")) {
            if (sendTo.equalsIgnoreCase("Student")) {
                loadStudentsForLecturer();
            } else if (sendTo.equalsIgnoreCase("Academic Leader")) {
                loadLeadersForLecturer();
            }
        } else if (role.equalsIgnoreCase("leader")) {
            if (sendTo.equalsIgnoreCase("Student")) {
                loadStudentsForLeader();
            } else if (sendTo.equalsIgnoreCase("Lecturer")) {
                loadLecturersForLeader();
            }
        } else if (role.equalsIgnoreCase("admin")) {
            if (sendTo.equalsIgnoreCase("Student")) {
                loadAllStudents();
            } else if (sendTo.equalsIgnoreCase("Lecturer")) {
                loadAllLecturers();
            } else if (sendTo.equalsIgnoreCase("Academic Leader")) {
                loadAllLeaders();
            }
        }
        
    }

    private void loadLecturersForStudent() {
        try {
            String intake = getStudentIntake(tpNumber);
            
            Set<String> lecturerIDs = new HashSet<>();
            String filePath = fileManager.getFilePath("module_assignments.txt");
            
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine();
            
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 3 && data[2].trim().equalsIgnoreCase(intake)) {
                    lecturerIDs.add(data[1].trim());
                }
            }
            br.close();
            
            
            for (String lecturerID : lecturerIDs) {
                String lecturerName = getStaffName(lecturerID);
                
                if (lecturerName != null && !lecturerName.isEmpty()) {
                    receiverComboBox.addItem(lecturerName);
                    receiverMap.put(lecturerName, lecturerID);
                } else {
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error in loadLecturersForStudent: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadLeadersForStudent() {
        try {
            String intake = getStudentIntake(tpNumber);
            
            Set<String> moduleIDs = new HashSet<>();
            BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath("module_assignments.txt")));
            String line = br.readLine();
            
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 3 && data[2].trim().equalsIgnoreCase(intake)) {
                    moduleIDs.add(data[0].trim());
                }
            }
            br.close();
            
            Set<String> leaderIDs = new HashSet<>();
            br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.MODULE.getFileName())));
            line = br.readLine();
            
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 6 && moduleIDs.contains(data[0].trim())) {
                    leaderIDs.add(data[5].trim());
                }
            }
            br.close();
            
            for (String leaderID : leaderIDs) {
                String leaderName = getStaffName(leaderID);
                if (leaderName != null && !leaderName.isEmpty()) {
                    receiverComboBox.addItem(leaderName);
                    receiverMap.put(leaderName, leaderID);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadStudentsForLecturer() {
        try {
            Set<String> intakes = new HashSet<>();
            BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath("module_assignments.txt")));
            String line = br.readLine();
            
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 3 && data[1].trim().equals(tpNumber)) {
                    intakes.add(data[2].trim());
                }
            }
            br.close();
            
            for (String intake : intakes) {
                List<StudentInfo> students = getStudentsInIntake(intake);
                for (StudentInfo student : students) {
                    String displayName = student.name + " (" + intake + ")";
                    receiverComboBox.addItem(displayName);
                    receiverMap.put(displayName, student.tpNumber);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadLeadersForLecturer() {
        try {
            // lecturer_assignments.txt format: lecturerID,leaderID
            // Find the ONE academic leader assigned to this lecturer
            BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath("lecturer_assignments.txt")));
            String line = br.readLine(); // Skip header
            
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 2 && data[0].trim().equals(tpNumber)) {
                    // Found the assigned leader
                    String leaderID = data[1].trim();
                    String leaderName = getStaffName(leaderID);
                    if (leaderName != null && !leaderName.isEmpty()) {
                        receiverComboBox.addItem(leaderName);
                        receiverMap.put(leaderName, leaderID);
                    }
                    break; // Only one leader per lecturer
                }
            }
            br.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadStudentsForLeader() {
        try {
            // Step 1: Find modules managed by this academic leader from module.txt
            Set<String> moduleIDs = new HashSet<>();
            BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.MODULE.getFileName())));
            String line = br.readLine(); // Skip header
            
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                // module.txt format: moduleID,moduleName,creditHour,sessionType,Level,TpNumber,gradingID
                // TpNumber (academic leader) is at index 5
                if (data.length >= 6 && data[5].trim().equals(tpNumber)) {
                    moduleIDs.add(data[0].trim());
                }
            }
            br.close();
            
            // Step 2: Find intakes that have these modules from module_assignments.txt
            Set<String> intakes = new HashSet<>();
            br = new BufferedReader(new FileReader(fileManager.getFilePath("module_assignments.txt")));
            line = br.readLine(); // Skip header
            
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                // module_assignments.txt format: moduleID,lecturerID,intakeCode
                if (data.length >= 3 && moduleIDs.contains(data[0].trim())) {
                    intakes.add(data[2].trim());
                }
            }
            br.close();
            
            // Step 3: Get all students in these intakes
            for (String intake : intakes) {
                List<StudentInfo> students = getStudentsInIntake(intake);
                for (StudentInfo student : students) {
                    String displayName = student.name + " (" + intake + ")";
                    receiverComboBox.addItem(displayName);
                    receiverMap.put(displayName, student.tpNumber);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadLecturersForLeader() {
        try {
            // lecturer_assignments.txt format: lecturerID,leaderID
            // Find all lecturers where leaderID matches this academic leader's tpNumber
            BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath("lecturer_assignments.txt")));
            String line = br.readLine(); // Skip header
            
            Set<String> lecturerIDs = new HashSet<>();
            
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 2 && data[1].trim().equals(tpNumber)) {
                    lecturerIDs.add(data[0].trim()); // This lecturer reports to me
                }
            }
            br.close();
            
            for (String lecturerID : lecturerIDs) {
                String lecturerName = getStaffName(lecturerID);
                if (lecturerName != null && !lecturerName.isEmpty()) {
                    receiverComboBox.addItem(lecturerName);
                    receiverMap.put(lecturerName, lecturerID);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadAllStudents() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.STUDENT.getFileName())));
            String line = br.readLine();
            
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 11) {
                    String studentID = data[8].trim();
                    String studentName = data[0].trim();
                    String intake = data[10].trim();
                    String displayName = studentName + " (" + intake + ")";
                    receiverComboBox.addItem(displayName);
                    receiverMap.put(displayName, studentID);
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadAllLecturers() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.STAFF.getFileName())));
            String line = br.readLine();
            
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                // role at index 12, tpNumber at index 10
                if (data.length >= 13 && data[12].trim().equalsIgnoreCase("lecturer")) {
                    String lecturerID = data[10].trim();
                    String lecturerName = data[0].trim();
                    receiverComboBox.addItem(lecturerName);
                    receiverMap.put(lecturerName, lecturerID);
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadAllLeaders() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.STAFF.getFileName())));
            String line = br.readLine();
            
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                // role at index 12, tpNumber at index 10
                if (data.length >= 13 && data[12].trim().equalsIgnoreCase("leader")) {
                    String leaderID = data[10].trim();
                    String leaderName = data[0].trim();
                    receiverComboBox.addItem(leaderName);
                    receiverMap.put(leaderName, leaderID);
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String[] getSendToOptions() {
        switch (role.toLowerCase()) {
            case "student":
                return new String[]{"Admin", "Lecturer", "Academic Leader"};
            case "lecturer":
                return new String[]{"Admin", "Student", "Academic Leader"};
            case "leader":
                return new String[]{"Admin", "Student", "Lecturer"};
            case "admin":
                return new String[]{"Student", "Lecturer", "Academic Leader"};
            default:
                return new String[]{"Admin"};
        }
    }

    private void submitFeedback() {
        String sendTo = (String) sendToComboBox.getSelectedItem();
        String category = (String) categoryComboBox.getSelectedItem();
        String feedbackText = feedbackTextArea.getText().trim();
        String receiverDisplay = (String) receiverComboBox.getSelectedItem();

        if (feedbackText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your feedback!");
            return;
        }

        if (feedbackText.length() < 10) {
            JOptionPane.showMessageDialog(this, "Feedback must be at least 10 characters!");
            return;
        }

        try {
            String receiverID = receiverMap.get(receiverDisplay);
            if (receiverID == null) {
                receiverID = "S000001";
            }
            
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String timestamp = now.format(formatter);
            
            String filePath = fileManager.getFilePath(DatabaseFile.FEEDBACK.getFileName());
            
            if (receiverID.equals("ALL")) {
                List<String> allReceiverIDs = getAllReceiversForRole(sendTo);
                int count = 0;
                
                // Get starting feedback ID number
                int currentMaxID = getMaxFeedbackNumber();
                
                BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true));
                for (String targetID : allReceiverIDs) {
                    currentMaxID++; // Increment for each new feedback
                    String feedbackID = String.format("FB%03d", currentMaxID);
                    String newFeedback = String.format("%s,%s,%s,%s,%s,%s,%s,%s,Pending,\n",
                        feedbackID, tpNumber, role, sendTo, targetID,
                        category,
                        feedbackText.replace(",", ";"),
                        timestamp);
                    bw.write(newFeedback);
                    count++;
                }
                bw.close();
                
                JOptionPane.showMessageDialog(this, 
                    String.format("Feedback sent to %d %ss successfully!", count, sendTo));
            } else {
                String feedbackID = generateFeedbackID();
                BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true));
                String newFeedback = String.format("%s,%s,%s,%s,%s,%s,%s,%s,Pending,\n",
                    feedbackID, tpNumber, role, sendTo, receiverID,
                    category,
                    feedbackText.replace(",", ";"),
                    timestamp);
                bw.write(newFeedback);
                bw.close();
                
                JOptionPane.showMessageDialog(this, "Feedback submitted successfully!\nFeedback ID: " + feedbackID);
            }

            feedbackTextArea.setText("");
            categoryComboBox.setSelectedIndex(0);
            sendToComboBox.setSelectedIndex(0);
            updateReceiverDropdown();

            loadSentFeedback();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error submitting feedback: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<String> getAllReceiversForRole(String role) {
        List<String> receivers = new ArrayList<>();
        try {
            if (role.equalsIgnoreCase("Student")) {
                if (this.role.equalsIgnoreCase("lecturer")) {
                    // Lecturer: only students in their classes
                    Set<String> intakes = new HashSet<>();
                    BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath("module_assignments.txt")));
                    String line = br.readLine();
                    
                    while ((line = br.readLine()) != null) {
                        String[] data = line.split(",");
                        if (data.length >= 3 && data[1].trim().equals(tpNumber)) {
                            intakes.add(data[2].trim());
                        }
                    }
                    br.close();
                    
                    for (String intake : intakes) {
                        receivers.addAll(getStudentIDsInIntake(intake));
                    }
                } else if (this.role.equalsIgnoreCase("leader")) {
                    // Academic Leader: students in their modules
                    Set<String> moduleIDs = new HashSet<>();
                    BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.MODULE.getFileName())));
                    String line = br.readLine();
                    
                    while ((line = br.readLine()) != null) {
                        String[] data = line.split(",");
                        if (data.length >= 6 && data[5].trim().equals(tpNumber)) {
                            moduleIDs.add(data[0].trim());
                        }
                    }
                    br.close();
                    
                    Set<String> intakes = new HashSet<>();
                    br = new BufferedReader(new FileReader(fileManager.getFilePath("module_assignments.txt")));
                    line = br.readLine();
                    
                    while ((line = br.readLine()) != null) {
                        String[] data = line.split(",");
                        if (data.length >= 3 && moduleIDs.contains(data[0].trim())) {
                            intakes.add(data[2].trim());
                        }
                    }
                    br.close();
                    
                    for (String intake : intakes) {
                        receivers.addAll(getStudentIDsInIntake(intake));
                    }
                } else {
                    // Admin: all students
                    BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.STUDENT.getFileName())));
                    String line = br.readLine();
                    while ((line = br.readLine()) != null) {
                        String[] data = line.split(",");
                        if (data.length >= 9) {
                            receivers.add(data[8].trim());
                        }
                    }
                    br.close();
                }
            } else if (role.equalsIgnoreCase("Lecturer")) {
                if (this.role.equalsIgnoreCase("leader")) {
                    // Academic Leader: only their subordinate lecturers
                    BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath("lecturer_assignments.txt")));
                    String line = br.readLine();
                    
                    while ((line = br.readLine()) != null) {
                        String[] data = line.split(",");
                        if (data.length >= 2 && data[1].trim().equals(tpNumber)) {
                            receivers.add(data[0].trim());
                        }
                    }
                    br.close();
                } else {
                    // Admin: all lecturers
                    BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.STAFF.getFileName())));
                    String line = br.readLine();
                    while ((line = br.readLine()) != null) {
                        String[] data = line.split(",");
                        if (data.length >= 13 && data[12].trim().equalsIgnoreCase("lecturer")) {
                            receivers.add(data[10].trim());
                        }
                    }
                    br.close();
                }
            } else if (role.equalsIgnoreCase("Academic Leader")) {
                if (this.role.equalsIgnoreCase("lecturer")) {
                    // Lecturer: only their ONE academic leader
                    BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath("lecturer_assignments.txt")));
                    String line = br.readLine();
                    
                    while ((line = br.readLine()) != null) {
                        String[] data = line.split(",");
                        if (data.length >= 2 && data[0].trim().equals(tpNumber)) {
                            receivers.add(data[1].trim());
                            break; // Only one leader
                        }
                    }
                    br.close();
                } else {
                    // Admin: all leaders
                    BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.STAFF.getFileName())));
                    String line = br.readLine();
                    while ((line = br.readLine()) != null) {
                        String[] data = line.split(",");
                        if (data.length >= 13 && data[12].trim().equalsIgnoreCase("leader")) {
                            receivers.add(data[10].trim());
                        }
                    }
                    br.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return receivers;
    }

    private String generateFeedbackID() {
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.FEEDBACK.getFileName());
            File file = new File(filePath);
            
            if (!file.exists()) {
                BufferedWriter bw = new BufferedWriter(new FileWriter(file));
                bw.write("feedbackID,senderID,senderRole,sendToRole,receiverID,category,feedback,timestamp,status,response\n");
                bw.close();
                return "FB001";
            }

            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line;
            int maxNum = 0;

            br.readLine();
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] data = line.split(",");
                if (data.length > 0) {
                    String id = data[0].trim();
                    if (id.startsWith("FB")) {
                        try {
                            int num = Integer.parseInt(id.substring(2));
                            maxNum = Math.max(maxNum, num);
                        } catch (NumberFormatException e) {
                        }
                    }
                }
            }
            br.close();

            return String.format("FB%03d", maxNum + 1);

        } catch (Exception e) {
            e.printStackTrace();
            return "FB001";
        }
    }
    
    private int getMaxFeedbackNumber() {
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.FEEDBACK.getFileName());
            File file = new File(filePath);
            
            if (!file.exists()) {
                return 0;
            }

            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line;
            int maxNum = 0;

            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] data = line.split(",");
                if (data.length > 0) {
                    String id = data[0].trim();
                    if (id.startsWith("FB")) {
                        try {
                            int num = Integer.parseInt(id.substring(2));
                            maxNum = Math.max(maxNum, num);
                        } catch (NumberFormatException e) {
                        }
                    }
                }
            }
            br.close();

            return maxNum;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void loadSentFeedback() {
        sentTableModel.setRowCount(0);

        try {
            String filePath = fileManager.getFilePath(DatabaseFile.FEEDBACK.getFileName());
            File file = new File(filePath);
            
            if (!file.exists()) return;

            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine();

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] data = line.split(",");
                if (data.length >= 9 && data[1].trim().equals(tpNumber)) {
                    String feedbackID = data[0].trim();
                    String sendToRole = data[3].trim();
                    String receiverID = data[4].trim();
                    String category = data[5].trim();
                    String feedback = data[6].trim().replace(";", ",");
                    String timestamp = data[7].trim();
                    String status = data[8].trim();

                    String receiverName = getPersonName(receiverID);

                    String displayFeedback = feedback.length() > 30 ? 
                                            feedback.substring(0, 30) + "..." : feedback;

                    sentTableModel.addRow(new Object[]{
                        feedbackID, timestamp, sendToRole, receiverName,
                        category, displayFeedback, status
                    });
                }
            }
            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadReceivedFeedback() {
        receivedTableModel.setRowCount(0);

        try {
            String filePath = fileManager.getFilePath(DatabaseFile.FEEDBACK.getFileName());
            File file = new File(filePath);
            
            if (!file.exists()) return;

            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine();

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] data = line.split(",");
                if (data.length >= 9 && data[4].trim().equals(tpNumber)) {
                    String feedbackID = data[0].trim();
                    String senderID = data[1].trim();
                    String senderRole = data[2].trim();
                    String category = data[5].trim();
                    String feedback = data[6].trim().replace(";", ",");
                    String timestamp = data[7].trim();
                    String status = data[8].trim();

                    String senderName = getPersonName(senderID);

                    String displayFeedback = feedback.length() > 30 ? 
                                            feedback.substring(0, 30) + "..." : feedback;

                    receivedTableModel.addRow(new Object[]{
                        feedbackID, timestamp, senderRole, senderName,
                        category, displayFeedback, status
                    });
                }
            }
            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void viewSentDetails() {
        int selectedRow = sentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a feedback to view!");
            return;
        }

        String feedbackID = sentTableModel.getValueAt(selectedRow, 0).toString();
        showFeedbackDetails(feedbackID);
    }

    private void viewReceivedDetails() {
        int selectedRow = receivedTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a feedback to view!");
            return;
        }

        String feedbackID = receivedTableModel.getValueAt(selectedRow, 0).toString();
        showFeedbackDetails(feedbackID);
    }

    private void showFeedbackDetails(String feedbackID) {
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.FEEDBACK.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine();

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 9 && data[0].trim().equals(feedbackID)) {
                    String senderID = data[1].trim();
                    String senderRole = data[2].trim();
                    String sendToRole = data[3].trim();
                    String receiverID = data[4].trim();
                    String category = data[5].trim();
                    String feedback = data[6].trim().replace(";", ",");
                    String timestamp = data[7].trim();
                    String status = data[8].trim();
                    String response = data.length >= 10 ? data[9].trim() : "";

                    StringBuilder details = new StringBuilder();
                    details.append("═════════════════════════════════════════\n");
                    details.append("           FEEDBACK DETAILS\n");
                    details.append("═════════════════════════════════════════\n\n");
                    details.append("Feedback ID: ").append(feedbackID).append("\n");
                    details.append("Date & Time: ").append(timestamp).append("\n");
                    details.append("From: ").append(getPersonName(senderID)).append(" (").append(senderRole).append(")\n");
                    details.append("To: ").append(getPersonName(receiverID)).append(" (").append(sendToRole).append(")\n");
                    details.append("Category: ").append(category).append("\n");
                    details.append("Status: ").append(status).append("\n\n");
                    details.append("─────────────────────────────────────────\n");
                    details.append("Feedback:\n");
                    details.append("─────────────────────────────────────────\n");
                    details.append(feedback).append("\n\n");
                    details.append("─────────────────────────────────────────\n");
                    details.append("Response:\n");
                    details.append("─────────────────────────────────────────\n");
                    details.append(response.isEmpty() ? "No response yet" : response);

                    JTextArea textArea = new JTextArea(details.toString());
                    textArea.setEditable(false);
                    textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
                    textArea.setLineWrap(true);
                    textArea.setWrapStyleWord(true);

                    JScrollPane scrollPane = new JScrollPane(textArea);
                    scrollPane.setPreferredSize(new Dimension(650, 450));

                    JOptionPane.showMessageDialog(this, scrollPane, 
                        "Feedback Details", JOptionPane.PLAIN_MESSAGE);

                    br.close();
                    return;
                }
            }
            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void respondToFeedback() {
        int selectedRow = receivedTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a feedback to respond!");
            return;
        }

        String feedbackID = receivedTableModel.getValueAt(selectedRow, 0).toString();
        String status = receivedTableModel.getValueAt(selectedRow, 6).toString();

        if (status.equalsIgnoreCase("Responded")) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "This feedback already has a response. Do you want to update it?",
                "Update Response",
                JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
        }

        String response = JOptionPane.showInputDialog(this,
            "Enter your response to feedback " + feedbackID + ":",
            "Respond to Feedback",
            JOptionPane.PLAIN_MESSAGE);

        if (response == null || response.trim().isEmpty()) {
            return;
        }

        try {
            List<String> lines = new ArrayList<>();
            BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.FEEDBACK.getFileName())));
            String line;
            
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 9 && data[0].trim().equals(feedbackID)) {
                    String updatedLine = String.format("%s,%s,%s,%s,%s,%s,%s,%s,Responded,%s",
                        data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7],
                        response.replace(",", ";"));
                    lines.add(updatedLine);
                } else {
                    lines.add(line);
                }
            }
            br.close();
            
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileManager.getFilePath(DatabaseFile.FEEDBACK.getFileName())));
            for (String l : lines) {
                bw.write(l);
                bw.newLine();
            }
            bw.close();
            
            JOptionPane.showMessageDialog(this, "Response submitted successfully!");
            loadReceivedFeedback();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error submitting response: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getStudentIntake(String tpNumber) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.STUDENT.getFileName())));
            String line = br.readLine();
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] data = line.split(",");
                if (data.length >= 11 && data[8].trim().equals(tpNumber)) {
                    br.close();
                    return data[10].trim();
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "N/A";
    }
    
    private String getStaffName(String tpNumber) {
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.STAFF.getFileName());
            
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine(); // Skip header
            
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                // staffinfo.txt: fullName,icNumber/passport,gender,dateOfBirth,nationality,race,address,contactNumber,jobTitle,yearOfService,tpNumber,password,role,academicLeader
                // TpNumber is at index 10 (column 11)
                if (data.length >= 11 && data[10].trim().equals(tpNumber)) {
                    br.close();
                    return data[0].trim(); // Return fullName (index 0)
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private String getPersonName(String tpNumber) {
        String name = getStaffName(tpNumber);
        if (name != null && !name.isEmpty()) {
            return name;
        }
        
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.STUDENT.getFileName())));
            String line = br.readLine();
            
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 9 && data[8].trim().equals(tpNumber)) {
                    br.close();
                    return data[0].trim();
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return tpNumber;
    }
    
    private List<StudentInfo> getStudentsInIntake(String intake) {
        List<StudentInfo> students = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.STUDENT.getFileName())));
            String line = br.readLine();
            
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 11 && data[10].trim().equalsIgnoreCase(intake)) {
                    students.add(new StudentInfo(data[8].trim(), data[0].trim()));
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return students;
    }
    
    private List<String> getStudentIDsInIntake(String intake) {
        List<String> studentIDs = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileManager.getFilePath(DatabaseFile.STUDENT.getFileName())));
            String line = br.readLine();
            
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 11 && data[10].trim().equalsIgnoreCase(intake)) {
                    studentIDs.add(data[8].trim());
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return studentIDs;
    }
    
    class StudentInfo {
        String tpNumber;
        String name;
        
        StudentInfo(String tpNumber, String name) {
            this.tpNumber = tpNumber;
            this.name = name;
        }
    }
}