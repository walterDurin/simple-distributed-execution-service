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
package com.rk.grid.cluster.slave;

import java.rmi.server.UID;

import com.rk.grid.cluster.shared.IRemoteResult;

public class RemoteResult<T> implements IRemoteResult<T>
{
	private final static String JVM_ID = (new UID()).toString();
    private static final long serialVersionUID = -4143095842963416459L;

    private T result;
	private String uid;

	private final Exception exception;
	private String jvmID;

    public RemoteResult()
    {
    	exception = null;
    }
    
    public RemoteResult(T result, String uid)
    {
		this.result = result;
		this.uid = uid;
		this.exception = null;
		this.jvmID = JVM_ID;
    }

    public RemoteResult(Exception exception, String uid)
    {
		this.exception = exception;
		this.uid = uid;
    }

    @Override
    public T getResult() throws Exception
    {
    	if(exception!=null)throw this.exception;
    	return (T) result;
    }

    @Override
    public String getUID()
    {
	    return this.uid;
    }

	/* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
	    return "RemoteResult [exception=" + this.exception + ", jvmID=" + this.jvmID + ", result=" + this.result + ", uid=" + this.uid + "]";
    }    
    
}

