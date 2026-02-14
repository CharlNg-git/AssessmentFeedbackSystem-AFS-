package Lecture;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import util.DatabaseFile;
import util.FileManager;

public class AssessmentService {

    private static final FileManager fileManager = new FileManager();

    public static List<Assessment> getAssessmentsForModule(String moduleId) {
        List<Assessment> assessments = new ArrayList<>();
        List<String> lines = readDataLines(DatabaseFile.ASSESSMENTS);

        for (String line : lines) {
            // Schema: assessmentID,moduleID,name,type,weight,dueDate
            // Legacy/Current: assessmentID,moduleID,name,weight,dueDate,
            String[] parts = line.split(",");
            if (parts.length >= 4) {
                if (parts[1].trim().equals(moduleId)) {
                    String id = parts[0].trim();
                    String modId = parts[1].trim();
                    String name = parts[2].trim();
                    String type = "Assignment"; // Default for legacy data
                    int weightLoc = 3;

                    // Check if we have the new schema (Type at index 3)
                    // If parts[3] is a number, it's likely weight (old schema)
                    // If parts[3] is text, it's Type
                    try {
                        Integer.parseInt(parts[3].trim());
                        // Is number -> Old Schema: Name, Weight, Date
                        weightLoc = 3;
                    } catch (NumberFormatException e) {
                        // Not number -> New Schema: Name, Type, Weight
                        type = parts[3].trim();
                        weightLoc = 4;
                    }

                    int weight = 0;
                    try {
                        if (parts.length > weightLoc)
                            weight = Integer.parseInt(parts[weightLoc].trim());
                    } catch (Exception e) {
                    }

                    assessments.add(new Assessment(id, modId, name, type, weight));
                }
            }
        }
        return assessments;
    }

    public static void addAssessment(Assessment assessment, String dueDate) {
        // Generate CSV line
        // assessmentID,moduleID,name,type,weight,dueDate
        String line = String.format("%s,%s,%s,%s,%d,%s",
                assessment.getId(),
                assessment.getModuleId(),
                assessment.getName(),
                assessment.getType(),
                assessment.getMaxMarks(),
                dueDate);
        appendLine(DatabaseFile.ASSESSMENTS, line);
    }

    // Helper: read all non-header, non-empty lines from a database file
    private static List<String> readDataLines(DatabaseFile dbFile) {
        List<String> lines = new ArrayList<>();
        String path = fileManager.getFilePath(dbFile.getFileName());
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lines;
    }

    // Helper: append a single CSV line to a database file
    private static void appendLine(DatabaseFile dbFile, String content) {
        String path = fileManager.getFilePath(dbFile.getFileName());
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path, true))) {
            bw.write(content);
            bw.newLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
