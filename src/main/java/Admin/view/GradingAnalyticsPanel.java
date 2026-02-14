package Admin.view;

import javax.swing.*;
import java.awt.*;

public class GradingAnalyticsPanel extends JPanel {
    private String tpNumber, role, name;
    private MainDashboard parentDashboard;

    public GradingAnalyticsPanel(String tpNumber, String role, String name, MainDashboard parentDashboard) {
        this.tpNumber = tpNumber;
        this.role = role;
        this.name = name;
        this.parentDashboard = parentDashboard;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Directly show GradingSystem (Modify Standard)
        add(new GradingSystem(tpNumber, role, name, parentDashboard), BorderLayout.CENTER);
    }

    // Empty refresh method for compatibility
    public void refreshData() {
        // Not needed anymore since we removed analytics
    }
}