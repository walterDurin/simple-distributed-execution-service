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
package grid.federation;

import grid.cluster.master.InvocationService;
import grid.cluster.shared.GridConfig;
import grid.cluster.shared.IBroker;
import grid.cluster.shared.IExecutable;
import grid.cluster.shared.IRemoteResultsHandler;
import grid.cluster.shared.IWorkQueue;
import grid.cluster.slave.RemoteExecutorException;
import grid.cluster.slave.RemoteResult;
import grid.server.ITaskResult;
import grid.server.TaskExecutor;

import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.remoting.RemoteLookupFailureException;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

/**
 * @author rkehoe
 * 
 */
public class FederatedCluster
{
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
		int port = Integer.parseInt(args[0]);
		String serviceName = args[1];
		String remoteServiceName = args[2];

		IBroker<Object> masterBroker = null;
		for (int i = 0; i < 100; i++)
		{
			try
			{
				masterBroker = getConnection(remoteServiceName);
				if (masterBroker != null)
					break;
			}
			catch (RemoteLookupFailureException e)
			{
				if (i % 100 == 0)
					System.out.println("Sleeping....");
			}
			Thread.sleep(100);
		}

		GridConfig gridConfig = masterBroker.getBrokerInfo().getConfig();
		List<String> jvmNodeParams = masterBroker.getBrokerInfo().getJvmNodeParams();
		InvocationService is = new InvocationService(port,jvmNodeParams,gridConfig,serviceName);
		is.getBroker().unPause();
		
		final TaskExecutor taskExecutor = new TaskExecutor(is);

		final IRemoteResultsHandler<Object> callback = masterBroker.getCallback();
		IWorkQueue<Object> workQueue = masterBroker.getWorkQueue();

		ExecutorService pool = Executors.newFixedThreadPool(3);

		masterBroker.unPause();
		
		while (!Thread.currentThread().isInterrupted())
		{
			final IExecutable<?> executable = workQueue.take();

			if (executable == null)
				continue;

			if (executable.equals(IExecutable.POISON))
			{
				break;
			}

			Callable<Object> callable = new Callable<Object>()
			{
				@Override
				public Object call() throws Exception
				{
					Future<ITaskResult<?>> future = taskExecutor.submit(executable);
					ITaskResult<?> iResult = future.get();

					String uid = executable.getUID();
					try
					{
						callback.accept(new RemoteResult<Object>(iResult,uid));
					}
					catch (Throwable t)
					{
						t.printStackTrace();
						try
						{
							callback.accept(new RemoteResult<Object>(new RemoteExecutorException("Error execution remote task '" + uid + "'", t), uid));
						}
						catch (RemoteException e)
						{
							throw new RuntimeException(e);
						}
					}
					return null;
				}

			};

			pool.submit(callable);
		}
		pool.shutdown();
 		taskExecutor.shutdown();
		System.out.println("Finished...!");
	}

	@SuppressWarnings("unchecked")
    private static IBroker<Object> getConnection(String serviceName)
	{
		RmiProxyFactoryBean s = new RmiProxyFactoryBean();
		s.setServiceUrl("rmi://localhost:" + 1234 + "/" + serviceName);
		s.setServiceInterface(IBroker.class);
		s.afterPropertiesSet();

		IBroker<Object> broker = (IBroker<Object>) s.getObject();
		return broker;
	}
}