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
package grid.util;

import grid.server.IJob;
import grid.server.ITaskResult;
import grid.server.IResultError;
import grid.server.IResultHandler;
import grid.server.ITask;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;


/**
 * Utility/Base implementation.
 * 
 * @author rkehoe
 *
 */
public abstract class Handler<T> implements IResultHandler<T>
{
	private CountDownLatch completed = new CountDownLatch(1);
	private final IJob<?> job;
	private int resultCount;
	private int rejectedCount;
	private long elapsedTimeMillis;
	private long startTimeMillis;
	private boolean started;
	private int errorCount;
    private final Map<String,Integer> threadCounts = new HashMap<String,Integer>();

    public Handler(IJob<?> job)
    {
		this.job = job;    	
    }
    
    public void await() throws InterruptedException
    {
    	this.completed.await();
    }
    
	/* (non-Javadoc)
	 * @see grid.server.IResultHandler#onCompleted()
	 */
	@Override
	public void onCompleted()
	{
		this.elapsedTimeMillis = System.currentTimeMillis() - this.startTimeMillis;
		log("Completed job "+this.job);
		completed.countDown();
	}

	/* (non-Javadoc)
	 * @see grid.server.IResultHandler#onError(java.lang.Throwable)
	 */
	@Override
	public void onError(Throwable e)
	{
		log(e);
	}

    public void log(Throwable e)
    {
		log("Error: "+e.getMessage());
		e.printStackTrace();
    }

    public void log(String msg)
    {
    	msg += " : "+this.toString();
    	System.out.println(msg);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
	    return "Handler [completed=" + this.completed + ", elapsedTimeMillis=" + this.elapsedTimeMillis + ", job=" + this.job + ", rejectedCount=" + this.rejectedCount
	            + ", resultCount=" + this.resultCount + "] "+threadCounts;
    }

    @Override
    public Map<String,Integer> getThreadCounts()
    {
    	return Collections.unmodifiableMap(this.threadCounts);
    }
    
	/* (non-Javadoc)
	 * @see grid.server.IResultHandler#onResult(grid.server.ITaskResult)
	 */
	@Override
	public void onResult(ITaskResult<T> r)
	{
		resultCount++;
		if(threadCounts.containsKey(r.getExecutionThreadName()))
		{
			int count = threadCounts.get(r.getExecutionThreadName());
			threadCounts.put(r.getExecutionThreadName(), ++count );
		}
		else
		{
			threadCounts.put(r.getExecutionThreadName(), 1);
		}
	}

	/* (non-Javadoc)
	 * @see grid.server.IResultHandler#onResult(grid.server.IResultError)
	 */
	@Override
	public void onResult(IResultError r)
	{
		this.errorCount++;
		log(r.toString());
	}

	/* (non-Javadoc)
	 * @see grid.server.IResultHandler#onStarted()
	 */
	@Override
	public void onStarted()
	{
		if(!started)
		{
			this.startTimeMillis = System.currentTimeMillis();
			log("Started job "+job);
		}
		this.started = true;
	}

	/* (non-Javadoc)
     * @see grid.server.IResultHandler#onRejection(java.util.Collection)
     */
    @Override
    public void onRejection(Collection<ITask<T>> rejectedTasks)
    {
    	rejectedCount+=rejectedTasks.size();
//    	log("The following tasks have been rejected:");
//	    for (ITask<T> iTask : rejectedTasks)
//        {
//	        log("\t"+iTask);
//        }
    }

}
