import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UDPClient {
    private static final String QUIT = "QUIT";
    private static final String KEYS = "KEYS";
    private static final String PUT = "PUT";
    private static final String DELETE = "DELETE";
    private static final String GET = "GET";

    private static final int port = 7848;
    private static final int maxPocketSize = 1024;

    private DatagramSocket socket;
    private InetAddress serverAddress;

    public static void main(String[] args) {
        UDPClient client = new UDPClient();
        client.startClient();
    }
    boolean timeOut = false;

    public void startClient() {
        if (timeOut == true) {
        System.out.println("Unknown IO Error. Command Not Successful");
    }else{
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(1000);
            serverAddress = InetAddress.getByName("localhost");

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Connected to UDP server!");
            System.out.print("Please Input Command in either of the following forms: \n       GET <key> \n       PUT <key> <value> \n       DELETE <key> \n       KEYS \n       QUIT \nEnter command: ");

            String command;

            while ((command = reader.readLine()) != null) {

                if (command.equals(QUIT)) {
                    cleanUp();
                    break;
                }
                String response = sendRequest(command);
                System.out.println(response);
                System.out.print("Enter command: (in right format): ");
            }
        }catch (SocketTimeoutException e) {
            String time = getCurrentTimeStamp();
            String info = getLogHeader("127.0.0.1", 7777);
            System.out.println(time + info + "Unknown IO Error. Command Not Successful");
            timeOut = true;
            } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

    private String getCurrentTimeStamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]");
        return dateFormat.format(new Date());
    }

     private String getLogHeader(String clientSocketIP, int clientSocketPort) {
        return "[" + clientSocketIP + ":" + clientSocketPort + "] ";
    }

    private String sendRequest(String command) {
        try {
            byte[] sendData = command.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, port);
            socket.send(sendPacket);

            byte[] receiveData = new byte[maxPocketSize];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);


            return new String(receivePacket.getData(), 0, receivePacket.getLength());
        } catch (IOException e) {
            String time = getCurrentTimeStamp();
            String info = getLogHeader("127.0.0.1", 7777);
            return time + info + "Unknown IO Error. Command Not Successful";
        }
    }


    private void cleanUp() {
        if (socket != null) {
            socket.close();
        }
    }
}
