package ClassModel;

import java.util.Scanner;
import com.fazecast.jSerialComm.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SerialCommunication {

    private Connection connection;
    private Statement statement;

    public SerialCommunication() {
        DBConnection dbConn = new DBConnection();
        connection = dbConn.getConnection();
    }

    public void updatePresentStatus(String tagKey) {
        try {
            boolean isPresent = false;
            statement = connection.createStatement();

            ResultSet tagSet = statement.executeQuery("SELECT * FROM rfidtags r, student s WHERE r.rfidKey = '" + tagKey + "' AND s.studentID = r.studentID");

            tagSet.next();
            int studentID = tagSet.getInt("studentID");
            int present = tagSet.getInt("present");
            String name = tagSet.getString("firstName");

            int updatedValue = 1;

            if (present == 1) {
                updatedValue = 0;
            }
            statement.execute("UPDATE student SET present = " + updatedValue + " WHERE studentID = " + studentID);
            System.out.println(name + " status updated");
            sendEmail(tagKey);
        } catch (SQLException ex) {
            System.out.println("Card not recognised");
        }
    }

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
//                System.out.println(key);
                String key = data.nextLine();
                System.out.println(key);
                comm.updatePresentStatus(key);
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }
    }
}
