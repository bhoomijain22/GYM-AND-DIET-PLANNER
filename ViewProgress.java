package dietplanner;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ViewProgress extends JFrame {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/diet";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";

    private JTextField userIdField;
    private JTextArea displayArea;
    private int userId;

    public ViewProgress(int userId) {
        this.userId = userId;

        setTitle("View Progress");
        setSize(600, 500);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        inputPanel.add(new JLabel("User ID:"));
        userIdField = new JTextField(String.valueOf(userId));
        userIdField.setEditable(false);
        inputPanel.add(userIdField);

        JPanel buttonPanel = new JPanel();
        JButton viewProgressButton = new JButton("View Progress");
        buttonPanel.add(viewProgressButton);

        displayArea = new JTextArea(15, 50);
        displayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(displayArea);

        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);

        viewProgressButton.addActionListener(e -> viewProgress());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void viewProgress() {
        StringBuilder progressDetails = new StringBuilder();

        // Fetch Activity Data (Calories Burned, Distance, Duration)
        String activityQuery = "SELECT Type, Duration, Distance, CaloriesBurned, Date FROM Activity WHERE UserID = ? ORDER BY Date DESC";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(activityQuery)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            boolean activityDataFound = false;

            while (rs.next()) {
                activityDataFound = true;
                progressDetails.append("Activity Type: ").append(rs.getString("Type"))
                        .append(", Duration: ").append(rs.getFloat("Duration")).append(" hrs")
                        .append(", Distance: ").append(rs.getFloat("Distance")).append(" km")
                        .append(", Calories Burned: ").append(rs.getFloat("CaloriesBurned"))
                        .append(", Date: ").append(rs.getString("Date")).append("\n");
                progressDetails.append("--------------------------------------------------------\n");
            }

            if (!activityDataFound) {
                progressDetails.append("No activity data found.\n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching activity data.");
        }

        // Fetch Nutrition Data (Calories Consumed, Food Items, Meal Type)
        String nutritionQuery = "SELECT MealType, FoodItem, Quantity, Calories, Date FROM Nutrition WHERE UserID = ? ORDER BY Date DESC";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(nutritionQuery)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            boolean nutritionDataFound = false;

            while (rs.next()) {
                nutritionDataFound = true;
                progressDetails.append("Meal Type: ").append(rs.getString("MealType"))
                        .append(", Food Item: ").append(rs.getString("FoodItem"))
                        .append(", Quantity: ").append(rs.getFloat("Quantity"))
                        .append(", Calories: ").append(rs.getFloat("Calories"))
                        .append(", Date: ").append(rs.getString("Date")).append("\n");
                progressDetails.append("--------------------------------------------------------\n");
            }

            if (!nutritionDataFound) {
                progressDetails.append("No nutrition data found.\n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching nutrition data.");
        }

        // Calculate Total Calories Burned, Consumed, and Deficit per Day
        progressDetails.append("\nCalories Burned, Consumed, and Deficit per Day:\n");
        
        // Get calories burned for each day
        String caloriesBurnedQuery = "SELECT Date, SUM(CaloriesBurned) AS TotalCaloriesBurned " +
                                     "FROM Activity WHERE UserID = ? GROUP BY Date ORDER BY Date DESC";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(caloriesBurnedQuery)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            boolean caloriesBurnedDataFound = false;
            while (rs.next()) {
                caloriesBurnedDataFound = true;
                String date = rs.getString("Date");
                float caloriesBurned = rs.getFloat("TotalCaloriesBurned");

                // Fetch corresponding calories consumed for the same day
                String caloriesConsumedQuery = "SELECT SUM(Calories) AS TotalCaloriesConsumed " +
                                               "FROM Nutrition WHERE UserID = ? AND Date = ? GROUP BY Date";
                try (PreparedStatement stmt2 = conn.prepareStatement(caloriesConsumedQuery)) {
                    stmt2.setInt(1, userId);
                    stmt2.setString(2, date);
                    ResultSet rs2 = stmt2.executeQuery();

                    float caloriesConsumed = 0;
                    if (rs2.next()) {
                        caloriesConsumed = rs2.getFloat("TotalCaloriesConsumed");
                    }

                    // Calculate calories deficit
                    float caloriesDeficit = caloriesBurned - caloriesConsumed;

                    // Display results for each day
                    progressDetails.append("Date: ").append(date)
                            .append(", Calories Burned: ").append(caloriesBurned)
                            .append(", Calories Consumed: ").append(caloriesConsumed)
                            .append(", Calories Deficit: ").append(caloriesDeficit)
                            .append(" kcal\n");
                }
            }

            if (!caloriesBurnedDataFound) {
                progressDetails.append("No calories burned data found.\n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching calories burned data.");
        }

        // Display combined progress
        displayArea.setText(progressDetails.toString());
    }

    public static void main(String[] args) {
        new ViewProgress(1);  // Example userId
    }
}
