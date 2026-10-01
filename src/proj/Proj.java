package proj;

import javax.swing.*;
import javax.swing.undo.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import javax.swing.text.*;

public class Proj extends JFrame implements ActionListener {
    JTextArea textArea;
    JMenuBar menuBar;
    JMenu fileMenu, editMenu, formatMenu;
    JMenuItem openItem, saveItem, closeItem, printItem, saveToDatabaseItem, loadFromDatabaseItem;
    JMenuItem cutItem, copyItem, pasteItem, undoItem, redoItem, findReplaceItem, fontItem, aboutItem;
    UndoManager undoManager;
    Connection conn;

    public Proj() {
        // Initialize UndoManager
        undoManager = new UndoManager();
        textArea = new JTextArea();
        textArea.getDocument().addUndoableEditListener(undoManager);
        add(new JScrollPane(textArea));

        // Initialize database connection
        initDatabaseConnection();

        // Create a menu bar
        menuBar = new JMenuBar();

        // Create file menu and its menu items
        fileMenu = new JMenu("File");
        openItem = new JMenuItem("Open");
        saveItem = new JMenuItem("Save");
        closeItem = new JMenuItem("Close");
        printItem = new JMenuItem("Print");
        saveToDatabaseItem = new JMenuItem("Save to Database");
        loadFromDatabaseItem = new JMenuItem("Load from Database");

        openItem.addActionListener(this);
        saveItem.addActionListener(this);
        closeItem.addActionListener(this);
        printItem.addActionListener(this);
        saveToDatabaseItem.addActionListener(this);
        loadFromDatabaseItem.addActionListener(this);

        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(closeItem);
        fileMenu.add(printItem);
        fileMenu.add(saveToDatabaseItem);
        fileMenu.add(loadFromDatabaseItem);

        // Create edit menu and its menu items
        editMenu = new JMenu("Edit");
        cutItem = new JMenuItem("Cut");
        copyItem = new JMenuItem("Copy");
        pasteItem = new JMenuItem("Paste");
        undoItem = new JMenuItem("Undo");
        redoItem = new JMenuItem("Redo");
        findReplaceItem = new JMenuItem("Find/Replace");

        cutItem.addActionListener(this);
        copyItem.addActionListener(this);
        pasteItem.addActionListener(this);
        undoItem.addActionListener(this);
        redoItem.addActionListener(this);
        findReplaceItem.addActionListener(this);

        editMenu.add(cutItem);
        editMenu.add(copyItem);
        editMenu.add(pasteItem);
        editMenu.add(undoItem);
        editMenu.add(redoItem);
        editMenu.add(findReplaceItem);

        // Create format menu and its menu items
        formatMenu = new JMenu("Format");
        fontItem = new JMenuItem("Font");

        fontItem.addActionListener(this);

        formatMenu.add(fontItem);

        // Create help menu and its menu items
        JMenu helpMenu = new JMenu("Help");
        aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(this);
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(formatMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        // Set frame properties
        setTitle("Text Editor");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    // Database connection setup
    private void initDatabaseConnection() {
        try {
            String url = "jdbc:mysql://localhost:3306/text_editor_db"; // Database URL
            String username = "root"; // DB username
            String password = ""; // DB password (adjust as needed)
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("Database connected.");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to the database: " + e.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getSource() == openItem) {
                openFile();
            } else if (e.getSource() == saveItem) {
                saveFile();
            } else if (e.getSource() == closeItem) {
                closeFile();
            } else if (e.getSource() == printItem) {
                printFile();
            } else if (e.getSource() == saveToDatabaseItem) {
                saveToDatabase();
            } else if (e.getSource() == loadFromDatabaseItem) {
                loadFromDatabase();
            } else if (e.getSource() == cutItem) {
                textArea.cut();
            } else if (e.getSource() == copyItem) {
                textArea.copy();
            } else if (e.getSource() == pasteItem) {
                textArea.paste();
            } else if (e.getSource() == undoItem) {
                if (undoManager.canUndo()) {
                    undoManager.undo();
                }
            } else if (e.getSource() == redoItem) {
                if (undoManager.canRedo()) {
                    undoManager.redo();
                }
            } else if (e.getSource() == findReplaceItem) {
                new FindReplaceDialog(this, textArea).setVisible(true);
            } else if (e.getSource() == fontItem) {
                
            } else if (e.getSource() == aboutItem) {
                new AboutDialog(this).setVisible(true);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage());
        }
    }

    // Save document to database
    private void saveToDatabase() {
        try {
            String content = textArea.getText();
            String sql = "INSERT INTO documents (content) VALUES (?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, content);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Document saved to database.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving to database: " + ex.getMessage());
        }
    }

    // Load document from database
    private void loadFromDatabase() {
        try {
            String sql = "SELECT content FROM documents ORDER BY id DESC LIMIT 1"; // Fetch latest entry
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String content = rs.getString("content");
                textArea.setText(content);
                JOptionPane.showMessageDialog(this, "Document loaded from database.");
            } else {
                JOptionPane.showMessageDialog(this, "No documents found in the database.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading from database: " + ex.getMessage());
        }
    }

   

    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                textArea.read(br, null);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error opening file: " + ex.getMessage());
            }
        }
    }

    private void saveFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                textArea.write(bw);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage());
            }
        }
    }

    private void closeFile() {
        textArea.setText("");
    }

    private void printFile() {
        try {
            textArea.print();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error printing file: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Proj::new);
    }
}

// Find and Replace Dialog
class FindReplaceDialog extends JDialog {
    private JTextField findField;
    private JTextField replaceField;
    private JButton findButton;
    private JButton replaceButton;
    private JTextArea textArea;

    public FindReplaceDialog(JFrame parent, JTextArea textArea) {
        super(parent, "Find and Replace", true);
        this.textArea = textArea;
        
        setLayout(new GridLayout(3, 2, 5, 5));

        add(new JLabel("Find:"));
        findField = new JTextField();
        add(findField);

        add(new JLabel("Replace with:"));
        replaceField = new JTextField();
        add(replaceField);

        findButton = new JButton("Find");
        replaceButton = new         JButton("Replace");

        findButton.addActionListener(e -> findText());
        replaceButton.addActionListener(e -> replaceText());

        add(findButton);
        add(replaceButton);

        setSize(300, 150);
        setLocationRelativeTo(parent);
    }

    private void findText() {
        String findText = findField.getText();
        String content = textArea.getText();
        int startIndex = content.indexOf(findText);
        if (startIndex != -1) {
            textArea.setCaretPosition(startIndex);
            textArea.setSelectionStart(startIndex);
            textArea.setSelectionEnd(startIndex + findText.length());
        } else {
            JOptionPane.showMessageDialog(this, "Text not found.");
        }
    }

    private void replaceText() {
        String findText = findField.getText();
        String replaceText = replaceField.getText();
        String content = textArea.getText();
        content = content.replaceFirst(findText, replaceText);
        textArea.setText(content);
    }
}

// About Dialog
class AboutDialog extends JDialog {
    public AboutDialog(JFrame parent) {
        super(parent, "About Text Editor", true);
        setLayout(new BorderLayout());
        JLabel aboutLabel = new JLabel("<html><h2>Text Editor</h2><p>Version 1.0<br>Created by User</p></html>");
        add(aboutLabel, BorderLayout.CENTER);
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> dispose());
        add(okButton, BorderLayout.SOUTH);
        setSize(300, 150);
        setLocationRelativeTo(parent);
    }
}

