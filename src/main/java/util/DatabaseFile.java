package util;

public enum DatabaseFile {

    ASSESSMENTS("assessments.txt"),
    ATTENDANCE("attendance.txt"),
    CLASS_REGISTER("class_register.txt"),
    CLASSES("classes.txt"),
    FEEDBACK("feedback.txt"),
    GRADING("grading.txt"),
    LECTURER_ASSIGNMENTS("lecturer_assignments.txt"),
    MARKS("marks.txt"),
    MODULE("module.txt"),
    MODULE_ASSIGNMENTS("module_assignments.txt"),
    STAFF("staffinfo.txt"),
    STUDENT("studentinfo.txt"),
    SUBMISSION("submission.txt"),
    USER("user.txt");

    private final String fileName;

    DatabaseFile(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
