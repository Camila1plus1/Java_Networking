import java.net.*;
import java.util.*;
import java.text.*;
import java.io.*;

public class TCPServer<K, V> {
    private ServerSocket serverSocket;
    final HashMap<K, V> keyValStore = new HashMap<>();
    int port = 7777;
    int clientCount = 0;

    TCPServer(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    TCPServer() throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public static void main(String[] args) {
        try {
            TCPServer server = new TCPServer();
            server.startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getCurrentTimeStamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]");
        return dateFormat.format(new Date());
    }

    private String getLogHeader(String clientSocketIP, int clientSocketPort) {
        return "[" + clientSocketIP + ":" + clientSocketPort + "] ";
    }

    public void startServer() {
        System.out.println("Server started. Listening for Clients on port " + port + "...");
        String time = getCurrentTimeStamp();
        String info = getLogHeader("127.0.0.1", 7777);
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                clientCount++;

                System.out.println(time + info + " Client Connection Successful! Total Clients: " + clientCount);
                ClientHandler<String, String>clientHandler = new ClientHandler<>(clientSocket);
                Thread thread = new Thread(clientHandler);
                thread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

@SuppressWarnings("unchecked")
class ClientHandler<K, V> implements Runnable {
    private Socket clientSocket;
    private DataInputStream inp;
    private DataOutputStream out;
    private HashMap<K, V> keyValStore;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        this.keyValStore = new HashMap<>();
        try {
            inp = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                String request = inp.readUTF();
                System.out.println("Received request from client: " + request);
                String[] tokens = request.split("\\s+");
                String command = tokens[0];

                switch (command) {
                    case "GET":
                        handleGet((K)tokens[1]);
                        break;
                    case "PUT":
                        handlePut((K)tokens[1], (V)tokens[2]);
                        break;
                    case "DELETE":
                        handleDelete((K)tokens[1]);
                        break;
                    case "KEYS":
                        handleKeys();
                        break;
                    case "QUIT":
                        handleQuit();
                        break;
                    default:
                        String time = getCurrentTimeStamp();
                        out.writeUTF(time + " Wrong format of command!");
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	private String getCurrentTimeStamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]");
        return dateFormat.format(new Date());
    }

    private String getLogHeader(String clientSocketIP, int clientSocketPort) {
        return "[" + clientSocketIP + ":" + clientSocketPort + "] ";
    }

	    private void handleGet(K key) throws IOException {

	        V value = keyValStore.get(key);
	        if (value != null) {
	            out.writeUTF(value.toString());
	        } else {
	        	String time = getCurrentTimeStamp();
	        	String info = getLogHeader("127.0.0.1", 7777);
	            out.writeUTF(time + " Error: [Err] The key '" + key + " ' doesn't exists in the Key Value Store");
	            System.out.println(time + info + " [Err] The key " + key + " is not in the KVS");
	        }
	    }

    private void handlePut(K key, V value) throws IOException {
    	int maxKeyLength = 10;
    	int maxValueLength = 10;
    	String time = getCurrentTimeStamp();
    	String info = getLogHeader("127.0.0.1", 7777);
    	 if(key.toString().length() > maxKeyLength || value.toString().length() > maxValueLength) {
        out.writeUTF(time + " Error: Key or value too long (max 10 characters).");
        System.out.println(time + info + " Error: Key or value length exceeds the limits of 100 characters.");
        return;
    }
        keyValStore.put(key, value);
        out.writeUTF(time + " Success: Key-value pair saved on the server! " + key + " with value " + " " + value + " saved successfully! ");
    }

    private void handleDelete(K key) throws IOException {
    	String time = getCurrentTimeStamp();
    	String info = getLogHeader("127.0.0.1", 7777);
        if (keyValStore.containsKey(key)) {
            keyValStore.remove(key);
            out.writeUTF(time + "Key-value pair deleted successfully");
        } else {
            out.writeUTF(time + " Error: The key '" + key + "' doesn't exists in the Key Value Store");
            System.out.println(time + info + "[Err] The key " + key + " is not in the KVS" );
        }
    }

    private void handleKeys() throws IOException {
    	String time = getCurrentTimeStamp();
    	if(keyValStore.isEmpty()){
    		out.writeUTF(time + " Key value store is empty, no keys found!");
    		System.out.println(time + "- Key value store is empty");
    		}else{
        List<K> keyList = new ArrayList<>(keyValStore.keySet());
		String keys = String.join(", ", keyList.toArray(new String[0]));
        String time1 = getCurrentTimeStamp();
        out.writeUTF(time + " Success: Keys - " + keys);
    }
   }

    private void handleQuit() throws IOException {
    	String time = getCurrentTimeStamp();
    	String info = getLogHeader("127.0.0.1", 7777);
        clientSocket.close();
        System.out.println(time + info + "Client disconnected: " + clientSocket);
    }
}