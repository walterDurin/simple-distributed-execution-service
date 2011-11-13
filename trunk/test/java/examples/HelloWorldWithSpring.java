package examples;

import grid.server.ITask;
import grid.server.ITaskObserver;
import grid.server.TaskExecutor;
import integration.proto.FibonacciTask;
import integration.proto.TestHandler;

import java.util.Collection;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 	Copyright 2011 rkehoe
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *	limitations under the License.
 *
 */

/**
 * Spring based example of distributed execution of {@link FibonacciTask}.
 * The {@link TaskExecutor} used is a Spring bean and there is
 * injection at runtime for injected fields (@see {@link Inject})
 * within the {@link FibonacciTask}.
 * 
 * @author rkehoe
 */
public class HelloWorldWithSpring
{

	public static void main(String[] args) throws Exception
	{
		ApplicationContext context = new ClassPathXmlApplicationContext("ExecutionServerPrototype-context.xml");		

		TaskExecutor executor = context.getBean(TaskExecutor.class);

		executor.unPause();

    	Collection<ITask<String>> tasks = FibonacciTask.generateTasks(3);
		
    	TestHandler resultHandler = new TestHandler("TestHelloWorld");
    	
    	ITaskObserver taskObserver = new ExampleTaskObserver();

		executor.execute(tasks,resultHandler,taskObserver);		
		
		resultHandler.await();
		
		executor.shutdown();
	}
}

class ExampleTaskObserver implements ITaskObserver
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
