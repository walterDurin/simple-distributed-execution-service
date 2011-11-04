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
package grid.cluster.slave;

import grid.cluster.shared.IBroker;
import grid.cluster.shared.IExecutable;
import grid.cluster.shared.IProgress;
import grid.cluster.shared.IProgressMonitor;
import grid.cluster.shared.IRemoteResultsHandler;
import grid.cluster.shared.IWorkQueue;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;


class RemoteExecutorNode<T> 
{
	private ExecutorService exeService;
	private IRemoteResultsHandler<T> resultsHandler;
	private IWorkQueue<T> workQueue;
	private int threadpoolSize;
	private Integer connectionID;
	protected String brokerServiceName;
	private transient ArrayList<IInterceptor> interceptors = new ArrayList<IInterceptor>();
	
    public RemoteExecutorNode(IBroker<T> broker) throws RemoteException
    {
    	this.resultsHandler = (IRemoteResultsHandler<T>) broker.getCallback();
    	this.workQueue = broker.getWorkQueue();
    	this.threadpoolSize = broker.getBrokerInfo().getConfig().getRemoteNodeThreadpoolSize();
    	this.connectionID = broker.getConnectionID();
    	this.brokerServiceName=broker.getBrokerInfo().getServiceName();
    }
      
    public void start() 
    {
    	ThreadFactory threadFactory = new ThreadFactory()
		{
    		private int counter = 0;
    		private String prefix = brokerServiceName+":Node-"+connectionID;// new UID().toString();
    		 
			@Override
			public Thread newThread(Runnable r)
			{
				return new Thread(r, prefix + ":Thread-" + counter++);
			}
		};

		/*
		 * We ADD 1 to the threadPoolSize because we soak up one thread fetching
		 * jobs off the work queue.
		 */
		this.exeService = Executors.newFixedThreadPool(this.threadpoolSize+1,threadFactory);
    	
    	@SuppressWarnings("unused")
        final Future<Object> future = this.exeService.submit(new Callable<Object>() 
    	{
    	    @Override
    	    public Object call() throws Exception
    	    {
    	    	try
                {
	                while (!Thread.currentThread().isInterrupted())
	                {
	                    IExecutable<T> executable = workQueue.take();
//	                    if(true)throw new RuntimeException();
	                    //TODO - for testing only -remove
	                    if(executable==null)continue;
	                    if(executable.equals(IExecutable.POISON))
	                    {
	                    	break;
	                    }
	                    submit(executable);
	                }
                }
                catch (Throwable e)
                {
                	e.printStackTrace();                	
                }
                finally
                {
                	stop();
                }
    		    return null;
    	    }
    	});
    	
    	IInterceptor methodInterceptor = new IInterceptor() {
			@Override
            public void process(IExecutable<?> ex)
            {
	            
            }
		};

        this.add(methodInterceptor);
    }

    final private synchronized void stop()
    {
    	log("Shutting down remote executor "+this);    	
    	this.exeService.shutdown();
    }

    private static void log(String x)
    {
	    System.out.println(x);
    }

	private void submit(final IExecutable<T> executable)
    {
    	preProcess(executable);
    	Runnable runnable = new Runnable() 
    	{
			@Override
            public void run()
            {
               	String uid = executable.getUID();
				try
                {
	                final T returnedValue = executable.call();
	                resultsHandler.accept(new RemoteResult<T>(returnedValue,uid));						
                }
                catch (Throwable t)
                {
                	t.printStackTrace();
					try
                    {
	                    resultsHandler.accept(new RemoteResult<T>(new RemoteExecutorException("Error execution remote task '"+uid+"'",t),uid));
                    }
                    catch (RemoteException e)
                    {
	                    throw new RuntimeException( e );
                    }
                }    
            }
		};
        exeService.submit(runnable);            
    }

    /**
     * @param executable
     */
    private void preProcess(IExecutable<T> executable)
    {
    	for (IInterceptor it : this.interceptors)
        {
	        it.process(executable);
        }
    }

	/**
     * @param i
     */
    public void add(IInterceptor interceptor)
    {
    	if(!this.interceptors.contains(interceptor))
    	this.interceptors.add(interceptor); 
    }
}
