import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

public class Admin_GUI extends JFrame {
    private JTextField idField;
    private JButton userDataButton;
    private JButton accessRecordsButton;
    private DefaultTableModel tableModel;
    private List<String[]> data;
    private String currentId;
    private JComboBox<String> filterComboBox; // Add a JComboBox for filtering
    private TableRowSorter<DefaultTableModel> tableSorter; // Add a TableRowSorter
    private JFrame tableFrame;
    private JTextField searchField;
    private JButton searchButton;
    // Define JTable and JScrollPane as instance variables
    private JTable userTable;
    private JScrollPane userScrollPane;

    // ADMIN LOGIN
    public Admin_GUI() {
        setTitle("Admin Control Panel");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 200);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 1));

        JLabel idLabel = new JLabel("Enter Admin ID:");
        idField = new JTextField(20);
        userDataButton = new JButton("User Data");
        accessRecordsButton = new JButton("Access Records");



        panel.add(idLabel);
        panel.add(idField);
        panel.add(userDataButton);
        panel.add(accessRecordsButton);
        add(panel, BorderLayout.CENTER);

        userDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentId = idField.getText().trim();
                if (validateAdminId(currentId)) {
                    // Check if the ID belongs to an admin
                    if (isAdmin(currentId)) {
                        // Open the user data viewer
                        displayUsersTable();
                    } else {
                        JOptionPane.showMessageDialog(null, "Invalid admin ID.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid ID format.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        accessRecordsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentId = idField.getText().trim();
                if (validateAdminId(currentId)) {
                    // Check if the ID belongs to an admin
                    if (isAdmin(currentId)) {
                        // Open the access records viewer
                        displayAccessRecordsTable();
                    } else {
                        JOptionPane.showMessageDialog(null, "Invalid admin ID.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid ID format.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    public boolean validateAdminId(String id) {
        return id.matches("\\d{9}"); // Assuming a valid ID is a 9-digit number
    }

    public boolean isAdmin(String id) {
        Connection connection = connectToDatabase();
        try {
            String query = "SELECT type FROM users WHERE ID = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String userType = resultSet.getString("type");
                return "admin".equals(userType);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // USER DATA GUI
    public void displayUsersTable() {
        if (currentId == null) {
            return;
        }
        data = fetchUsersData();
        setTitle("User Data Viewer");
        createUsrTableFrame("User Data", "Name", "ID", "Type", "Status");
    }

    public void createUsrTableFrame(String title, String... columnNames) {
        tableFrame = new JFrame();
        tableFrame.setTitle(title);
        
        tableFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        tableFrame.setSize(800, 600);
        tableFrame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel controlPanel = new JPanel();
        tableModel = new DefaultTableModel();
        userTable = new JTable(tableModel); // Create the JTable
        userScrollPane = new JScrollPane(userTable); // Create the JScrollPane

        controlPanel.add(userScrollPane);
        mainPanel.add(controlPanel, BorderLayout.CENTER);
        tableFrame.add(mainPanel);
        tableModel.setColumnIdentifiers(columnNames);


        // Create the filter dropdown menu
        String[] filterOptions = {"All", "Admin", "Student", "Staff"};
        filterComboBox = new JComboBox<>(filterOptions);
        mainPanel.add(filterComboBox, BorderLayout.NORTH);

        // Add an action listener to the filter dropdown
        filterComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyFilter(); // Call a method to apply the selected filter
            }
        });

        // Initialize the TableRowSorter
        tableSorter = new TableRowSorter<>(tableModel);
        tableSorter.setRowFilter(null); // Initially, no filter is applied

        // Apply the TableRowSorter to your JTable
        userTable.setRowSorter(tableSorter);

        // Add columns and data
        tableModel.setColumnIdentifiers(new String[]{"Name", "ID", "Type", "Status", "Toggle"});

        data = fetchUsersData();

        // Add a new button for each row (user)
        for (String[] row : data) {
            JButton toggleButton = new JButton("...");
            ButtonEditor buttonEditor = new ButtonEditor(toggleButton, this, userTable); // Pass 'this' as the second argument

            toggleButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Handle the button click here and toggle user status
                    int selectedRow = userTable.getSelectedRow(); // Get the selected row
                    if (selectedRow >= 0) {
                        String userId = (String) userTable.getValueAt(selectedRow, 1); // Assuming ID is in the second column
                        buttonEditor.toggleUserStatus(userId);
                        //displayUsersTable(); // Refresh the user data table
                    }
                }
            });
        // Create an array to hold the data for the current row, including the button
        String[] rowDataWithButton = new String[row.length + 1];
        System.arraycopy(row, 0, rowDataWithButton, 0, row.length);
        rowDataWithButton[rowDataWithButton.length - 1] = "";
        
        tableModel.addRow(rowDataWithButton);
        userTable.getColumnModel().getColumn(userTable.getColumnCount() - 1).setCellRenderer(new ButtonRenderer());
        userTable.getColumnModel().getColumn(userTable.getColumnCount() - 1).setCellEditor(new ButtonEditor(toggleButton, null,userTable));
        }
        
        // Set the visibility of the existing frame to true (if it exists)
        if (tableFrame.isVisible()) {
            tableFrame.setVisible(true);
        } else {
            tableFrame.setVisible(true);
        }
    }

    // ACCESS RECORDS GUI
    public void createAdmTableFrame(String title, String... columnNames) {
        JFrame tableFrame = new JFrame(title);
        tableFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        tableFrame.setSize(800, 600);
        tableFrame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel controlPanel = new JPanel();
        
        tableModel = new DefaultTableModel();
        JTable Admin_table = new JTable(tableModel);
        tableSorter = new TableRowSorter<>(tableModel); // Initialize tableSorter
        Admin_table.setRowSorter(tableSorter); // Set the TableRowSorter for the table

        JScrollPane scrollPane2 = new JScrollPane(Admin_table);

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search by User ID"));
        
        searchField = new JTextField();
        searchButton = new JButton("Search");
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        mainPanel.add(searchPanel, BorderLayout.NORTH);

        // Add an action listener to the search button
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //String searchUserId = searchField.getText();
                String searchText = searchField.getText().trim();
                if (!searchText.isEmpty()) {
                    // Call a method to filter records based on the user's input
                    filterAccessRecordsBySearchText(searchText);
                } else {
                    // If the search field is empty, show all records
                    tableSorter.setRowFilter(null);
                }
            }
        });

        searchPanel.setBorder(BorderFactory.createTitledBorder("Search by Text"));
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        // Add the search panel to the main panel
        mainPanel.add(searchPanel, BorderLayout.NORTH);

        controlPanel.add(scrollPane2);
        mainPanel.add(controlPanel, BorderLayout.CENTER);
        tableFrame.add(mainPanel);
        tableModel.setColumnIdentifiers(columnNames);

        data = fetchAccessData();
        for (String[] row : data) {
            tableModel.addRow(row);
        }

        // Set column widths based on the maximum content length in each column
        for (int i = 0; i < Admin_table.getColumnCount(); i++) {
            TableColumn column = Admin_table.getColumnModel().getColumn(i);
            int maxWidth = 0;
            for (int row = 0; row < Admin_table.getRowCount(); row++) {
                TableCellRenderer renderer = Admin_table.getCellRenderer(row, i);
                Component comp = Admin_table.prepareRenderer(renderer, row, i);
                int width = comp.getPreferredSize().width + Admin_table.getIntercellSpacing().width;
                if (width > maxWidth) {
                    maxWidth = width;
                }
            }
        column.setPreferredWidth(maxWidth);
        }
    tableFrame.setVisible(true);
    }

    public void displayAccessRecordsTable() {
        if (currentId == null) {
            return;
        }

        data = fetchAccessData();
        setTitle("Access Records Viewer");
        createAdmTableFrame("Access Records", "Name","ID", "Login_Timestamp", "Logout_Timestamp");
    
        // Define which columns should be editable (e.g., assuming all columns except the first one are editable)
        boolean[] editableColumns = new boolean[data.get(0).length];
        for (int i = 0; i < editableColumns.length; i++) {
            editableColumns[i] = (i != 0); // Set to true for editable columns, false for non-editable
        }
    }

    // Method to apply the selected filter
    public void applyFilter() {
        String selectedFilter = (String) filterComboBox.getSelectedItem();
        RowFilter<DefaultTableModel, Object> rowFilter = null;

        if (selectedFilter.equals("All")) {
            // No filter is applied, so set rowFilter to null
            rowFilter = null;
        } else {
            final int filterColumnIndex = 2; // Assuming Type column is at index 2
            String regex = ""; // Initialize the regex pattern
        
            // Construct the regex pattern based on the selected filter
            if (selectedFilter.equals("Student")) {
                regex = "student";
            } else if (selectedFilter.equals("Staff")) {
                regex = "staff";
            } else if (selectedFilter.equals("Admin")) {
                regex = "admin";
            }
            // Set rowFilter with the constructed regex pattern
            rowFilter = RowFilter.regexFilter(regex, filterColumnIndex);
        }

        // Set the filter on the TableRowSorter
        tableSorter.setRowFilter(rowFilter);
    }

    // Add a new method for filtering based on search text
    public void filterAccessRecordsBySearchText(String searchText) {
    if (searchText.isEmpty()) {
        // If the search text is empty, show all records
        tableSorter.setRowFilter(null);
    } else {
        // Create a RowFilter that matches the search text
        RowFilter<DefaultTableModel, Object> rowFilter = RowFilter.regexFilter("(?i)" + searchText); // Case-insensitive search

        // Set the filter on the TableRowSorter
        tableSorter.setRowFilter(rowFilter);
    }
}

    // DATA GRAB FUNTIONS
    public List<String[]> fetchUsersData() {
        List<String[]> userData = new ArrayList<>();
        Connection connection = null;

        try {
            connection = connectToDatabase();
            String query2 = "SELECT * FROM users";
            PreparedStatement statement = connection.prepareStatement(query2);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String[] rowData = new String[resultSet.getMetaData().getColumnCount()];
                for (int i = 0; i < rowData.length; i++) {
                    rowData[i] = resultSet.getString(i + 1);
                }
                userData.add(rowData);
            }
    
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
        e.printStackTrace();
        } finally {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        }
    return userData;
    }

    public List<String[]> fetchAccessData() {
        List<String[]> accessData = new ArrayList<>();
        Connection connection = null;
    
        try {
            connection = connectToDatabase();
            String query = "SELECT * FROM access";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
    
            while (resultSet.next()) {
                String[] rowData = new String[resultSet.getMetaData().getColumnCount()];
                for (int i = 0; i < rowData.length; i++) {
                    rowData[i] = resultSet.getString(i + 1);
                }
                accessData.add(rowData);
            }
    
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return accessData;
    }

    public String getUserIDForRow(int row) {
        if (row >= 0 && row < data.size()) {
            return data.get(row)[1]; // Assuming ID is in the second column
        }
        return null;
    }

    //DATA CONNECTION METHOD
    public Connection connectToDatabase() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/sunlab";
            String username = "Kennon"; //CHANGE THIS TO CONNCET TO YOUR MYSQL DATABASE
            String password = "Err0r$.KM"; //CHANGE THIS TO CONNCET TO YOUR MYSQL DATABASE
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e) { }
        return connection;
    }

    public void updateUsersTableView() {
        // Clear the existing data in the table model
        tableModel.setRowCount(0);
    
        // Fetch the updated user data from the database
        data = fetchUsersData();
    
        // Add the updated data to the table model
        for (String[] row : data) {
            JButton toggleButton = new JButton("...");
            ButtonEditor buttonEditor = new ButtonEditor(toggleButton, this, userTable);
    
            toggleButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int selectedRow = userTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        String userId = (String) userTable.getValueAt(selectedRow, 1);
                        buttonEditor.toggleUserStatus(userId);
                    }
                }
            });
    
            String[] rowDataWithButton = new String[row.length + 1];
            System.arraycopy(row, 0, rowDataWithButton, 0, row.length);
            rowDataWithButton[rowDataWithButton.length - 1] = "";
    
            tableModel.addRow(rowDataWithButton);
        }
        
        // Repaint the table to reflect the changes
        userTable.repaint();
    }

    // ACTIVATE/DEACTIVATE USER METHOD
    public void toggleUserStatus(String userId) {
        Connection connection = connectToDatabase();
        String UptQuery = "UPDATE users SET STATUS = CASE WHEN STATUS = 'Active' THEN 'Deactive' ELSE 'Active' END WHERE ID = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(UptQuery);
            preparedStatement.setString(1, userId);
            int rowsUpdated = preparedStatement.executeUpdate();
    
            if (rowsUpdated > 0) {
                System.out.println("User status updated successfully.");
            } else {
                System.out.println("User status update failed. User not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Admin_GUI().setVisible(true);
            }
        });
    }
}

class ButtonRenderer extends JButton implements TableCellRenderer {
    public ButtonRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        //setText((value == null) ? "" : value.toString());
    String status = table.getValueAt(row, table.getColumnCount() - 2).toString();
        
        if (status.equalsIgnoreCase("Active")) {
            setBackground(Color.GREEN);
        } else if (status.equalsIgnoreCase("Deactive")) {
            setBackground(Color.RED);
        } else {
            setBackground(null); // Default background color
        }
        setText((value == null) ? "" : value.toString());
        return this;
    
    }
}

class ButtonEditor extends DefaultCellEditor {
    private JButton button;
    private String userId; // Store the user ID for the clicked button
    private Admin_GUI admin_GUI;

    public ButtonEditor(JButton button, Admin_GUI admin_GUI, JTable table) {
        super(new JCheckBox());
        this.button = button;
        this.button.setFocusPainted(false);
        this.admin_GUI = admin_GUI; // Set the admin_GUI instance variable

        this.button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Handle the button click here and toggle user status
                if (admin_GUI != null) {
                     // Retrieve the user ID from the table model
                    String selectedUserId = admin_GUI.getUserIDForRow(table.getSelectedRow());
                    if (selectedUserId != null) {
                            admin_GUI.toggleUserStatus(selectedUserId);
                            admin_GUI.updateUsersTableView(); // Update the view after toggling
                    }
                }
            }
        });
    }

    public void toggleUserStatus(String string) {
        if (admin_GUI != null) {
            admin_GUI.toggleUserStatus(userId);
        }
    }

    // Method to set the user ID
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        userId = table.getValueAt(row, 0).toString(); // Get the user ID from the table
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        return button.getText();
    }
}

class CustomTableModel extends DefaultTableModel {
    private final boolean[] editableColumns;

    public CustomTableModel(Object[][] data, Object[] columnNames, boolean[] editableColumns) {
        super(data, columnNames);
        this.editableColumns = editableColumns;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        // Allow editing only if the corresponding editableColumns entry is true
        return editableColumns[column];
    }
}





