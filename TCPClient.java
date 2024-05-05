import java.net.*;
import java.text.*;
import java.util.*;
import java.io.*;

public class TCPClient {
    private final String QUIT = "QUIT";
    private final String KEYS = "KEYS";
    private final String PUT = "PUT";
    private final String DELETE = "DELETE";
    private final String GET = "GET";

    private DataOutputStream dataOut;
    private DataInputStream dataIn;
    private Socket socket;

    public static void main(String[] args) {
        TCPClient client = new TCPClient();
        client.startClient();
    }

    public void startClient() {
        try {
            socket = new Socket("localhost", 7777);
            socket.setSoTimeout(1000);

            dataOut = new DataOutputStream(socket.getOutputStream());
            dataIn = new DataInputStream(socket.getInputStream());

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Connected to TCP server!");
            System.out.println("Please Input Command in either of the following forms: \n       GET<key> \n       PUT<key><value> \n       DELETE<key> \n       KEYS\n       QUIT");
          
            String command;

            while ((command = reader.readLine()) != null) {

                if (command.equals(QUIT)) {
                    cleanUp();
                    break;
                }
                String response = sendRequest(command);
                System.out.println(response);
                System.out.println("Enter command: (in right format)");
                System.out.println("Please Input Command in either of the following forms: \n       GET<key> \n       PUT<key><value> \n       DELETE<key> \n       KEYS\n       QUIT");
            }
		} catch (SocketTimeoutException e) {
			String time = getCurrentTimeStamp();
            System.out.println(time + " Unknown IO Error. Command Not Successful");
            startClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getCurrentTimeStamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]");
        return dateFormat.format(new Date());
    }

   String sendRequest(String command) throws IOException {
        String[] tokens = command.split("\\s+");
        String action = tokens[0];
        String time = getCurrentTimeStamp();

        switch (action) {
            case GET:
            	if (tokens.length != 2) {
                    System.out.println(time + " Error: Invalid command format for GET.");
                    return "";
                }
                handleGetRequest(action, tokens[1]);
                break;
            case PUT:
            	if (tokens.length != 3) {
                    System.out.println(time +"Error: Invalid command format for PUT.");
                    return "";
                }
                handlePutRequest(action, tokens[1], tokens[2]);
                break;

            case DELETE:
            	if (tokens.length != 2) {
                    System.out.println(time +"Error: Invalid command format for DELETE.");
                    return "";
                }
                handleDelRequest(action, tokens[1]);
                break;
            case KEYS:
            	if (tokens.length != 1) {
                    System.out.println(time + "Error: Invalid command format for KEYS.");
                    return "";
                }
                handleKeysRequest(action);
                break;
            default:
                System.out.println(time + " Error: Invalid command");
                return "";
        }

        return dataIn.readUTF();
    }

    private void handleGetRequest(String action, String key) throws IOException {
        dataOut.writeUTF(action + " " + key);
    }

    private void handlePutRequest(String action, String key, String value) throws IOException {
        dataOut.writeUTF(action + " " + key + " " + value);
    }

    private void handleDelRequest(String action, String key) throws IOException {
        dataOut.writeUTF(action + " " + key);
    }

    private void handleKeysRequest(String action) throws IOException {
        dataOut.writeUTF(action);
    }

    private void cleanUp() {
        try {
            if (socket != null) {
                socket.close();
            }
            if (dataOut != null) {
                dataOut.close();	
            }
            if (dataIn != null) {
                dataIn.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
