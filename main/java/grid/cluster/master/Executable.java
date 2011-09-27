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

import grid.cluster.shared.IExecutable;

import java.util.concurrent.Callable;


/**
 * @author rkehoe
 *
 */
public class Executable<T> implements IExecutable<T>
{
    private static final long serialVersionUID = 1350865490035672495L;
    
	private final long id;
	private final Callable<T> callable;

    public Executable(Callable<T> c, long id)
    {
		this.callable = c;
		this.id = id;
    }

	/* (non-Javadoc)
     * @see grid.common.IExecutable#getUID()
     */
    @Override
    public String getUID()
    {
	    return ""+id;
    }

	/* (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public T call() throws Exception
    {
	    return this.callable.call();
    }

	/* (non-Javadoc)
     * @see grid.common.IExecutable#getCallable()
     */
    @Override
    public Callable<T> getCallable()
    {
	    return this.callable;
    }

	/* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
	    return "Executable [callable=" + this.callable + ", id=" + this.id + "]";
    }

    
}
