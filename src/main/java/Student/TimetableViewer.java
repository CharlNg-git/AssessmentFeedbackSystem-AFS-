package Student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import util.FileManager;
import util.DatabaseFile;

public class TimetableViewer extends JPanel {
    private String tpNumber;
    private String intakeCode;
    private JTable weeklyTable;
    private DefaultTableModel weeklyModel;
    private JPanel upcomingPanel;
    private JLabel weekInfoLabel;
    private List<ClassSchedule> allClasses;
    private String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    private int weekOffset = 0; // 0 = current week, 1 = next week, -1 = previous week
    
    // Use FileManager instead of hardcoded path
    private final FileManager fileManager = new FileManager();

    public TimetableViewer(String tpNumber) {
        this.tpNumber = tpNumber;
        this.intakeCode = getStudentIntake(tpNumber);
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Header
        add(createHeader(), BorderLayout.NORTH);

        // Main Content
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // Weekly Timetable
        mainPanel.add(createWeeklyTimetablePanel(), BorderLayout.CENTER);

        // Upcoming Classes
        mainPanel.add(createUpcomingClassesPanel(), BorderLayout.EAST);

        add(mainPanel, BorderLayout.CENTER);

        // Load Data
        loadTimetable();
        loadUpcomingClasses();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(255, 165, 0));
        header.setPreferredSize(new Dimension(0, 80));

        JLabel title = new JLabel("  My Weekly Timetable & Schedule");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        JLabel userInfoLabel = new JLabel(tpNumber + " | Intake: " + intakeCode + "  ");
        userInfoLabel.setFont(new Font("Arial", Font.BOLD, 18));
        userInfoLabel.setForeground(Color.WHITE);
        header.add(userInfoLabel, BorderLayout.EAST);

        return header;
    }

    private JPanel createWeeklyTimetablePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);

        String[] columns = {"Time", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        weeklyModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        weeklyTable = new JTable(weeklyModel);
        weeklyTable.setRowHeight(80);  // Increased from 60 to 80
        weeklyTable.setFont(new Font("Arial", Font.PLAIN, 11));
        weeklyTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        weeklyTable.getTableHeader().setBackground(new Color(255, 165, 0));
        weeklyTable.getTableHeader().setForeground(Color.WHITE);
        weeklyTable.getTableHeader().setPreferredSize(new Dimension(0, 50)); // Increased height for two-line headers
        weeklyTable.setGridColor(new Color(200, 200, 200));

        weeklyTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        for (int i = 1; i < columns.length; i++) {
            weeklyTable.getColumnModel().getColumn(i).setPreferredWidth(150);
        }

        for (int hour = 8; hour <= 18; hour++) {
            String timeSlot = String.format("%02d:00", hour);
            weeklyModel.addRow(new Object[]{timeSlot, "", "", "", "", "", "", ""});
        }

        JScrollPane scroll = new JScrollPane(weeklyTable);
        scroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(255, 165, 0), 2),
            "Weekly Schedule",
            0, 0, new Font("Arial", Font.BOLD, 16)));

        panel.add(scroll, BorderLayout.CENTER);

        // Control Panel - Week Navigation and Refresh
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBackground(Color.WHITE);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Week Info Label (Top row)
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        infoPanel.setBackground(Color.WHITE);
        
        weekInfoLabel = new JLabel(getCurrentWeekInfo());
        weekInfoLabel.setFont(new Font("Arial", Font.BOLD, 14));
        weekInfoLabel.setForeground(new Color(255, 165, 0));
        
        infoPanel.add(weekInfoLabel);
        controlPanel.add(infoPanel);
        
        // Buttons Panel (Bottom row)
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        buttonsPanel.setBackground(Color.WHITE);

        // Previous Week Button
        JButton prevWeekBtn = new JButton("◄ Previous Week");
        prevWeekBtn.setBackground(new Color(70, 130, 180));
        prevWeekBtn.setForeground(Color.WHITE);
        prevWeekBtn.setFont(new Font("Arial", Font.BOLD, 13));
        prevWeekBtn.setFocusPainted(false);
        prevWeekBtn.addActionListener(e -> changeWeek(-1));

        // Next Week Button
        JButton nextWeekBtn = new JButton("Next Week ►");
        nextWeekBtn.setBackground(new Color(70, 130, 180));
        nextWeekBtn.setForeground(Color.WHITE);
        nextWeekBtn.setFont(new Font("Arial", Font.BOLD, 13));
        nextWeekBtn.setFocusPainted(false);
        nextWeekBtn.addActionListener(e -> changeWeek(1));

        // Current Week Button
        JButton currentWeekBtn = new JButton("Current Week");
        currentWeekBtn.setBackground(new Color(255, 165, 0));
        currentWeekBtn.setForeground(Color.WHITE);
        currentWeekBtn.setFont(new Font("Arial", Font.BOLD, 13));
        currentWeekBtn.setFocusPainted(false);
        currentWeekBtn.addActionListener(e -> resetToCurrentWeek());

        // Refresh Button
        JButton refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.setBackground(new Color(34, 139, 34));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFont(new Font("Arial", Font.BOLD, 13));
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> {
            loadTimetable();
            loadUpcomingClasses();
        });

        // Add all buttons to buttons panel
        buttonsPanel.add(prevWeekBtn);
        buttonsPanel.add(nextWeekBtn);
        buttonsPanel.add(currentWeekBtn);
        buttonsPanel.add(refreshBtn);
        
        controlPanel.add(buttonsPanel);

        panel.add(controlPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createUpcomingClassesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setPreferredSize(new Dimension(350, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
            "Upcoming Classes",
            0, 0, new Font("Arial", Font.BOLD, 16)));

        upcomingPanel = new JPanel();
        upcomingPanel.setLayout(new BoxLayout(upcomingPanel, BoxLayout.Y_AXIS));
        upcomingPanel.setBackground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(upcomingPanel);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void loadTimetable() {
        allClasses = new ArrayList<>();

        try {
            String filePath = fileManager.getFilePath(DatabaseFile.CLASS_REGISTER.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine();

            // Get current date for filtering
            java.time.LocalDate currentDate = java.time.LocalDate.now();

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 10 && data[3].equals(tpNumber)) {
                    String classID = data[0];
                    String moduleID = data[1];
                    String day = data[7];
                    String startTime = data[5];
                    String endTime = data[6];
                    String location = data[8];
                    String date = data[4];

                    // Parse and filter by date
                    try {
                        java.time.LocalDate classDate = parseFlexibleDate(date);
                        
                        // Only show classes on or after current date
                        if (!classDate.isBefore(currentDate)) {
                            String moduleName = getModuleName(moduleID);

                            ClassSchedule schedule = new ClassSchedule(classID, moduleID, moduleName, 
                                                                       day, startTime, endTime, location, date);
                            allClasses.add(schedule);
                        }
                    } catch (Exception e) {
                        System.out.println("Error parsing date for timetable: " + date + " - " + e.getMessage());
                    }
                }
            }
            br.close();

            // Clear the table
            for (int i = 0; i < weeklyModel.getRowCount(); i++) {
                for (int j = 1; j < weeklyModel.getColumnCount(); j++) {
                    weeklyModel.setValueAt("", i, j);
                }
            }

            // Get the week dates for the current offset
            java.time.LocalDate startOfWeek = getStartOfWeek(weekOffset);
            
            // Update table headers with dates
            updateTableHeaders(startOfWeek);

            // Populate timetable for the selected week
            for (ClassSchedule cs : allClasses) {
                try {
                    java.time.LocalDate classDate = parseFlexibleDate(cs.date);
                    java.time.DayOfWeek dayOfWeek = classDate.getDayOfWeek();
                    
                    // Check if class falls within the displayed week
                    if (!classDate.isBefore(startOfWeek) && !classDate.isAfter(startOfWeek.plusDays(6))) {
                        int dayIndex = dayOfWeek.getValue() - 1; // Monday = 0, Sunday = 6
                        
                        int startHour = Integer.parseInt(cs.startTime.substring(0, 2));
                        int rowIndex = startHour - 8;

                        if (rowIndex >= 0 && rowIndex < weeklyModel.getRowCount()) {
                            String cellContent = String.format("<html><b>%s</b><br>%s - %s<br>%s<br><i>%s</i></html>",
                                cs.moduleName, cs.startTime, cs.endTime, cs.location, cs.date);
                            weeklyModel.setValueAt(cellContent, rowIndex, dayIndex + 1);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error processing class for week view: " + cs.date);
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading timetable: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get the start of week (Monday) for the given offset
     * weekOffset: 0 = current week, 1 = next week, -1 = previous week
     */
    private java.time.LocalDate getStartOfWeek(int weekOffset) {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.DayOfWeek currentDay = today.getDayOfWeek();
        
        // Calculate days to subtract to get to Monday
        int daysToMonday = currentDay.getValue() - 1;
        java.time.LocalDate monday = today.minusDays(daysToMonday);
        
        // Add week offset
        return monday.plusWeeks(weekOffset);
    }

    /**
     * Update table column headers to show dates
     */
    private void updateTableHeaders(java.time.LocalDate startOfWeek) {
        String[] columnNames = new String[8];
        columnNames[0] = "Time";
        
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MM/dd");
        
        for (int i = 0; i < 7; i++) {
            java.time.LocalDate date = startOfWeek.plusDays(i);
            String dayName = days[i];
            String dateStr = date.format(formatter);
            // Shorten day names for better fit
            String shortDay = dayName.substring(0, 3);
            columnNames[i + 1] = String.format("<html><center>%s<br>%s</center></html>", shortDay, dateStr);
        }
        
        weeklyModel.setColumnIdentifiers(columnNames);
    }

    /**
     * Get current week information for display
     */
    private String getCurrentWeekInfo() {
        java.time.LocalDate startOfWeek = getStartOfWeek(weekOffset);
        java.time.LocalDate endOfWeek = startOfWeek.plusDays(6);
        
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy");
        
        if (weekOffset == 0) {
            return "Current Week: " + startOfWeek.format(formatter) + " - " + endOfWeek.format(formatter);
        } else if (weekOffset > 0) {
            return "Week +" + weekOffset + ": " + startOfWeek.format(formatter) + " - " + endOfWeek.format(formatter);
        } else {
            return "Week " + weekOffset + ": " + startOfWeek.format(formatter) + " - " + endOfWeek.format(formatter);
        }
    }

    /**
     * Change the displayed week
     */
    private void changeWeek(int offset) {
        weekOffset += offset;
        weekInfoLabel.setText(getCurrentWeekInfo());
        loadTimetable();
    }

    /**
     * Reset to current week
     */
    private void resetToCurrentWeek() {
        weekOffset = 0;
        weekInfoLabel.setText(getCurrentWeekInfo());
        loadTimetable();
    }

    /**
     * Parse date string that can be in various formats:
     * - 2026-02-12 (standard format)
     * - 2026-2-12 (single digit month/day)
     * - 2025-11-10 (mixed)
     */
    private java.time.LocalDate parseFlexibleDate(String dateStr) throws Exception {
        try {
            // Try standard format first
            return java.time.LocalDate.parse(dateStr, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e1) {
            try {
                // Try flexible format (handles single or double digits)
                String[] parts = dateStr.split("-");
                if (parts.length == 3) {
                    int year = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]);
                    int day = Integer.parseInt(parts[2]);
                    return java.time.LocalDate.of(year, month, day);
                }
                throw new Exception("Invalid date format: " + dateStr);
            } catch (Exception e2) {
                throw new Exception("Cannot parse date: " + dateStr);
            }
        }
    }

    private void loadUpcomingClasses() {
        upcomingPanel.removeAll();

        if (allClasses.isEmpty()) {
            JLabel noClasses = new JLabel("No upcoming classes scheduled");
            noClasses.setFont(new Font("Arial", Font.ITALIC, 14));
            noClasses.setForeground(Color.GRAY);
            noClasses.setAlignmentX(Component.CENTER_ALIGNMENT);
            upcomingPanel.add(Box.createVerticalStrut(20));
            upcomingPanel.add(noClasses);
        } else {
            // Get current date
            java.time.LocalDate currentDate = java.time.LocalDate.now();
            
            List<ClassSchedule> sortedClasses = new ArrayList<>(allClasses);
            sortedClasses.sort((c1, c2) -> {
                try {
                    // Sort by date first
                    java.time.LocalDate d1 = parseFlexibleDate(c1.date);
                    java.time.LocalDate d2 = parseFlexibleDate(c2.date);
                    int dateCompare = d1.compareTo(d2);
                    if (dateCompare != 0) return dateCompare;
                    
                    // Then by time
                    return c1.startTime.compareTo(c2.startTime);
                } catch (Exception e) {
                    return c1.startTime.compareTo(c2.startTime);
                }
            });

            List<ClassSchedule> todayClasses = new ArrayList<>();
            List<ClassSchedule> upcomingClasses = new ArrayList<>();

            for (ClassSchedule cs : sortedClasses) {
                try {
                    java.time.LocalDate classDate = parseFlexibleDate(cs.date);
                    
                    if (classDate.equals(currentDate)) {
                        todayClasses.add(cs);
                    } else if (classDate.isAfter(currentDate)) {
                        upcomingClasses.add(cs);
                    }
                } catch (Exception e) {
                    System.out.println("Error parsing date in upcoming classes: " + cs.date);
                }
            }

            if (!todayClasses.isEmpty()) {
                JLabel todayLabel = new JLabel("TODAY (" + currentDate + ")");
                todayLabel.setFont(new Font("Arial", Font.BOLD, 14));
                todayLabel.setForeground(new Color(255, 165, 0));
                todayLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                upcomingPanel.add(Box.createVerticalStrut(10));
                upcomingPanel.add(todayLabel);
                upcomingPanel.add(Box.createVerticalStrut(10));

                for (ClassSchedule cs : todayClasses) {
                    upcomingPanel.add(createClassCard(cs, true));
                    upcomingPanel.add(Box.createVerticalStrut(10));
                }
            }

            if (!upcomingClasses.isEmpty()) {
                JLabel upcomingLabel = new JLabel("UPCOMING CLASSES");
                upcomingLabel.setFont(new Font("Arial", Font.BOLD, 14));
                upcomingLabel.setForeground(new Color(70, 130, 180));
                upcomingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                upcomingPanel.add(Box.createVerticalStrut(15));
                upcomingPanel.add(upcomingLabel);
                upcomingPanel.add(Box.createVerticalStrut(10));

                // Show only next 5 upcoming classes to avoid clutter
                int count = 0;
                for (ClassSchedule cs : upcomingClasses) {
                    if (count >= 5) break;
                    upcomingPanel.add(createClassCard(cs, false));
                    upcomingPanel.add(Box.createVerticalStrut(10));
                    count++;
                }
            }
        }

        upcomingPanel.revalidate();
        upcomingPanel.repaint();
    }

    private JPanel createClassCard(ClassSchedule cs, boolean isToday) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(isToday ? new Color(255, 250, 205) : new Color(240, 248, 255));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(isToday ? new Color(255, 165, 0) : new Color(70, 130, 180), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        card.setMaximumSize(new Dimension(320, 170));

        JLabel moduleLabel = new JLabel(cs.moduleName);
        moduleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        moduleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel dateLabel = new JLabel(cs.date + " (" + cs.day + ")");
        dateLabel.setFont(new Font("Arial", Font.BOLD, 12));
        dateLabel.setForeground(new Color(255, 100, 0));
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel timeLabel = new JLabel(cs.startTime + " - " + cs.endTime);
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel locationLabel = new JLabel(cs.location);
        locationLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        locationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel classIDLabel = new JLabel("Class: " + cs.classID);
        classIDLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        classIDLabel.setForeground(Color.GRAY);
        classIDLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(moduleLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(dateLabel);
        card.add(timeLabel);
        card.add(locationLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(classIDLabel);

        return card;
    }

    private int getDayIndex(String day) {
        for (int i = 0; i < days.length; i++) {
            if (days[i].equalsIgnoreCase(day)) {
                return i;
            }
        }
        return -1;
    }

    private String getModuleName(String moduleID) {
        try {
            String filePath = fileManager.getFilePath(DatabaseFile.MODULE.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 2 && data[0].equals(moduleID)) {
                    br.close();
                    return data[1];
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return moduleID;
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

    class ClassSchedule {
        String classID, moduleID, moduleName, day, startTime, endTime, location, date;

        ClassSchedule(String classID, String moduleID, String moduleName, String day,
                     String startTime, String endTime, String location, String date) {
            this.classID = classID;
            this.moduleID = moduleID;
            this.moduleName = moduleName;
            this.day = day;
            this.startTime = startTime;
            this.endTime = endTime;
            this.location = location;
            this.date = date;
        }
    }
}