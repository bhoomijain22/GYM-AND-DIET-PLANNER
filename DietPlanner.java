package dietplanner;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class DietPlanner extends JFrame {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/diet";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";

    private CardLayout cardLayout;
    private JPanel mainPanel;

    private int loggedInUserId = -1;
    private int loggedInGoalId = -1;

    public DietPlanner() {
        setTitle("Diet Planner");
        setSize(400, 300);
        setLocation(600, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createLoginPanel(), "Login");
        mainPanel.add(createRegisterPanel(), "Register");
        mainPanel.add(createGoalPanel(), "SetGoal");
        mainPanel.add(createDashboardPanel(), "Dashboard");

        add(mainPanel);
        cardLayout.show(mainPanel, "Login");

        setVisible(true);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));

        JLabel emailLabel = new JLabel("Email:");
        JTextField emailField = new JTextField();

        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField();

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        panel.add(emailLabel);
        panel.add(emailField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(loginButton);
        panel.add(registerButton);

        loginButton.addActionListener(e -> {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            int userId = loginUser(email, password);
            if (userId != -1) {
                loggedInUserId = userId;
                loggedInGoalId = fetchGoalId(userId);
                System.out.println("Login successful. UserID: " + loggedInUserId + ", GoalID: " + loggedInGoalId);
                JOptionPane.showMessageDialog(this, "Login successful!");
                cardLayout.show(mainPanel, "Dashboard");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid email or password.");
            }
        });

        registerButton.addActionListener(e -> cardLayout.show(mainPanel, "Register"));

        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridLayout(8, 2, 10, 10));

        JLabel nameLabel = new JLabel("Name:");
        JTextField nameField = new JTextField();

        JLabel ageLabel = new JLabel("Age:");
        JTextField ageField = new JTextField();

        JLabel genderLabel = new JLabel("Gender:");
        String[] genders = {"Male", "Female", "Other"};
        JComboBox<String> genderBox = new JComboBox<>(genders);

        JLabel heightLabel = new JLabel("Height (cm):");
        JTextField heightField = new JTextField();

        JLabel weightLabel = new JLabel("Weight (kg):");
        JTextField weightField = new JTextField();

        JLabel emailLabel = new JLabel("Email:");
        JTextField emailField = new JTextField();

        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField();

        JButton registerButton = new JButton("Register");
        JButton backButton = new JButton("Back");

        panel.add(nameLabel);
        panel.add(nameField);
        panel.add(ageLabel);
        panel.add(ageField);
        panel.add(genderLabel);
        panel.add(genderBox);
        panel.add(heightLabel);
        panel.add(heightField);
        panel.add(weightLabel);
        panel.add(weightField);
        panel.add(emailLabel);
        panel.add(emailField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(registerButton);
        panel.add(backButton);

        registerButton.addActionListener(e -> {
            try {
                String name = nameField.getText();
                String email = emailField.getText();
                String password = new String(passwordField.getPassword());
                int age = Integer.parseInt(ageField.getText());
                float height = Float.parseFloat(heightField.getText());
                float weight = Float.parseFloat(weightField.getText());
                String gender = (String) genderBox.getSelectedItem();

                if (registerUser(name, age, gender, height, weight, email, password)) {
                    loggedInUserId = getLastInsertedUserId();
                    System.out.println("Registered. UserID: " + loggedInUserId);
                    JOptionPane.showMessageDialog(this, "Registration successful! Now set your goal.");
                    cardLayout.show(mainPanel, "SetGoal");
                } else {
                    JOptionPane.showMessageDialog(this, "Registration failed!");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Invalid input. Please fill all fields correctly.");
            }
        });

        backButton.addActionListener(e -> cardLayout.show(mainPanel, "Login"));

        return panel;
    }

    private JPanel createGoalPanel() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));

        JLabel goalLabel = new JLabel("Goal Type:");
        String[] goalOptions = {"Weight Loss", "Muscle Gain", "Maintain Fitness"};
        JComboBox<String> goalBox = new JComboBox<>(goalOptions);

        JLabel targetLabel = new JLabel("Target Weight (kg):");
        JTextField targetField = new JTextField();

        JLabel deadlineLabel = new JLabel("Deadline (YYYY-MM-DD):");
        JTextField deadlineField = new JTextField();

        JButton saveGoalButton = new JButton("Save Goal");

        panel.add(goalLabel);
        panel.add(goalBox);
        panel.add(targetLabel);
        panel.add(targetField);
        panel.add(deadlineLabel);
        panel.add(deadlineField);
        panel.add(new JLabel());
        panel.add(saveGoalButton);

        saveGoalButton.addActionListener(e -> {
            try {
                String goalType = (String) goalBox.getSelectedItem();
                float targetValue = Float.parseFloat(targetField.getText());
                String deadline = deadlineField.getText();

                if (saveUserGoal(goalType, targetValue, deadline)) {
                    loggedInGoalId = fetchGoalId(loggedInUserId);
                    System.out.println("Goal saved. GoalID: " + loggedInGoalId);
                    JOptionPane.showMessageDialog(this, "Goal saved successfully!");
                    cardLayout.show(mainPanel, "Dashboard");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to save goal!");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Invalid goal input.");
            }
        });

        return panel;
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel label = new JLabel("Welcome to your Dashboard!");
        JButton manageActivityButton = new JButton("Manage Activity");
        JButton manageNutritionButton = new JButton("Manage Nutrition");
        JButton viewProgressButton = new JButton("View Progress");

        panel.add(label);
        panel.add(manageActivityButton);
        panel.add(manageNutritionButton);
        panel.add(viewProgressButton);

        manageActivityButton.addActionListener(e -> manageActivity());
        manageNutritionButton.addActionListener(e -> manageNutrition());
        viewProgressButton.addActionListener(e -> viewProgress());

        return panel;
    }

    private boolean registerUser(String name, int age, String gender, float height, float weight, String email, String password) {
        String query = "INSERT INTO User (Name, Age, Gender, Height, Weight, Email, Password) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, name);
            stmt.setInt(2, age);
            stmt.setString(3, gender);
            stmt.setFloat(4, height);
            stmt.setFloat(5, weight);
            stmt.setString(6, email);
            stmt.setString(7, password);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private int loginUser(String email, String password) {
        String query = "SELECT UserID FROM User WHERE Email = ? AND Password = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("UserID");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private boolean saveUserGoal(String goalType, float targetValue, String deadline) {
        String query = "INSERT INTO Goal (UserID, GoalType, TargetValue, Deadline, Progress) VALUES (?, ?, ?, ?, 0)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, loggedInUserId);
            stmt.setString(2, goalType);
            stmt.setFloat(3, targetValue);
            stmt.setString(4, deadline);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private int getLastInsertedUserId() {
        String query = "SELECT UserID FROM User ORDER BY UserID DESC LIMIT 1";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt("UserID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private int fetchGoalId(int userId) {
        String query = "SELECT GoalID FROM Goal WHERE UserID = ? ORDER BY GoalID DESC LIMIT 1";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("GoalID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void manageActivity() {
        System.out.println("Attempting to open ActivityManager with UserID: " + loggedInUserId + ", GoalID: " + loggedInGoalId);
        if (loggedInUserId != -1 && loggedInGoalId != -1) {
            new ActivityManager(loggedInUserId, loggedInGoalId);
            this.setVisible(false);
        } else {
            JOptionPane.showMessageDialog(this, "Missing user or goal ID!");
        }
    }

    private void manageNutrition() {
        if (loggedInUserId != -1) {
            new ManageNutrition(loggedInUserId);
            this.setVisible(false);
        } else {
            JOptionPane.showMessageDialog(this, "Missing user ID!");
        }
    }

    private void viewProgress() {
    if (loggedInUserId != -1) {
        new ViewProgress(loggedInUserId); // Open the ViewProgress window for the logged-in user
        this.setVisible(false);  // Hide the current window
    } else {
        JOptionPane.showMessageDialog(this, "No user logged in. Please log in first.");
    }
}


    public static void main(String[] args) {
        SwingUtilities.invokeLater(DietPlanner::new);
    }
}
