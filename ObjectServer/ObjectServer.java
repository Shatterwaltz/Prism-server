package objectserver;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.io.ObjectInputStream;
public class ObjectServer{

    //concurrentHashMap is thread safe. resizes self when needed, initial size 5. 
    //Holds mapping of event codes to desktops.
    private static ConcurrentHashMap<String, DesktopConnection> eventsTable = new ConcurrentHashMap(5);
    
    
    public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException{
       
        Socket connection;
        int queueSize = 3;
        int port=80;
        ServerSocket socket = new ServerSocket(port, queueSize); 
        //ServerSocket socket = new ServerSocket(2004, 10);
        ObjectInputStream objIn;
        //Primary loop
        while(true){
        System.out.println("Waiting on connection");
        //accept connection
        connection = socket.accept();
        objIn=new ObjectInputStream(connection.getInputStream());
        
        //Wait for client to identify as a mobile device or a desktop, then
        //create thread of appropriate type and give the socket over to it.
        String type=objIn.readObject().toString();
            System.out.println("Connected device is type: "+type);
        if(type.equals("D")){
            DesktopConnection conn = new DesktopConnection(connection, objIn);
            conn.start();
        }else{
            MobileConnection conn = new MobileConnection(connection, objIn);
            conn.start();
        }
        System.out.println("Connected");
        }
        
    }
    
    //Add to eventsTable if the key is unique
    public static boolean addConnection(String code, DesktopConnection desktop){
        if(eventsTable.containsKey(code)){
            return false;
        }else{
            eventsTable.put(code, desktop);
            return true;
        }
        
    }
    
    //Delete from eventsTable
    public static void deleteConnection(String code){
        eventsTable.remove(code);
    }
    
    //Check uniqueness of a specified key
    public static boolean codeExists(String code){
        return eventsTable.containsKey(code);
    }
    
    //Retrieve connection related to a particular key
    public static DesktopConnection getDesktop(String code){
        return eventsTable.get(code);
    }
}
