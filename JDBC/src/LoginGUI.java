import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class LoginGUI extends JFrame {
    JFrame frame;
    JLabel usernameLabel, passwordLabel;
    JTextField usernameField;
    JPasswordField passwordField;
    JButton submitButton;
    //
    // GUIS
    public LoginGUI() {
        setTitle("Login Screen");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);

        // Create a JPanel to hold the components
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Create a title label
        JLabel titleLabel = new JLabel("Penn State SunLab", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

        // Create buttons
        JButton swipeButton = new JButton("Swipe");
        JButton adminLoginButton = new JButton("Admin Login");
        JButton exitButton = new JButton("Exit");
        // Set background colors
        swipeButton.setBackground(new Color(173, 216, 230));
        adminLoginButton.setBackground(new Color(173, 255, 47));
        exitButton.setBackground(new Color(255, 100, 100));
        
        Dimension buttonSize = new Dimension(120, 40);
        swipeButton.setPreferredSize(buttonSize);
        adminLoginButton.setPreferredSize(buttonSize);
        exitButton.setPreferredSize(buttonSize);

        // Add action listeners to the buttons
        swipeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Open the InOutGUI here
                openInOutGUI();
            }
        });

        adminLoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Open the Admin_GUI here
                Admin_GUI adminGUI = new Admin_GUI();
                adminGUI.setVisible(true);
            }
        });

        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Exit the program
                System.exit(0);
            }
        });

        // Create a panel for the buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3,1));
        buttonPanel.add(swipeButton);
        buttonPanel.add(adminLoginButton);
        buttonPanel.add(exitButton);

        // Add components to the main panel
        panel.add(titleLabel, BorderLayout.NORTH);

        panel.add(buttonPanel, BorderLayout.CENTER);

        // Add the main panel to the frame
        add(panel);
    }

    public void OpenAdminLogIn(){
        new Admin_GUI().setVisible(true);
    }

    private void openInOutGUI() {
            JFrame inoutFrame = new JFrame("In/Out Options");
            inoutFrame.setSize(800, 150);
            inoutFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Close only this frame
            setLocationRelativeTo(null);

            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(3, 1));
        
            JLabel promptLabel = new JLabel("Enter ID Number:");
            JTextField idField = new JTextField(20);
            JRadioButton enterRadioButton = new JRadioButton("Entering");
            JRadioButton exitRadioButton = new JRadioButton("Exiting");
            JButton submitButton = new JButton("Submit");
            JLabel messageLabel = new JLabel();
            panel.add(promptLabel);
            panel.add(idField);
            panel.add(enterRadioButton);
            panel.add(exitRadioButton);
            panel.add(submitButton);
            panel.add(messageLabel);
            inoutFrame.add(panel);
            inoutFrame.setVisible(true);

            ButtonGroup RadioButt=new ButtonGroup();
            RadioButt.add(enterRadioButton);
            RadioButt.add(exitRadioButton);

            // to update the database access records
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm");
            String timestamp = dateFormat.format(new java.util.Date());
            
            //Code the submit button
            submitButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String id = idField.getText();
                    if (validateID(id)) {
                        if ("Deactive".equals(getStatus(id))) {
                            messageLabel.setText("Card input invalid");
                        } else {
                            boolean entering = enterRadioButton.isSelected();
                            boolean exiting = exitRadioButton.isSelected();
                            switch (getAction(entering, exiting)) {
                                case "enter":
                                    messageLabel.setText("Entered successfully");
                                    // Log access time for entering here
                                    updateTimestampInDatabase(id, timestamp);
                                    break;
                                case "exit":
                                    messageLabel.setText("Exited successfully");
                                    // Log access time for exiting here
                                    updateTimestampOutDatabase(id, timestamp);
                                    break;
                                case "none":
                                    messageLabel.setText("Select 'Entering' or 'Exiting.'");
                                    break;
                                default:
                                    messageLabel.setText("Invalid action");
                                    break;
                            }
                        }
                    } else {
                        messageLabel.setText("Invalid ID");
                    }
            }
        });
    }
    //
    // DATABASE CONNECTION
    public Connection connectToDatabase() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/sunlab";
            String username = "Kennon";
            String password = "Err0r$.KM";
            
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e) { }
        return connection;
    }
    //
    // GET METHODS/ VALIDATORS
    public String getUserType(String id) {
        Connection connection = connectToDatabase();;
        try {
            connection = connectToDatabase();
            String query = "SELECT type FROM users WHERE ID = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getString("type");
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // User not found
    }

    private String getAction(boolean entering, boolean exiting) {
        if (entering && !exiting) {return "enter";}
        else if (!entering && exiting) {return "exit";}
        else {return "none";}
    }

    private String getStatus(String id) {
            Connection connection = null;
            PreparedStatement preparedStatement = null;
            ResultSet resultSet = null;
            String status = "Unknown"; // Default status if not found or error occurs

            try {
                // Establish a database connection
                connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sunlab", "Kennon", "Err0r$.KM");
                // Prepare and execute the SQL query
                String query = "SELECT STATUS FROM users WHERE ID = ?";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, id);
                resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    // Retrieve the status from the result set
                    status = resultSet.getString("STATUS");
                    System.out.println(status);
                }
            } catch (SQLException e) {e.printStackTrace();}
            finally {
                try {
                    // Close resources
                    if (resultSet != null) resultSet.close();
                    if (preparedStatement != null) preparedStatement.close();
                    if (connection != null) connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return status;
        }
    
    public String getUserNameById(String userId) {
            Connection connection = null;
            PreparedStatement preparedStatement = null;
            ResultSet resultSet = null;
            String userName = null;
        
            try {
                connection = connectToDatabase();
        
                // Define your SQL query to get the user's name
                String query = "SELECT name FROM users WHERE ID = ?";
        
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, userId);
                resultSet = preparedStatement.executeQuery();
        
                if (resultSet.next()) {
                    userName = resultSet.getString("name");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    // Close resources
                    if (resultSet != null) resultSet.close();
                    if (preparedStatement != null) preparedStatement.close();
                    if (connection != null) connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return userName;
        }
    
        private boolean validateID(String id) {
        return id.matches("\\d{9}"); // Assuming a valid ID is a 9-digit number
    }

    // UPDATE FUNCTIONS
        public void updateTimestampInDatabase(String id, String timestamp) {
            Connection connection = null;
            PreparedStatement preparedStatement = null;

            try {
                connection = connectToDatabase();

                // Get the user's ID info via users table
                String userName = getUserNameById(id);

                // Enter user info into access records
                String insertSQL = "INSERT INTO access (name, ID, Timestamp) VALUES (?, ?, ?)";
                //String query = "UPDATE access SET timestamp = ? WHERE ID = ?";
                
                preparedStatement = connection.prepareStatement(insertSQL);
                //preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, userName);
                preparedStatement.setString(2, id);
                preparedStatement.setString(3, timestamp);
                
                int rowsUpdated = preparedStatement.executeUpdate();
                
                if (rowsUpdated > 0) {
                    System.out.println("Data updated successfully.");
                } else {
                    System.out.println("Data update failed. User not found.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (preparedStatement != null) {
                        preparedStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }

        public void updateTimestampOutDatabase(String userId, String timestamp) {
            Connection connection = null;
            PreparedStatement preparedStatement = null;
        
            try {
                connection = connectToDatabase();
                String query = "UPDATE access SET in_out = ? WHERE ID = ?";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, timestamp);
                preparedStatement.setString(2, userId);
                
                int rowsUpdated = preparedStatement.executeUpdate();
                
                if (rowsUpdated > 0) {
                    System.out.println("Timestamp updated successfully.");
                } else {
                    System.out.println("Timestamp update failed. User not found.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (preparedStatement != null) {
                        preparedStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    //
    // MAIN
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginGUI().setVisible(true);
            }
        });
    }
}