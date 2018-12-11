package client;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

//creates a client in which the constructor sets up a socket and starts listening for input
public class BankClient
{
	ClientConnection cc;
	Scanner console = null;
	String input = null;
    Socket s = null;
	private final String  menu =     "1. Login your account.(Enter EGN and account number) "
	 		+ "\n2. Check your account balance. "
	 		+ "\n3. Deposit money in account.(Enter a sum)"
	 		+ "\n4. Withdraw money from account.(Enter a sum)"
	 		+ "\n5. Transfer cash to another account(Enter EGN and account number)."
	 		+ "\n6. Create new account."
	 		+ "\n7. \"quit\" to quit ";
	
	//creates a client which connects to the server and waits for the user to input
	public static void main(String[] args)
	{
		new BankClient();
	}
	
	
	public BankClient()
	{
		  System.out.println("Hello and welcome to our bank!");
		  System.out.println(menu);
		
		try
		{
			//localhost because it is on this computer 4242 because we just chose it there are 65k ports we can use 
			s = new Socket("localhost", 4242);
			
			cc = new ClientConnection(s, this);
			cc.start();
		
		listenForInput();
		} catch(UnknownHostException e)
		{
			e.printStackTrace();
		} catch (IOException e )
		{
			e.printStackTrace();
		}
	}
	
	//gets input from console delays thread with 1 millisecond if it doesn't
	//if quit is entered stops reading
	public void listenForInput()
	{
		  console = new Scanner(System.in);

		while(true)
		{
			//slows down the thread
			while(!console.hasNextLine()) {
				try
				{
					//slows down thread
					Thread.sleep(1);
				} catch(InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			
			  	input = console.nextLine();
			  	//checks if a client is logged in if he is he is allowed to use certain options
			if(!cc.isLoggedIn())
			{
				if(input.equals("2") || input.equals("3") || input.equals("4") || input.equals("5"))
			{
					System.out.println("You haven't logged in to use that command.");
					input = "10";
				}
			}
			
			if(input.toLowerCase().equalsIgnoreCase("quit")) 
			{
				System.out.println("Exiting... Thank you for using our services.");
				 break;
			}
			//sends what the users option choice
			cc.sendStringToServer(input);
		}
	}
}
