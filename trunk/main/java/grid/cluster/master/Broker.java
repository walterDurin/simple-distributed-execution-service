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

import grid.cluster.shared.BrokerInfo;
import grid.cluster.shared.GridConfig;
import grid.cluster.shared.IBroker;
import grid.cluster.shared.IExecutable;
import grid.cluster.shared.IRemoteResult;
import grid.cluster.shared.IRemoteResultsHandler;
import grid.cluster.shared.IWorkQueue;
import grid.util.BlockingMap;
import grid.util.GateLatch;

import java.io.Serializable;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.remoting.rmi.RmiServiceExporter;


/**
 * Enables/Controls access (via RMI) to remote 
 * JVMs. There is one broker per cluster.
 * 
 * The broker is responsible for creating the JVM cluster
 * with the given library name.
 * 
 * @author rkehoe
 *
 */
public class Broker<V> implements IBroker<V>
{
    private static final long serialVersionUID = -7711272717758613782L;
	/**
     * @param x
     */
    private static void log(String x)
    {
	    System.out.println(x);
    }

	transient private LinkedBlockingQueue<IExecutable<V>> blockingTransferWorkQueue = new LinkedBlockingQueue<IExecutable<V>>(100);
	
	transient private WorkQueue workQueue;
	transient private ResultsHandler resultsHandler;
	transient private BlockingMap<String, IRemoteResult<V>> blockingMap;
	transient private RmiServiceExporter rmiServiceExporter;
	transient private List<Process> processList = new ArrayList<Process>();
	transient private final List<String> jvmNodeParams;
	transient private int connectionID = 0;

	private final int port;
	private int workQueueCallNumber;
	private final int clusterSize;
	private final String libraryName;

	private final String serviceName;
	private final BrokerInfo brokerInfo;

    private GateLatch pauseGate = new GateLatch();

	public Broker(int port, List<String> jvmNodeParams, GridConfig gridConfig, ResultsHandler resultsHandler,String serviceName) throws RemoteException
    {
    	this(port,jvmNodeParams,gridConfig,serviceName);
    	this.resultsHandler = resultsHandler;    	
    }

    public Broker(int port, List<String> jvmNodeParams, GridConfig gridConfig,String serviceName) throws RemoteException
    {
    	this.port = port;
		this.jvmNodeParams = jvmNodeParams;
		this.serviceName = serviceName;
		this.clusterSize = gridConfig.getClusterSize();
		this.libraryName = gridConfig.getLibraryName();
		this.blockingMap = new BlockingMap<String, IRemoteResult<V>>(2*clusterSize);
		this.resultsHandler = new ResultsHandler();
    	this.workQueue = new WorkQueue();
    	this.brokerInfo = new BrokerInfo(gridConfig,jvmNodeParams,serviceName,port);    	
    }
    
    /**
     * @return the clusterSize
     */
    public int getClusterSize()
    {
	    return this.clusterSize;
    }
    
    /**
     * @return the port
     */
    public int getPort()
    {
	    return this.port;
    }
    
    /**
     * @return the libraryName
     */
    public String getLibraryName()
    {
	    return this.libraryName;
    }
    
    public void start() throws Exception
    {
    	doRMIExport();		
    	creatCluster();    
    }

	/* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
	    return "Broker [clusterSize=" + this.clusterSize + ", libraryName=" + this.libraryName + ", port=" + this.port + "]";
    }

	/**
	 * Only used to collect results and insert them into blocking results Map.
	 * @author rkehoe
	 */
	private final class ResultsHandler extends UnicastRemoteObject implements IRemoteResultsHandler<V>,Serializable
	{		
		private static final long serialVersionUID = 2362910027458128946L;
		protected ResultsHandler() throws RemoteException
	    {
	        super();
	    }
	    @Override
	    public void accept(IRemoteResult<V> r) throws RemoteException
	    {
			blockingMap.putIfAbsent(r.getUID(), r);
	    }
	}
	
    private final FutureTask<V> getFutureRemoveTask(final String key)
    {
		Callable<V> task = new Callable<V>()
        {
        	@Override
            public V call() throws Exception
            {
                IRemoteResult<V> result = blockingMap.removeWhenPresent(key);                
                return result.getResult();
            }
        };
        return  new FutureTask<V>(task);
    }

	public FutureTask<V> dispatch(IExecutable<V> exe)
    {
    	try
        {
    		System.out.println("Enqueue: UID-"+exe.getUID()+"; "+exe.toString());
			this.blockingTransferWorkQueue.put(exe); // ... to the tail of the queue    							

	        FutureTask<V> future = getFutureRemoveTask(exe.getUID());
	        return future;
        }
        catch (Exception e)
        {
	        throw new RuntimeException( e );
        }
    }

	/* (non-Javadoc)
     * @see grid.cluster.shared.IBroker#unPause()
     */
    @Override
    public void unPause() throws RemoteException
    {
    	pauseGate.open();	    
    }

    /* (non-Javadoc)
     * @see grid.cluster.shared.IBroker#pause()
     */
    @Override
    public void pause() throws RemoteException
    {
    	pauseGate.close();	    
    }

	/* (non-Javadoc)
     * @see grid.common.IBroker#getWorkQueue()
     */
    @Override
    public IWorkQueue<V> getWorkQueue() throws RemoteException
    {
    	workQueueCallNumber++;
	    return (IWorkQueue<V>) this.workQueue;
    }

	private final class WorkQueue extends UnicastRemoteObject implements IWorkQueue<V>
	{
        private static final long serialVersionUID = 3288781574087157091L;

        protected WorkQueue() throws RemoteException
        {
	        super();
        }

		/* (non-Javadoc)
         * @see grid.common.IWorkQueue#take(grid.common.IExecutable)
         */
        @Override
        public IExecutable<V> take() throws RemoteException
        {
        	try
            {
        		pauseGate.await();
	            IExecutable<V> take = blockingTransferWorkQueue.take(); //... from the head of the queue
	            return take;
            }
            catch (InterruptedException e)
            {
	            throw new RemoteException( "Error retrieving work task.",e );
            }
        }
    }

    private void creatCluster()
    {
    	for (int i = 0; i < clusterSize; i++)
        {
    		Process process = NodeProcessFactory.createClusterNode(port,libraryName,jvmNodeParams,this.serviceName);
    		this.processList.add(process);
        }
    }

    private void doRMIExport() throws RemoteException
    {
    	/*
    	 * 	<bean id="broker" class="grid.client.Broker"></bean>
			<bean class="org.springframework.remoting.rmi.RmiServiceExporter">
				<property name="serviceName" value="clustered-executor-service" />
				<property name="service" ref="broker" />
        		<property name="serviceInterface" value="grid.common.IBroker"/>
				<property name="registryPort" value="1234" />
			</bean>
    	 */
    	try
        {
    		RmiServiceExporter s = new RmiServiceExporter(); 
    		s.setServiceName(this.serviceName);
    		s.setService(this);
    		s.setServiceInterface(grid.cluster.shared.IBroker.class);
    		s.setRegistryPort(this.port);
    		s.afterPropertiesSet();	        
    		this.rmiServiceExporter = s;
        }
        catch (Throwable e)
        {
        	e.printStackTrace();
	        throw new RuntimeException(e);
        }
		
    }

	/**
     * @throws RemoteException 
	 * @throws InterruptedException 
	 * @throws NoSuchObjectException 
     * 
     */
    @SuppressWarnings("unchecked")
    public void shutDown() throws RemoteException, InterruptedException
    {
    	for (int ii = 0; ii <= this.processList.size();ii++)
        {
    		this.dispatch((IExecutable<V>) IExecutable.POISON);	        	        
        }
    	
    	for (Process process : this.processList)
        {
    		if(process!=null)
    		{
    			int exitValue = process.waitFor();	        	        
    			Broker.log("Exit value for Process "+process+" = "+exitValue);    			
    		}
        }

    	try
        {
	        UnicastRemoteObject.unexportObject(workQueue, true);
        }
        catch (NoSuchObjectException e)
        {
        	e.printStackTrace();
        }

        try
        {
	        UnicastRemoteObject.unexportObject(resultsHandler,true);
        }
        catch (NoSuchObjectException e)
        {
        	e.printStackTrace();
        }
        
		this.rmiServiceExporter.destroy();
		Broker.log("Shutting down");
	}
    
	/* (non-Javadoc)
     * @see grid.common.IBroker#getCallback()
     */
    @Override
    public IRemoteResultsHandler<V> getCallback() throws RemoteException
    {
	    return (IRemoteResultsHandler<V>) this.resultsHandler;
    }

    /* (non-Javadoc)
     * @see grid.cluster.shared.IBroker#getConnectionID()
     */
    @Override
    public Integer getConnectionID() throws RemoteException
    {
	    return connectionID++;
    }

	/* (non-Javadoc)
     * @see grid.cluster.shared.IBroker#getBrokerInfo()
     */
    @Override
    public BrokerInfo getBrokerInfo() throws RemoteException
    {
	    return brokerInfo;
    }
}
