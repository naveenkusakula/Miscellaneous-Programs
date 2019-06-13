import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
	private Scanner scanner;
/*Authentication method to authenticate user by creating a new connection using
 * Socket to establish connection with server program and send request to server to authenticate user 
 * if authenticted cookie is returned if not empty cookie is returned
 * #9 and #esc are implemented to exit and #9 to call NOOP opertaion function  
 */
	String auth(String serverName, int port) {
		String cookie = "";
		String usrname = "";
		String pass = "";
		try {
			Socket client = new Socket(serverName, port);
			PrintWriter out = new PrintWriter(client.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

			scanner = new Scanner(System.in);
			System.out.println("Enter #9 to get authentication status or provide below details to login\n"
					+ "Enter #esc to terminate the client");
			System.out.println("Enter Username:");
			usrname = scanner.nextLine();
			if (usrname.equals("#9")) {
				// System.out.print("in");
				client.close();
				noop(serverName, port, cookie);
				return cookie;
			} else if (usrname.equals("#esc")) {
				client.close();
				out.close();
				in.close();
				System.out.print("Disconnected Sucessfully from the server");
				return "#terminateclient#";
			}

			System.out.println("Enter Password:");
			pass = scanner.nextLine();
			if (pass.equals("#9")) {
				// System.out.print("inpass");
				client.close();
				noop(serverName, port, cookie);
				return cookie;
			} else if (pass.equals("#esc")) {
				client.close();
				out.close();
				in.close();
				System.out.print("Disconnected Sucessfully from the server");
				return "#terminateclient#";
			}

			out.println("AUTH " + usrname + " 3901chat/1.0\r\nPassword: " + pass + "\r\n\r\n");

			String line = "";

			while ((line = in.readLine()) != null) {
				// System.out.println("FromServer:" + line);
				if (line.substring(13, 16).equals("200")) {

					cookie = in.readLine();
					cookie = cookie.substring(12);
					// System.out.print(cookie);
					break;
				} else {
					System.out.println(line.substring(17) + ", please enter valid username/Password");
					break;
				}

			}
			out.close();
			in.close();
			client.close();
			return cookie;

		} catch (IOException e) {
			System.out.print("Couldn't open socket for connection might be invalid host/portnumber");
			System.exit(1);
		}
		return cookie;
	}
/*
 * Below function sends NOOP request to sever and get the status at different states of program using 
 * if request response is not equal to 200-success then error message is displayed
 * and message is returned if any messages are returned in response   
 */
	private String noop(String serverName, int port, String cookie) {
		// TODO Auto-generated method stub
		PrintWriter out;
		String temp = "";
		try {
			Socket client = new Socket(serverName, port);
			out = new PrintWriter(client.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			out.println("NOOP NOOP 3901chat/1.0\r\nCookie: " + cookie + "\r\n\r\n");
			String line = "";
			while ((line = in.readLine()) != null) {
				// System.out.print(line);
				if (line.substring(13, 16).equals("200")) {
					while ((line = in.readLine()) != null) {
						// System.out.println("Success message: " + line);
						if (line.equals("")) {
							continue;
						}
						temp = temp + line + "\n";
					}
				} else if (line.substring(13, 16).equals("403")) {
					System.out.println(line.substring(17));
					break;
				}
			}
			out.close();
			in.close();
			client.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.print("Couldn't open socket for connection might be invalid host/portnumber");
			System.exit(1);
		}
		return temp;
	}
/*
 * Below function will send BYE request and logs out of the server if not error is displayed 
 */
	private void bye(String serverName, int port, String cookie) {
		// TODO Auto-generated method stub
		PrintWriter out;

		try {
			Socket client = new Socket(serverName, port);
			out = new PrintWriter(client.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			out.println("BYE BYE 3901chat/1.0\r\nCookie: " + cookie + "\r\n\r\n");
			String line = "";
			while ((line = in.readLine()) != null) {
				// System.out.print(line);
				if (line.substring(13, 16).equals("200")) {
					System.out.println("Logged out sucessfully");
					break;
				} else if (line.substring(13, 16).equals("403")) {
					System.out.println(line.substring(17));
					break;
				}
			}
			out.close();
			in.close();
			client.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.print("Couldn't open socket for connection might be invalid host/portnumber");
			System.exit(1);
		}

	}
/*
 * below method takes a valid room name and exit the given room by sending exit request   
 */
	private void exit(String serverName, int port, String validroom, String cookie) {
		// TODO Auto-generated method stub
		PrintWriter out;

		try {
			Socket client = new Socket(serverName, port);
			out = new PrintWriter(client.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

			out.println("Exit " + validroom + " 3901chat/1.0\r\nCookie: " + cookie + "\r\n\r\n");
			String line = "";
			while ((line = in.readLine()) != null) {
				// System.out.print(line);
				if (line.substring(13, 16).equals("200")) {
					while ((line = in.readLine()) != null) {
						System.out.println("Exited room " + validroom + " sucessfully");

					}
				} else if (line.substring(13, 16).equals("402")) {
					System.out.println(line.substring(17));
					break;
				}
			}
			out.close();
			in.close();
			client.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.print("Couldn't open socket for connection might be invalid host/portnumber");
			System.exit(1);
		}

	}
/*
 * Below method sends takes room and cookie as input and send request to server to get send message 
 * and also receives any response from server and retunrs it
 */
	private void say(String serverName, int port, String cookie, String validroom) {
		// TODO Auto-generated method stub
		PrintWriter out;

		try {
			Socket client = new Socket(serverName, port);
			out = new PrintWriter(client.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			Scanner sc = new Scanner(System.in);
			System.out.println("Enter message to be sent (Hit 'ENTER' twice to send / once for next line/paragraph ):");
			String msg = "";
			String ip;
			while (sc.hasNextLine()) {
				ip = sc.nextLine();
				if (ip.isEmpty()) {
					break;
				}
				msg += ip + "\n";
			}
			// System.out.print(msg.length());
			out.println("SAY " + validroom + " 3901chat/1.0\r\nCookie: " + cookie + "\r\nContent-Length: "
					+ msg.length() + "\r\n\r\n" + msg + "\r\n\r\n");
			String line = "";
			while ((line = in.readLine()) != null) {
				// System.out.print(line);
				if (line.substring(13, 16).equals("200")) {
					while ((line = in.readLine()) != null) {
						if (line.equals("")) {
							continue;
						}
						System.out.println(line);

					}
				} else {
					System.out.println(line);
					break;
				}
			}
			out.close();
			in.close();
			client.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.print("Couldn't open socket for connection might be invalid host/portnumber");
			System.exit(1);
		}

	}
/*
 * Below Method sends ENTER request to server with cookie a input and displays error if any 
 *   request is failed
 */
	private String enterroom(String serverName, int port, String cookie) {
		// TODO Auto-generated method stub
		PrintWriter out;
		String room = "";
		try {
			Socket client = new Socket(serverName, port);
			out = new PrintWriter(client.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			Scanner sc = new Scanner(System.in);
			System.out.println("Enter Room name");
			String roomname = sc.nextLine();
			roomname=roomname.trim();
			
			out.println("ENTER " + roomname + " 3901chat/1.0\r\nCookie: " + cookie + "\r\n\r\n");
			String line = "";
			while ((line = in.readLine()) != null) {
				// System.out.print(line);
				if (line.substring(13, 16).equals("200")) {
					while ((line = in.readLine()) != null) {
						System.out.println("Entered room " + roomname + " sucesfully");
						room = roomname;
						return room;
					}
				} else if (line.substring(13, 16).equals("402")) {
					System.out.println(line.substring(17));
					break;
				}
			}
			out.close();
			in.close();
			client.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.print("Couldn't open socket for connection might be invalid host/portnumber");
			System.exit(1);
		}catch(StringIndexOutOfBoundsException e)
		{
			System.out.println("Room name will not contain spaces");
		}
		return room;
	}
/*
 * Main function takes host name and port number and also maintains different states of the client program
 * using while loops and state storing variables  
 */
	public static void main(String[] args) {
		String serverName = "";
		int port = -1;
		try {
		if ((args.length == 4) && ((args[0].equals("-h") && args[2].equals("-p"))||(args[0].equals("-p") && args[2].equals("-h")))) {
			String arg1 = args[0];
			String arg2 = args[2];
			if (arg1.equals("-h") && arg2.equals("-p")) {
				serverName = args[1];
				port = Integer.parseInt(args[3]);
			} else if (arg1.equals("-p") && arg2.equals("-h")) {
				serverName = args[3];
				port = Integer.parseInt(args[1]);
			} 

		} else {
			System.err.println("Invalid arguments");
			System.exit(1);
		}
		}
		catch(NumberFormatException e)
		{
			System.err.println("Invalid arguments");
			System.exit(1);
		}

		String cookie = "";
		ChatClient n = new ChatClient();

		System.setProperty("java.net.preferIPv4Stack", "true");

		boolean auth = false;
		while (true) {

			cookie = n.auth(serverName, port);
			// System.out.println(cookie);
			if (cookie.equals("#terminateclient#")) {
				break;
			}
			if (!cookie.equals("")) {
				System.out.println("Logged in Sucessfully.....");
				auth = true;
			}
			while (auth) {

				System.out.println("Enter #1 to Enter Chatroom");
				System.out.println("Enter #2 to Logout");
				System.out.println("Enter #9 to get status of authentication (Noop)");
				Scanner sc = new Scanner(System.in);
				String tmp = sc.nextLine();
				if (tmp.equals("#1")) {

					String validroom = n.enterroom(serverName, port, cookie);

					while (!validroom.equals("")) {
						System.out.println("Enter #1 to Send & Receive messages");
						System.out.println("Enter #2 to Enter another chatroom");
						System.out.println("Enter #3 to Exit current chatroom");
						System.out.println("Enter #4 to Logout");
						System.out.println("Enter #9 to get messages");
						tmp = sc.nextLine();
						if (tmp.equals("#1")) {
							n.say(serverName, port, cookie, validroom);
						} else if (tmp.equals("#2")) {
							String temp = n.enterroom(serverName, port, cookie);
							if (!temp.equals("")) {
								validroom = temp;
								continue;
							}

						} else if (tmp.equals("#3")) {
							n.exit(serverName, port, validroom, cookie);
							validroom = "";
							break;
						} else if (tmp.equals("#4")) {
							n.bye(serverName, port, cookie);
							validroom = "";
							auth = false;
							break;
						} else if (tmp.equals("#9")) {
							String temp = n.noop(serverName, port, cookie);
							System.out.println(temp);
						}

					}
				} else if (tmp.equals("#2")) {
					System.out.println("in logout");
					n.bye(serverName, port, cookie);
					auth = false;

				} else if (tmp.equals("#9")) {
					String temp = n.noop(serverName, port, cookie);
					if (temp.isEmpty()) {
						System.out.println("User already authenticated");
					}
				}
			}

		}

		

	}

}
