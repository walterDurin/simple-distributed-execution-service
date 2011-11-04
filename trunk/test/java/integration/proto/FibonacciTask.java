package integration.proto;

import grid.cluster.shared.IProgressMonitor;
import grid.server.ITask;
import grid.server.ITaskResult;
import grid.server.Inject;
import grid.server.Monitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

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
     * This field will be injected if this task is executed
     * on the grid. The {@link #resultsCache} will then be a 
     * distributed and shared amongst all other executing
     * tasks.
     * If this task is not executed on the grid, then it will still
     * work as the {@link #resultsCache} will just use
     * the initialising local HashMap as a cache. 
     */
    @Inject(name = "cacheService")
	public Map<Integer, Long> resultsCache = null;//new HashMap<Integer, Long>();

    @Monitor
    private IProgressMonitor<String> monitor;
    
	private final Integer id;
	private String version = "Caf1";

    public FibonacciTask(int id)
    {
		this.id = id;
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

	/* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
	    return "FibonacciTask [id=" + this.id + ", pre=" + this.resultsCache + ", version=" + this.version + ", getID()=" + this.getID() + ", getInvocationNamespace()="
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
    @Override
    public ITaskResult<String> call() throws Exception
    {
		resultsCache.put(0, 0L);
		resultsCache.put(1, 1L);

		monitor.accept("This is a progress msg from task "+getID());

		String result = "Fibonacci result = "+this.fib(20)+" (Calculated by "+Thread.currentThread().getName()+")";
		System.out.println("Task: "+getID()+" executed - Result = "+result);		
		System.out.println("Calculation cache: "+this.resultsCache.values() );

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