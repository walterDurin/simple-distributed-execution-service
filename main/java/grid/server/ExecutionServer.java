/**
 * Copyright 2011 rkehoe
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */
package grid.server;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;


import com.google.common.base.Function;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;

/**
 * What does this do!? 
 * What does this do that a {@link ITaskExecutor} does not!
 * 
 * Well it batches up the tasks from a {@link IJob} and multiplexes {@link IResultHandler}s
 * 
 * It also splits & dispatches tasks to the correct service, i.e., it checks service type
 * while the task executor does not.
 * 
 * @author rkehoe
 * 
 */
public class ExecutionServer
{
	/**
     * 
     */
	//TODO - parameterize???
    private static final int BATCH_SIZE = 100;
	private final HashMap<String, ITaskExecutor> executorMap;

	public ExecutionServer(Collection<ITaskExecutor> taskExecutors)
	{
		this.executorMap  = new HashMap<String, ITaskExecutor>();
		for (ITaskExecutor e : taskExecutors)
        {
	        this.executorMap.put(e.getServiceName(), e);
        }
	}

	/**
     * Un-Pauses {@link ITaskExecutor} which is 
     * initialized in a paused state.
     */
    public void start()
    {
    	this.unPause();
    }

	/**
     * Pauses {@link ITaskExecutor}'s 
     */
    public void pause()
    {
        for (ITaskExecutor ex : this.executorMap.values())
        {
        	ex.pause();
        }
    }

	/**
     * Un-Pauses {@link ITaskExecutor} which is 
     * initialized in a paused state.
     */
    public void unPause()
    {
        for (ITaskExecutor ex : this.executorMap.values())
        {
        	ex.unPause();
        }
    }

	public void shutdown()
	{
		for (ITaskExecutor e : this.executorMap.values())
        {
			e.shutdown();
        }		
	}
	
	public <T> void run(IJob<T> job) throws Exception
	{
		Iterator<ITask<T>> it = job.tasks();
		Collection<ITask<T>> batch = new LinkedList<ITask<T>>();
		ProxyJobResultHandler<T> proxyHandler = new ProxyJobResultHandler<T>(job.handler());

		try
        {
	        for (int ii = 0; it.hasNext(); ii++)
	        {
	        	ITask<T> task = it.next();
	        	batch.add(task);
	        	if (batch.size() == BATCH_SIZE)
	        	{
	        		submitBatch(batch,proxyHandler);
	        		batch = new LinkedList<ITask<T>>();
	        	}
	        }
        }
        finally 
        {
        	submitBatch(batch,proxyHandler);
        	proxyHandler.onCompleted();        	
        }
	}

	/*
	 * Submits tasks to any task executor that can handle them.
	 * Tasks that can not find any suitable executors are
	 * rejected.
	 * 
	 * @param batch
	 * @param proxyHandler
	 */
	private final <T> void submitBatch(Collection<ITask<T>> batch, ProxyJobResultHandler<T> proxyHandler)
	{
		if (batch.size()==0)return;

		ListMultimap<String, ITask<T>> tasksByService = Multimaps.index(batch, new Function<ITask<T>, String>()
		{
			public String apply(ITask<T> task)
			{
				return task.getInvocationNamespace();
			}
		});

		Map<String, Collection<ITask<T>>> tasksByServiceMap = tasksByService.asMap();
		
		for (String requiredService : tasksByServiceMap.keySet())
        {
	        if(this.executorMap.containsKey(requiredService))
	        {
	        	ITaskExecutor taskExecutor = this.executorMap.get(requiredService);
	        	try
                {
	                IResultHandler<T> delegate = proxyHandler.newDelegate();
					taskExecutor.execute(tasksByServiceMap.get(requiredService), delegate);
                }
                catch (Exception e)
                {
                	e.printStackTrace();
                	proxyHandler.onError(e);
                }
	        }
	        else
	        {
	        	proxyHandler.onRejection(tasksByServiceMap.get(requiredService));
	        }
        }		
	}
		
	private static final class ProxyJobResultHandler<T> implements IResultHandler<T>
	{
		private final IResultHandler<T> jobHandler;
		private final Set<IResultHandler<T>> delegates = new HashSet<IResultHandler<T>>();
		
        public ProxyJobResultHandler(IResultHandler<T> jobHandler)
        {
			this.jobHandler = jobHandler;
        }

        public void onStarted(DelegateHandler handler)
        {
        	this.jobHandler.onStarted();
        }

        public void onCompleted(DelegateHandler handler)
        {
        	synchronized(delegates)
        	{
        		this.delegates.remove(handler);
        		if(this.delegates.size()==0)this.jobHandler.onCompleted();        		
        	}
        }

        public IResultHandler<T> newDelegate()
        {
        	DelegateHandler d = new DelegateHandler(this);
        	this.delegates.add(d);
	        return d;
        }

        /** 
         * Real completion actually depends on any delegates if they exist...
         * @see #onCompleted(DelegateHandler)
         */
        public void onCompleted()
        {
        	synchronized(delegates)
        	{
        		if(this.delegates.size()==0)this.jobHandler.onCompleted();        		
        	}
        }

        public void onStarted()
        {
	        this.jobHandler.onStarted();
        }

        public void onError(Throwable e)
        {
	        this.jobHandler.onError(e);
        }

        public void onRejection(Collection<ITask<T>> rejectedTasks)
        {
	        this.jobHandler.onRejection(rejectedTasks);
        }

        public void onResult(ITaskResult<T> r)
        {
	        this.jobHandler.onResult(r);
        }

        public void onResult(IResultError r)
        {
	        this.jobHandler.onResult(r);
        }
        
		/* (non-Javadoc)
         * @see grid.server.IResultHandler#getThreadCounts()
         */
        @Override
        public Map<String, Integer> getThreadCounts()
        {
	        return this.jobHandler.getThreadCounts();
        }

    	private final class DelegateHandler implements IResultHandler<T>
    	{
    		private final ProxyJobResultHandler<T> handler;
    		
            public DelegateHandler(ProxyJobResultHandler<T> handler)
            {
    			this.handler = handler;
            }

            public void onStarted()
            {
    	        this.handler.onStarted(this);
            }		

            public void onCompleted()
            {
    	        this.handler.onCompleted(this);
            }

            public void onError(Throwable e)
            {
    	        this.handler.onError(e);
            }

            public void onRejection(Collection<ITask<T>> rejectedTasks)
            {
    	        this.handler.onRejection(rejectedTasks);
            }

            public void onResult(ITaskResult<T> r)
            {
    	        this.handler.onResult(r);
            }

            public void onResult(IResultError r)
            {
    	        this.handler.onResult(r);
            }

			/* (non-Javadoc)
             * @see grid.server.IResultHandler#getThreadCounts()
             */
            @Override
            public Map<String, Integer> getThreadCounts()
            {
	            return this.handler.getThreadCounts();
            }
    	}
	}
}
