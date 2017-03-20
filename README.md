# Chat
 ### Local or Public Access chat server based on pure Java.


 ## Summary
 
This project implements Java socket interaction protocol to allow network chat from multiple clients. The server script (ChatServer.java and ChatHandler.java) are at the core of this project, allowing connections from any source using socket interaction by managing received strings with multithreading. The server accepts commands from clients which can take action on the state of a client object or the server itself, depending on permissions. The project includes a functional interactive GUI to work with the written server script. 

 > In order to compile and test the GUI, it is necessary to open the project in a relatively new version of IntelliJ IDEA (free to download at https://www.jetbrains.com/idea/ ). If this is not possible, contact me for a recently built JAR artifact that includes the IntelliJ dependencies for testing.
 
