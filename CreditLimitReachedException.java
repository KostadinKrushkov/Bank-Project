package server;

public class CreditLimitReachedException extends Exception
{
	public String getMessage()
	{
		return "Can withdraw after credit limit (5000) is reached";
	}
}
