package client;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientConnection extends Thread
{

	Socket s;
	DataInputStream din;
	DataOutputStream dout;
	//bool variable to check if client can work with certain options that need you to be logged in
	private boolean loggedIn = false;

	
	public boolean isLoggedIn() 
	{
		return loggedIn;
	}

	//constructor of the thread - gets the socket and the client from the Client class
	public ClientConnection (Socket socket, BankClient client)
	{
		s = socket;
		try
		{
			din = new DataInputStream(s.getInputStream());
			dout = new DataOutputStream(s.getOutputStream());
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	//It sends the data given to the server
	public void sendStringToServer(String text)
	{
		try
		{
			dout.writeUTF(text);
			dout.flush();
		} catch(IOException e)
		{
			e.printStackTrace();
			close();
		}
	}
	
	//reads from socket constantly with 1 millisecond per read if there is something to read it does if it doesn't it waits for the 1 millisecond 
	public void run()
	{
		try
		{
			din = new DataInputStream(s.getInputStream());
			dout = new DataOutputStream(s.getOutputStream());
			//this loop is always working because it is always reading new input
			while(true)
			{
				try
				{	
					//checks if there is anything to be read  - available() returns the number of incoming bytes
					while(din.available() == 0)
					{
						try
						{
							Thread.sleep(1);
						} catch(InterruptedException e)
						{
							e.printStackTrace();
						}
					}
					//reads from the sockets input stream
					String reply = din.readUTF();
					if(reply.equals("You have been logged in.") || reply.equals("Account created"))
						loggedIn = true;
					if(reply.equals("Such account does not exist."))
						loggedIn = false;
					if(reply.equals("Exiting... Thank you for using our services."))
						loggedIn = false;
	
					System.out.println(reply);
				} catch(IOException e)
				{
					e.printStackTrace();
				}
			}
		}catch(IOException e)
		{
			e.printStackTrace();
			close();
		}
	}
	
	//closes input and output streams and the socket
	public void close()
	{
		
		try {
			din.close();
			dout.close();
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}