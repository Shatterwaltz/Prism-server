
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.util.Scanner;
public class ObjectMobile {

    public static void main(String[] args) throws UnknownHostException, IOException{
        //Connection info
        InetAddress ip = InetAddress.getByName("192.168.0.15"); // 192.168.0.4
        int port=80; 
        
        //Create socket from connection info
        Socket socket = new Socket(ip, port);
        //Socket socket = new Socket("localhost", 2004); 
        //Set up writing to socket
        ObjectOutputStream objOut = new ObjectOutputStream(socket.getOutputStream()); 
        objOut.flush();
        //read from keyboard
        Scanner in = new Scanner(System.in); 
        //Tell server this is a mobile. 
        //Must be done immediately after connection.
        objOut.writeObject("M"); 
        objOut.flush();
        System.out.println("connected: ");
        //user has to send event code to server next
        System.out.println("Enter code:");
        objOut.writeObject(in.nextLine());
        //user has to send name to server
        System.out.println("Enter name:");
        objOut.writeObject(in.nextLine());
        
        //create instance of thread to read messages
        receiveThread r = new receiveThread(socket);
        r.start();
        
        //loop writes images encoded as byte arrays to socket. Send command "exit" before closing.
        while(true){
            objOut.writeObject(in.nextLine());
            objOut.flush();
        }
    }
    
}

class receiveThread extends Thread{
    //Read from socket
    ObjectInputStream objIn;
    public receiveThread(Socket socket) throws IOException{
        //initialize scanner
        objIn=new ObjectInputStream(socket.getInputStream());
    }
    
    //thread simply prints messages received from socket
    public void run(){        
        //try catch prevents whole client from crashing if socket encounters error
        while(true){
            try{
                System.out.println(objIn.readObject());
            }catch(Exception e){

            }
        }
    }
}