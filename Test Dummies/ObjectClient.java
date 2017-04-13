
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.util.Scanner;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.File;
public class ObjectClient {

    public static void main(String[] args) throws UnknownHostException, IOException{
        //Connection info
        InetAddress ip = InetAddress.getByName("192.168.1.102"); 
        int port=80; 
        
        //Create socket from connection info
        Socket socket = new Socket(ip, port);
        //Socket socket = new Socket("localhost", 2004); 
        //Set up reading from socket
        ObjectOutputStream objOut = new ObjectOutputStream(socket.getOutputStream());
        objOut.flush();
        //Read from keyboard
        Scanner in = new Scanner(System.in); 
        
        //Tell server this is a desktop. Must be done immediately after connection.
        objOut.writeObject("D"); 
        objOut.flush();
        System.out.println("connected: ");
        //create instance of thread to read messages
        receiveThread r = new receiveThread(socket);
        r.start();
        
        //loop writes to socket. Send command "exit" before closing.
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
            try{
                //validation loop
                boolean validated = false;
                while(!validated){
                    if(objIn.readObject().toString().equals("Validated"))
                        validated=true;
                    else
                        System.out.println("Invalid");
                }
                System.out.println("Validated");
                
                //Image receive loop. Expects name followed by encoded image
                int imgInt=0;
                while(true){
                    //imgInt prevents image overrwrite because it's used in the filepath
                    imgInt++;
                    System.out.println("Waiting for name/image");
                    //expect name first
                    String name = objIn.readObject().toString();
                    System.out.println("Received name: "+name);
                    //create file path
                    File file = new File("C:\\Users\\danie\\Documents\\TestImages\\"+name+imgInt+".jpg");
                    //read and save image encoded as byte array
                    byte[] o = (byte[])objIn.readObject();
                    //write image to file
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(o);
                    fos.close();
                    System.out.println("Image saved");
                }
            }catch(Exception e){

            
        }
    }
}