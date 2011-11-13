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
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


/**
 * Defines an execution service for {@link ITask}.
 * 
 * @author rkehoe
 */
public interface ITaskExecutor
{
	/**
	 * The Service Name (a.k.a library) that this executor
	 * employs.
	 * @return service name
	 */
	public String getServiceName();
	
	/**
	 * Immediately returns a CompletionService - Non-blocking/Asynchronous contract. 
	 * Will return  immediately, when all jobs submitted.
	 * @param <T>
	 * @param <T>
	 * @param job
	 * @return ICompletionService<T>
	 * @throws BatchException
	 */
	public abstract <T> CompletionService<ITaskResult<T>> execute(Collection<ITask<T>> tasks) throws Exception;

	/**
	 * A-Synchronous contract - handles the results to 
	 * an {@link IResultHandler} callback handler. 
	 * Will return when all jobs executed or exception thrown.
	 * @param <T>
	 * @param job
	 * @param handler
	 * @throws Exception 
	 */
	public abstract <T> void execute(Collection<ITask<T>> tasks, IResultHandler<T> handler) throws Exception;

    /**
	 * A-Synchronous contract - handles the results to 
	 * an {@link IResultHandler} callback handler. 
	 * Will return when all jobs executed or exception thrown.
	 * @param <T>
     * @param tasks
     * @param handler
     * @param taskObserver
     * @throws Exception
     */
    public abstract <T> void execute(Collection<ITask<T>> tasks, IResultHandler<T> handler, ITaskObserver taskObserver) throws Exception;

	/**
	 * Asynchronous submission of a batch job (with timeouts)
	 * @param <T>
	 * @param <T>
	 * @param job
	 * @param time
	 * @param tu
	 * @return a list of results.
	 * @throws BatchException
	 */
	public abstract <T> List<Future<ITaskResult<T>>> submit(Collection<ITask<T>> tasks, long time, TimeUnit tu) throws Exception;

	/**
	 * Asynchronous submission of a batch job.
	 * @param <T>
	 * @param <T>
	 * @param job
	 * @return a list of results
	 * @throws BatchException
	 */
	public abstract <T> Collection<Future<ITaskResult<T>>> submit(Collection<ITask<T>> tasks) throws Exception;
		
    public <T> Future<ITaskResult<T>> submit(ITask<T> task);
    
    public void shutdown();

	/**
     * 
     */
    void unPause();

	/**
     * 
     */
    void pause();
}
