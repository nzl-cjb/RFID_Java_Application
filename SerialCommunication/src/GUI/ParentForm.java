/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import ClassModel.DBConnection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
public class ParentForm extends javax.swing.JPanel {

    private Connection connection;
    private Statement statement;
    private JFrame frame;
    private int maxUsername;
    private int maxPassword;
    private int maxEmail;
    private int maxPhoneNumber;

    /**
     * Creates new form NewStudentForm
     */
    public ParentForm(JFrame frame) {
        initialiseConstructor(frame);
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
        txtUsername.setTransferHandler(null);
        txtPassword.setTransferHandler(null);
        txtConfirmPassword.setTransferHandler(null);
        txtPhoneNumber.setTransferHandler(null);
        txtEmail.setTransferHandler(null);

        /*
        Gets the column size for specified columns. The reason for this is so that any updates made in the
        database will not have to be manually updated. As part of minimising user error, they are limited to 
        entering strings with length equal to one less than the column size.
         */
        try {
            DatabaseMetaData meta = connection.getMetaData();
            ResultSet columns = meta.getColumns(null, null, "parent", null);

            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                switch (columnName) {
                    case "username":
                        maxUsername = columns.getInt("COLUMN_SIZE") - 1;
                        break;
                    case "password":
                        maxPassword = columns.getInt("COLUMN_SIZE") - 1;
                        break;
                    case "phoneNumber":
                        maxPhoneNumber = columns.getInt("COLUMN_SIZE") - 1;
                        break;
                    case "email":
                        maxEmail = columns.getInt("COLUMN_SIZE") - 1;
                        break;
                    default:
                        break;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(StudentForm.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        txtUsername.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                String key = String.valueOf(e.getKeyChar());
                Pattern pattern = Pattern.compile("[a-zA-Z0-9]");
                Matcher matcher = pattern.matcher(key);
                if (txtUsername.getText().length() >= maxUsername) {
                    e.consume();
                } else if (!matcher.find()) {
                    e.consume();
                }
            }
        });

        txtPassword.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                String key = String.valueOf(e.getKeyChar());
                if (txtPassword.getText().length() >= maxPassword) {
                    e.consume();
                }
            }
        });

        txtConfirmPassword.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                String key = String.valueOf(e.getKeyChar());
                if (txtConfirmPassword.getText().length() >= maxPassword) {
                    e.consume();
                }
            }
        });
        
        txtPhoneNumber.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                String key = String.valueOf(e.getKeyChar());
                Pattern pattern = Pattern.compile("[0-9]");
                Matcher matcher = pattern.matcher(key);
                if (txtPhoneNumber.getText().length() >= maxPhoneNumber) {
                    e.consume();
                } else if (!matcher.find()) {
                    e.consume();
                }
            }
        });
        
        txtEmail.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                String key = String.valueOf(e.getKeyChar());
                if (txtConfirmPassword.getText().length() >= maxEmail) {
                    e.consume();
                }
            }
        });
    }

    /**
     * Clears the fields in the parent form
     */
    public void clearForm() {
        txtUsername.setText(null);
        txtPassword.setText(null);
        txtConfirmPassword.setText(null);
        txtPhoneNumber.setText(null);
        txtEmail.setText(null);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblUsername = new javax.swing.JLabel();
        lblPassword = new javax.swing.JLabel();
        lblPhoneNumber = new javax.swing.JLabel();
        lblConfirmPassword = new javax.swing.JLabel();
        lblTitle = new javax.swing.JLabel();
        btnSubmit = new javax.swing.JButton();
        lblRFID = new javax.swing.JLabel();
        txtConfirmPassword = new javax.swing.JFormattedTextField();
        txtPassword = new javax.swing.JFormattedTextField();
        txtUsername = new javax.swing.JFormattedTextField();
        btnHome = new javax.swing.JButton();
        txtPhoneNumber = new javax.swing.JFormattedTextField();
        txtEmail = new javax.swing.JFormattedTextField();

        setBackground(new java.awt.Color(0, 204, 204));

        lblUsername.setText("Username:");
        lblUsername.setPreferredSize(new java.awt.Dimension(90, 20));

        lblPassword.setText("Password: ");
        lblPassword.setPreferredSize(new java.awt.Dimension(90, 20));

        lblPhoneNumber.setText("Phone Number:");
        lblPhoneNumber.setPreferredSize(new java.awt.Dimension(90, 20));

        lblConfirmPassword.setText("Confirm Password:");
        lblConfirmPassword.setMinimumSize(new java.awt.Dimension(90, 15));
        lblConfirmPassword.setPreferredSize(new java.awt.Dimension(90, 20));

        lblTitle.setFont(new java.awt.Font("Calibri", 0, 36)); // NOI18N
        lblTitle.setText("Add Parent");
        lblTitle.setPreferredSize(new java.awt.Dimension(200, 44));

        btnSubmit.setText("Submit");
        btnSubmit.setMaximumSize(new java.awt.Dimension(60, 25));
        btnSubmit.setMinimumSize(new java.awt.Dimension(60, 25));
        btnSubmit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSubmitActionPerformed(evt);
            }
        });

        lblRFID.setText("Email");
        lblRFID.setPreferredSize(new java.awt.Dimension(25, 20));

        txtConfirmPassword.setBackground(new java.awt.Color(255, 255, 255));
        txtConfirmPassword.setPreferredSize(new java.awt.Dimension(4, 20));

        txtPassword.setBackground(new java.awt.Color(255, 255, 255));
        txtPassword.setPreferredSize(new java.awt.Dimension(4, 20));

        txtUsername.setBackground(new java.awt.Color(255, 255, 255));
        txtUsername.setPreferredSize(new java.awt.Dimension(4, 20));

        btnHome.setText("Home");
        btnHome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHomeActionPerformed(evt);
            }
        });

        txtPhoneNumber.setBackground(new java.awt.Color(255, 255, 255));
        txtPhoneNumber.setPreferredSize(new java.awt.Dimension(4, 20));

        txtEmail.setBackground(new java.awt.Color(255, 255, 255));
        txtEmail.setPreferredSize(new java.awt.Dimension(4, 20));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(95, 95, 95)
                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(269, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblUsername, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                    .addComponent(lblPhoneNumber, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblRFID, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblConfirmPassword, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblPassword, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnSubmit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 134, Short.MAX_VALUE)
                        .addComponent(btnHome))
                    .addComponent(txtConfirmPassword, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtPassword, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtUsername, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtPhoneNumber, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtEmail, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(125, 125, 125))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(32, Short.MAX_VALUE)
                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtConfirmPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblConfirmPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPhoneNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPhoneNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblRFID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSubmit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnHome))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * This method 
     * @param evt 
     */
    private void btnSubmitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSubmitActionPerformed
        try {
            String username = txtUsername.getText();
            String password = txtPassword.getText();
            String phoneNumber = txtPhoneNumber.getText();
            String email = txtEmail.getText();
            
            statement = connection.createStatement();
            ResultSet usernameParentSet = statement.executeQuery("SELECT count(*) AS count FROM parent WHERE username = '" + username + "'");
            int usernameParentCount = 0;
            try {
                usernameParentSet.next();
                usernameParentCount= usernameParentSet.getInt("count");
            } catch (Exception e) {
            }
            
            ResultSet usernameTeacherSet = statement.executeQuery("SELECT count(*) AS count FROM teacher WHERE username = '" + username + "'");
            int usernameTeacherCount = 0;
            try {
                usernameParentSet.next();
                usernameTeacherCount = usernameTeacherSet.getInt("count");
            } catch (Exception e) {  
            }
            ResultSet emailParentSet = statement.executeQuery("SELECT count(*) AS count FROM parent WHERE email = '" + email + "'");
            int emailParentCount = 0;
            try {
                emailParentSet.next();
                emailParentCount= usernameParentSet.getInt("count");
            } catch (Exception e) {
            }
            
            ResultSet emailTeacherSet = statement.executeQuery("SELECT count(*) AS count FROM teacher WHERE email = '" + email + "'");
            int emailTeacherCount = 0;
            try {
                emailTeacherSet.next();
                emailTeacherCount = usernameTeacherSet.getInt("count");
            } catch (Exception e) {
            }
            
            
            boolean passwordMatch = (txtPassword.getText().equals(txtConfirmPassword.getText()));
            
            if (usernameParentCount == 0 && usernameTeacherCount == 0 && emailParentCount == 0 && emailTeacherCount == 0 && passwordMatch) {
                statement.execute("INSERT INTO parent (parentID, username, password, phoneNumber, email) "
                        + "VALUES (NULL, '" + username + "', '" + password + "', '" + phoneNumber + "', '" + email + "')");
                JOptionPane.showMessageDialog(this,
                        "Parent successfully added",
                        "RFID System",
                        JOptionPane.PLAIN_MESSAGE);
                clearForm();
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(ParentForm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnSubmitActionPerformed

    private void btnHomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHomeActionPerformed
        frame.remove(this);
        frame.add(new HomeForm(frame));
        frame.pack();
    }//GEN-LAST:event_btnHomeActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnHome;
    private javax.swing.JButton btnSubmit;
    private javax.swing.JLabel lblConfirmPassword;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblPhoneNumber;
    private javax.swing.JLabel lblRFID;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblUsername;
    private javax.swing.JFormattedTextField txtConfirmPassword;
    private javax.swing.JFormattedTextField txtEmail;
    private javax.swing.JFormattedTextField txtPassword;
    private javax.swing.JFormattedTextField txtPhoneNumber;
    private javax.swing.JFormattedTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}
