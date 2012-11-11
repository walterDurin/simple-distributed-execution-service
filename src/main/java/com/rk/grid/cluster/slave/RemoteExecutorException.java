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

import java.rmi.RemoteException;

/**
 * @author rkehoe
 *
 */
public class RemoteExecutorException extends RemoteException
{
    private static final long serialVersionUID = 8300576424011252401L;

    @SuppressWarnings("unused")
    private RemoteExecutorException()
    {
    }

    public RemoteExecutorException(String msg, Throwable ex)
    {
    	super(msg, ex);
    }

    public RemoteExecutorException(String msg)
    {
    	super(msg);
    }
}
