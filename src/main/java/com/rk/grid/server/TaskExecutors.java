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
package com.rk.grid.server;

import java.util.Arrays;
import java.util.List;

import com.rk.grid.cluster.master.GridExecutorService;
import com.rk.grid.cluster.shared.GridConfig;

/**
 * Factory for {@link TaskExecutor}
 * 
 * @author rkehoe
 *
 */
public class TaskExecutors {

	/**
	 * @param numberClusterNodes - number of node in the cluster
	 * @param threadsPerNode - the size of the operational ThreadPool on each node
	 * @param nodeJVMArguments - variable length arguments (zero or more) 
	 * @return
	 */
	public static ITaskExecutor newFixedCluster(int numberClusterNodes, int threadsPerNode, String ... nodeJVMArguments )
    {
		return newFixedCluster(null, numberClusterNodes, threadsPerNode, nodeJVMArguments);
    }

	/**
	 * @param injectionContextFileName - the name of the Spring bean file that will provide beans for injected (with @link {@link Inject})
	 * @param numberClusterNodes - number of node in the cluster
	 * @param threadsPerNode - the size of the operational ThreadPool on each node
	 * @param nodeJVMArguments - variable length arguments (zero or more) 
	 * @return
	 */
	public static ITaskExecutor newFixedCluster(String injectionContextFileName, int numberClusterNodes, int threadsPerNode, String ... nodeJVMArguments )
    {
    	//TODO - can we streamline GridConfig? 
    	/**
    	<bean id="remoteInvocationrService" class="grid.cluster.master.InvocationService"> 
			<constructor-arg index="0" value="1234"/> 	<!-- broker port -->
			<constructor-arg index="1" >				<!-- node jvm args -->
				<util:list list-class="java.util.ArrayList">
					<value>-Dtangosol.coherence.cacheconfig=dist-cache-config.xml</value>
					<value>-Xms16m</value>
					<value>-Xmx100m</value>				
				</util:list>		
			</constructor-arg>
			<constructor-arg index="2" >				<!-- config for nodes & service -->
				<bean class="grid.cluster.shared.GridConfig">
					<property name="clusterSize" value="5" />
					<property name="remoteNodeThreadpoolSize" value="5" />
					<property name="libraryPath" value="C:\temp\Caf1.jar" />
					<property name="libraryName" value="Caf1" />
					<property name="injectionContext" value="Injection-context.xml" />				
				</bean>
			</constructor-arg>
			<constructor-arg index="3" value="Broker_A"/> 	<!-- broker service name -->
		</bean>
		*/
		try
        {			
			GridConfig gridConfig = new GridConfig();
			gridConfig.setClusterSize(numberClusterNodes);
			gridConfig.setRemoteNodeThreadpoolSize(threadsPerNode);
			gridConfig.setInjectionContext(injectionContextFileName);
			
			List<String> nodeCmdLineArgs = Arrays.asList(nodeJVMArguments);
			IInvocationService invocationService = new GridExecutorService(gridConfig,nodeCmdLineArgs);
	        TaskExecutor taskExecutor = new TaskExecutor(invocationService);
	        return taskExecutor;
        }
        catch (Exception e)
        {
	        throw new RuntimeException( e );
        }
    }

}
