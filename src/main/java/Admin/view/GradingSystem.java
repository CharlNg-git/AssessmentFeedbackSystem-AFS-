package Admin.view;

import util.FileManager;
import util.DatabaseFile;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class GradingSystem extends JPanel {
    private String tpNumber;
    private FileManager fileManager = new FileManager();
    
    // Stores references to the text fields for each grade
    private Map<String, JTextField[]> gradeFields; 
    
    // The exact order of grades corresponding to your CSV columns
    private final String[] gradeLabels = {
        "A+", "A", "B+", "B", "C+", "C", "C-", "D", "F+", "F", "F-"
    };

    public GradingSystem(String tpNumber, String role, String name, MainDashboard parent) {
        this.tpNumber =tpNumber;
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        add(createHeader(), BorderLayout.NORTH);
        add(createFormPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        loadCurrentRules();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(255, 165, 0));
        header.setPreferredSize(new Dimension(0, 80));
        
        JLabel title = new JLabel("  Manage Grading Standards");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);
        
        JLabel userInfo = new JLabel(tpNumber + " | ADMIN  ");
        userInfo.setFont(new Font("Arial", Font.BOLD, 18));
        userInfo.setForeground(Color.WHITE);
        header.add(userInfo, BorderLayout.EAST);
        
        return header;
    }

    private JScrollPane createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        
        gradeFields = new LinkedHashMap<>();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // --- Table Headers ---
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblG = new JLabel("Grade");
        lblG.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(lblG, gbc);
        
        gbc.gridx = 1; 
        JLabel lblMin = new JLabel("Min Mark");
        lblMin.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(lblMin, gbc);
        
        gbc.gridx = 2;
        JLabel lblMax = new JLabel("Max Mark");
        lblMax.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(lblMax, gbc);

        // --- Generate Rows for each Grade ---
        int row = 1;
        for (String grade : gradeLabels) {
            // Label
            gbc.gridx = 0; gbc.gridy = row;
            JLabel lbl = new JLabel(grade);
            lbl.setFont(new Font("Arial", Font.BOLD, 14));
            panel.add(lbl, gbc);

            // Min Field
            gbc.gridx = 1;
            JTextField txtMin = new JTextField(6);
            txtMin.setFont(new Font("Arial", Font.PLAIN, 14));
            panel.add(txtMin, gbc);

            // Max Field
            gbc.gridx = 2;
            JTextField txtMax = new JTextField(6);
            txtMax.setFont(new Font("Arial", Font.PLAIN, 14));
            panel.add(txtMax, gbc);

            // Store references
            gradeFields.put(grade, new JTextField[]{txtMin, txtMax});
            row++;
        }

        // Wrap in ScrollPane
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        return scrollPane;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 20));
        panel.setBackground(Color.WHITE);

        JButton btnSave = new JButton("Save New Standard");
        btnSave.setBackground(new Color(46, 204, 113)); // Green
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Arial", Font.BOLD, 16));
        btnSave.setPreferredSize(new Dimension(220, 45));
        btnSave.setFocusPainted(false);
        btnSave.addActionListener(e -> saveNewRule());

        JButton btnRefresh = new JButton("Refresh/Reset");
        btnRefresh.setBackground(new Color(52, 152, 219)); // Blue
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFont(new Font("Arial", Font.BOLD, 16));
        btnRefresh.setPreferredSize(new Dimension(220, 45));
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadCurrentRules());

        panel.add(btnSave);
        panel.add(btnRefresh);
        return panel;
    }

    private void loadCurrentRules() {
        String path = fileManager.getFilePath(DatabaseFile.GRADING.getFileName());
        String lastLine = "";

        System.out.println("=== Loading Current Grading Rules ===");

        // 1. Read the LAST line to get current active rules
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty() && !line.startsWith("gradingID")) {
                    lastLine = line;
                }
            }
        } catch (Exception e) { 
            System.err.println("Error loading grading rules: " + e.getMessage());
            e.printStackTrace(); 
        }

        // 2. Populate Fields if file exists
        if (!lastLine.isEmpty()) {
            String[] data = lastLine.split(",");
            System.out.println("Current rule: " + lastLine);
            
            // CSV Structure: ID, Date, A+min, A+max...
            int dataIndex = 2; // Start after ID and Date
            for (String grade : gradeLabels) {
                if (dataIndex + 1 < data.length) {
                    JTextField[] fields = gradeFields.get(grade);
                    if (fields != null) {
                        fields[0].setText(data[dataIndex].trim());     // Min
                        fields[1].setText(data[dataIndex + 1].trim()); // Max
                        System.out.println("  " + grade + ": " + data[dataIndex].trim() + " - " + data[dataIndex + 1].trim());
                    }
                    dataIndex += 2;
                }
            }
        } else {
            System.out.println("No existing rules found. Using defaults.");
            // Set default values
            setDefaultRules();
        }
    }

    private void setDefaultRules() {
        // Default grading scheme
        String[][] defaults = {
            {"A+", "90", "100"},
            {"A", "80", "89"},
            {"B+", "75", "79"},
            {"B", "70", "74"},
            {"C+", "65", "69"},
            {"C", "60", "64"},
            {"C-", "55", "59"},
            {"D", "50", "54"},
            {"F+", "45", "49"},
            {"F", "40", "44"},
            {"F-", "0", "39"}
        };

        for (String[] def : defaults) {
            JTextField[] fields = gradeFields.get(def[0]);
            if (fields != null) {
                fields[0].setText(def[1]); // Min
                fields[1].setText(def[2]); // Max
            }
        }
    }

    private void saveNewRule() {
        // 1. Validate Inputs - Check all fields are filled and valid
        for (String grade : gradeLabels) {
            JTextField[] fields = gradeFields.get(grade);
            String minStr = fields[0].getText().trim();
            String maxStr = fields[1].getText().trim();

            if (minStr.isEmpty() || maxStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields for grade " + grade);
                return;
            }
            try {
                double minVal = Double.parseDouble(minStr);
                double maxVal = Double.parseDouble(maxStr);
                
                if (minVal < 0 || maxVal > 100) {
                    JOptionPane.showMessageDialog(this, 
                        "Invalid range for " + grade + ".\nValues must be between 0 and 100.");
                    return;
                }
                
                if (minVal > maxVal) {
                    JOptionPane.showMessageDialog(this, 
                        "Invalid range for " + grade + ".\nMin (" + minVal + ") cannot be greater than Max (" + maxVal + ").");
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid number format for " + grade);
                return;
            }
        }

        // 2. Check for overlaps between grade ranges
        for (int i = 0; i < gradeLabels.length; i++) {
            JTextField[] fields1 = gradeFields.get(gradeLabels[i]);
            int min1 = Integer.parseInt(fields1[0].getText().trim());
            int max1 = Integer.parseInt(fields1[1].getText().trim());
            
            for (int j = i + 1; j < gradeLabels.length; j++) {
                JTextField[] fields2 = gradeFields.get(gradeLabels[j]);
                int min2 = Integer.parseInt(fields2[0].getText().trim());
                int max2 = Integer.parseInt(fields2[1].getText().trim());
                
                // Check if ranges overlap: they overlap if NOT (range1 completely before range2 OR range1 completely after range2)
                boolean overlap = !(max1 < min2 || max2 < min1);
                
                if (overlap) {
                    JOptionPane.showMessageDialog(this, 
                        "ERROR: Grade ranges overlap!\n\n" + 
                        gradeLabels[i] + ": " + min1 + " - " + max1 + "\n" +
                        gradeLabels[j] + ": " + min2 + " - " + max2 + "\n\n" +
                        "Please ensure all ranges are distinct with no overlaps.\n\n" +
                        "Example: If A+ is 90-100, then A should be 80-89 (not 80-90).",
                        "Overlapping Ranges",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }
        
        // 3. Check for gaps in coverage (0-100 must be fully covered)
        boolean[] covered = new boolean[101]; // 0-100
        for (String grade : gradeLabels) {
            JTextField[] fields = gradeFields.get(grade);
            int min = Integer.parseInt(fields[0].getText().trim());
            int max = Integer.parseInt(fields[1].getText().trim());
            
            for (int i = min; i <= max; i++) {
                covered[i] = true;
            }
        }
        
        // Find gaps
        StringBuilder gaps = new StringBuilder();
        int gapStart = -1;
        for (int i = 0; i <= 100; i++) {
            if (!covered[i]) {
                if (gapStart == -1) gapStart = i;
            } else {
                if (gapStart != -1) {
                    if (gaps.length() > 0) gaps.append(", ");
                    if (gapStart == i - 1) {
                        gaps.append(gapStart);
                    } else {
                        gaps.append(gapStart).append("-").append(i - 1);
                    }
                    gapStart = -1;
                }
            }
        }
        if (gapStart != -1) {
            if (gaps.length() > 0) gaps.append(", ");
            gaps.append(gapStart).append("-100");
        }
        
        // Block saving if gaps exist
        if (gaps.length() > 0) {
            JOptionPane.showMessageDialog(this,
                "ERROR: Incomplete grade coverage!\n\n" +
                "The following marks are not covered by any grade:\n" + 
                gaps.toString() + "\n\n" +
                "All marks from 0-100 must be assigned to a grade.\n" +
                "Please adjust your ranges to cover all marks.",
                "Incomplete Coverage",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 4. Generate New ID
        String newID = generateGradingID();
        String date = LocalDate.now().toString();

        StringBuilder sb = new StringBuilder();
        sb.append(newID).append(",").append(date);

        // Append grades in strict order
        for (String grade : gradeLabels) {
            JTextField[] fields = gradeFields.get(grade);
            sb.append(",").append(fields[0].getText().trim()); // Min
            sb.append(",").append(fields[1].getText().trim()); // Max
        }

        sb.append(",Passed"); // Append Status

        // 5. Write to File (Append Mode)
        try {
            File file = new File(fileManager.getFilePath(DatabaseFile.GRADING.getFileName()));
            boolean fileExists = file.exists();
            
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            
            if (!fileExists) {
                // Write header
                bw.write("gradingID,date,A+_min,A+_max,A_min,A_max,B+_min,B+_max,B_min,B_max,C+_min,C+_max,C_min,C_max,C-_min,C-_max,D_min,D_max,F+_min,F+_max,F_min,F_max,F-_min,F-_max,CT");
                bw.newLine();
            }
            
            bw.write(sb.toString());
            bw.newLine();
            bw.close();
            
            JOptionPane.showMessageDialog(this, 
                "New Grading Standard Saved Successfully!\n\nGrading ID: " + newID + "\nDate: " + date,
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
                
            System.out.println("Saved new grading rule: " + sb.toString());
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Error saving file: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private String generateGradingID() {
        String prefix = "G";
        int maxNum = 0;
        
        try {
            BufferedReader br = new BufferedReader(
                new FileReader(fileManager.getFilePath(DatabaseFile.GRADING.getFileName()))
            );
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length > 0 && data[0].startsWith(prefix)) {
                    try {
                        String numPart = data[0].substring(prefix.length());
                        int num = Integer.parseInt(numPart);
                        if (num > maxNum) maxNum = num;
                    } catch (NumberFormatException e) {}
                }
            }
            br.close();
        } catch (IOException e) {
            // File doesn't exist yet, start from 1
        }
        
        return String.format("%s%03d", prefix, maxNum + 1);
    }
}