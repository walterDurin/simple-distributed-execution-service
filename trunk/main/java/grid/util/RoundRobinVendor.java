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
package grid.util;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author rkehoe
 *
 * @param <T>
 */
public class RoundRobinVendor<T>
{
	private final ArrayList<T>	cont	= new ArrayList<T>();
	private long	           counter	 = 0;

    public RoundRobinVendor() { }
    
    public RoundRobinVendor(Collection<T> c)
    {
    	cont.addAll(cont);
    }
    
	public T get()
	{
		if (cont.size() == 0) return null;
		
		int i = (int) (counter%cont.size());

		return cont.get(i);
	}

	public synchronized void add(T t)
	{
		cont.add(t);
	}

    public synchronized int size()
    {
	    return this.cont.size();
    }
}
