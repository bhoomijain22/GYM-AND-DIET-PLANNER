package dietplanner;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ActivityManager extends JFrame {
private static final String DB_URL = "jdbc:mysql://localhost:3306/diet";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "root";


private JTextField userIdField, typeField, durationField, distanceField, caloriesField, dateField, goalIdField;
private JTextArea displayArea;

private int userId;
private int goalId;

public ActivityManager(int userId, int goalId) {
    this.userId = userId;
    this.goalId = goalId;

    setTitle("Activity Manager");
    setSize(600, 500);
    setLayout(new BorderLayout());

    JPanel inputPanel = new JPanel(new GridLayout(7, 2, 10, 10));
    inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    inputPanel.add(new JLabel("User ID:"));
    userIdField = new JTextField(String.valueOf(userId));
    userIdField.setEditable(false);
    inputPanel.add(userIdField);

    inputPanel.add(new JLabel("Type:"));
    typeField = new JTextField();
    inputPanel.add(typeField);

    inputPanel.add(new JLabel("Duration (hrs):"));
    durationField = new JTextField();
    inputPanel.add(durationField);

    inputPanel.add(new JLabel("Distance (km):"));
    distanceField = new JTextField();
    inputPanel.add(distanceField);

    inputPanel.add(new JLabel("Calories Burned:"));
    caloriesField = new JTextField();
    inputPanel.add(caloriesField);

    inputPanel.add(new JLabel("Date (YYYY-MM-DD):"));
    dateField = new JTextField();
    inputPanel.add(dateField);

    inputPanel.add(new JLabel("Goal ID:"));
    goalIdField = new JTextField(String.valueOf(goalId));
    goalIdField.setEditable(false);
    inputPanel.add(goalIdField);

    JPanel buttonPanel = new JPanel();
    JButton addButton = new JButton("Add Activity");
    JButton viewButton = new JButton("View My Activities");
    buttonPanel.add(addButton);
    buttonPanel.add(viewButton);

    displayArea = new JTextArea(10, 50);
    displayArea.setEditable(false);
    JScrollPane scrollPane = new JScrollPane(displayArea);

    add(inputPanel, BorderLayout.NORTH);
    add(buttonPanel, BorderLayout.CENTER);
    add(scrollPane, BorderLayout.SOUTH);

    addButton.addActionListener(e -> addActivity());
    viewButton.addActionListener(e -> viewActivities());

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setVisible(true);
}

private void addActivity() {
    String query = "INSERT INTO Activity (UserID, Type, Duration, Distance, CaloriesBurned, Date, GoalID) VALUES (?, ?, ?, ?, ?, ?, ?)";
    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setInt(1, userId);
        stmt.setString(2, typeField.getText());
        stmt.setFloat(3, Float.parseFloat(durationField.getText()));
        stmt.setFloat(4, Float.parseFloat(distanceField.getText()));
        stmt.setFloat(5, Float.parseFloat(caloriesField.getText()));
        stmt.setString(6, dateField.getText());
        stmt.setInt(7, goalId);

        stmt.executeUpdate();
        JOptionPane.showMessageDialog(this, "Activity added successfully!");

    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error adding activity.");
    }
}

private void viewActivities() {
    String query = "SELECT * FROM Activity WHERE UserID = ?";
    displayArea.setText("");
    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setInt(1, userId);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            displayArea.append("Activity ID: " + rs.getInt("ActivityID") +
                    ", Type: " + rs.getString("Type") +
                    ", Duration: " + rs.getFloat("Duration") +
                    " hrs, Distance: " + rs.getFloat("Distance") +
                    " km, Calories: " + rs.getFloat("CaloriesBurned") +
                    ", Date: " + rs.getString("Date") + "\n");
        }

        if (displayArea.getText().isEmpty()) {
            displayArea.setText("No activities found.");
        }

    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error fetching activities.");
    }
}

// Optional: test manually if needed
public static void main(String[] args) {
    new ActivityManager(1, 101);  // Example userId and goalId
}


} 
