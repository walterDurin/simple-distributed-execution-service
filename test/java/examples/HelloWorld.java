package examples;

import grid.server.ITask;
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
 * 
 * @author rkehoe
 */
public class HelloWorld
{

	public static void main(String[] args) throws Exception
	{
		ApplicationContext context = new ClassPathXmlApplicationContext("ExecutionServerPrototype-context.xml");		

		TaskExecutor te = context.getBean(TaskExecutor.class);

		te.unPause();

    	Collection<ITask<String>> tasks = FibonacciTask.generateTasks(3);
		
    	TestHandler handler = new TestHandler("TestHelloWorld");

		te.execute(tasks,handler);		
		
		handler.await();
		
		te.shutdown();
	}

}