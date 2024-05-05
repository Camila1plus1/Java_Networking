import java.net.*;
import java.util.*;
import java.text.*;
import java.io.*;

@SuppressWarnings("unchecked")
public class UDPServer<K, V> implements Runnable{
    private static final int port = 7848;
    private static final int max_Packet_Size = 1024;
    private DatagramSocket socket;
    private HashMap<K, V> keyValStore;

    UDPServer() throws SocketException {
        this.socket = new DatagramSocket(port);
        this.keyValStore = new HashMap<>();
    }

    public static void main(String[] args) {
        try {
            UDPServer<String, String> server = new UDPServer<>();
            server.run();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
@Override
public void run() {
    System.out.println("Server started. Listening for Clients on port " + port + "...");
    while (true) {
        try {
            byte[] buffer = new byte[max_Packet_Size];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            
            InetAddress clientAddress = packet.getAddress();
            int clientPort = packet.getPort();

            Thread requestThread = new Thread(() -> {
                String request = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received request from client: " + request);
                String[] tokens = request.split("\\s+");
                String command = tokens[0];

                String response = processCommand(command, Arrays.copyOfRange(tokens, 1, tokens.length));
                byte[] responseData = response.getBytes();
                DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, clientAddress, clientPort);
                try {
                    socket.send(responsePacket);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            requestThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


    private String processCommand(String command, String[] arguments) {
    String time = getCurrentTimeStamp();

    switch (command) {
        case "GET":
            if (arguments.length != 1) {
                return time + " Invalid GET command format";
            }
            return handleGetRequest((K) arguments[0]);
        case "PUT":
            if (arguments.length != 2) {
                return time + " Invalid PUT command format";
            }
            return handlePutRequest((K) arguments[0], (V) arguments[1]);
        case "DELETE":
            if (arguments.length != 1) {
                return time + " Invalid DELETE command format";
            }
            return handleDeleteRequest((K) arguments[0]);
        case "KEYS":
            if (arguments.length != 0) {
                return time + " Invalid KEYS command format";
            }
            return handleKeysRequest();
        case "QUIT":
            return time + " Server disconnected";
        default:
            return time + " Invalid command";
    }
}

        public String getCurrentTimeStamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]");
        return dateFormat.format(new Date());
    }

    private String getLogHeader(String clientSocketIP, int clientSocketPort) {
        return "Client [/" + clientSocketIP + ":" + clientSocketPort + "] ";
    }

    private String handleGetRequest(K key) {
        String time = getCurrentTimeStamp();
        String info = getLogHeader("127.0.0.1", 7777);
        V value = keyValStore.get(key);
        if (value != null) {
            return value.toString();
        } else {
            System.out.println(time + info + "The key " + key + "' doesn't exists in KVS");
            return time + " Error [Err] The key '" + key + "' doesn't exists in Key Value Store";
        }
    }

    private String handlePutRequest(K key, V value) {
        int maxKeyLength = 10;
        int maxValueLength = 10;
        String time = getCurrentTimeStamp();
        String info = getLogHeader("127.0.0.1", 7777);
        if(key.toString().length() > maxKeyLength || value.toString().length() > maxValueLength) {
        System.out.println(time + info + " Key or value length exceeds the limits of 100 characters.");
        return time +  " Error [Err] Key or value too long (max 10 characters).";
    }else{
        keyValStore.put(key, value);
        return time + " Success: Key-value pair saved on the server! " + key + " with value '" + value + "' saved successfully!";
    }
}

    private String handleDeleteRequest(K key) {
        String time = getCurrentTimeStamp();
        String info = getLogHeader("127.0.0.1", 7777);
        if (keyValStore.containsKey(key)) {
            keyValStore.remove(key);
            return time + " Key-value pair deleted successfully";
        } else {
            System.out.println(time + info + " Key '" + key + " doesn't exists");
            return time +  " Error: '" + key + "' Key not found";
        }
    }

    private String handleKeysRequest() {
        String time = getCurrentTimeStamp();
        String info = getLogHeader("127.0.0.1", 7777);
        if(keyValStore.isEmpty()){
            System.out.println(time + info + "KVS is empty");
        return time + " Error: Key Value store is empty, no keys found!";
    }else{
        List<K> keyList = new ArrayList<>(keyValStore.keySet());
        String keys = String.join(", ", keyList.toArray(new String[0]));
        return time + " Success: Keys - " + keys;
    }
    }
}