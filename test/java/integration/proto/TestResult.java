/**
 * 
 */
package integration.proto;

import grid.server.ITaskResult;

/**
 * @author rkehoe
 *
 */
public class TestResult implements ITaskResult<String>
{
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
	private final String id;
	private String result;
	private String executionThreadName;

	/**
     * 
     */
    public TestResult(String id)
    {
		this.id = id;
    	this.executionThreadName = Thread.currentThread().getName();
    }
    
	/**
     * @param id2
     * @param string
     */
    public TestResult(String id, String result)
    {
    	this.id = id;
    	this.result = result;
    	this.executionThreadName = Thread.currentThread().getName();
    }

	/* (non-Javadoc)
	 * @see grid.server.ITaskResult#getTaskID()
	 */
	@Override
	public String getTaskID()
	{
		return id;
	}

	/* (non-Javadoc)
     * @see grid.server.ITaskResult#get()
     */
    @Override
    public String get()
    {
	    return result;
    }

	/* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
	    return "TestResult [id=" + this.id + ", result=" + this.result + "]";
    }

	/* (non-Javadoc)
     * @see grid.server.ITaskResult#getExecutionThreadName()
     */
    @Override
    public String getExecutionThreadName()
    {
	    return executionThreadName;
    }
    
    

}
