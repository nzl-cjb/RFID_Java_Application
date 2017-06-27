/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import ClassModel.DBConnection;
import com.fazecast.jSerialComm.SerialPort;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author Callum
 */
public class StudentForm extends javax.swing.JPanel {

    private Connection connection;
    private Statement statement;
    private int parentIndex = 0;
    private JFrame frame;
    private boolean editing;
    private String editRFID;
    private int maxFirstName;
    private int maxLastName;

    /**
     * The default constructor will be called when a staff member enrolls a new
     * student in the school
     *
     * @param frame the frame storing the panel
     */
    public StudentForm(JFrame frame) {
        this.editing = false;
        initialiseConstructor(frame);
    }

    /**
     * This constructor will be called when a staff member wants to update the
     * RFID key of a student. Presumably, the student has lost their existing
     * card and requires a new one.
     *
     * @param frame the frame storing the panel
     * @param studentID the studentID of the student we will be editing
     */
    public StudentForm(JFrame frame, int studentID) {
        this.editing = true;
        initialiseConstructor(frame);

        lblTitle.setText("Edit Student");
        txtFirstName.setEditable(false);
        txtLastName.setEditable(false);

        populateForm(studentID);
    }

    /**
     * Populates the text fields with the information related to the studentID
     *
     * @param studentID the studentID of the student we will be editing
     */
    public void populateForm(int studentID) {
        try {
            ResultSet studentSet = statement.executeQuery("SELECT * FROM student WHERE studentID = " + studentID);
            studentSet.next();
            String firstName = studentSet.getString("firstName");
            String lastName = studentSet.getString("lastName");
            int studentNumber = studentSet.getInt("studentNumber");
            int parentID = studentSet.getInt("parentID");

            ResultSet rfidSet = statement.executeQuery("SELECT rfidKey, studentID FROM rfidtags WHERE studentID = " + studentID);
            rfidSet.next();
            String rfidKey = rfidSet.getString("rfidKey");

            ResultSet parentSet = statement.executeQuery("SELECT username FROM parent WHERE parentID = " + parentID);
            parentSet.next();
            String username = parentSet.getString("username");

            txtFirstName.setText(firstName);
            txtLastName.setText(lastName);
            txtStudentNumber.setText("" + studentNumber);
            txtRFIDKey.setText(rfidKey);
            editRFID = rfidKey;

            /**
             * Sets the index of the parent of the student. If a staff member
             * tries to change the parent of the student, it will automatically
             * change back to the index generated in this for loop.
             */
            for (int i = 0; i < getParents().length; i++) {
                if (getParents()[i].equals(username)) {
                    parentIndex = i;
                    comboParent.setSelectedIndex(parentIndex);
                    break;
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(StudentForm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method was created to reduce duplicate code for the two different
     * constructors. Initial values for the class are set within this method.
     *
     * @param frame the frame storing the panel
     */
    public void initialiseConstructor(JFrame frame) {
        this.frame = frame;
        DBConnection dbConnection = new DBConnection();
        connection = dbConnection.getConnection();
        initComponents();
        comboParent.setModel(new javax.swing.DefaultComboBoxModel<>(getParents()));
        comboParent.setSelectedItem(null);
        txtRFIDKey.setEditable(false);
        txtFirstName.setTransferHandler(null);
        txtLastName.setTransferHandler(null);
        txtStudentNumber.setTransferHandler(null);

        /*
        Gets the column size for specified columns. The reason for this is so that any updates made in the
        database will not have to be manually updated. As part of minimising user error, they are limited to 
        entering strings with length equal to one less than the column size.
         */
        try {
            DatabaseMetaData meta = connection.getMetaData();
            ResultSet columns = meta.getColumns(null, null, "student", null);

            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                if (columnName.equals("firstName")) {
                    maxFirstName = columns.getInt("COLUMN_SIZE") - 1;
                } else if (columnName.equals("lastName")) {
                    maxLastName = columns.getInt("COLUMN_SIZE") - 1;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(StudentForm.class.getName()).log(Level.SEVERE, null, ex);
        }

        /*
        Helps minimise user input error by controlling the characters they can enter, as well as the length 
        of the string.
         */
        txtFirstName.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                String key = String.valueOf(e.getKeyChar());
                Pattern pattern = Pattern.compile("[a-zA-Z]");
                Matcher matcher = pattern.matcher(key);
                if (txtFirstName.getText().length() >= maxFirstName) {
                    e.consume();
                } else if (!matcher.find()) {
                    e.consume();
                }
            }
        });

        /*
        Helps minimise user input error by controlling the characters they can enter, as well as the length 
        of the string.
         */
        txtLastName.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                String key = String.valueOf(e.getKeyChar());
                Pattern pattern = Pattern.compile("[a-zA-Z]");
                Matcher matcher = pattern.matcher(key);
                if (txtLastName.getText().length() >= maxLastName) {
                    e.consume();
                } else if (!matcher.find()) {
                    e.consume();
                }
            }
        });

        /*
        If a new user is being added, the highest student is retrieved. 1 is added to it, and this will be the 
        student number of the new user. This ensures no duplicate student numbers are assigned.
         */
        if (editing == false) {
            txtStudentNumber.setText("" + getHighestStudentnumber());
            txtStudentNumber.setEditable(false);
            txtRFIDKey.setText(null);
        }
    }

    /**
     * Clears the fields in the student form
     */
    public void clearForm() {
        lblFirstName.setText("First Name: ");
        lblLastName.setText("Last Name: ");
        lblStudentNumber.setText("Student Number: ");
        lblParent.setText("Parent: ");
        lblRFID.setText("RFID: ");
        txtFirstName.setText(null);
        txtLastName.setText(null);
        txtStudentNumber.setText("" + getHighestStudentnumber());
        comboParent.setSelectedItem(null);
        txtRFIDKey.setText(null);
    }

    /**
     * This method gets the next available student number. If no entries exist,
     * then 100000000 is returned as a default value.
     *
     * @return largest student number + 1
     */
    public int getHighestStudentnumber() {
        try {
            statement = connection.createStatement();
            ResultSet rowCountSet = statement.executeQuery("SELECT studentNumber FROM student ORDER BY studentNumber DESC LIMIT 1");
            rowCountSet.next();
            return rowCountSet.getInt("studentNumber") + 1;

        } catch (SQLException ex) {
            Logger.getLogger(StudentForm.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 100000000;
    }

    /**
     * This method retrieves data from the RFID reader dedicated to adding/
     * editing students, rather than other readers that track whether a student
     * is in the school or not. The method opens a serial port (specific to the
     * reader), and reads the key associated to any cards that are scanned.
     */
    public void searchForRFID() {
        /**
         * Scans through available ports until it finds "COM4". This is the name
         * of the Arduino dedicated to adding / editing students. This will be
         */
        SerialPort[] ports = SerialPort.getCommPorts();
        SerialPort chosenPort = null;
        for (int i = 0; i < ports.length; i++) {
            System.out.println(ports[i].getSystemPortName());
            if (ports[i].getSystemPortName().equals("COM4")) {
                chosenPort = ports[i];
            }
        }

        /**
         * The RFID label is updated with green text notifying the user that the
         * port is open and they may attempt to scan the RFID.
         */
        if (chosenPort.openPort()) {
            lblRFID.setText("<html>RFID: <font color='green'>Open</font></html>");
            lblRFID.paintImmediately(lblRFID.getVisibleRect());
        } else {
            return;
        }

        /**
         * Continuously scans the reader until either 3 seconds has passed or
         * the reader has detected a card. If a card is successfully read, the
         * RFID text field will be updated with the key stored on the card.
         */
        String before = txtRFIDKey.getText();
        long startTime = System.currentTimeMillis();
        while (true && (System.currentTimeMillis() - startTime) < 3000) {
            chosenPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);

            Scanner data = new Scanner(chosenPort.getInputStream());

            /**
             * Attempts to read input from the reader. A check to see if the
             * length of the key is 11 characters long however, as the reader
             * sometimes didn't read the full key.
             */
            try {
                String key = data.nextLine();
                if (key.length() == 11) {
                    txtRFIDKey.setText(key);
                    break;
                }
            } catch (Exception e) {
            }
        }
        chosenPort.closePort();
        lblRFID.setText("RFID: ");
//        if (txtRFIDKey.getText().equals(before)) {
//            txtRFIDKey.setText(null);
//        }
    }

    /**
     * This method is used for populating the parent combo box.
     *
     * @return a string array of all parents in the database
     */
    public String[] getParents() {
        try {
            ArrayList<String> parentList = new ArrayList<String>();
            statement = connection.createStatement();
            ResultSet rowCountSet = statement.executeQuery("SELECT COUNT(*) AS count FROM parent");
            rowCountSet.next();
            int count = rowCountSet.getInt("count");

            String[] parentArray = new String[count];
            int i = 0;
            ResultSet parentSet = statement.executeQuery("SELECT * FROM parent");

            while (parentSet.next()) {
                parentArray[i] = parentSet.getString("username");
                i++;
            }
            return parentArray;
        } catch (SQLException ex) {
            Logger.getLogger(StudentForm.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblFirstName = new javax.swing.JLabel();
        lblLastName = new javax.swing.JLabel();
        lblParent = new javax.swing.JLabel();
        comboParent = new javax.swing.JComboBox<>();
        if (this.editing == true) {
            comboParent.setEditable(false);
        }
        lblStudentNumber = new javax.swing.JLabel();
        lblTitle = new javax.swing.JLabel();
        btnSubmit = new javax.swing.JButton();
        txtStudentNumber = new javax.swing.JFormattedTextField();
        btnScanRFID = new javax.swing.JButton();
        txtLastName = new javax.swing.JFormattedTextField();
        txtFirstName = new javax.swing.JFormattedTextField();
        btnHome = new javax.swing.JButton();
        txtRFIDKey = new javax.swing.JFormattedTextField();
        lblRFID = new javax.swing.JLabel();

        setBackground(new java.awt.Color(0, 204, 204));

        lblFirstName.setText("First Name: ");
        lblFirstName.setPreferredSize(new java.awt.Dimension(90, 20));

        lblLastName.setText("Last Name: ");
        lblLastName.setPreferredSize(new java.awt.Dimension(90, 20));

        lblParent.setText("Parent: ");
        lblParent.setPreferredSize(new java.awt.Dimension(90, 20));

        comboParent.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboParent.setPreferredSize(new java.awt.Dimension(56, 20));
        comboParent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboParentActionPerformed(evt);
            }
        });

        lblStudentNumber.setText("Student Number: ");
        lblStudentNumber.setMinimumSize(new java.awt.Dimension(90, 15));
        lblStudentNumber.setPreferredSize(new java.awt.Dimension(90, 20));

        lblTitle.setFont(new java.awt.Font("Calibri", 0, 36)); // NOI18N
        lblTitle.setText("Add Student");
        lblTitle.setPreferredSize(new java.awt.Dimension(200, 44));

        btnSubmit.setText("Submit");
        btnSubmit.setMaximumSize(new java.awt.Dimension(60, 25));
        btnSubmit.setMinimumSize(new java.awt.Dimension(60, 25));
        btnSubmit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSubmitActionPerformed(evt);
            }
        });

        txtStudentNumber.setBackground(new java.awt.Color(255, 255, 255));
        txtStudentNumber.setPreferredSize(new java.awt.Dimension(4, 20));

        btnScanRFID.setText("Scan");
        btnScanRFID.setPreferredSize(new java.awt.Dimension(75, 20));
        btnScanRFID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnScanRFIDActionPerformed(evt);
            }
        });

        txtLastName.setBackground(new java.awt.Color(255, 255, 255));
        txtLastName.setPreferredSize(new java.awt.Dimension(4, 20));

        txtFirstName.setBackground(new java.awt.Color(255, 255, 255));
        txtFirstName.setPreferredSize(new java.awt.Dimension(4, 20));

        btnHome.setText("Home");
        btnHome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHomeActionPerformed(evt);
            }
        });

        txtRFIDKey.setBackground(new java.awt.Color(255, 255, 255));
        txtRFIDKey.setPreferredSize(new java.awt.Dimension(4, 20));

        lblRFID.setText("RFID: ");
        lblRFID.setPreferredSize(new java.awt.Dimension(90, 20));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(95, 95, 95)
                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblFirstName, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                    .addComponent(lblParent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblStudentNumber, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblLastName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblRFID, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnSubmit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 134, Short.MAX_VALUE)
                        .addComponent(btnHome))
                    .addComponent(txtFirstName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtLastName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtStudentNumber, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(comboParent, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtRFIDKey, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(25, 25, 25)
                .addComponent(btnScanRFID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(31, Short.MAX_VALUE)
                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtFirstName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFirstName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtLastName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblLastName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblStudentNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtStudentNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(comboParent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblParent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnScanRFID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtRFIDKey, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblRFID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSubmit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnHome))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * This method calls the searchForRFID() method when the scan button is
     * clicked
     *
     * @param evt the "Scan" button has been clicked
     */
    private void btnScanRFIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnScanRFIDActionPerformed
        searchForRFID();
    }//GEN-LAST:event_btnScanRFIDActionPerformed

    /**
     * This method calls either the addStudent() or editStudent() method
     * depending on editing variable
     *
     * @param evt the "Submit" button has been clicked
     */
    private void btnSubmitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSubmitActionPerformed
        if (editing == false) {
            String firstName = txtFirstName.getText();
            String lastName = txtLastName.getText();
            String parent = comboParent.getItemAt(parentIndex);
            String rfidKey = txtRFIDKey.getText();
            String studentNumber = txtStudentNumber.getText();

            addStudent(firstName, lastName, studentNumber, parent, rfidKey);
        } else {
            editStudent();
        }
    }//GEN-LAST:event_btnSubmitActionPerformed

    /**
     * This method attempts to add a new student to the database.
     */
    private void addStudent(String firstName, String lastName, String studentNumber, String parent, String rfidKey) {
        System.out.println("Called");
        try {
            /**
             * This block of code checks to see if the studentID is unique. This
             * should never be a problem, as it is generated automatically.
             */
            statement = connection.createStatement();
            ResultSet studentNumberSet = statement.executeQuery("SELECT count(*) AS count FROM student WHERE studentNumber = " + studentNumber);
            studentNumberSet.next();
            int studentNumberCount = studentNumberSet.getInt("count");

            /**
             * This block of code checks to see if the scanned RFID tag is
             * already registered in the database.
             */
            ResultSet tagSet = statement.executeQuery("SELECT count(*) AS count FROM rfidtags WHERE rfidkey = '" + rfidKey + "'");
            tagSet.next();
            int tagCount = tagSet.getInt("count");

            /**
             * Searches the parent table for the parentID relating to the
             * username selected in the combo box
             */
            ResultSet parentSet = statement.executeQuery("SELECT parentID FROM parent WHERE username = '" + parent + "'");
            parentSet.next();
            int parentID = parentSet.getInt("parentID");
            int present = 1;

            /**
             * Checks if all the variables are valid before inserting the new
             * records into the database. This ensures that all data stored in
             * the database is valid.
             */
            if (firstName.length() != 0 && lastName.length() != 0 && rfidKey.length() != 0 && studentNumberCount == 0 && tagCount == 0) {
                /**
                 * Inserts the new entry into the student table
                 */
                statement.execute("INSERT INTO student (studentID, parentID, studentNumber, firstName, lastName, present) "
                        + "VALUES (NULL, " + parentID + ", " + studentNumber + ", '" + firstName + "', '" + lastName + "', " + present + ")");

                /**
                 * Query that returns the studentID relating to the
                 * studentNumber just entered into the database. This is so that
                 * we are able to link an RFID tag to the studentID.
                 */
                ResultSet studentIDSet = statement.executeQuery("SELECT studentID FROM student WHERE studentNumber = " + studentNumber);
                studentIDSet.next();
                int studentID = studentIDSet.getInt("studentID");

                /**
                 * Inserts a new entry into the rfidTags table with the RFID key
                 * and the studentID figured out above.
                 */
                statement.execute("INSERT INTO rfidtags (tagID, studentID, rfidKey) VALUES (NULL, " + studentID + ", '" + rfidKey + "')");
                JOptionPane.showMessageDialog(this,
                        "Student successfully added",
                        "RFID System",
                        JOptionPane.PLAIN_MESSAGE);
                clearForm();
            } else {
                if (tagCount != 0) {
                    lblRFID.setText("<html>RFID: <font color='red'>(In Use)</font></html>");
                    lblRFID.paintImmediately(lblRFID.getVisibleRect());
                }
                throw new Exception();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            /**
             * The below code sets a red "(*)" next to any fields that have not
             * been filled out properly.
             */
            if (firstName.length() == 0) {
                lblFirstName.setText("<html>First Name: <font color='red'>(*)</font></html>");
                lblFirstName.paintImmediately(lblFirstName.getVisibleRect());
            }
            if (lastName.length() == 0) {
                lblLastName.setText("<html>Last Name: <font color='red'>(*)</font></html>");
                lblLastName.paintImmediately(lblLastName.getVisibleRect());
            }
            if (parent.length() == 0) {
                lblParent.setText("<html>Parent: <font color='red'>(*)</font></html>");
                lblParent.paintImmediately(lblParent.getVisibleRect());
            }
            if (rfidKey.length() == 0) {
                lblRFID.setText("<html>RFID: <font color='red'>(*)</font></html>");
                lblRFID.paintImmediately(lblRFID.getVisibleRect());
            }
        }
    }

    /**
     * This method brings up the information related to an existing student in
     * the database, with the intention of updating the RFID key associated with
     * them.
     */
    public void editStudent() {
        if (editRFID.equals(txtRFIDKey.getText())) {
            JOptionPane.showMessageDialog(this,
                    "RFID key has not been changed",
                    "RFID System",
                    JOptionPane.PLAIN_MESSAGE);
        } else {
            try {
                statement = connection.createStatement();
                ResultSet rfidNumberSet = statement.executeQuery("SELECT count(*) AS count FROM rfidTags WHERE rfidKey = '" + txtRFIDKey.getText() + "'");
                rfidNumberSet.next();
                int rfidCount = rfidNumberSet.getInt("count");

                if (rfidCount == 0) {
                    statement.executeUpdate("UPDATE rfidTags set rfidKey = '" + txtRFIDKey.getText() + "' where rfidKey = '" + editRFID + "'");
                    JOptionPane.showMessageDialog(this,
                            "Successfully updated RFID",
                            "RFID System",
                            JOptionPane.PLAIN_MESSAGE);
                    frame.remove(this);
                    frame.add(new StudentForm(frame));
                    frame.pack();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Another student already has this RFID assigned to them!",
                            "RFID System",
                            JOptionPane.PLAIN_MESSAGE);
                }
            } catch (SQLException ex) {
                Logger.getLogger(StudentForm.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    /**
     * This method is used to either override the users selection or update the
     * index of the student. This is dependent on whether the student is being
     * updated or created for the first time.
     *
     * @param evt the combo box is selected
     */
    private void comboParentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboParentActionPerformed
        if (editing == false) {
            parentIndex = comboParent.getSelectedIndex();
        } else {
            comboParent.setSelectedIndex(parentIndex);
        }
    }//GEN-LAST:event_comboParentActionPerformed

    /**
     * This method removes the student panel from the frame and replaces the
     * panel with the home panel.
     *
     * @param evt
     */
    private void btnHomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHomeActionPerformed
        frame.remove(this);
        frame.add(new HomeForm(frame));
        frame.pack();
    }//GEN-LAST:event_btnHomeActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnHome;
    private javax.swing.JButton btnScanRFID;
    private javax.swing.JButton btnSubmit;
    private javax.swing.JComboBox<String> comboParent;
    private javax.swing.JLabel lblFirstName;
    private javax.swing.JLabel lblLastName;
    private javax.swing.JLabel lblParent;
    private javax.swing.JLabel lblRFID;
    private javax.swing.JLabel lblStudentNumber;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JFormattedTextField txtFirstName;
    private javax.swing.JFormattedTextField txtLastName;
    private javax.swing.JFormattedTextField txtRFIDKey;
    private javax.swing.JFormattedTextField txtStudentNumber;
    // End of variables declaration//GEN-END:variables
}
