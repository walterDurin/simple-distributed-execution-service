package integration.proto;

import java.util.HashMap;

/**
 * In mathematics, the Fibonacci numbers are the following sequence of numbers:
 * 0,1,1,2,3,5,8,13...
 * 
 * The first two Fibonacci numbers are 0 and 1, and each remaining number is the
 * sum of the previous two:
 * 
 * F(n) = F(n-1) + F(n-2) where F(0) = 0 and F(1)=1
 * 
 * NB also a good example of recursion.
 * 
 */
public class Fibonacci
{
	static HashMap<Integer, Long>	pre	= new HashMap<Integer, Long>();

	static
	{
		pre.put(0, 0L);
		pre.put(1, 1L);
	}

	public static long fib(int n)
	{
		Long x = pre.get(n);

		if (x != null)
		{
			return x;
		}
		
		if (n <= 1)
		{
			return n;
		}
		else // else where n >= 2
		{
			long f = fib(n - 1) + fib(n - 2);
			pre.put(n, f);
			return f;
		}
	}

	public static void main(String[] args)
	{
		int N = 100;
		for (int i = 1; i <= N; i++)
		{
			System.out.println(i + ": " + fib(i));
		}
	}
}