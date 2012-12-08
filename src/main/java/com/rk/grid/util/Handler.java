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
package com.rk.grid.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import com.rk.grid.server.IResultError;
import com.rk.grid.server.IResultHandler;
import com.rk.grid.server.ITask;
import com.rk.grid.server.ITaskResult;


/**
 * Utility/Base implementation.
 * 
 * @author rkehoe
 *
 */
public abstract class Handler<T> implements IResultHandler<T>
{
	private CountDownLatch completed = new CountDownLatch(1);
	private int resultCount = 0;
	private int rejectedCount = 0;
	private long elapsedTimeMillis;
	private long startTimeMillis;
	private boolean started  = false;
	private int errorCount = 0;
    private final Map<String,Integer> threadCounts = new ConcurrentHashMap<String,Integer>();
	private final String jobDescription;

    public Handler(String jobDescription)
    {
		this.jobDescription = jobDescription;
    }
    
    /**
     * @return the resultCount
     */
	@Override
    public int getResultCount()
    {
	    return this.resultCount;
    }
    
    /**
     * @return the elapsedTimeMillis
     */
	@Override
    public long getElapsedTimeMillis()
    {
	    return this.elapsedTimeMillis;
    }
    
    /**
     * @return the errorCount
     */
	@Override
    public int getErrorCount()
    {
	    return this.errorCount;
    }
    
    /**
     * @return the rejectedCount
     */
	@Override
    public int getRejectedCount()
    {
	    return this.rejectedCount;
    }
    
    
    /**
     * Waits/blocks until completion of tasks - i.e., until
     * this handler's {@link #onCompleted()} method is called. 
     * @throws InterruptedException
     */
    public void await() throws InterruptedException
    {
    	this.completed.await();
    }
    
	/* (non-Javadoc)
	 * @see grid.server.IResultHandler#onCompleted()
	 */
	@Override
	public synchronized void onCompleted()
	{
		completed.countDown();
		this.elapsedTimeMillis = System.currentTimeMillis() - this.startTimeMillis;
		log("Completed job "+this.jobDescription);
	}

	/* (non-Javadoc)
	 * @see grid.server.IResultHandler#onError(java.lang.Throwable)
	 */
	@Override
	public synchronized void onError(Throwable e)
	{
		this.errorCount++;
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
	    return "Handler [completed=" + this.completed + ", resultCount=" + this.resultCount + ", errorCount=" + this.errorCount+ ", rejectedCount=" + this.rejectedCount + ", elapsedTimeMillis="
	            + this.elapsedTimeMillis + ", startTimeMillis=" + this.startTimeMillis + ", started=" + this.started  + ", threadCounts="
	            + this.threadCounts + ", jobDescription=" + this.jobDescription + "]";
    }

	@Override
    public synchronized Map<String,Integer> getThreadCounts()
    {
    	return Collections.unmodifiableMap(this.threadCounts);
    }
    
	/* (non-Javadoc)
	 * @see grid.server.IResultHandler#onResult(grid.server.ITaskResult)
	 */
	@Override
	public synchronized void onResult(ITaskResult<T> r)
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
	public synchronized void onResult(IResultError r)
	{
		this.errorCount++;
		log(r.toString());
	}

	/* (non-Javadoc)
	 * @see grid.server.IResultHandler#onStarted()
	 */
	@Override
	public synchronized void onStarted()
	{
		if(!started)
		{
			this.startTimeMillis = System.currentTimeMillis();
			log("Started job "+jobDescription);
		}
		this.started = true;
	}

	/* (non-Javadoc)
     * @see grid.server.IResultHandler#onRejection(java.util.Collection)
     */
    @Override
    public synchronized void onRejection(Collection<ITask<T>> rejectedTasks)
    {
    	rejectedCount+=rejectedTasks.size();
//    	log("The following tasks have been rejected:");
//	    for (ITask<T> iTask : rejectedTasks)
//        {
//	        log("\t"+iTask);
//        }
    }

}
