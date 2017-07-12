package ClassModel;

import java.util.Scanner;
import com.fazecast.jSerialComm.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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

//    The start and finish time of the school
    private final String startTime = "09:00";
    private final String finishTime = "15:00";
    private final int leniance = 15;

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
     * @return whether the update was successful or not
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
//            sendEmail(tagKey);
            updateAttendance(studentID, updatedValue, tagKey);
            return true;
        } catch (SQLException ex) {
            /**
             * The card that a reader detected was either read incorrectly or
             * the rfidKey does not exist in the database.
             */
            System.out.println("Card not recognised");
        }
        return false;
    }

    /**
     * This method will either create a new entry in the attendance table or
     * update an already existing row depending on whether the student is
     * logging in or out.
     *
     * @param studentID the studentID of the student whose attendance is being
     * updated
     * @param currentStatus the binary value that determines whether a student
     * is in school or not
     * @param tagKey the tagkey relating to the student
     * @return whether the attendance records were successfully updated or not
     */
    public boolean updateAttendance(int studentID, int currentStatus, String tagKey) {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/YYYY");
        DateFormat timeFormat = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        String logDate = dateFormat.format(date);
        String logTime = timeFormat.format(date);

        boolean addToExcuses = false;

        if (currentStatus == 1) {
            try {
                /**
                 * Creates a new attendance record in the attendance table.
                 */
                statement.execute("INSERT INTO attendance (attendanceID, studentID, date, signInTime, signOutTime)"
                        + "VALUES (NULL, " + studentID + ", '" + logDate + "', '" + logTime + "', NULL)");

                /**
                 * Check to see if the student has arrived later than the school
                 * start time. If they are, a new entry is created in the excuse
                 * table
                 */
                if (getHour(logTime) > getHour(startTime)) {
                    addToExcuses = true;
                } else if (getHour(logTime) == getHour(startTime) && getMinute(logTime) > getMinute(startTime)) {
                    addToExcuses = true;
                }

                /**
                 * Check to see if addToExcuses variable is true, if it is, a
                 * new entry is created in the excuses table and an email is
                 * sent to the parent.
                 */
                if (addToExcuses == true) {
                    /**
                     * Retrieve attendanceID for the entry that we just created
                     * in the attendance table.
                     */
                    ResultSet attendanceIDSet = statement.executeQuery("SELECT * FROM attendance WHERE studentID = " + studentID + " AND date = '" + logDate + "' AND signInTime = '" + logTime + "'");
                    attendanceIDSet.next();
                    int attendanceID = attendanceIDSet.getInt("attendanceID");

                    addExcuse(attendanceID);
                    sendEmail(tagKey);
                }
                return true;
            } catch (SQLException ex) {
                Logger.getLogger(SerialCommunication.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        } else {
            try {
                /**
                 * Retrieve attendanceID based on entries for the student where
                 * there is no sign out time
                 */
                ResultSet attendanceIDSet = statement.executeQuery("SELECT * FROM attendance WHERE studentID = " + studentID + " AND signOutTime IS NULL");
                attendanceIDSet.next();
                int attendanceID = attendanceIDSet.getInt("attendanceID");
                statement.execute("UPDATE attendance SET signOutTime = '" + logTime + "' WHERE  attendanceID = " + attendanceID);

                /**
                 * Check to see if the student has departed earlier than the
                 * school finish time. If they have, a new entry is created in
                 * the excuse table.
                 */
                if (getHour(logTime) < getHour(finishTime)) {
                    addToExcuses = true;
                } else if (getHour(logTime) == getHour(finishTime) && getMinute(logTime) < getMinute(finishTime)) {
                    addToExcuses = true;
                }

                /**
                 * Check to see if addToExcuses variable is true, if it is, a
                 * new entry is created in the excuses table and an email is
                 * sent to the parent.
                 */
                if (addToExcuses == true) {
                    addExcuse(attendanceID);
                    sendEmail(tagKey);
                }
                return true;
            } catch (SQLException ex) {
                Logger.getLogger(SerialCommunication.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        }
    }

    /**
     * This method creates a new entry in the excuse table based on the
     * attendance number
     *
     * @param attendanceID the attendance number that the excuse relates to
     * @return whether the entry was successful or not
     */
    public boolean addExcuse(int attendanceID) {
        try {
            /**
             * Insert the new record into the excuses table.
             */
            statement.execute("INSERT INTO excuses (excuseID, attendanceID, verifiedByStaff, excuse)"
                    + "VALUES (NULL, " + attendanceID + ", 0, NULL)");
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(SerialCommunication.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * This method will convert a time in sting format to a split string array
     * then convert the first string(hour) to an integer. This value will be
     * returned.
     *
     * @param time the time in string format
     * @return an integer representation of the hour portion of the string time
     */
    public int getHour(String time) {
        String[] arrayStringTime = time.split(":");
        int hour = Integer.parseInt(arrayStringTime[0]);
        return hour;
    }

    /**
     * This method will convert a time in sting format to a split string array
     * then convert the second string(minute) to an integer. This value will be
     * returned.
     *
     * @param time the time in string format
     * @return an integer representation of the minute portion of the string
     * time
     */
    public int getMinute(String time) {
        String[] arrayStringTime = time.split(":");
        int minute = Integer.parseInt(arrayStringTime[0]);
        return minute;
    }

    /**
     * Sends an email to the parents of the student associated with the tagKey
     * identified.
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
                message = " arrived at school late.";
            } else {
                message = " departed school early.";
            }

            String body = "We wish to inform you that " + name + " has just " + message + "\n "
                    + "Please log into the web application to verify the matter. \n"
                    + "Kind regards, \n"
                    + "The RFID team";

            Mail mail = new Mail(email, subject, body);
        } catch (SQLException ex) {
            Logger.getLogger(SerialCommunication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Runs
     *
     * @param args
     */
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
