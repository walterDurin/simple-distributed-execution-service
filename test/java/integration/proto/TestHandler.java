/**
 * 
 */
package integration.proto;

import grid.server.ITaskResult;
import grid.util.Handler;

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
		log("Handler - result: "+r.get());
	}
}
