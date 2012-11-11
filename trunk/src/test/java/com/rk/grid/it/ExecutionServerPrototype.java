/**
 * 
 */
package com.rk.grid.it;

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

import com.rk.grid.federation.FederatedCluster;
import com.rk.grid.server.ExecutionServer;
import com.rk.grid.server.IJob;
import com.rk.grid.server.IResultHandler;
import com.rk.grid.server.ITask;
import com.rk.grid.server.ITaskExecutor;
import com.rk.grid.testing.FibonacciTask;
import com.rk.grid.testing.TestHandler;
import com.rk.grid.util.Handler;
import com.rk.grid.util.ProcessUtil;


/**
 * NB. EXPERIMENTAL ONLY!
 * 
 * A Spring based JUnit demo/test of a grid with 
 * multiple brokers (federated grid). 
 * 
 * @author rkehoe
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/ExecutionServerPrototype-context.xml"})
public class ExecutionServerPrototype
{
	@Autowired
	private Set<ITaskExecutor>	taskExecutors;
		
	@BeforeClass
	public static void setup()
	{
		
	}
	
	@Test
	//@Ignore
	public void singleBroker() throws Throwable
	{
		final ExecutionServer server = new ExecutionServer(this.taskExecutors);

		int amount = 1001;
		final TestJob job = new TestJob(amount);
        Handler<String> handler = new TestHandler(job.toString());
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

	@Test
	//@Ignore
	public void multiBroker() throws Throwable
	{
		createFederatedBroker();

		final ExecutionServer server = new ExecutionServer(this.taskExecutors);

		int amount = 1031;
		final TestJob job = new TestJob(amount);
        Handler<String> handler = new TestHandler(job.toString());
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
    	cmdStrs.add("Remote-Grid-1");
    	cmdStrs.add("Grid-A");
    	cmdStrs.add("1234");
    	cmdStrs.add("localhost");
    	
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
        	Collection<ITask<String>> tasks = FibonacciTask.generateTasks(taskCount,"fibNamespaceA");
	        return tasks.iterator(); 
        }
	}
	
}
