package objectserver;

import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
public class DesktopConnection extends Thread{
    //Socket for this connection
    Socket socket;
    //ID of this thread instance
    static int ID = 0;
    int thisID=0;
    //Read from socket
    //Use to check if socket has data available to be read
    InputStreamReader input;
    //Write to socket
    ObjectOutputStream objOut;
    ObjectInputStream objIn;
    //Event key this desktop is related to
    String code = "";
    //Queue of encoded images to be written to desktop
    //LinkedList<Object> queue = new LinkedList();
    ConcurrentLinkedQueue<Tuple> queue = new ConcurrentLinkedQueue();
    public DesktopConnection(Socket connection, ObjectInputStream objIn) throws IOException{
        socket=connection;
        thisID=++ID;
        
        //Display IP, initialize socket IO
        input=new InputStreamReader(socket.getInputStream());
        objOut=new ObjectOutputStream(socket.getOutputStream());
        objOut.flush();
        //objIn = new ObjectInputStream(socket.getInputStream());
        this.objIn=objIn;
        System.out.println("D"+thisID+":"+socket.getInetAddress());
    }
    
    //Add an image encoded as text to queue
    public void enqueue(String name, Object item){
        //Add name/image pair to queue
        queue.add(new Tuple(name, item));
    }
    
    @Override
    public void run(){
        boolean validated = false;
        boolean exiting=false;
        try{
            //Validation loop, check against eventsTable in server and add 
            //The pair (code, this thread instance) when unique one is received
            while(!validated){
                System.out.println("D"+thisID+": Waiting for code");
                //receive code
                code = objIn.readObject().toString();
                System.out.println("D"+thisID+": Received code "+code);
                //if code is unique, add to eventsTable and notify desktop
                //else, inform desktop that code was not unique.
                if(ObjectServer.addConnection(code, this)){
                    System.out.println("D"+thisID+": Code "+code+" successfully registered.");
                    validated=true;
                    objOut.writeObject("Validated");
                }else{
                    System.out.println("D"+thisID+": Code "+code+" already exists.");
                    objOut.writeObject("Invalid");
                }
                objOut.flush();
            }
            //After validated, begin main loop
            while(true){
                //if exit command received, delete from table and begin emptying queue then notify when done.
                //otherwise, continue receiving photos and sending. 
                if(exiting){
                    while(!queue.isEmpty()){
                        processNext();
                    }
                    objOut.writeObject("safe to exit");
                    objOut.flush();
                    objOut.close();
                    objIn.close();
                    socket.close();
                    return;
                }else{
                    //check if a line is waiting in socket. If nothing, then continue queue operations.
                    if(input.ready()){
                        //if desktop sends the exit command, delete it from the 
                        //eventsTable and wait for queue to finish.
                        if(objIn.readObject().toString().equals("exit")){
                            ObjectServer.deleteConnection(code);
                            System.out.println("D"+thisID+": exiting");
                            exiting=true;
                        }
                    }
                    //if items exist in queue, send them to the desktop.
                    if(!queue.isEmpty()){
                        processNext();
                    }
                }
            }
        }catch(Exception e){
            //If error, print error, remove connection from table, and exit thread.
            System.out.println("error: "+e);
            ObjectServer.deleteConnection(code);
            return;
        }
    }
    
    public void processNext() throws IOException{
        //Retrieve next item from queue
        Tuple data = queue.remove();
        System.out.println("D"+thisID+": received an object");
        //Send name to server
        String name=data.getName();
        System.out.println("D"+thisID+": sent name \""+name+"\"");
        objOut.writeObject(name);
        objOut.flush();
        //Send image to server
        objOut.writeObject(data.getData());
        objOut.flush();
        System.out.println("D"+thisID+": sent item to desktop");
    }
}

//Holds an image and the name of the user that took the image
class Tuple{
    private String name;
    private Object data;
    public Tuple(String name, Object data){
        this.name=name;
        this.data=data;
    }
    public String getName(){
        return name;
    }
    public Object getData(){
        return data;
    }
}