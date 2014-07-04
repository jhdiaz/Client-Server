//Author: Jeffrey Diaz
//Partner: James Berger


import java.io.*;
import java.net.*;

public class Client {
	public static boolean requestMode = false;
	public static boolean chatMode = false;
	public static boolean serverStarted = false;
	public static boolean online = false;
	public static int n = 0;
	public static int myPort = 0;
	public static int connectedPort = 0;
	public static String messageFromServer = null;
	public static String chatterName = "";
	public static String chatterAddress = "";
	public static String chatterPort = "";
	public static String connectedServer = "";
	public static String myName = "";
	public static String myAddress = "";
	public static String mode = "offline";

	public static void main(String[] args){
		try{
			boolean serverStarted = false;
			ServerSocket serverSocket0;
			Listener listener = null;
			Thread t0;

			while(!serverStarted){
				int port = getPort(n);
				try{
					serverSocket0 = new ServerSocket(port);
					listener = new Listener(serverSocket0);
					t0 = new Thread(listener);
					t0.start();
					myPort = port;
					System.out.println("Client's Listener was started on port "+Integer.toString(myPort)+".");
					serverStarted = true;
					n = 0;
				}
				catch(IOException e){
					System.out.println("Could not start client's Listener on port "+Integer.toString(port)+".");
					n++;
				}
			}

			System.out.print(mode+"> ");
			myAddress = InetAddress.getLocalHost().toString().split("/")[0];
			InputStreamReader reader = new InputStreamReader(System.in);
			BufferedReader input = new BufferedReader(reader);
			String line = "";

			while((line = input.readLine()) != null){
				if(chatMode){					
					String msg = line;
					if(msg.equals("quit")){
						System.out.println("You have left the chat session.");
						chatMode = false;
						mode = "online:"+myName;
						
						try{
							Socket socket = new Socket(connectedServer, connectedPort);
							OutputStream os = socket.getOutputStream();
							os.write(("quit-chat"+" "+myName+" "+chatterName).getBytes());
							os.close();
							socket.close();
						}
						catch(IOException e){
							System.out.println("Could not send quit request to server for update.");
						}

						//Send that I quit to other chatter.
						Socket chatterSocket = new Socket(chatterAddress, Integer.parseInt(chatterPort));
						OutputStream chatterOS = chatterSocket.getOutputStream();
						chatterOS.write("chatter-quit".getBytes());
						chatterOS.close();
						chatterSocket.close();
					}
					else if(msg.equals("")){
					}
					else{
						Socket socket = new Socket(chatterAddress, Integer.parseInt(chatterPort));
						OutputStream os = socket.getOutputStream();
						os.write(msg.getBytes());
						os.close();
						socket.close();
					}
				}
				else{
					String[] arguments = line.split(" ");

					if(requestMode){
						if(line.equals("yes")){
							String requesterName = messageFromServer.split(" ")[1];
							String requesterAddress = messageFromServer.split(" ")[2];
							String requesterPort = messageFromServer.split(" ")[3];
						
							Socket socket = new Socket(connectedServer, connectedPort);
							OutputStream os = socket.getOutputStream();
							os.write(("yes"+" "+myName+" "+requesterName+" "+requesterAddress+" "+requesterPort).getBytes());
							os.close();
							socket.close();

							mode = myName+":inChatWith:"+requesterName;
							chatMode = true;
							chatterName = requesterName;
							chatterAddress = requesterAddress;
							chatterPort = requesterPort;
							requestMode = false;
							messageFromServer = null;
							System.out.println("You are now chatting with '"+chatterName+"'.");
						}
						else if(line.equals("no")){
							System.out.println("Denied chat request.");

							String requesterName = messageFromServer.split(" ")[1];
							String requesterAddress = messageFromServer.split(" ")[2];
							String requesterPort = messageFromServer.split(" ")[3];

							Socket socket = new Socket(connectedServer, connectedPort);
							OutputStream os = socket.getOutputStream();
							os.write(("no"+" "+requesterName+" "+requesterAddress+" "+requesterPort).getBytes());
							os.close();
							socket.close();
							requestMode = false;
							messageFromServer = null;
						}
						else{
							System.out.println("yes/no");
						}
						line = "";
					}
					else{
						if(arguments.length<=1){
							if(line.equals("exit")){
								listener.kill();
								System.out.println("myaddress = "+myAddress);
								Socket mySocket = new Socket(myAddress, myPort);
								OutputStream os = mySocket.getOutputStream();
								os.write(1);
								os.close();
								mySocket.close();

								/*						
								if(serverStarted){
									Socket socket = new Socket(myAddress, connectedPort);
									OutputStream os1 = socket.getOutputStream();
									os1.write("kill-server".getBytes());
									os1.close();
									socket.close();
								}
								*/

								if(online){
									Socket socket = new Socket(connectedServer, connectedPort);
									OutputStream os1 = socket.getOutputStream();
									os1.write(("offline-request"+" "+myName).getBytes());
									os.close();
									socket.close();
									online = false;
									mode = "offline";
								}

								System.out.println("Program terminated.");
								break;
							}
							/*
							else if(line.equals("start-server")){
								try{
									int port = getPort(n);
									n++;
									ServerSocket serverSocket1 = new ServerSocket(port);
									Thread t1 = new Thread(new Server(serverSocket1));
									t1.start();
									serverStarted = true;
									mode = "server";
									System.out.println("Server has been started on '"+myAddress+"' with the port '"+Integer.toString(port)+"'.");
								}
								catch(IOException e){
									System.err.println("IOException when trying to start server.");
								}
							}
							*/
							else if(line.equals("go-offline")){
								if(online){
									Socket socket = new Socket(connectedServer, connectedPort);
									OutputStream os = socket.getOutputStream();
									os.write(("offline-request"+" "+myName).getBytes());
									os.close();
									socket.close();
									online = false;
									mode = "offline";
									System.out.println("You are now offline.");
								}
								else{
									System.out.println("You are already offline.");
								}
							}
							else if(line.equals("")){

							}
							else{
								System.err.println("Invalid command.");
							}
						}
						else if(arguments.length==2){
							if(arguments[0].equals("connect-to")){
								if(arguments[1].equals(myName)){
									System.out.println("You cannot chat with yourself.");
								}
								else if(online){
									String userIWantToChat = arguments[1];
									Socket socket = new Socket(connectedServer, connectedPort);
									OutputStream os = socket.getOutputStream();
									os.write(("connect-request"+" "+myName+" "+userIWantToChat).getBytes());
									os.close();
									socket.close();							

									System.out.print("Attempting to connect to: "+userIWantToChat+"...");

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

									if(messageFromServer.equals("otherUserIsAlreadyConnected")){
										System.out.println("The user '"+userIWantToChat+"' is already connected to someone else.");
									}
									else if(messageFromServer.equals("no")){
										System.out.println("Your request has been denied.");
									}
									else if(messageFromServer.equals("noSuchUserOnline")){
										System.out.println("No such user is online");
									}
									else if(messageFromServer.split(" ")[0].equals("success-connect")){
										mode = myName+":inChatWith:"+userIWantToChat;
										chatMode = true;
										chatterName = userIWantToChat;
										chatterAddress = messageFromServer.split(" ")[1];
										chatterPort = messageFromServer.split(" ")[2];
										System.out.println("You are now chatting with '"+chatterName+"'.");
									}
									else{
										System.out.println("Connect-to failed. messageFromServer ="+messageFromServer);
									}
									messageFromServer = null;
								}
								else{
									System.err.println("You are not registered with a server.");
								}
							}
							else{
								System.err.println("Invalid command.");
							}
						}
						else if(arguments.length==3){
							if(arguments[0].equals("go-online") && !online){
								try{
									System.out.print("Attempting to go online...");
									String server = arguments[1];
									String username = arguments[2];
									boolean successful = false;
									n = 0;

									while(!successful){
										int port = getPort(n);
										try{
											Socket socket = new Socket(server, port);

											if(socket.isClosed()){
												throw new SocketException();
											}

											OutputStream os = socket.getOutputStream();
											os.write(("online-request"+" "+username+" "+myAddress+" "+Integer.toString(myPort)).getBytes());
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
												//TODO est this
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
									}
								}
								catch(IOException e){
									System.err.println("Could not go online.");
								}
							}
							else if(arguments[0].equals("go-online") && online){
								System.err.println("You are already online.");
							}
							else{
								System.err.println("invalid command.");
							}
						}
					}
				}
				Thread.sleep(1);
				System.out.print(mode+"> ");
			}
		}
		catch(IOException e){
			System.err.println("IOException in Client class.");
		}
		catch(InterruptedException e){
			System.err.println("InterruptedException in Client class.");
		}
	}

	public static int getPort(int n){
		int port = (50000 + 24) + 128 * n;
		return port;
	}

	private static class Listener implements Runnable{
		private ServerSocket serverSocket;
		private boolean killed = false;

		public Listener(ServerSocket serverSocket){
			this.serverSocket = serverSocket;
		}

		public void run(){
			try{
				while(!killed){
					Socket clientSocket = serverSocket.accept();
					InputStream is = clientSocket.getInputStream();
					InputStreamReader reader = new InputStreamReader(is);
					BufferedReader br = new BufferedReader(reader);
					String messageRecieved = br.readLine();
					messageFromServer = messageRecieved;
					String[] messages = messageRecieved.split(" ");
	
					if(chatMode){
						if(messages.length==1 && messages[0].equals("chatter-quit")){
							System.out.println("The user '"+chatterName+"' has quit the chat.");
							chatMode = false;
							mode = "online:"+myName;
						}
						else{
							System.out.println("Recieved message from '"+chatterName+"': "+messageFromServer);
							System.out.print(mode+"> ");
						}
						messageFromServer = null;
						
					}
					else{
						if(messages.length == 4){
							if(messages[0].equals("chatRequested")){
								requestMode = true;
								System.out.println("You have recieved a chat request from '"+messages[1]+"'");
								System.out.println("Do you accept this request?: yes/no");
							}
							else if(messages[0].equals("online-request")){
								//System.out.println("I AM NOT A SERVER.");
								String senderAddress = messages[2];
								int senderPort = Integer.parseInt(messages[3]);
								Socket socket = new Socket(senderAddress, senderPort);
								OutputStream os = socket.getOutputStream();
								os.write("not-server".getBytes());
								os.close();
								socket.close();
							}
						}
					}
				}
			}
			catch(IOException e){
				System.err.println("IOException in Listener.");
			}
		}

		public void kill(){
			this.killed = true;
		}
	}
}
