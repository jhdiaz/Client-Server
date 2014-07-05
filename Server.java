//Author: Jeffrey Diaz

import java.io.*;
import java.net.*;

public class Server implements Runnable{
	private ServerSocket serverSocket;
	private int serverPort;

	public Server(ServerSocket serverSocket, int serverPort){
		this.serverSocket = serverSocket;
		this.serverPort = serverPort;
	}

	public void run(){
		try{
			int initialSize = 30;
			String[] users = new String[initialSize];
			int top = 0;
			String myAddress = InetAddress.getLocalHost().toString().split("/")[0];

			//initialize array
			for(int i=0;i<users.length;i++){
				users[i] = "";
			}

			while(true){
				System.out.println("Server is running at '"+myAddress+"' on the port '"+Integer.toString(serverPort)+"'.");
				Socket clientSocket = serverSocket.accept();
				String message = "";
				InputStream inputStream = clientSocket.getInputStream();
				InputStreamReader isr = new InputStreamReader(inputStream);
				BufferedReader br = new BufferedReader(isr);
				message = br.readLine();
				String[] arguments = message.split(" ");
				inputStream.close();
				clientSocket.close();

				System.out.println("Message recieved in server: "+message);

				if(arguments.length<=1){
					if(message == null || message.equals("kill-server")){
						System.out.println("Killing server.");
						break;
					}
				}
				else if(arguments.length==2){
					if(arguments[0].equals("offline-request")){
						String name = arguments[1];
						int index = 0;
						System.out.println("User '"+name+"' has asked to go offline.");
						
						for(int i=0;i<top;i++){
							if(users[i].split(":")[0].equals(name)){
								index = i;
								top--;
								users[i] = "";
							}
						}
						
						//sort array so that empty strings go after names
						for(int i=0;i<users.length;i++){
							for(int j=0;j<users.length-1;j++){
								if(users[j].equals("")){
									users[j] = users[j+1];
									users[j+1] = "";
								}
							}
						}
						
						System.out.println("PRINTING ARRAY. top = "+Integer.toString(top));
						//print array
						for(int i=0;i<users.length;i++){
							System.out.println(users[i]);
						}
						
					}
				}
				else if(arguments.length==3){
					if(arguments[0].equals("connect-request")){
						System.out.println("Recieved request to connect from: "+arguments[1]);

						String requester = arguments[1];
						String requestee = arguments[2];
						boolean inArray = false;
						String requesteeMode = "";					
						boolean canConnect = false;
						int requesteeIndex = 0;
						int requesterIndex = 0;

                                                for(int i=0;i<top;i++){
                                                        if(requester.equals(users[i].split(":")[0])){
                                                                requesterIndex = i;
                                                                break;
                                                        }
                                                }

                                                String requesterAddress =  users[requesterIndex].split(":")[1];
						String requesterPort =  users[requesterIndex].split(":")[2];

						for(int i=0;i<top;i++){
							if(requestee.equals(users[i].split(":")[0])){
								inArray = true;
								requesteeIndex = i;
								requesteeMode = users[i].split(":")[3];
								if(requesteeMode.equals("notConnected")){
									canConnect = true;
								}
								break;
							}
						}

						if(inArray){
							if(canConnect){
								String requesteeAddress = users[requesteeIndex].split(":")[1];
								String requesteePort = users[requesteeIndex].split(":")[2];

								Sender sender = new Sender("chatRequested"+" "+requester+" "+requesterAddress+" "+requesterPort, requesteeAddress, requesteePort);
								Thread t1 = new Thread(sender);
								t1.start();
							}
							else{
								Sender sender = new Sender("otherUserIsAlreadyConnected", requesterAddress, requesterPort);
								Thread t1 = new Thread(sender);
								t1.start();
							}
						}
						else{
                                                                Sender sender = new Sender("noSuchUserOnline", requesterAddress, requesterPort);
                                                                Thread t1 = new Thread(sender);
                                                                t1.start();

						}
					}
					else if(arguments[0].equals("quit-chat")){
						System.out.println("The user '"+arguments[1]+"' has asked to quit the chat.");

						String quitterName = arguments[1];
						String otherChatter = arguments[2];
						int quitterIndex = 0;
						int otherIndex = 0;
						String quitterAddress = "";
						String quitterPort = "";
						String otherAddress = "";
						String otherPort = "";

						for(int i=0;i<top;i++){
							if(quitterName.equals(users[i].split(":")[0])){
								quitterIndex = i;
								break;
							}
						}

						for(int i=0;i<top;i++){
							if(otherChatter.equals(users[i].split(":")[0])){
								otherIndex = i;
								break;
							}
						}

						quitterAddress = users[quitterIndex].split(":")[1];
						otherAddress = users[otherIndex].split(":")[1];
						quitterPort = users[quitterIndex].split(":")[2];
						otherPort = users[otherIndex].split(":")[2];

						String quitterTemp = users[quitterIndex].split(":")[0]+":"+users[quitterIndex].split(":")[1]+":"+users[quitterIndex].split(":")[2]+":";
						users[quitterIndex] = quitterTemp+"notConnected";

						String otherTemp = users[otherIndex].split(":")[0]+":"+users[otherIndex].split(":")[1]+":"+users[otherIndex].split(":")[2]+":";
						users[otherIndex] = otherTemp+"notConnected";
					}
				}
				else if(arguments.length==4){
					if(arguments[0].equals("no")){
						Sender sender = new Sender("no", message.split(" ")[2], message.split(" ")[3]);
						Thread t1 = new Thread(sender);
						t1.start();
					}
					else if(arguments[0].equals("online-request")){
						System.out.println("Recieved request to go online.");
						String usernameWanted = arguments[1];
						String senderAddress = arguments[2];
						String senderPort = arguments[3];

						boolean inArray = false;
						for(int i=0;i<top;i++){
							if(usernameWanted.equals(users[i].split(":")[0])){
								inArray = true;
								break;
							}
						}

						//Name is registered, user cannot go online.
						if(inArray){
							Sender sender = new Sender("fail", senderAddress, senderPort);
							Thread t0 = new Thread(sender);
							t0.start();
						}
						//Name is not registered, user can go online.
						else{							
							if(top<users.length){
								users[top] = usernameWanted+":"+senderAddress+":"+senderPort+":"+"notConnected";
								top++;
							}
							//User array is out of space and we will be doubling its size.
							else{
								initialSize *= 2;
								String[] temp = new String[initialSize];
								for(int i=0;i<users.length;i++){
									temp[i] = users[i];
								}
								users = temp;
								users[top] = usernameWanted+":"+senderAddress+":"+senderPort+":"+"notConnected";
								top++;
							}

							System.out.println("senderAddress = "+senderAddress+", senderPort = "+senderPort);

							Sender sender = new Sender("success-online"+" "+myAddress+" "+Integer.toString(serverPort), senderAddress, senderPort);
							Thread t0 = new Thread(sender);
							t0.start();
						}
					}
				}
	                        else if(arguments.length==5){
                                                if(arguments[0].equals("yes")){
                                                        String requestee = arguments[1]; //requestee
                                                        String requester = arguments[2]; //requester
							String requesterAddress = arguments[3];
							String requesterPort = arguments[4];
							int requesteeIndex = 0;
							int requesterIndex = 0;
							String requesteeAddress = "";
							String requesteePort = "";

							//System.out.println("First requester port is: "+arguments[4]);

                                                        for(int i=0;i<top;i++){
                                                                if(requestee.equals(users[i].split(":")[0])){
                                                                        String temp = users[i].split(":")[0]+":"+users[i].split(":")[1]+":"+users[i].split(":")[2]+":";
                                                                        users[i] = temp+"connected";
									requesteeIndex = i;
                                                                        break;
                                                                }
                                                        }

                                                        for(int i=0;i<top;i++){
                                                                if(requester.equals(users[i].split(":")[0])){
                                                                        String temp = users[i].split(":")[0]+":"+users[i].split(":")[1]+":"+users[i].split(":")[2]+":";
                                                                        users[i] = temp+"connected";
									requesterIndex = i;
									break;
                                                                }
                                                        }

							requesteeAddress = users[requesteeIndex].split(":")[1];
							requesteePort = users[requesteeIndex].split(":")[2];
							requesterAddress = users[requesterIndex].split(":")[1];
							requesterPort = users[requesterIndex].split(":")[2];

							System.out.println("requesteeAddress = "+requesteeAddress+", requesterAddress ="+requesterAddress);
							System.out.println("requesteePort = "+requesteePort+", requesterPort ="+requesterPort);

                                                        Sender sender = new Sender("success-connect"+" "+requesteeAddress+" "+requesteePort, requesterAddress, requesterPort);
                                                        Thread t1 = new Thread(sender);
                                                        t1.start();
                                                }
                                }
				clientSocket.close();
			}
			serverSocket.close();
		}
		catch(IOException e){
			System.err.println("IOException in server");
		}
	}

	//Used to send responses back to clients.
	private class Sender implements Runnable{
		String messageToSend;
		String senderAddress;
		String senderPort;

		public Sender(String messageToSend, String senderAddress, String senderPort){
			this.messageToSend = messageToSend;
			this.senderAddress = senderAddress;
			this.senderPort = senderPort;
		}

		public void run(){
			try{
				Socket socket = new Socket(senderAddress, Integer.parseInt(senderPort));
				OutputStream os = socket.getOutputStream();
				os.write(messageToSend.getBytes());
				os.close();
				socket.close();
			}
			catch (IOException e){
				System.err.println("IOException in Sender.");
			}
		}
	}
}
