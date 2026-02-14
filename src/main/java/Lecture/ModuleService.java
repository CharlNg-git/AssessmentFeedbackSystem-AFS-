package Lecture;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import util.DatabaseFile;
import util.FileManager;

public class ModuleService {

    private static final FileManager fileManager = new FileManager();

    // Quick inner class for Module model if not exists, or I should create it
    // separately.
    // Let's create it properly.

    public static List<String[]> getModulesForLecturer(String lecturerId) {
        List<String[]> modules = new ArrayList<>();

        // 1. Find Module IDs assigned to this lecturer
        List<String> assignmentLines = readDataLines(DatabaseFile.MODULE_ASSIGNMENTS);
        List<String> assignedModuleIds = new ArrayList<>();
        // Schema: moduleID,tpNumber,IntakeID
        for (String line : assignmentLines) {
            String[] parts = line.split(",");
            if (parts.length >= 2) {
                if (parts[1].trim().equals(lecturerId)) {
                    assignedModuleIds.add(parts[0].trim());
                }
            }
        }

        // 2. Get Module Details for those IDs
        List<String> moduleLines = readDataLines(DatabaseFile.MODULE);
        // Schema: moduleID,moduleName,credirHour,sessionType,tpNumber(Leader)
        for (String line : moduleLines) {
            String[] parts = line.split(",");
            if (parts.length >= 2) {
                String mid = parts[0].trim();
                String mName = parts[1].trim();

                if (assignedModuleIds.contains(mid)) {
                    // Return ID, Name
                    modules.add(new String[] { mid, mName });
                }
            }
        }
        return modules;
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
