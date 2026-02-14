package Lecture;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import util.DatabaseFile;
import util.FileManager;

public class GradingService {

    private static final FileManager fileManager = new FileManager();

    public static List<StudentResult> getResults(String moduleId, String assessmentId) {
        List<StudentResult> results = new ArrayList<>();
        List<String> lines = readDataLines(DatabaseFile.MARKS);
        // tpNumber,moduleID,assessmentID,marks,grade,gradingID

        // 1. Read Feedback first to map it
        List<String> feedbackLines = readDataLines(DatabaseFile.FEEDBACK);
        java.util.Map<String, String> feedbackMap = new java.util.HashMap<>();
        // Schema: feedbackID,tpNumber,moduleID,feedbackType,message
        // Message format: "Assessment {ID}: {Feedback}"

        for (String fLine : feedbackLines) {
            String[] fParts = fLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1); // Split by comma, ignoring commas
                                                                                    // in quotes
            if (fParts.length >= 5) {
                String fSid = fParts[1].trim();
                String fMsg = fParts[4].trim().replace("\"", ""); // Remove quotes

                // Check if this feedback is for the current assessment
                String prefix = "Assessment " + assessmentId + ": ";
                if (fMsg.startsWith(prefix)) {
                    // Extract actual feedback
                    feedbackMap.put(fSid, fMsg.substring(prefix.length()));
                }
            }
        }

        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length >= 5) {
                if (parts[1].trim().equals(moduleId) && parts[2].trim().equals(assessmentId)) {
                    double marks = 0;
                    try {
                        marks = Double.parseDouble(parts[3].trim());
                    } catch (Exception e) {
                    }

                    String sid = parts[0].trim();
                    String feedback = feedbackMap.getOrDefault(sid, "");
                    results.add(new StudentResult(sid, parts[2].trim(), marks, feedback));
                }
            }
        }
        return results;
    }

    public static void saveResult(String studentId, String moduleId, String assessmentId, double marks,
            String feedback) {
        // 1. Calculate Grade (Simple logic for now)
        String grade = calculateGrade(marks);
        String gradingId = "G001"; // Hardcoded for simplified scope as per current schema

        // 2. Write to marks.txt
        // tpNumber,moduleID,assessmentID,marks,grade,gradingID
        // logic: Remove existing if present? Or append? appending creates duplicates.
        // Real implementation should update.
        // For this assignment, file rewrite is safer.

        // TODO: Update logic. Read all, replace matching line, write back.
        List<String> lines = readDataLines(DatabaseFile.MARKS);
        List<String> newLines = new ArrayList<>();
        newLines.add("tpNumber,moduleID,assessmentID,marks,grade,gradingID"); // Header

        boolean found = false;

        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length >= 3) {
                if (parts[0].trim().equals(studentId) &&
                        parts[1].trim().equals(moduleId) &&
                        parts[2].trim().equals(assessmentId)) {

                    // Replace this line
                    String newLine = String.format("%s,%s,%s,%.1f,%s,%s",
                            studentId, moduleId, assessmentId, marks, grade, gradingId);
                    newLines.add(newLine);
                    found = true;
                } else {
                    newLines.add(line);
                }
            }
        }

        if (!found) {
            String newLine = String.format("%s,%s,%s,%.1f,%s,%s",
                    studentId, moduleId, assessmentId, marks, grade, gradingId);
            newLines.add(newLine);
        }

        writeAllLinesWithHeader(DatabaseFile.MARKS, newLines);

        // 3. Save Feedback if exists
        if (feedback != null && !feedback.isEmpty()) {
            // feedbackID,tpNumber,moduleID,feedbackType,message
            String feedId = "F" + System.currentTimeMillis();
            // Sanitize feedback to remove commas/newlines if simpler CSV parsing is needed,
            // or quote it. The previous logic quoted it.
            String safeFeedback = feedback.replace("\"", "'");

            String feedLine = String.format("%s,%s,%s,lecturer,\"%s: %s\"",
                    feedId, studentId, moduleId, "Assessment " + assessmentId, safeFeedback);

            appendLine(DatabaseFile.FEEDBACK, feedLine);
            System.out.println("Feedback saved for " + studentId); // Debug
        }
    }

    public static String calculateGrade(double marks) {
        if (marks >= 80)
            return "A";
        if (marks >= 75)
            return "A-";
        if (marks >= 70)
            return "B+";
        if (marks >= 65)
            return "B";
        if (marks >= 60)
            return "B-";
        if (marks >= 55)
            return "C+";
        if (marks >= 50)
            return "C";
        if (marks >= 40)
            return "D";
        return "F";
    }

    public static double calculateGPA(double marks) {
        if (marks >= 80)
            return 4.0;
        if (marks >= 75)
            return 3.7;
        if (marks >= 70)
            return 3.3;
        if (marks >= 65)
            return 3.0;
        if (marks >= 60)
            return 2.7;
        if (marks >= 55)
            return 2.3;
        if (marks >= 50)
            return 2.0;
        if (marks >= 40)
            return 1.0;
        return 0.0;
    }

    // Helper: read non-header, non-empty lines from a database file
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

    // Helper: overwrite file with full content (including header)
    private static void writeAllLinesWithHeader(DatabaseFile dbFile, List<String> lines) {
        String path = fileManager.getFilePath(dbFile.getFileName());
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            for (String l : lines) {
                bw.write(l);
                bw.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper: append a single CSV line
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
