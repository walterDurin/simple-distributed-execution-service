package examples;

import grid.server.ITask;
import grid.server.ITaskExecutor;
import grid.server.TaskExecutors;
import integration.proto.FibonacciTask;
import integration.proto.TestHandler;

import java.util.Collection;

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
 * Simple non-Spring example of distributed execution  of 
 * {@link FibonacciTask} - but with no runtime injection 
 * (@see {@link Inject}).
 * 
 * @author rkehoe
 */
public class HelloWorld
{
	public static void main(String[] args) throws Exception
	{
		ITaskExecutor executor = TaskExecutors.newFixedCluster(3,1,"-Xms20m");

		executor.unPause();

    	Collection<ITask<String>> tasks = FibonacciTask.generateTasks(3);

    	TestHandler handler = new TestHandler("TestHelloWorld");

		executor.execute(tasks,handler);		
		
		handler.await();
		
		executor.shutdown();
	}

}
