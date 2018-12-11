package server;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class BankServer 
{
	
	ServerSocket ss;
	//used to store all the connections to send info to all clients at once - currently not in use
	ArrayList<ServerConnection> connections = new ArrayList<ServerConnection>();
	
	//creates a bank server which itself starts a server connection
	public static void main(String []args)
	{
		new BankServer();
	}
	
	public BankServer()
	{
		try
		{
			ss = new ServerSocket(4242);
			
			while(true)
			{
				Socket s = ss.accept();
				ServerConnection sc = new ServerConnection(s,this);
				sc.start();	
	
				connections.add(sc);
			}//while
		} catch(IOException e)
		{
			e.printStackTrace();
		}//catch
	}//Server
}//Class
