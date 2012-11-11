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

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class GateLatch
{
	private static final int	OPEN	= 0;
	private static final int	CLOSED	= -1;

	private static class Sync extends AbstractQueuedSynchronizer
	{
		private static final long	serialVersionUID	= 3L;

		private Sync()
		{
			setState(CLOSED);
		}

		@Override
		protected int tryAcquireShared(int acquires)
		{
			return (getState()==OPEN)? 1 : -1;
		}

		@Override
		protected boolean tryReleaseShared(final int state)
		{
			setState(state);
			return true;
		}
	}

	private final Sync	sync	= new Sync();

	public void close()
	{
		sync.releaseShared(CLOSED);
	}

	public void open()
	{
		sync.releaseShared(OPEN);
	}

	public void await() throws InterruptedException
	{
		sync.acquireSharedInterruptibly(0);
	}
}
