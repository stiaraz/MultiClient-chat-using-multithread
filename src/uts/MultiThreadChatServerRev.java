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

public class MultiThreadChatServerRev {
    private static ServerSocket serverSocket = null;
  // The client socket.
  private static Socket clientSocket = null;
  

  // This chat server can accept up to maxClientsCount clients' connections.
  private static final int maxClientsCount = 20;
  private static final clientThread[] threads = new clientThread[maxClientsCount]; //create array of object berjumlah 10 buah array sesuai deklarasi diatas

  public static void main(String args[]) {

    // The default port number.
    int portNumber = 2222;
    int count = 0;
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
        clientSocket = serverSocket.accept(); //blocking
        int i = 0;
        for (i = 0; i < maxClientsCount; i++) {
          if (threads[i] == null) { //ketika belum ada isinya maka create sebuah thread 
              count++;
              (threads[i] = new clientThread(clientSocket, threads)).start(); //passing parameter berupa socket dan pointer untuk satu semua thread agar bisa berhubungan
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
class clientThreads extends Thread {

  private DataInputStream is = null;
  private PrintStream os = null;
  private Socket clientSocket = null;
  private final clientThreads[] threads;
  private int maxClientsCount;
  private String id; //bikin id untuk masing-masing thread kalau misal mau chat antar thread
  private String[] groupId = new String[100]; //bikin id untuk masing-masing thread kalau join ke group tertentu
  private int countGroup; //jumlah grup terdaftar
  
  public clientThreads(Socket clientSocket, clientThreads[] threads) {
    this.clientSocket = clientSocket;
    this.threads = threads;
    maxClientsCount = threads.length;
  }

  public void run() {
    int maxClientsCount = this.maxClientsCount;
    clientThreads[] threads = this.threads; //ngisi array dengan thread

    try {
      /*
       * Create input and output streams for this client.
       */
      is = new DataInputStream(clientSocket.getInputStream());
      os = new PrintStream(clientSocket.getOutputStream());
//      os.println("Enter your name.");
      String name = is.readLine().trim();
      int checkIdd = 0;
      do{
        checkIdd = 0;
        for(int i = 0; i<maxClientsCount; i++){
            if(threads[i].id!=null){
                if(name.equals(threads[i].id)){
                    checkIdd = 1;
                }                
            }
            else{
                break;
            }
        }
        if(checkIdd==1){
            os.println("Ur username has been taken.\nPlease type another username : ");
            name = is.readLine().trim();
        }
        else {
            this.id = name;
            this.countGroup = -1;
            os.println("Hello " + name
                + " to our chat room.");
            os.println("To leave enter /quit in a new line");
            for (int i = 0; i < maxClientsCount; i++) {
              if (threads[i] != null && threads[i] != this) { //ketika user baru masuk, kirim nama user yg baru masuk ke masing-masing thread dengan cara looping dari array
                threads[i].os.println("*** A new user " + name
                    + " entered the chat room !!! ***");
              }
      } 
        }
     }while(checkIdd == 1);
     
      
      while (true) {
        String line = is.readLine();
        String newLine = "";
        if (line.startsWith("/quit")) {
          break;
        }

        String[] split = line.split("\\s+");

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
        else if(split[0].equals("@pm")){
            int checkPm = 0;
            for(int i = 2; i<split.length; i++){                
                newLine = newLine.concat(split[i]);
                newLine = newLine.concat(" ");
            }
            for (int i = 0; i < maxClientsCount; i++) {
                if (threads[i] != null && split[1].equals(threads[i].id)) {
                  checkPm = 1;
                  threads[i].os.println("private <" + name + "> " + newLine); //print input thread tujuan
                  continue;
                }
            }
            if(checkPm == 1){
                this.os.println("> Your message sent!");
            }
            else{
                this.os.println("> username cannot be found!");
            }
        }
        else if(split[0].equals("@join")){
            for(int i =0; i<100; i++){
                if(this.groupId[i]==null){
                    this.groupId[i] = split[1];
                    this.os.println("> Join successfull!");
                    break;
                }
            }
            for (int k = 0; k < maxClientsCount; k++) {
                if (threads[k] != null) {
                    for(int l = 0; l<threads[k].groupId.length; l++ ){
                        if(split[1].equals(threads[k].groupId[l])){
                            threads[k].os.println("<" + split[1] + "> " + this.id + " join group!");    
                        }
                    }
                }
            }
        }
        else if(split[0].equals("@gr")){
            int checkGroup = 0;
            for(int i = 2; i<split.length; i++){                
                newLine = newLine.concat(split[i]);
                newLine = newLine.concat(" ");
            }
            for (int a = 0; a < this.groupId.length; a++){
                if( split[1].equals(this.groupId[a])){
                    System.out.println("masuk");
                    for (int i = 0; i < maxClientsCount; i++) {
                        if (threads[i] != null) {
                            for(int j = 0; j<threads[i].groupId.length; j++ ){
                                if(split[1].equals(threads[i].groupId[j])){
                                    threads[i].os.println("group " + split[1] + " <" + name + "> " + newLine);
                                    checkGroup = 1;
                                }
                            }
                        }
                    }
                }
            }
            if(checkGroup == 0){
                this.os.println("<" + split[1] + "> u'r not join this group!");
            }

        }
        else if(split[0].equals("@kick")){
            for (int i = 0; i < maxClientsCount; i++) {
                if (threads[i] != null && split[1].equals(threads[i].id)) {
                    for(int j = 0; j < threads[i].groupId.length; j++){
                        if(split[2].equals(threads[i].groupId[j])){
                            threads[i].groupId[j] = null;
                            threads[i].os.println("you r kicked by <" + name + "> from group " + split[2]);
                            this.os.println("you r kicking <" + threads[i].id + "> from group " +split[2]);
                        }
                    }
                }
            }
        }
        else if(split[0].equals("@info")){
            int checkId = 0;            
            for(int i = 0; i<maxClientsCount; i++){
                if(threads[i]!=null){
                    if(split[1].equals(threads[i].id)){
                        checkId = 1;
                        this.os.println("Information about " + threads[i].id);
                        this.os.println("Username : " + threads[i].id);
                        this.os.println("----Group----");
                        for(int j=0; j<threads[i].groupId.length; j++){
                            if(threads[i].groupId[j] != null){
                                this.os.println("- " + threads[i].groupId[j]);                            
                            }
                        }
                        break;
                    }
                    else{
                        
                    }
                }
                else {
                    break;
                }
            }
            if(checkId == 0){
                this.os.println("> username cannot be found!");
            }
            else{
                this.os.println("Information Done");
            }
        }
        else {
            for (int i = 0; i < maxClientsCount; i++) {
              if (threads[i] != null) {
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
