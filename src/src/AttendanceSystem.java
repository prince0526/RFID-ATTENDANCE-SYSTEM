package src.src;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.io.File;
import javax.imageio.ImageIO;

public class AttendanceSystem extends JFrame {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/attendance_system";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "titamaver";

    private JTextField empNumberField;
    private JTable attendanceTable;
    private DefaultTableModel tableModel;
    private JLabel photoLabel;
    private JLabel messageLabel;

    private Connection conn;
    private boolean isLoggedIn = false; // Admin login flag

    public AttendanceSystem() {
        setTitle("Attendance Monitoring System");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLookAndFeel();
        setUpUI();

        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database connection failed!", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void setUpUI() {
        JTabbedPane tabbedPane = new JTabbedPane();

        // Employee Tab (Time In and Out)
        JPanel employeePanel = new JPanel();
        employeePanel.setBackground(new Color(243, 243, 243)); // Light background

        // Employee RFID Section
        JPanel rfidPanel = new JPanel();
        rfidPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        rfidPanel.setBackground(new Color(243, 243, 243));
        rfidPanel.setLayout(new BoxLayout(rfidPanel, BoxLayout.Y_AXIS));

        JLabel rfidLabel = new JLabel("Scan RFID Tag to Time In/Out");
        rfidLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        rfidLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        rfidPanel.add(rfidLabel);

        empNumberField = new JTextField();
        empNumberField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        empNumberField.setPreferredSize(new Dimension(300, 40));
        empNumberField.setAlignmentX(Component.CENTER_ALIGNMENT);
        empNumberField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        rfidPanel.add(empNumberField);

        photoLabel = new JLabel();
        photoLabel.setHorizontalAlignment(JLabel.CENTER);
        photoLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        rfidPanel.add(photoLabel);

        messageLabel = new JLabel("", JLabel.CENTER);
        messageLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        messageLabel.setForeground(new Color(34, 34, 34));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        rfidPanel.add(messageLabel);

        employeePanel.add(rfidPanel);

        tabbedPane.addTab("Employee", employeePanel);

        // Admin Tab
        JPanel adminPanel = new JPanel();
        adminPanel.setBackground(new Color(243, 243, 243));

        // Admin Login Section
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));
        loginPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        loginPanel.setBackground(new Color(243, 243, 243));

        JTextField adminUsernameField = new JTextField();
        JPasswordField adminPasswordField = new JPasswordField();
        JButton adminLoginButton = new JButton("Login as Admin");

        adminLoginButton.setBackground(null); // Remove background color
        adminLoginButton.setForeground(Color.BLACK); // Set text color to black
        adminLoginButton.setFocusPainted(false);
        adminLoginButton.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Remove bold font if needed
        adminLoginButton.setPreferredSize(new Dimension(200, 40));
        adminLoginButton.setBorder(BorderFactory.createLineBorder(Color.GRAY)); // Set a neutral border color

        loginPanel.add(new JLabel("Username:"));
        loginPanel.add(adminUsernameField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(adminPasswordField);
        loginPanel.add(adminLoginButton);

        adminPanel.add(loginPanel, BorderLayout.NORTH);

        // Admin Controls Section
        JPanel adminControlsPanel = new JPanel(new FlowLayout());
        adminControlsPanel.setBackground(new Color(243, 243, 243));

        JButton viewAttendanceButton = new JButton("View Attendance");
        JButton eraseRecordsButton = new JButton("Erase Records");

        // Remove background color and set text color to black for both buttons
        viewAttendanceButton.setBackground(null);
        viewAttendanceButton.setForeground(Color.BLACK);
        viewAttendanceButton.setFocusPainted(false);
        viewAttendanceButton.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Remove bold font if needed
        viewAttendanceButton.setPreferredSize(new Dimension(200, 40));
        viewAttendanceButton.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        eraseRecordsButton.setBackground(null);
        eraseRecordsButton.setForeground(Color.BLACK);
        eraseRecordsButton.setFocusPainted(false);
        eraseRecordsButton.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Remove bold font if needed
        eraseRecordsButton.setPreferredSize(new Dimension(200, 40));
        eraseRecordsButton.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        adminControlsPanel.add(viewAttendanceButton);
        adminControlsPanel.add(eraseRecordsButton);
        adminPanel.add(adminControlsPanel, BorderLayout.CENTER);

        // Attendance Table Section
        tableModel = new DefaultTableModel(new String[]{"Employee Name", "Date", "Time In", "Time Out"}, 0);
        attendanceTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(attendanceTable);
        adminPanel.add(tableScrollPane, BorderLayout.SOUTH);

        tabbedPane.addTab("Admin", adminPanel);

        getContentPane().add(tabbedPane);

        // Handle Admin Login
        adminLoginButton.addActionListener(e -> adminLoginAction(adminUsernameField, adminPasswordField, tableScrollPane));

        // Handle View Attendance Button
        viewAttendanceButton.addActionListener(e -> viewAttendanceAction());

        // Handle Erase Records Button
        eraseRecordsButton.addActionListener(e -> eraseRecordsAction());

        // RFID Input Handling
        empNumberField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    processRFIDInput(empNumberField.getText());
                    empNumberField.setText("");
                }
            }
        });
    }

    private void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

   private void processRFIDInput(String rfidTag) {
    try {
        String query = "SELECT * FROM employees WHERE rfid_tag = ?";
        PreparedStatement pst = conn.prepareStatement(query);
        pst.setString(1, rfidTag);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            String empNumber = rs.getString("employee_number");
            String name = rs.getString("name");
            String photoPath = rs.getString("photo_path");
            String date = new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
            String timeNow = new SimpleDateFormat("HH:mm:ss").format(new java.util.Date());

            // Load and center the employee image
            ImageIcon photoIcon = loadImage(photoPath);
            photoLabel.setIcon(photoIcon);
            photoLabel.setHorizontalAlignment(JLabel.CENTER);  // Centering the photo
            photoLabel.setVerticalAlignment(JLabel.CENTER);  // Ensures vertical centering

            // Center the message text
            messageLabel.setHorizontalAlignment(JLabel.CENTER);
            messageLabel.setVerticalAlignment(JLabel.CENTER);

            String attendanceQuery = "SELECT * FROM attendance WHERE employee_number = ? AND date = ?";
            PreparedStatement attStmt = conn.prepareStatement(attendanceQuery);
            attStmt.setString(1, empNumber);
            attStmt.setString(2, date);
            ResultSet attRs = attStmt.executeQuery();

            if (attRs.next()) {
                String timeIn = attRs.getString("time_in");
                String timeOut = attRs.getString("time_out");

                if (timeIn != null && timeOut == null) {
                    String updateQuery = "UPDATE attendance SET time_out = ? WHERE employee_number = ? AND date = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                    updateStmt.setString(1, timeNow);
                    updateStmt.setString(2, empNumber);
                    updateStmt.setString(3, date);
                    updateStmt.executeUpdate();

                    // Display a message for time out
                    messageLabel.setText("<html><b>Goodbye " + name + ", Have a Nice Day!</b><br>" +
                            "You timed out on " + date + " at " + timeNow + "</html>");
                }
            } else {
                String insertQuery = "INSERT INTO attendance (employee_number, date, time_in) VALUES (?, ?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setString(1, empNumber);
                insertStmt.setString(2, date);
                insertStmt.setString(3, timeNow);
                insertStmt.executeUpdate();

                // Display a message for time in
                messageLabel.setText("<html><b>Welcome " + name + ", Great to Have You Back!</b><br>" +
                        "You timed in on " + date + " at " + timeNow + "</html>");
            }
        } else {
            messageLabel.setText("Invalid RFID Tag. Please try again.");
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
}

    

    private ImageIcon loadImage(String photoPath) {
        try {
            Image img = ImageIO.read(new File(photoPath));
            ImageIcon photoIcon = new ImageIcon(img.getScaledInstance(150, 150, Image.SCALE_SMOOTH));
            return photoIcon;
        } catch (Exception e) {
            return new ImageIcon(); // return an empty icon if image not found
        }
    }

    private void adminLoginAction(JTextField adminUsernameField, JPasswordField adminPasswordField, JScrollPane tableScrollPane) {
        String username = adminUsernameField.getText();
        String password = new String(adminPasswordField.getPassword());

        if (username.equals("admin") && password.equals("password")) {
            // Admin login success
            JOptionPane.showMessageDialog(this, "Admin Login Successful!", "Login", JOptionPane.INFORMATION_MESSAGE);
            isLoggedIn = true; // Set login flag
            loadAttendanceData();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid Admin Credentials", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAttendanceData() {
        if (!isLoggedIn) return; // Prevent loading data if not logged in

        try {
            String query = "SELECT attendance.employee_number, employees.name, attendance.date, attendance.time_in, attendance.time_out " +
                           "FROM attendance " +
                           "JOIN employees ON attendance.employee_number = employees.employee_number";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Clear previous table data
            tableModel.setRowCount(0);

            // Add new data to the table
            while (rs.next()) {
                String empName = rs.getString("name");
                String date = rs.getString("date");
                String timeIn = rs.getString("time_in");
                String timeOut = rs.getString("time_out");
                tableModel.addRow(new Object[]{empName, date, timeIn, timeOut});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void viewAttendanceAction() {
        if (!isLoggedIn) {
            JOptionPane.showMessageDialog(this, "Please log in as admin first!", "Login Required", JOptionPane.WARNING_MESSAGE);
        } else {
            loadAttendanceData();
        }
    }

    private void eraseRecordsAction() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to erase all attendance records?", 
                "Confirm Erase", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String deleteQuery = "DELETE FROM attendance";
                Statement stmt = conn.createStatement();
                stmt.executeUpdate(deleteQuery);
                JOptionPane.showMessageDialog(this, "All records have been erased.", "Records Erased", JOptionPane.INFORMATION_MESSAGE);
                tableModel.setRowCount(0); // Clear the table view
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AttendanceSystem().setVisible(true));
    }
}
