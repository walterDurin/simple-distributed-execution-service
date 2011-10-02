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
package grid.cluster.master;

import grid.cluster.shared.GridConfig;
import grid.cluster.shared.IExecutable;
import grid.server.IInvocationService;

import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;


/**
 * This is a proxy {@link ExecutorService} to the cluster nodes.
 * 
 * @author rkehoe
 *
 */
public class RemoteInvocationService extends AbstractExecutorService implements IInvocationService
{
	private static final int DEFAULT_BROKER_PORT = 54321;
	private static final String DEFAULT_SERVICE_NAME = "DefaultRIBroker";

	/**
	 * This base executor service is tethered
	 * to the remote JVM's executors. 
	 * Therefore the size of this pool should be
	 * a function of the number of cluster nodes. [ 1:1 ???]
	 */
	private ExecutorService localExecutorService;
	private long executorCount = 0;
	private final Broker<Object> broker;
	private String serviceName;

    /**
     * @param brokerPort
     * @param jvmNodeParams
     * @param gridConfig
     * @param serviceName
     * @throws RemoteException
     */
    public RemoteInvocationService(int brokerPort, List<String> jvmNodeParams, GridConfig gridConfig,String serviceName) throws RemoteException
    {
    	this.broker = new Broker<Object>(brokerPort,jvmNodeParams,gridConfig,serviceName);
    	this.serviceName = gridConfig.getLibraryName();
    }
    
	/**
     * @param gridConfig
     * @param nodeCmdLineArgs
	 * @throws RemoteException 
     */
    public RemoteInvocationService(GridConfig gridConfig, List<String> jvmNodeParams) throws RemoteException
    {
    	this(DEFAULT_BROKER_PORT,jvmNodeParams,gridConfig,DEFAULT_SERVICE_NAME);
    }

	/* (non-Javadoc)
     * @see grid.cluster.master.IRemoteExecutorService#start()
     */
    @Override
    public void start() throws Exception
    {
    	this.log("Starting local execution service of size="+this.getBroker().getClusterSize()+" for Broker "+this.serviceName);
    	this.localExecutorService = Executors.newFixedThreadPool(this.getBroker().getClusterSize());
    	this.broker.start();
    }

    /* (non-Javadoc)
     * @see grid.cluster.master.IRemoteExecutorService#getServiceName()
     */
    @Override
    public String getNamespace()
    {
	    return this.serviceName;
    }
    
	/* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
	    return "InvocationService [broker=" + this.getBroker() + "]";
    }

	/* (non-Javadoc)
	 * @see java.util.concurrent.ExecutorService#awaitTermination(long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException
	{
		return this.localExecutorService.awaitTermination(timeout, unit);
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.ExecutorService#isShutdown()
	 */
	@Override
	public boolean isShutdown()
	{
		boolean shutdown = this.localExecutorService.isShutdown();
		return shutdown;
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.ExecutorService#isTerminated()
	 */
	@Override
	public boolean isTerminated()
	{
		boolean terminated = this.localExecutorService.isTerminated();
		return terminated;
	}

    private void log(String x)
    {
	    System.out.println(x);
    }

	/* (non-Javadoc)
	 * @see java.util.concurrent.ExecutorService#shutdown()
	 */
	@Override
	public void shutdown()
	{
		this.localExecutorService.shutdown();
		log("Shutting down Broker");
		try
        {
	        this.broker.shutDown();
        }
        catch (Exception e)
        {
	        throw new RuntimeException( e );
        }
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.ExecutorService#shutdownNow()
	 */
	@Override
	public List<Runnable> shutdownNow()
	{
		List<Runnable> shutdownNow = this.localExecutorService.shutdownNow();
		log("Shutting down Broker");
		try
        {
	        this.broker.shutDown();
        }
        catch (Exception e)
        {
	        throw new RuntimeException( e );
        }
		return shutdownNow;
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Executor#execute(java.lang.Runnable)
	 */
	@Override
	public void execute(Runnable command)
	{
		this.localExecutorService.execute(command);
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.AbstractExecutorService#newTaskFor(java.util.concurrent.Callable)
	 */
    @SuppressWarnings("unchecked")
    @Override
	protected <K> RunnableFuture<K> newTaskFor(Callable<K> callable)
	{
    	IExecutable<K> exe = this.getExecutable(callable);
    	FutureTask<?> future = this.broker.dispatch((IExecutable<Object>) exe);
        return (RunnableFuture<K>) future; 		
	}
	
	private final <K> IExecutable<K> getExecutable(Callable<K> c)
	{
		return new Executable<K>(c,++executorCount);
	}

	/**
     * @return the broker
     */
    public Broker<Object> getBroker()
    {
	    return broker;
    }

	/* (non-Javadoc)
     * @see grid.server.IInvocationService#pause()
     */
    @Override
    public void pause() 
    {
    	try
        {
	        this.broker.pause();
        }
        catch (RemoteException e)
        {
	        throw new RuntimeException( e );
        }
    }

	/* (non-Javadoc)
     * @see grid.server.IInvocationService#unPause()
     */
    @Override
    public void unPause()
    {
	    try
        {
	        this.broker.unPause();
        }
        catch (RemoteException e)
        {
	        throw new RuntimeException( e );
        }
	    
    }
}
