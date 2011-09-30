package integration.proto;

import grid.server.ITask;
import grid.server.ITaskResult;
import grid.server.Inject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
public class FibonacciTask implements ITask<String>
{
	public static class Calculation implements Serializable
	{
		private static final long serialVersionUID = 1L;
		public final String comment;
		public final long calculation;
		private final long number;

		public Calculation(long number, long calculation) {
			this.number = number;
			this.calculation = calculation;
			this.comment = Thread.currentThread().getName();
		}

		@Override
		public String toString() {
			return "Calculation [comment=" + comment + ", calculation="
					+ calculation + ", number=" + number + "]";
		}
	}
	
    private static final long serialVersionUID = 1L;

    /**
     * This field will be injected if this task is executed
     * on the grid.
     */
    @Inject(name = "cacheService")
	public Map<Integer, Calculation> preCalcCache = new HashMap<Integer, Calculation>();

	private final Integer id;
	private String version = "Caf1";

    public FibonacciTask(int id)
    {
		this.id = id;
    }

	public long fib(int n)
	{
		Calculation calcZero = new Calculation(0,0L);
		Calculation calcOne = new Calculation(1,1L);
		
		preCalcCache.put(0, calcZero);
		preCalcCache.put(1, calcOne);

		Calculation x = preCalcCache.get(n);

		if (x != null)
		{
			return x.calculation;
		}
		
		if (n <= 1)
		{
			return n;
		}
		else // else where n >= 2
		{
			long calc = fib(n - 1) + fib(n - 2);
			preCalcCache.put(n, new Calculation(n,calc));
			return calc;
		}
	}

	/* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
	    return "FibonacciTask [id=" + this.id + ", pre=" + this.preCalcCache + ", version=" + this.version + ", getID()=" + this.getID() + ", getInvocationNamespace()="
	            + this.getInvocationNamespace() + ", toString()=" + super.toString() + "]";
    }

	/* (non-Javadoc)
     * @see grid.server.ITask#getID()
     */
    @Override
    public String getID()
    {
	    return this.getClass().getSimpleName()+"-ID:"+id;
    }

	/* (non-Javadoc)
     * @see grid.server.ITask#getInvocationNamespace()
     */
    @Override
    public String getInvocationNamespace()
    {
	    return version;
    }

	/* (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    @SuppressWarnings("unchecked")
    @Override
    public ITaskResult<String> call() throws Exception
    {
		String result = "Fibonacci result = "+this.fib(20);
		System.out.println("Task: "+getID()+" executed - Result = "+result);		
		System.out.println("Calculation cache: "+this.preCalcCache.values() );
		return new TestResult(getID(),result);
    }
    
    public static Collection<ITask<String>> generateTasks(int count)
    {
	    Collection<ITask<String>> list = new ArrayList<ITask<String>>();
	    
	    for (int i = 1; i <= count; i++)
        {
	        ITask<String> task = new FibonacciTask(i);
			list.add(task);
        }
	    
		return list;
    }

    public static void main(String[] args) throws Exception
    {
	    FibonacciTask task = new FibonacciTask(4);
		String result = "Fibonacci result = "+task.fib(40);
		System.out.println(result);
    }
}