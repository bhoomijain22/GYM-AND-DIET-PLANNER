package dietplanner;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ManageNutrition extends JFrame {
private static final String DB_URL = "jdbc:mysql://localhost:3306/diet";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "root";

private JTextField userIdField, mealTypeField, foodItemField, quantityField, caloriesField, dateField;
private JTextArea displayArea;
private int userId;

public ManageNutrition(int userId) {
    this.userId = userId;

    setTitle("Manage Nutrition");
    setSize(600, 500);
    setLayout(new BorderLayout());

    JPanel inputPanel = new JPanel(new GridLayout(6, 2, 10, 10));
    inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    inputPanel.add(new JLabel("User ID:"));
    userIdField = new JTextField(String.valueOf(userId));
    userIdField.setEditable(false);
    inputPanel.add(userIdField);

    inputPanel.add(new JLabel("Meal Type:"));
    mealTypeField = new JTextField();
    inputPanel.add(mealTypeField);

    inputPanel.add(new JLabel("Food Item:"));
    foodItemField = new JTextField();
    inputPanel.add(foodItemField);

    inputPanel.add(new JLabel("Quantity:"));
    quantityField = new JTextField();
    inputPanel.add(quantityField);

    inputPanel.add(new JLabel("Calories:"));
    caloriesField = new JTextField();
    inputPanel.add(caloriesField);

    inputPanel.add(new JLabel("Date (YYYY-MM-DD):"));
    dateField = new JTextField();
    inputPanel.add(dateField);

    JPanel buttonPanel = new JPanel();
    JButton addButton = new JButton("Add Nutrition");
    JButton viewButton = new JButton("View Nutrition");
    JButton backButton = new JButton("Back");  // Back Button
    buttonPanel.add(addButton);
    buttonPanel.add(viewButton);
    buttonPanel.add(backButton);  // Add the Back button

    displayArea = new JTextArea(10, 50);
    displayArea.setEditable(false);
    JScrollPane scrollPane = new JScrollPane(displayArea);

    add(inputPanel, BorderLayout.NORTH);
    add(buttonPanel, BorderLayout.CENTER);
    add(scrollPane, BorderLayout.SOUTH);

    addButton.addActionListener(e -> addNutrition());
    viewButton.addActionListener(e -> viewNutrition());
    backButton.addActionListener(e -> goBack());  // Action for Back button

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setVisible(true);
}

private void addNutrition() {
    String query = "INSERT INTO Nutrition (UserID, MealType, FoodItem, Quantity, Calories, Date) VALUES (?, ?, ?, ?, ?, ?)";
    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setInt(1, userId);
        stmt.setString(2, mealTypeField.getText());
        stmt.setString(3, foodItemField.getText());
        stmt.setFloat(4, Float.parseFloat(quantityField.getText()));
        stmt.setFloat(5, Float.parseFloat(caloriesField.getText()));
        stmt.setString(6, dateField.getText());

        stmt.executeUpdate();
        JOptionPane.showMessageDialog(this, "Nutrition added successfully!");

    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error adding nutrition.");
    }
}

private void viewNutrition() {
    String query = "SELECT * FROM Nutrition WHERE UserID = ?";
    displayArea.setText("");
    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setInt(1, userId);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            displayArea.append("Nutrition ID: " + rs.getInt("NutritionID") +
                    ", Meal Type: " + rs.getString("MealType") +
                    ", Food Item: " + rs.getString("FoodItem") +
                    ", Quantity: " + rs.getFloat("Quantity") +
                    ", Calories: " + rs.getFloat("Calories") +
                    ", Date: " + rs.getString("Date") + "\n");
        }

        if (displayArea.getText().isEmpty()) {
            displayArea.setText("No nutrition data found.");
        }

    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "SQL Error: " + e.getMessage());
    }
}

private void goBack() {
    // Dispose the current window (ManageNutrition) and open the previous panel (or window)
   dispose();
 
    // For example, if you want to go back to a login or home window:
    // new PreviousWindow().setVisible(true); 
    // Note: Replace `PreviousWindow` with the class of your previous window.
}

public static void main(String[] args) {
    new ManageNutrition(1);  // Example userId
}

}

