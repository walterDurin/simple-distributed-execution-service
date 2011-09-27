package integration.proto;

import grid.server.ITask;
import grid.server.ITaskResult;
import grid.server.Inject;

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
	private static int count = 0;

    private static final long serialVersionUID = 1L;

//    static HashMap<Integer, Long>	pre	= new HashMap<Integer, Long>();

    @Inject(name = "cacheService")
	public Map<Integer, Long> preCalcCache;// = pre;

	private final Integer id;
	private String version = "Caf1";

    public FibonacciTask(int id)
    {
		this.id = id;
    }

	public long fib(int n)
	{
		preCalcCache.put(0, 0L);
		preCalcCache.put(1, 1L);

		Long x = preCalcCache.get(n);

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
			preCalcCache.put(n, f);
			return f;
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
		String result = "Fibonacci result = "+this.fib(4);
//		Thread.sleep(++count*1);
		System.out.println("Task: "+getID()+" executed - Result = "+result);
		return new TestResult(getID(),result);
    }
    
    public static Collection<ITask<String>> generateTasks(int count,int modCount)
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