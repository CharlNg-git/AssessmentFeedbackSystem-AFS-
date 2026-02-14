package Lecture;

public class StudentResult {
    private String studentId;
    private String assessmentId;
    private double marks;
    private String feedback;

    public StudentResult(String studentId, String assessmentId, double marks, String feedback) {
        this.studentId = studentId;
        this.assessmentId = assessmentId;
        this.marks = marks;
        this.feedback = feedback;
    }

    public String getStudentId() { return studentId; }
    public String getAssessmentId() { return assessmentId; }
    public double getMarks() { return marks; }
    public String getFeedback() { return feedback; }

    public void setMarks(double marks) { this.marks = marks; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
}
