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

/**
 * @author rkehoe
 *
 */
public class GridConfig implements Serializable
{
    private static final long serialVersionUID = 1L;

	public static final int DEFAULT_THREADPOOL_SIZE = 10;

	public static final int DEFAULT_CLUSTER_SIZE = 4;

	private int remoteNodeThreadpoolSize = DEFAULT_THREADPOOL_SIZE;
	private int clusterSize = DEFAULT_CLUSTER_SIZE; 
	private String libraryPath = "";
	private String libraryName = "";
	private String injectionContext = "";
	private String executionNamespace = "";
	
	public boolean libraryPathDefined()
	{
		return !this.libraryPath.isEmpty();
	}
	
	public boolean libraryNameDefined()
	{
		return !this.libraryName.isEmpty();
	}
	
	public boolean injectionContextDefined()
	{
		return this.injectionContext!=null && !this.injectionContext.isEmpty();
	}
	/**
     * @return the remoteNodeThreadpoolSize
     */
    public int getRemoteNodeThreadpoolSize()
    {
	    return this.remoteNodeThreadpoolSize;
    }
    
	/**
     * @param remoteNodeThreadpoolSize the remoteNodeThreadpoolSize to set
     */
    public void setRemoteNodeThreadpoolSize(int remoteNodeThreadpoolSize)
    {
	    this.remoteNodeThreadpoolSize = remoteNodeThreadpoolSize;
    }

	/**
     * @return
     */
    public String getLibraryPath()
    {
	    return libraryPath;
    }

    /**
     * @param libraryPath the libraryPath to set
     */
    public void setLibraryPath(String libraryPath)
    {
	    this.libraryPath = libraryPath;
    }

	/**
     * @param libraryName the libraryName to set
     */
    public void setLibraryName(String libraryName)
    {
	    this.libraryName = libraryName;
    }

	/**
     * @return the libraryName
     */
    public String getLibraryName()
    {
	    return libraryName;
    }

	/**
     * @param clusterSize the clusterSize to set
     */
    public void setClusterSize(int clusterSize)
    {
	    this.clusterSize = clusterSize;
    }

	/**
     * @return the clusterSize
     */
    public int getClusterSize()
    {
	    return clusterSize;
    }

	/**
     * @param injectionContext the injectionContext to set
     */
    public void setInjectionContext(String injectionContext)
    {
	    this.injectionContext = injectionContext;
    }

	/**
     * @return the injectionContext
     */
    public String getInjectionContext()
    {
	    return injectionContext;
    }

	/**
	 * @return the executionNamespace
	 */
    public String getExecutionNamespace()
    {
	    return executionNamespace;
    }

	/**
	 * @param executionNamespace the executionNamespace to set
	 */
    public void setExecutionNamespace(String executionNamespace)
    {
	    this.executionNamespace = executionNamespace;
    }
}
