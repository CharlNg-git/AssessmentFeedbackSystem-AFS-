package Lecture;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import util.DatabaseFile;
import util.FileManager;

public class StudentService {

    private static final FileManager fileManager = new FileManager();
    public static Map<String, String> getStudentNames() {
        Map<String, String> names = new HashMap<>();
        List<String> lines = readDataLines(DatabaseFile.STUDENT);
        // fullName,ic,gender,dob,nation,race,addr,tpNumber,...
        // tpNumber index 8, Name index 0
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length >= 9) {
                names.put(parts[8].trim(), parts[0].trim());
            }
        }
        return names;
    }

    public static List<String> getStudentsForModule(String moduleId) {
        List<String> validStudentIds = new ArrayList<>();
        String targetIntake = "";

        // 1. Find the Intake Code for this Module (from class_register.txt)
        List<String> regLines = readDataLines(DatabaseFile.CLASS_REGISTER);
        // Schema: classID,moduleID,intakeCode,tpNumber,date...
        for (String line : regLines) {
            String[] parts = line.split(",");
            if (parts.length >= 3) {
                if (parts[1].trim().equals(moduleId)) {
                    targetIntake = parts[2].trim();
                    break; // Found the intake for this module
                }
            }
        }

        if (targetIntake.isEmpty()) {
            return validStudentIds; // No intake found
        }

        // 2. Find all students in this Intake (from studentinfo.txt)
        List<String> studLines = readDataLines(DatabaseFile.STUDENT);
        // Schema:
        // fullName,ic,gender,dob,nation,race,addr,contact,tpNumber,password,intakeCode
        for (String line : studLines) {
            String[] parts = line.split(",");
            if (parts.length >= 11) {
                String intake = parts[10].trim();
                if (intake.equalsIgnoreCase(targetIntake)) {
                    String sid = parts[8].trim();
                    if (!validStudentIds.contains(sid)) {
                        validStudentIds.add(sid);
                    }
                }
            }
        }
        return validStudentIds;
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
}
