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

import grid.cluster.shared.IExecutable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


/**
 * @author rkehoe
 *
 * @param <K> the param type for {@link ITask#call(Object)}
 */
public class TaskExecutor implements ITaskExecutor
{
	private final IInvocationService invocationService;
	
	/**
	 * Determines how many batches of tasks can be dispatched/handed
	 * to the {@link #invocationService} at any one time.
	 * This is [therefore] a key gateway throttle as each {@link #invocationService} 
	 * is tied to {@link #serviceName}.
	 * 
	 * There if no point in having this of similar magnitude to
	 * the cluster size as this thread deals with batches/multiple tasks.
	 */
	private final ExecutorService localExecutor;
	private final String serviceName;

    public TaskExecutor(IInvocationService es) throws Exception
    {
    	this.invocationService = es;
    	this.serviceName = es.getNamespace();
    	
    	es.start();
    	
    	localExecutor = Executors.newFixedThreadPool(3);    
    }

	/* (non-Javadoc)
     * @see grid.server.ITaskExecutor#execute(java.util.Collection, grid.server.IResultHandler)
     */
    @Override
    public <T> void execute(final Collection<ITask<T>> tasks, final IResultHandler<T> handler) throws Exception
    {
    	if(tasks.size()==0)return;
    	
//		final CompletionService<ITaskResult<T>> cs = new ExecutorCompletionService<ITaskResult<T>>(this.localExecutor);
		final CompletionService<ITaskResult<T>> cs = new ExecutorCompletionService<ITaskResult<T>>(this.invocationService);

		int size = 0;
    	for (final ITask<T> t : tasks)
    	{
    			cs.submit(t);
    			size++;
    	}

    	final  int totalSubmissionCount = size;

    	Callable<?> callable = new Callable<Object>() 
    	{
			@Override
            public Object call() throws Exception
            {				
		    	try
				{
		    		handler.onStarted();
					
		    		// ... task not necessarily started!
					for (int i = 0; i < totalSubmissionCount; i++)
					{
						Future<ITaskResult<T>> future = cs.take();
		                try
		                {
		                	ITaskResult<T> t = future.get();
			                handler.onResult(t);
		                }
		                catch (Throwable e)
		                {
		               		handler.onError(e);
		                }
					}
				}
				catch (Exception e)
				{
					handler.onError(new BatchException(e));
				}
				finally
				{
					handler.onCompleted();
				}
	            return null;
            }
		};

		this.localExecutor.submit(callable);
	}

	/* (non-Javadoc)
     * @see grid.server.ITaskExecutor#execute(java.util.Collection)
     */
    @Override
    public <T> CompletionService<ITaskResult<T>> execute(Collection<ITask<T>> tasks) throws Exception
    {
		final CompletionService<ITaskResult<T>> cs = new ExecutorCompletionService<ITaskResult<T>>(invocationService);

		for (final ITask<T> t : tasks)
		{
			cs.submit(t);
		}

		return cs;
    }    

	/* (non-Javadoc)
     * @see grid.server.ITaskExecutor#submit(java.util.Collection)
     */
    @Override
    public <T> Collection<Future<ITaskResult<T>>> submit(Collection<ITask<T>> tasks) throws Exception
    {
    	return this.submit(tasks,0,null);
    }

	/* (non-Javadoc)
     * @see grid.server.ITaskExecutor#submit(java.util.Collection, long, java.util.concurrent.TimeUnit)
     */
    @Override
    public <T> List<Future<ITaskResult<T>>> submit(Collection<ITask<T>> tasks, long timeout, TimeUnit tu) throws Exception
    {
    	List<Future<ITaskResult<T>>> all = new ArrayList<Future<ITaskResult<T>>>();
	    try
	    {
			if(timeout==0)
		    {
				all = invocationService.invokeAll(tasks);
		    }
		    else
		    {
		    	all = invocationService.invokeAll(tasks,timeout,tu);	    	
		    }
	    }
	    catch (InterruptedException e)
	    {
	    	throw new BatchException( e );
	    }	    	
	    return all;
    }

	/**
     * 
     */
    public void shutdown()
    {
    	this.localExecutor.shutdown();
    	this.invocationService.shutdown();
    }

    @Override
    public void pause()
    {
    	this.invocationService.pause();
    }
    
    @Override
    public void unPause()
    {
    	this.invocationService.unPause();
    }
    
	/* (non-Javadoc)
     * @see grid.server.ITaskExecutor#submit(grid.batch.ITask)
     */
    @Override
    public <T> Future<ITaskResult<T>> submit(ITask<T> task)
    {
    	// TODO: do we check???
    	//if(!this.serviceName.equals(task.serviceName()))throw new RuntimeException("Incorrect service version");
    	return this.invocationService.submit(task);
    }

    @SuppressWarnings("unchecked")
    public Future<ITaskResult<?>> submit(IExecutable<?> executable)
    {
    	return this.invocationService.submit((Callable<ITaskResult<?>>) executable.getCallable());
    }

	/* (non-Javadoc)
     * @see grid.server.ITaskExecutor#getServiceName()
     */
    @Override
    public String getServiceName()
    {
	    return serviceName;
    }
}
