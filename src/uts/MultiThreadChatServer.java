/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uts;

/**
 *
 * @author Owner
 */
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author ayya
 */
public class MultiThreadChatServer {

  // The server socket.
  private static ServerSocket serverSocket = null;
  // The client socket.
  private static Socket clientSocket = null;

  // This chat server can accept up to maxClientsCount clients' connections.
  private static final int maxClientsCount = 20;
  private static final clientThread[] threads = new clientThread[maxClientsCount];

  public static void main(String args[]) {

    // The default port number.
    int portNumber = 2222;
    int count=0;
    if (args.length < 1) {
      System.out
          .println("Usage: java MultiThreadChatServer <portNumber>\n"
              + "Now using port number=" + portNumber);
    } else {
      portNumber = Integer.valueOf(args[0]).intValue();
    }

    /*
     * Open a server socket on the portNumber (default 2222). Note that we can
     * not choose a port less than 1023 if we are not privileged users (root).
     */
    try {
      serverSocket = new ServerSocket(portNumber);
    } catch (IOException e) {
      System.out.println(e);
    }

    /*
     * Create a client socket for each connection and pass it to a new client
     * thread.
     */
    while (true) {
      try {
        clientSocket = serverSocket.accept();
        int i = 0;
        for (i = 0; i < maxClientsCount; i++) {
          if (threads[i] == null) {
            (threads[i] = new clientThread(clientSocket, threads)).start();
            break;
          }
        }
        if (i == maxClientsCount) {
          PrintStream os = new PrintStream(clientSocket.getOutputStream());
          os.println("Server too busy. Try later.");
          os.close();
          clientSocket.close();
        }
      } catch (IOException e) {
        System.out.println(e);
      }
    }
  }
}
/*
 * The chat client thread. This client thread opens the input and the output
 * streams for a particular client, ask the client's name, informs all the
 * clients connected to the server about the fact that a new client has joined
 * the chat room, and as long as it receive data, echos that data back to all
 * other clients. When a client leaves the chat room this thread informs also
 * all the clients about that and terminates.
 */
class clientThread extends Thread {

  private DataInputStream is = null;
  private PrintStream os = null;
  private Socket clientSocket = null;
  private final clientThread[] threads;
  private int maxClientsCount;
  private String id;
  private String[] groupid= new String[100];
  private int countgr;

  public clientThread(Socket clientSocket, clientThread[] threads) {
    this.clientSocket = clientSocket;
    this.threads = threads;
    maxClientsCount = threads.length;
  }
  public class ScheduledTask extends TimerTask {
      Date now;
      public void run(){
          now = new Date();
          System.out.println("Time is: "+ now);
      }
  }
  
  public void run() {
    int maxClientsCount = this.maxClientsCount;
    clientThread[] threads = this.threads;

    try {
      /*
       * Create input and output streams for this client.
       */
      is = new DataInputStream(clientSocket.getInputStream());
      os = new PrintStream(clientSocket.getOutputStream());
      Timer time = new Timer();
      uts.ScheduledTask st = new uts.ScheduledTask();
      time.schedule(st, 0, 5000);
      //os.println("Enter your name.");
      for (int i = 0; i <= 5; i++) {
                os.println("Task Scheduler " + i);
                Thread.sleep(5000);
                
                if (i == 5) {
                        os.println("--Task Scheduler Closed--");
                }
        }
      String name = is.readLine().trim();
      this.id = name;
      this.countgr = -1;
      int cek=0;
      do{
          cek=0;
          for (int i = 0; i < maxClientsCount; i++) {
        if (threads[i] != null && threads[i] != this) {
          if(name.equals(threads[i].id)){
              cek=1;
        }
         }
          else{
                  break;
                  }
      }
          if(cek==1){
              os.println("Ur username has been taken.\nPlease type another username : ");
            name = is.readLine().trim();
          }
          else{
              this.id=name;
              this.countgr=-1;
              os.println("Hello " + name
                + " to our chat room.");
            os.println("To leave enter /quit in a new line");
            for(int i=0; i<maxClientsCount;i++){
                if (threads[i] != null && threads[i] != this) { //ketika user baru masuk, kirim nama user yg baru masuk ke masing-masing thread dengan cara looping dari array
                threads[i].os.println("*** A new user " + name
                    + " entered the chat room !!! ***");
            }
          }
          }
      }while(cek==1);
      
      
      while (true) {
        String line = is.readLine();
        String newLine = "";
        if (line.startsWith("/quit")) {
          break;
        }
        String at = "@";
        String[] msg = line.split("#");
        if(line.equals("@username")){
            this.os.println("<" + name + "> Your username is " + this.id);
        }
        else if(line.equals("@time")){
            LocalDateTime now = LocalDateTime.now();    
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm:ss");
            String formatDateTime = now.format(formatter);
            this.os.println("<" + name + "> Time is " + formatDateTime);
        }
        else if(line.equals("@day")){
            LocalDate localDate = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE");
            String formatDateTime = localDate.format(formatter);
            this.os.println("<" + name + "> Today is " + formatDateTime);
        }
        else if(msg[0].equals("@pm")){
            int cekpm=0;
            for(int i = 2; i<msg.length; i++){                
                newLine = newLine.concat(msg[i]);
                newLine = newLine.concat(" ");
            }
            for (int i = 0; i < maxClientsCount; i++) {
                if (threads[i] != null && msg[1].equals(threads[i].id)) {
                    cekpm=1;
                  threads[i].os.println("private <" + name + "> " + newLine); //print input thread tujuan
                  continue;
                }
            }
            if(cekpm==1){
                this.os.println("> Your message sent!");
            }
            else{
                this.os.println("> username cannot be found!");
            }
            
        }
        else if(msg[0].equals("@join")){
            for(int i =0; i<100; i++){
                if(this.groupid[i]==null){
                    this.groupid[i] = msg[1];
                    this.os.println("> Join successfull!");
                    break;
                }
            }
for (int k = 0; k < maxClientsCount; k++) {
                if (threads[k] != null) {
                    for(int l = 0; l<threads[k].groupid.length; l++ ){
                        if(msg[1].equals(threads[k].groupid[l])){
                            threads[k].os.println("<" + msg[1] + "> " + this.id + " join group!");    
                        }
                    }
                }
            }
        }
        else if(msg[0].equals("@gr")){
            int cekgr=0;
            for(int i = 2; i<msg.length; i++){                
                newLine = newLine.concat(msg[i]);
                newLine = newLine.concat(" ");
            }            
            for (int b  = 0; b < this.groupid.length;  b++) {
                if(msg[1].equals(this.groupid[b])){
                    System.out.println("in");
                    for(int i=0; i<maxClientsCount;i++){
                        if(threads[b] != null){
                            for(int j = 0; j<threads[i].groupid.length; j++ ){
                             if(msg[1].equals(threads[i].groupid[j])){
                            threads[i].os.println("group " + msg[1] + " <" + name + "> " + newLine);    
                            cekgr=1;
                        }   
                            }
                        }
                    }
                }
                
        }
            if(cekgr==0){
                this.os.println("<" + msg[1] + "> u'r not join this group!");
            }
        }
        /*else if(msg[0].equals("@br")){
            for(int i = 1; i<msg.length; i++){                
                newLine = newLine.concat(msg[i]);
                newLine = newLine.concat(" ");
            }
            for (int i = 0; i < maxClientsCount; i++) {
              if (threads[i] != null) {
                threads[i].os.println("broadcast <" + name + "> " + newLine); //print input ke masing-masing thread
              }
            }            
        }*/ 
        else if(msg[0].equals("@kick")){
            //int cekick=0;
            for (int i = 0; i < maxClientsCount; i++) {
                if (threads[i] != null && msg[1].equals(threads[i].id)) {
                    for(int j=0; j<threads[i].groupid.length; j++){
                        if(msg[2].equals(threads[i].groupid[j])){
                            threads[i].groupid[j] = null;
                            threads[i].os.println("you are kicked by <" + name + "> from group" + msg[2]);
                            //cekick=1;
                    this.os.println("you r kicking <" + threads[i].id + ">" +msg[2]);
                    //threads[i].clientSocket.close();
                    
                        }
                    }
                    
                }
            }
            
        }   
        else if(msg[0].equals("@info")){
            int cekid=0;
            for(int i=0; i<maxClientsCount; i++){
            if(threads[i]!=null){
                if(msg[1].equals(threads[i].id)){
                    cekid=1;
                    this.os.println("Information about " + threads[i].id);
                        this.os.println("Username : " + threads[i].id);
                        this.os.println("Group:");
                        for(int j=0; j<threads[i].groupid.length;j++){
                            if(threads[i].groupid[j]!=null){
                                this.os.println("- " + threads[i].groupid[j]);
                            }
                        }
                        break;
                }
                else{
                   
                }
            }
            else{
                break;
            }
        }
          if(cekid==0){
              this.os.println("> username cannot be found!");
          }
          else{
              this.os.println("Information Done");
          }
        }
        else {
            for(int i=0; i<maxClientsCount; i++){
                if(threads[i]!=null){
                  threads[i].os.println("broadcast <" + name + "> " + line); //print input ke masing-masing thread  
                }
            }
            
        }
      }
      for (int i = 0; i < maxClientsCount; i++) {
        if (threads[i] != null && threads[i] != this) {
          threads[i].os.println("*** The user " + name
              + " is leaving the chat room !!! ***");
        }
      }
      os.println("*** Bye " + name + " ***");

      /*
       * Clean up. Set the current thread variable to null so that a new client
       * could be accepted by the server.
       */
      for (int i = 0; i < maxClientsCount; i++) {
        if (threads[i] == this) {
          threads[i] = null;
        }
      }

      /*
       * Close the output stream, close the input stream, close the socket.
       */
      is.close();
      os.close();
      clientSocket.close();
    } catch (IOException e) {
    }
  }
  }
