//Author: Jeffrey Diaz

import java.net.*;
import java.io.*;

public class ServerStarter{
	public static int n = 0;
	public static boolean serverStarted = false;

	public static void main(String[] args){

		/*
		String rootServer = "";
		String myAddress = InetAddress.getLocalHost().toString().split("/")[0];
		System.out.println("MY address "+ myAddress);

		
		if(args.length==1){
			System.out.println("Root Server");
			rootServer = arguments[0];
		}
		else if(args.length>1){
			System.out.println("Wrong number of arguments.");
		}
		*/

		while(!serverStarted){
			int port = getPort(n);
			try{
				ServerSocket serverSocket = new ServerSocket(port);
				Server server = new Server(serverSocket, port);
				Thread thread = new Thread(server);
				thread.start();
				serverStarted = true;
				System.out.println("Server started on port "+Integer.toString(port)+".");

				/*
				if(!rootServer.equals("")){
					

					//********************************
				boolean successful = false;
				n = 0;

				while(!successful){
					int port = getPort(n);
					try{
						Socket socket = new Socket(rootServer, port);

						if(socket.isClosed()){
							throw new SocketException();
						}

						OutputStream os = socket.getOutputStream();
						os.write(("server-request"+" "+myAddress+" "+Integer.toString(myPort)).getBytes());
						os.close();
						socket.close();

						while(true){
							if(messageFromServer == null){
								System.out.print(".");
							}
							else{
								System.out.print("\n");
								break;
							}
							Thread.sleep(500);
						}

						if(messageFromServer.split(" ")[0].equals("success-online")){
							mode = "online:"+username;
							myName = username;
							online = true;
							connectedServer = messageFromServer.split(" ")[1];
							connectedPort = Integer.parseInt(messageFromServer.split(" ")[2]);
							System.out.println("You are now online.");
							System.out.println("The server you are connected to is at "+connectedServer+" "+connectedPort);
							successful = true;
						}
						else if(messageFromServer.split(" ")[0].equals("fail")){
							System.out.println("Username is in use.");
							successful = true;
						}
						else if(messageFromServer.split(" ")[0].equals("not-server")){
							System.out.println("Port had client on it, not server: "+Integer.toString(port));
							n++;
						}

						messageFromServer = null;
					}
					catch(SocketException e){
						System.out.println("No server on port '"+Integer.toString(port)+"'.");
						n++;
					}

					//*********************************
				}
				*/

			}
			catch(IOException e){
				System.out.println("Could not start server on port "+Integer.toString(port)+".");
				n++;
			}
		}
	}

	public static int getPort(int x){
		int port = (50000 + 24) + 128 * x;
		return port;
	}

}
