/**
 * 
 */
package integration.proto;

import grid.federation.FederatedCluster;
import grid.server.ExecutionServer;
import grid.server.IJob;
import grid.server.IResultHandler;
import grid.server.ITask;
import grid.server.ITaskExecutor;
import grid.util.Handler;
import grid.util.ProcessUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author rkehoe
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration()
public class ExecutionServerPrototype
{
	@Autowired
	private Set<ITaskExecutor>	taskExecutors;
		
	@BeforeClass
	public static void setup()
	{
		
	}
	
	@Test
	public void singleBroker() throws Throwable
	{
		boolean federated = false;
		createServer(4,federated);
	}

	@Test
	@Ignore
	public void multiBroker() throws Throwable
	{
		createFederatedBroker();

		boolean federated = true;
		createServer(200,federated);
		
	}

    private void createFederatedBroker()
    {
		String classpath = System.getProperty("java.class.path");            
    	final ArrayList<String> cmdStrs = new ArrayList<String>();
    	
    	cmdStrs.add("java");

    	cmdStrs.add("-Dtangosol.coherence.cacheconfig=dist-cache-config.xml");
    	cmdStrs.add("-cp");
    	cmdStrs.add(classpath);
    	cmdStrs.add(FederatedCluster.class.getName());        	
    	cmdStrs.add("5432");
    	cmdStrs.add("Broker_B");
    	cmdStrs.add("Broker_A");
    	
    	(new Thread(new Runnable() 
        {
			@Override
            public void run()
            {
				try
                {
	                Process process = ProcessUtil.create(cmdStrs);          
                }
                catch (Exception e)
                {
	                throw new RuntimeException( e );
                }	            
            }
		})).start();

    }

	public void createServer(int amount, boolean federated) throws Throwable
	{
		final ExecutionServer server = new ExecutionServer(this.taskExecutors);

		final TestJob job = new TestJob(amount);
        Handler<String> handler = new TestHandler(job);
        job.setHandler(handler);

        (new Thread(new Runnable() 
        {
			@Override
            public void run()
            {
				try
                {
	                server.run(job);
                }
                catch (Exception e)
                {
	                throw new RuntimeException( e );
                }	            
            }
		})).start();
        
        if(!federated)
        	server.start();
		
        job.await();
		
        server.shutdown();

		System.out.println("RESULT: "+job.handler());
		Map<String, Integer> threadCounts = job.handler().getThreadCounts();
		for (Entry<String, Integer> count : threadCounts.entrySet())
        {
			System.out.println("\tThread: "+count.getKey()+" ; Execution count: "+count.getValue());
        }
	}

	class TestJob implements IJob<String>
	{
		private Handler<String> handler;
		private int taskCount;

        public TestJob(int taskCount)
        {
			this.taskCount = taskCount;
        }
        
        public void setHandler(Handler<String> handler)
        {
			this.handler = handler;
        }
        
        public void await() throws InterruptedException
        {
        	handler.await();
        }

		/* (non-Javadoc)
         * @see grid.server.IJob#getID()
         */
        @Override
        public String getID()
        {
	        return "TestJobIDxxx";
        }

		/* (non-Javadoc)
         * @see grid.server.IJob#name()
         */
        @Override
        public String name()
        {
	        return "TestJobNamexxx";
        }

		/* (non-Javadoc)
         * @see grid.server.IJob#handler()
         */
        @Override
        public IResultHandler<String> handler()
        {
	        return this.handler;
        }

		/* (non-Javadoc)
         * @see grid.server.IJob#tasks()
         */
        @Override
        public Iterator<ITask<String>> tasks()
        {
        	Collection<ITask<String>> tasks = FibonacciTask.generateTasks(taskCount, 3);
	        return tasks.iterator(); 
        }
	}
	
}
