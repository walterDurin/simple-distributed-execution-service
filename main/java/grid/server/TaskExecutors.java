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

import grid.cluster.master.RemoteInvocationService;
import grid.cluster.shared.GridConfig;

import java.util.Arrays;
import java.util.List;

/**
 * Factory for {@link TaskExecutor}
 * 
 * @author rkehoe
 *
 */
public class TaskExecutors {

	/**
	 * @param numberClusterNodes
	 * @param threadsPerNode
	 * @param nodeJVMArguments
	 * @return
	 */
	public static TaskExecutor newFixedCluster(int numberClusterNodes, int threadsPerNode, String ... nodeJVMArguments )
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
			
			List<String> nodeCmdLineArgs = Arrays.asList(nodeJVMArguments);
			IInvocationService invocationService = new RemoteInvocationService(gridConfig,nodeCmdLineArgs);
	        TaskExecutor taskExecutor = new TaskExecutor(invocationService);
	        return taskExecutor;
        }
        catch (Exception e)
        {
	        throw new RuntimeException( e );
        }
    }

}
