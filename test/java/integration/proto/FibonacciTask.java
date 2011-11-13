package integration.proto;

import grid.cluster.shared.ITaskMonitor;
import grid.server.ITask;
import grid.server.ITaskResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.annotation.Resource;

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
    public Map<Integer, Long> resultsCache = null;

    @Resource
    private ITaskMonitor<String> monitor;
    
	private final Integer id;
	private String version = "Caf1";

    public FibonacciTask(int id)
    {
		this.id = id;
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

		monitor.notify("Starting progress on task.",this.getID());

		String result = "Fibonacci result = "+this.fib(40)+" (Calculated by "+Thread.currentThread().getName()+")";
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