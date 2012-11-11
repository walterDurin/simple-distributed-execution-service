package com.rk.grid.testing;

import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.Callable;

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
public class Fibonacci implements Callable<String>,Serializable
{
    private static final long serialVersionUID = 1L;

	private HashMap<Integer, Long>	resultsCache	= new HashMap<Integer, Long>();
	
	private final int number;

    public Fibonacci(int number)
    {
		this.number = number;
    }

	/* (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public String call() throws Exception
    {
		resultsCache.put(0, 0L);
		resultsCache.put(1, 1L);

		long fib = this.fib(number);
	    return "Fibonacci("+number+") = "+fib+" @"+Thread.currentThread().getName()+";";
    }

	public long fib(int n)
	{
		Long x = resultsCache.get(n);

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
			resultsCache.put(n, f);
			return f;
		}
	}

	public static void main(String[] args) throws Exception
	{
		int N = 92;
		for (int i = 1; i <= N; i++)
		{
			System.out.println(new Fibonacci(i).call());
		}
	}
}