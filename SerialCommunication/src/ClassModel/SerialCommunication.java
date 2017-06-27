package ClassModel;

import java.util.Scanner;
import com.fazecast.jSerialComm.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class will run indefinitely by separate RFID readers and Arduinos to
 * scan for students entering and exiting the school.
 *
 * @author Callum
 */
public class SerialCommunication {

    private Connection connection;
    private Statement statement;

    /**
     * The default constructor creates a new DBConnection object, which contains
     * the credentials needed to establish a connection with the database.
     */
    public SerialCommunication() {
        DBConnection dbConn = new DBConnection();
        connection = dbConn.getConnection();
    }

    /**
     * Updates the status of a student in the database whose studentID is
     * associated with the tagKey that one of the RFID readers detected.
     *
     * @param tagKey the tagKey stored on the RFID card scanned by a reader
     */
    public boolean updatePresentStatus(String tagKey) {
        try {
            boolean isPresent = false;
            statement = connection.createStatement();

            /**
             * Retrieves the information relating to the student in the database
             * where the tagKey matches an rfidKey. The tagSet will either
             * contain 0 or 1 results.
             */
            ResultSet tagSet = statement.executeQuery("SELECT * FROM rfidtags r, student s WHERE r.rfidKey = '" + tagKey + "' AND s.studentID = r.studentID");

            tagSet.next();
            /**
             * Retieve the studentID, the "present" status and the firstName of
             * the student associated with the tagKey.
             */
            int studentID = tagSet.getInt("studentID");
            int present = tagSet.getInt("present");
            String name = tagSet.getString("firstName");

            int updatedValue = 1;

            if (present == 1) {
                updatedValue = 0;
            }
            /**
             * Updates the "present" status to the opposite of what it was
             * before. If a student was absent(0), they are then updated to
             * present(1).
             */
            statement.execute("UPDATE student SET present = " + updatedValue + " WHERE studentID = " + studentID);
            System.out.println(name + " status updated");
            sendEmail(tagKey);
            return true;
        } catch (SQLException ex) {
            /**
             * The card that a reader detected was either read incorrectly or the rfidKey does not exist in the database.
             */
            System.out.println("Card not recognised");
        }
        return false;
    }

    /**
     * Sends an email to the parents of the student associated with the tagKey identified.
     * 
     * @param tagKey the tagKey read by an RFID reader
     */
    public void sendEmail(String tagKey) {
        try {
            ResultSet tagSet = statement.executeQuery("SELECT s.firstName AS name, p.email AS email, s.present AS present FROM rfidtags r, student s, parent p WHERE r.rfidKey = '" + tagKey + "' AND r.studentID = s.studentID AND s.parentID = p.parentID");
            tagSet.next();
            String email = tagSet.getString("email");
            int present = tagSet.getInt("present");
            String name = tagSet.getString("name");

            String subject = "Urgent! Please read";
            String message = "";
            if (present == 1) {
                message = " arrived at school.";
            } else {
                message = " departed school.";
            }

            String body = "We wish to inform you that " + name + " has just " + message + "\nKind regards, \nThe RFID team";

            Mail mail = new Mail(email, subject, body);
        } catch (SQLException ex) {
            Logger.getLogger(SerialCommunication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String args[]) {

        SerialCommunication comm = new SerialCommunication();
        Scanner scan = new Scanner(System.in);
        SerialPort[] ports = SerialPort.getCommPorts();

        int count = 1;
        for (SerialPort port : ports) {
            System.out.println(count + ") " + port.getBaudRate());
            count++;
        }

//        int portNo = scan.nextInt();
        SerialPort chosenPort = ports[0];
//        chosenPort.setBaudRate(19200);

        if (chosenPort.openPort()) {
            System.out.println("Opened port");
        } else {
            System.out.println("Couldn't open port");
            return;
        }
        while (true) {
            chosenPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);

            Scanner data = new Scanner(chosenPort.getInputStream());

            try {
                String key = data.nextLine();
                System.out.println(key);
                comm.updatePresentStatus(key);
            } catch (Exception e) {
            }
        }
    }
}
