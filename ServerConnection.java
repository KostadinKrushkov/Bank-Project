package server;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

//TODO  fix loggedInAccount to work with only EGN and Number
//TODO 	create logout option and create account for current login


public class ServerConnection extends Thread
{
	//Files
	//readers, writers, input and output streams 
	private BufferedReader reader = null;
	private BufferedReader loginReader = null;
	private PrintWriter writer = null;
	private DataInputStream din;
	private DataOutputStream dout;
	//Socket and BankServer object instantiated in constructor
	private Socket socket;
	private BankServer server;
	//boolean variable to check if it okay to output
	private boolean print = true;
	//file name and path
	private final String fileName = "D:\\Java Programming\\My code\\BankProject\\data.txt";
	private final Path path = Paths.get("D:\\Java Programming\\My code\\BankProject\\data.txt");
	//yesNoMaybe = loggedIn - checks if user is logged in
	private boolean yesNoMaybe = false;
	//menu - every user option 
	private String menu;
	private List<String> list = new ArrayList<String>();
	//loggedInAccount holds the users info (login/created account)
	private String loggedInAccount = "";
	//EGNandID holds the users EGN and ID
	private String EGNandID = "";
	
	//creates Thread and instantiate all the objects for reading 
	//writing sending and receiving information from the server
	public ServerConnection(Socket socket, BankServer server)
	{
		super("ServerConnectionThread");
		this.socket = socket;
		this.server = server;
		try
		{
			loginReader = new BufferedReader(new FileReader(fileName));
			reader = new BufferedReader(new FileReader(fileName));
			writer = new PrintWriter(new FileWriter(fileName, true));
			din = new DataInputStream(socket.getInputStream());
			dout = new DataOutputStream(socket.getOutputStream());
		}catch (IOException e)
		{
			e.printStackTrace();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}//serverConnection
	
	//Sends data from one client to another client
	public void sendStringToClient(String text)
	{
		try
		{
			dout.writeUTF(text);
			dout.flush();
		} catch(IOException e)
		{
			e.printStackTrace();
		}
	}//sendStringToClient
	
	//Sends data from one client to all other clients - currently is not used
	public void sendStringToAllClients(String text)
	{
		for(int index = 0; index < server.connections.size(); index++){
			ServerConnection sc = server.connections.get(index);
			sc.sendStringToClient(text);
		}
	}//sendStringToAllClients
	
	public void menu()
	{
		  menu =    "1. Login your account.(Enter EGN and account number) "
		 		+ "\n2. Check your account balance. "
		 		+ "\n3. Deposit money in account.(Enter a sum)"
		 		+ "\n4. Withdraw money from account.(Enter a sum)"
		 		+ "\n5. Transfer cash to another account(Enter EGN and account number)."
		 		+ "\n6. Create new account."
		 		+ "\n7. \"quit\" to quit ";
		 sendStringToClient(menu);
	}
	
	//reads from socket constantly with 1 millisecond per read if there is something to read it does if it doesn't it waits for the 1 millisecond
	public void run()
	{
		try
		{
			while(true)
			{
				//waits until input
				while(din.available() == 0)
				{
					try
					{
						Thread.sleep(1);
					}catch(InterruptedException e)
					{
						e.printStackTrace();
					}
				}//while
				//only reads using the 256 symbols UTF offers
				String textIn = din.readUTF();

					String arguments[] = textIn.split(" ");
					//the users option
					switch(arguments[0])
					{
					case "1":
						//boolean yesNoMaybe - same as loggedIn - check if user is logged in 
						if(yesNoMaybe)
						{
							sendStringToClient("You have already signed in. Quit to sign out");
							break;
						}
						sendStringToClient("Enter your EGN and your account number");
						 yesNoMaybe = case1();
						if(yesNoMaybe)
							sendStringToClient("You have been logged in.");
						else 
							sendStringToClient("Such account does not exist.");
						break;
					case "2":
						case2();
						break;
					case "3":
						case3();
						break;
					case "4":
						case4();
						break;
					case "5":
						case5();
						break;
					case "6":
						case6();
						break;
					case "10":
						sendStringToClient("Try again");
						break;
					 default:
						 sendStringToClient("You didn't enter one of the options. Try again: ");
						 menu();
						break;
					}
			}//while
			
		} catch(IOException e)
		{
			e.printStackTrace(); 
			try {
			din.close();
			dout.close();
			socket.close();
			writer.close();
			reader.close();
			} catch(IOException ex)
			{
				e.printStackTrace();
			}//catch
		}//catch
	}//run
	
	//log in account sleeps thread until input
	//fill list of clients in file to be able to use in other cases
	private boolean case1()
	{
		String line = null;
		String fileWords[] = null;
		String words[] = null;
		String textIn = null;
		boolean yesNoMaybe = false;

		//clears list because it is filled every time the function is ran

		list.clear();
		while(true)
		{
			try {
				while(din.available() == 0)
				{
					try
					{
						Thread.sleep(1);
					}catch(InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}//while
			
			try {
				//User's log in info
				textIn = din.readUTF();
				words = textIn.split("\\s+");

				//Reads the whole file
				while((line = loginReader.readLine()) != null)
				{
				     list.add(line.trim());
					 fileWords = line.split("\\s+");
					 
					 if(words[0].equals(fileWords[3]) && words[1].equals(fileWords[4]))
					 {			
						 EGNandID = fileWords[3] + " " + fileWords[4];
						 loggedInAccount = line.trim();
						 yesNoMaybe  = true;
					 }
				}
				return yesNoMaybe;
			} catch (IOException e)
			{
				e.printStackTrace();
				try {
					loginReader.close();
				} catch (IOException e1) 
				{
					e1.printStackTrace();
				}
			}
			
		}//while
	}//case1
	
	//prints out the person current balance by accessing the list of clients
	private void case2()
	{
		sendStringToClient("You have chosen the second option");
		for(String word : list)
		{
			if(word.trim().endsWith(EGNandID))
			{
				String param[] = word.split("\\s+");
				sendStringToClient("Your current balance is: " + param[2]);
			}
		}
	}//case2
	
	//takes input from user and changes his balance based on their input - in file and list
	private void case3()
	{
		//clears list because it is filled every time the function is ran
		list.clear();
		double sum = 0;
		List<String> fileContent = null;
		sendStringToClient("Please enter the amount of money you want to depoist:");
		try {
			while(din.available() == 0)
			{
				try
				{
					Thread.sleep(1);
				}catch(InterruptedException e)
				{
					e.printStackTrace();
				}
			}//while
			String text = din.readUTF();
			sum = Double.parseDouble(text);
			if(sum < 0)
				throw new Exception();
			
			fileContent = new ArrayList<>(Files.readAllLines(path, StandardCharsets.UTF_8));
	    	String changedLine = "";

			for (int k = 0; k < fileContent.size(); k++)
			{
			    if (fileContent.get(k).trim().endsWith(EGNandID))
			    {
			    	
			    	String temp[] = loggedInAccount.split("\\s+");
			    	temp[2] = String.valueOf(sum + Double.parseDouble(temp[2]));
			    	for(String tem : temp)
			    	{
			    		changedLine += tem + " ";
			    	}
			    	
			    	loggedInAccount = changedLine;
			        fileContent.set(k, changedLine);
			        break;
			    }
			}
			Files.write(path, fileContent, StandardCharsets.UTF_8);
			sendStringToClient("Account has been updates. Current balance is " + changedLine);

			for(String t : fileContent)
			{
				list.add(t.trim());
			}
			fileContent = null;
			
		} catch(Exception e)
		{
			sendStringToClient("The amount of money must be in digits and possitive!");
			e.printStackTrace();
		}
	}//case3
	
	//takes input from user and changes his balance based on their input - in file and list
	private void case4()
	{
		list.clear();
		double sum = 0;
		sendStringToClient("Please enter the amount of money you want to withdraw:");
		try {
			while(din.available() == 0)
			{
				try
				{
					Thread.sleep(1);
				}catch(InterruptedException e)
				{
					e.printStackTrace();
				}
			}//while
			//only reads using the 256 symbols UTF offers
			String text = din.readUTF();
			sum = Double.parseDouble(text);
			if(sum < 0)
				throw new Exception();
			
			
			List<String> fileContent = new ArrayList<>(Files.readAllLines(path, StandardCharsets.UTF_8));
	    	String changedLine = "";

			for (int k = 0; k < fileContent.size(); k++)
			{
			    if (fileContent.get(k).equals(loggedInAccount))
			    {
			    	
			    	String temp[] = loggedInAccount.split("\\s+");
			    	
			    	temp[2] = String.valueOf(Double.parseDouble(temp[2]) - sum);
			    	if(Double.parseDouble(temp[2]) < -5000)
			    		throw new CreditLimitReachedException();
			    	for(String tem : temp)
			    	{
			    		changedLine += tem + " ";
			    	}
			    	
			    	loggedInAccount = changedLine;
			        fileContent.set(k, changedLine);
			        break;
			    }
			}
			Files.write(path, fileContent, StandardCharsets.UTF_8);
			sendStringToClient("Account has been updates. Current balance is " + changedLine);

			for(String t : fileContent)
			{
				list.add(t.trim());
			}
			fileContent = null;
			
		} catch(CreditLimitReachedException e)
		{
			sendStringToClient(e.getMessage());
			e.printStackTrace();
		}
		catch(Exception e)
		{
			sendStringToClient("The amount of money must be in digits and possitive!");
			e.printStackTrace();
		}
	}//case4
	
	//takes input from user and changes his balance based on their input - in file and in list
	//also uses user's input to change another client's balance - in file and in list
	private void case5()
	{
		list.clear();
		double sum = 0;
		sendStringToClient("Please enter the amount of money you want to transfer, an EGN and an account number: ");
		try {
			while(din.available() == 0)
			{
				try
				{
					Thread.sleep(1);
				}catch(InterruptedException e)
				{
					e.printStackTrace();
				}
			}//while
			//only reads using the 256 symbols UTF offers
			String text = din.readUTF();
			String[] separate = text.split("\\s+");
			String transferTo = separate[1] + " " + separate[2];
			sum = Double.parseDouble(separate[0]);
			if(sum < 0)
				throw new Exception();
			
			
			List<String> fileContent = new ArrayList<>(Files.readAllLines(path, StandardCharsets.UTF_8));
	    	String changedLine = "";
	    	String chLine = "";

			for (int k = 0; k < fileContent.size(); k++)
			{
			    if (fileContent.get(k).equals(loggedInAccount))
			    {
			    	
			    	String temp[] = loggedInAccount.split("\\s+");
			    	temp[2] = String.valueOf(Double.parseDouble(temp[2]) - sum);
			    	if(Double.parseDouble(temp[2]) < -5000)
			    		throw new CreditLimitReachedException();
			    	for(String tem : temp)
			    	{
			    		changedLine += tem + " ";
			    	}
			    	
			    	loggedInAccount = changedLine;
			        fileContent.set(k, changedLine);
			        
			        break;
			    }
			}
			    
			    for (int j = 0; j < fileContent.size(); j++)
				{
				    if (fileContent.get(j).trim().endsWith(transferTo))
				    {
				    
				    	String fixLine[] = fileContent.get(j).split("\\s+");					    	
				    	fixLine[2] = String.valueOf(Double.parseDouble(fixLine[2]) + sum);
				    	
				    	for(String tem : fixLine)
				    	{
				    		chLine += tem + " ";
				    	}
				    	
				    	transferTo = chLine;	
				        fileContent.set(j, chLine);
				        
				        break;
				    }
				}
			    
			Files.write(path, fileContent, StandardCharsets.UTF_8);
			sendStringToClient("Account has been updates. Current balance is " + changedLine);
			sendStringToClient("Transfered account has been updates. Current balance is " + chLine);


			for(String t : fileContent)
			{
				list.add(t.trim());
			}
			fileContent = null;
			
		}
		catch(CreditLimitReachedException e)
		{
			sendStringToClient(e.getMessage());
			e.printStackTrace();
		}
		catch(Exception e)
		{
			sendStringToClient("The amount of money must be in digits and possitive!");
			e.printStackTrace();
		}
	}//case5
	
	//checks if user entered correctly information and creates account in file and in list
	// user gets logged in instantly
	private void case6()
	{
		list.clear();
		sendStringToClient("Please enter your first and last names followed by the amount you are starting with"
				+ "\n your EGN and the account you are referencing(1, 2 , 3...)."
							+ "\n" + "Example: Kostadin Krushkov 0.00 9805069047 2");
					String entry = "";
					while(true)
					{
						try 
						{
							while(din.available() == 0)
							{
								try
								{
									Thread.sleep(1);
								}catch(InterruptedException e)
								{
									e.printStackTrace();
								}
							}
						} catch (IOException e2) 
						{
							e2.printStackTrace();
						}//while
						try 
						{
							entry = din.readUTF();
						} catch (IOException e1) 
						{
							e1.printStackTrace();
						}
						loggedInAccount = entry.trim();
						

					String words[] = entry.split("\\s+");
					EGNandID = words[3] + " " + words[4];
					double amount = 0;
					
					if(words.length > 5)
					{
							sendStringToClient("You entered more than the five elements asked");
						break;
					}
					if(words.length < 5)
					{
						sendStringToClient("You entered less than the five elements asked");
						break;
					}
					if(words[3].length() != 10)
					{
						sendStringToClient("Please make sure your EGN is correct");
						break;
					}
					try 
					{
						amount = Double.parseDouble(words[2]);
						if(amount < 0)
						{
							amount = 0;
							sendStringToClient("You must enter a positive value!");
							break;
						}
					} catch (Exception e)
					{
						
						sendStringToClient("You didn't enter a number as your amount!" );
						break;
					}
					
					String currentLine = null;
					String fileWords[] = new String[5];
					try {
						while((currentLine = reader.readLine()) != null)
						{
							 list.add(currentLine.trim());
							 fileWords = currentLine.split("\\s+");
							 
							 if(words[0].equals(fileWords[0]) && words[1].equals(fileWords[1]) && words[3].equals(fileWords[3]) && words[4].equals(fileWords[4]))
							 {
								 sendStringToClient("You have already created an account with this number: " + words[4] + ". Please try again with a new one!");
								 list.clear();
								 loggedInAccount = "";
								 menu();
								 print = false;
									break;
							 }
							 else
							 {
								 print = true;
							 }
						}
					} catch (IOException e)
					{
						e.printStackTrace();
					}
				
					String accountEntry = "";
					for(String word : words)
					{
						accountEntry += word + " ";
					}
					
					if(print) 
					{
					list.add(entry.trim());
					writer.println(accountEntry);
					sendStringToClient("Account created");
					writer.flush();
					break;
					}

					}
	}
}//class