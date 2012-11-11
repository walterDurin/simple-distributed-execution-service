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
package com.rk.grid.util;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * This is a Map-like container whose {@link #get(String)} method blocks *if* a
 * key/value pair doesn't exist within the container - the calling thread is
 * unblocked when another thread inserts. This class essentially says
 * "wait until the thing-associated-with-this-key is delivered".
 * 
 * It's a bit like Map based version of {@link BlockingQueue}.
 * 
 * @author RKehoe
 * 
 * @param <K,E>
 */
public class BlockingMap<K, V>
{
	/**
	 * We need to synchronise on this map anyways SO we will not bother to make
	 * it a ConcurrentHashMap.
	 */
    private final ConcurrentMap<K, Entry<V>>	map;

	/**
     * @return
     * @see java.util.Map#keySet()
     */
    public Set<K> keySet()
    {
	    return this.map.keySet();
    }

	private int	                             numberOfCountDownLatches	= 0;

	private int	                             numberOfInsertions	     = 0;

	private int	                             numberOfRemovals;

	private int 							numberOfHitsWithWaiting;

	private int 							numberOfNonWaitHits;

	private class Entry<T>
	{
		private T		       object;
		private CountDownLatch	latch;

		Entry(T obj)
		{
			this.object = obj;
			numberOfInsertions++;
		}

		Entry()
		{
			this.latch = new CountDownLatch(1);
			BlockingMap.this.numberOfCountDownLatches++;
		}

		@Override
		public String toString()
		{
			return "Entry[" + object + ",Latch count=" + (latch != null ? latch.getCount() : "null") + "]";
		}

		/**
		 * @return
		 * @throws InterruptedException
		 */
		final T getObject() throws InterruptedException
		{
			if (this.object != null)
			{
				BlockingMap.this.numberOfNonWaitHits++;
				return this.object;
			} else
			{
				this.latch.await();
				BlockingMap.this.numberOfHitsWithWaiting++;
				this.latch = null;
				return this.object;
			}
		}

        public T getObject(long timeout, TimeUnit unit) throws InterruptedException
        {
			if (this.object != null)
			{
				BlockingMap.this.numberOfNonWaitHits++;
				return this.object;
			} else
			{
				BlockingMap.this.numberOfHitsWithWaiting++;
				boolean successfullCountdownWithNoTimeout = this.latch.await(timeout,unit);
				if(successfullCountdownWithNoTimeout)
				{
					this.latch = null;
					return this.object;					
				}
				else
				{
					return null;
				}
			}
        }

		final boolean isEmpty()
		{
			return this.object == null;
		}

		final void setObject(T t)
		{
			numberOfInsertions++;
			this.object = t;
			this.latch.countDown();
		}
	}


	private final ExecutorService executorService;

	public BlockingMap(int poolSize)
	{
		this(new ConcurrentHashMap<K, Entry<V>>(),poolSize);
	}

    public BlockingMap(ConcurrentMap<K, Entry<V>> map, int poolSize)
	{
		this.map = map;
		executorService = Executors.newFixedThreadPool(poolSize);
	}

	/**
	 * Associates a key with a value.
	 * 
	 * Will NOT re-associate a key with a different value once an association
	 * made - this is in order to maintain a consistent view of the world across 
	 * different threads.
	 * 
	 * @param key
	 * @param value
	 * @return true if key not already associated with value
	 * 
	 */
	public boolean putIfAbsent(K key, V value)
	{
		if(key == null || value == null )
		{
			throw new IllegalArgumentException("Nulls in Key/Value -> "+key+":"+value);
		}
		
		synchronized(this.map)
		{
			Entry<V> entry = this.map.get(key);
			if(entry==null)
			{
				entry = new Entry<V>(value);
				return this.map.putIfAbsent(key, entry) != null; 
			}
			else
			{
				if (entry.isEmpty())
				{
					entry.setObject(value);
					return true;
				} 
				else
				{
					return false;
				}
			}
		}
	}

	public boolean _putIfAbsent(K key, V value)
	{
		synchronized(this.map)
		{
		Entry<V> entry = this.map.get(key);
		
			/*
			 * If there is an entry already this means that there's someone waiting
			 * - so we add the value and release/countdown the latch.
			 */
			if (entry != null)
			{
				{
				if (entry.isEmpty())
				{
					entry.setObject(value);
					return true;
				} else
				{
					return false;
					// /*
					// * Can't go realigning things at
					// * this stage - may have Threads
					// * blocking already...
					// */
					// if(!entry.obj.equals(value))
					// {
					// String msg =
					// "Key '"+key+"' already associated with value '"+entry.obj+"'"
					// +
					// " and cannot be re-associated with value '"+value+"'";
					// throw new IllegalArgumentException(msg);
					// }
				}
				}
			} else
			{
				if (this.map.putIfAbsent(key, new Entry<V>(value)) != null)
				{
					return true;
				} else
					return false;
			}
			
		}
	}

	public boolean contains(String key)
	{
		Entry<V> entry = this.map.get(key);
		return entry!=null && !entry.isEmpty();
	}

	/**
	 * A *blocking* removal method.
	 * 
	 * @param key
	 * @return
	 * @throws InterruptedException
	 */
	public V removeWhenPresent(K key)
	{
//		// This gives *all* current waiting clients
//		// a reference
//		 E t = this.get(key);
//		 this.map.remove(key);
//		 return t;

		/*
		 *  This gives only one waiting client the object
		 */
		// make sure entry exists or wait until inserted
		try
        {
	        this.get(key);
	        Entry<V> entry = this.map.remove(key);
	        if (entry != null)
	        {
	        	this.numberOfRemovals++;
	        	return entry.getObject();
	        } else
	        {
	        	return null;
	        }
        }
        catch (InterruptedException e)
        {
	        throw new RuntimeException( e );
        }
	}

	/**
	 * A non-blocking removal method.
	 * 
	 * @param key
	 * @return
	 * @throws InterruptedException
	 */
	public V removeIfPresent(K key) throws InterruptedException
	{
		Entry<V> entry = this.map.remove(key);
		if (entry != null && !entry.isEmpty())
		{
			this.numberOfRemovals++;
			return entry.getObject();
		} 
		else
		{
			return null;
		}
	}

	@Override
	public String toString()
	{
		String msg = "Size=" + this.size() + "; Insertions=" + this.numberOfInsertions + "; Removals=" + this.numberOfRemovals + "; " +
				"Latches used=" + this.numberOfCountDownLatches +
				"; Hits with no wait=" + this.numberOfNonWaitHits +
				"; Hits with waits=" + this.numberOfHitsWithWaiting
				;
		return msg;
	}

	public String contentsToString()
	{
		return this.map.toString();
	}

	/**
	 * Will BLOCK if value doesn't exist, i.e., until an entry 
	 * is made with this key by a separate thread.
	 * 
	 * @param key
	 * @return
	 * @throws InterruptedException
	 */
	public V get(K key) throws InterruptedException
	{
		Entry<V> entry = this.map.get(key);

		if (entry == null)
		{
            this.map.putIfAbsent(key, new Entry<V>());
            entry = this.map.get(key);
		}

		return entry.getObject();
	}

	/**
	 * Will BLOCK for timeout if value doesn't exist, 
	 * i.e., until an entry is made with this key 
	 * by a separate thread.
	 * 
	 * @param key
	 * @param timeout
	 * @param unit
	 * @return value if no timeout - null if timeout
	 * @throws InterruptedException
	 */
	public V get(K key, long timeout, TimeUnit unit) throws InterruptedException
	{
		Entry<V> entry = this.map.get(key);

		if (entry == null)
		{
            this.map.putIfAbsent(key, new Entry<V>());
            entry = this.map.get(key);
		}

		return entry.getObject(timeout,unit);
	}

    private static void log(String x)
    {
	    System.out.println(x);
    }

	public static void main(String[] args) throws InterruptedException
	{
		
		final BlockingMap<String, String> map2 = new BlockingMap<String, String>(10);

		map2.putIfAbsent("Hello", "World");

		log("Hello > " + map2.get("Hello"));

		int nThreads = 3;
		for (int i = 0; i < nThreads; i++)
		{
			final int j = i;
			Thread t = new Thread()
			{
				@Override
				public void run()
				{
					try
					{
						String key = "T1." + j;
						log("Requesting item w/ key '" + key + "' for Thread " + this.getName());
//						String string = map2.get(key);
//						print("Received item w/ key '" + key + "' -> " + string + " for Thread " + this.getName());
						Future<String> stringF = map2.getFuture(key);
						log("Received item w/ key '" + key + "' -> " + stringF.get() + " for Thread " + this.getName());
					} catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			};
			t.start();
			Thread t2 = new Thread()
			{
				@Override
				public void run()
				{
					String key = "T2." + j;
                    log("Requesting item w/ key '" + key + "' for Thread " + this.getName());
                    String string = map2.removeWhenPresent(key);
                    log("Received item w/ key '" + key + "' -> " + string + " for Thread " + this.getName());
				}
			};
			t2.start();
		}
		Thread.sleep(2000);
		for (int i = nThreads - 1; i >= 0; i--)
		{
			final int j = i;
			Thread t = new Thread()
			{
				@Override
				public void run()
				{
					map2.putIfAbsent("T1." + j, "Hello from T3." + j);
					map2.putIfAbsent("T2." + j, "Hello from T3." + j);
				}
			};
			t.start();
			Thread.sleep(200);
		}

		log("Contents: " + map2);
		
//		map2.executorService.shutdown();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable
	{
		this.executorService.shutdown();
	    super.finalize();
	}
	
    /**
     * Gets a FutureTask of a get() operation.
     * @param key
     * @return
     */
    public FutureTask<V> getFutureRemoveTask(final K key)
    {
		Callable<V> task = new Callable<V>()
        {
        	@Override
            public V call() throws Exception
            {
                return BlockingMap.this.removeWhenPresent(key);
            }
        };
        return  new FutureTask<V>(task);
    }

    /**
     * Gets a FutureTask of a get() operation with the value to return.
     * @param key
     * @param returnValue the value to return upon successful completion.
     * @return FutureTask with returnValue
     */
    public FutureTask<V> getFutureRemoveTask(final K key, final V returnValue)
    {
		Callable<V> task = new Callable<V>()
        {
        	@Override
            public V call() throws Exception
            {
                BlockingMap.this.removeWhenPresent(key);
                return (V) returnValue;
            }
        };
        FutureTask<V> ft = new FutureTask<V>(task);
        return ft;
    }

    /**
     * @param key
     * @return
     */
    public Future<V> getFuture(final K key)
    {
    	Future<V> future = executorService.submit(
    		new Callable<V>()
    		{
				@Override
	            public V call() throws Exception
	            {
		            return BlockingMap.this.get(key);
	            }}
    		);
	    return future; 
    }

	/**
	 * @return
	 */
	public int size()
	{
		return this.map.size();
	}

}
