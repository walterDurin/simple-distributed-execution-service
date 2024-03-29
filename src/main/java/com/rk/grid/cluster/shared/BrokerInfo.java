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
package com.rk.grid.cluster.shared;

import java.io.Serializable;
import java.util.List;

import com.sun.corba.se.pept.broker.Broker;

/**
 * A simle data object that holds {@link Broker} configuration 
 * and is used in remoting.
 * 
 * @author rkehoe
 *
 */
public class BrokerInfo implements Serializable
{
    private static final long serialVersionUID = 1L;
	private final GridConfig gridConfig;
	private final List<String> jvmNodeParams;
	private final String serviceName;
	private final int port;

	/**
	 * @param port 
	 * @param serviceName 
     * 
     */
    public BrokerInfo(GridConfig gridConfig, List<String> jvmNodeParams, String serviceName, int port)
    {
		this.gridConfig = gridConfig;
		this.jvmNodeParams = jvmNodeParams;
		this.serviceName = serviceName;
		this.port = port;
    }
    
	public GridConfig getConfig()
	{
		return gridConfig;		
	}

	/**
     * @return
     */
    public List<String> getJvmNodeParams()
    {
    	return jvmNodeParams;
    }

	/**
     * @return the serviceName
     */
    public String getServiceName()
    {
	    return serviceName;
    }

	/**
     * @return the port
     */
    public int getPort()
    {
	    return port;
    }

}
