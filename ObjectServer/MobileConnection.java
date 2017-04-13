package objectserver;

import java.net.Socket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
public class MobileConnection extends Thread{
    //Socket for this connection
    Socket socket;
    //ID of this thread instance
    static int ID = 0;
    int thisID=0;
    //Read from socket
    ObjectInputStream objIn;
    //Write to socket    
    ObjectOutputStream objOut;
    //Event key this desktop is related to
    String code="";
    String name="";
    String message="";
    public MobileConnection(Socket connection, ObjectInputStream objIn) throws IOException{
        //Receive the socket and input stream created during connection
        //and initialize related local variables
        socket=connection;
        thisID=++ID;
        //Display IP, initialize socket IO
        System.out.println("M"+thisID+":"+socket.getInetAddress());
        objOut=new ObjectOutputStream(socket.getOutputStream());
        objOut.writeObject("Registered as mobile");
        objOut.flush();
        this.objIn=objIn;
    }
    
    @Override
    public void run(){
        boolean validated=false;
        try{
            //Validation loop, check against eventsTable in server and progress
            //when user enters an existing code.
            while(!validated){
                System.out.println("M"+thisID+": Waiting for code");
                code = objIn.readObject().toString();
                System.out.println("M"+thisID+": Received code "+code);
                //if code exists, respond to mobile and exit validation loop.
                //else, inform mobile and continue loop.
                if(ObjectServer.codeExists(code)){
                    System.out.println("M"+thisID+": Code "+code+" successfully registered.");
                    validated=true;
                    objOut.writeObject("Validated");
                }else{
                    System.out.println("M"+thisID+": Code "+code+" does not exist.");
                    objOut.writeObject("Invalid");
                }
                objOut.flush();
            }
            //Get name from mobile and respond
            name=objIn.readObject().toString();
            objOut.writeObject("Name received");
            System.out.println("M"+thisID+": Name: "+name);
            
            while(true){
                //Receive data from mobile, forward to associated desktop's queue.
                System.out.println("M"+thisID+": Waiting for new data");
                Object received=objIn.readObject();
                System.out.println("M"+thisID+": Received data");
                //if exit command received, close socket/thread.
                if(received.toString().equals("exit")){
                    socket.close();
                    System.out.println("M"+thisID+": Exiting");
                    objIn.close();
                    objOut.close();
                    return;
                }else{
                    ObjectServer.getDesktop(code).enqueue(name, received);
                    System.out.println("queued");
                    objOut.writeObject("Received");
                }
            }
        }catch(Exception e){
            System.out.println("error: "+e);
            return;
        }
    }
}
