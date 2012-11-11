package com.rk.grid.testing;

import com.rk.grid.server.ITaskObserver;

public class TestObserver implements ITaskObserver
{
	/* (non-Javadoc)
     * @see grid.server.IProgressObserver#update(java.lang.String, java.lang.Object)
     */
    @Override
    public void update(String taskID, Object arg)
    {
    	System.out.println("Progress: task="+taskID+" - "+arg);
    }
}
