package com.rk.grid.testing;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import com.rk.grid.cluster.shared.ITaskMonitor;
import com.rk.grid.server.ITask;
import com.rk.grid.server.ITaskResult;


/**
 * 
 * This is an {@link ITask} version of the {@link Fibonacci} class.
 * 
 * The primary difference is the @link {@link Inject} annotation
 * on {@link #resultsCache}
 * 
 */
public class FibonacciTask implements ITask<String>
{
	
    private static final long serialVersionUID = 1L;

    /**
     * Tip: Use @Resource rather than @Autowired when
     * dealing with Map<,> 
     */
    @Resource(name="cacheService")
    public Map<Integer, Long> resultsCache = new HashMap<Integer,Long>();

    @Resource
    private ITaskMonitor<String> monitor = new ITaskMonitor<String>() {

        private static final long serialVersionUID = 1L;

		@Override
        public void notify(String msg, String taskID) throws RemoteException
        {
			//Do nothing - as this is just a dummy implementation for non grid execution service
        }};
    
	private final Integer id;
	
	private String invocationNamespace = "RemoteService";

    public FibonacciTask(int id, String namespace)
    {
		this.id = id;
		this.invocationNamespace = namespace;
    }

	public long fib(int n) throws Exception
	{
		Long x = resultsCache.get(n);

		if (x != null)
		{
			monitor.notify("Retrieved result from cache: "+n+"->"+x,this.getID());
			return x;
		}
		
		if (n <= 1)
		{
			return n;
		}
		else // else where n >= 2
		{
			long f = fib(n - 1) + fib(n - 2);
			monitor.notify("Adding result to cache: "+n+"->"+f,this.getID());
			resultsCache.put(n, f);
			return f;
		}
	}

	/* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
	    return "FibonacciTask [id=" + this.id + ", pre=" + this.resultsCache + ", version=" + this.invocationNamespace + ", getID()=" + this.getID() + ", getInvocationNamespace()="
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
	    return invocationNamespace;
    }

	/* (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public ITaskResult<String> call() throws Exception
    {
		resultsCache.put(0, 0L);
		resultsCache.put(1, 1L);

		monitor.notify("Starting progress on task.",this.getID());

		String result = "Fibonacci result = "+this.fib(40)+" (Calculated by "+Thread.currentThread().getName()+")";
		System.out.println("Task: "+getID()+" executed - Result = "+result);		
		System.out.println("Calculation cache: "+this.resultsCache.values() );

		return new TestResult(getID(),result);
    }
    
    public static Collection<ITask<String>> generateTasks(int count)
    {
    	return generateTasks(count, "namespace");
    }
    
    public static Collection<ITask<String>> generateTasks(int count, String namespace)
    {
	    Collection<ITask<String>> list = new ArrayList<ITask<String>>();
	    for (int i = 1; i <= count; i++)
        {
	        ITask<String> task = new FibonacciTask(i,namespace);
			list.add(task);
        }
		return list;
    }

    public static void main(String[] args) throws Exception
    {
	    FibonacciTask task = new FibonacciTask(4,"");
		String result = "Fibonacci result = "+task.fib(40);
		System.out.println(result);
    }
}