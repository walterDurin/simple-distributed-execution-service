/**
 * 
 */
package com.rk.grid.testing;

import com.rk.grid.server.ITaskResult;
import com.rk.grid.util.Handler;


/**
 * @author rkehoe
 *
 */
public class TestHandler extends Handler<String>
{

    public TestHandler(String jobDescription)
    {
	    super(jobDescription);
    }
    
	@Override
	public void onResult(ITaskResult<String> r)
	{
		super.onResult(r);
	}
}
