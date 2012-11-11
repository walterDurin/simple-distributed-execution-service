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
import java.rmi.Remote;
import java.util.concurrent.Callable;

/**
 * This is just a Callable, with a UID, that
 * can be Serialized for RMI.
 * 
 * @author rkehoe
 *
 */
public interface IExecutable<V> extends Callable<V>,Serializable,Remote
{
	public final static IExecutable<Object> POISON = new IExecutable<Object>() 
	{
        private static final long serialVersionUID = 1L;

        public String getUID()
        {
	        return toString();
        }

        public Object call() throws Exception
        {
	        return null;
        }
		public String toString() {
			return "POISON";
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		/**
		 * We want to be able to compare instances
		 * accross JVMs!
		 */
		@Override
		public boolean equals(Object obj)
		{
			if ( this == obj ) return true;
			if(obj==null)return false;
			if(!(obj instanceof IExecutable<?>))return false;
			IExecutable<?> exe =(IExecutable<?>)obj;
			if(exe.getUID().equals(getUID()))return true;			
		    return false;
		}

        public Callable<Object> getCallable()
        {
	        return null;
        }
	};

	String getUID();

	/**
     * @return
     */
    Callable<V> getCallable();

}
