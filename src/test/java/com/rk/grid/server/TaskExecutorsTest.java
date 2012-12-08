/**
 * 	Copyright 2012 RK
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
package com.rk.grid.server;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rk.grid.testing.ErrorTask;
import com.rk.grid.testing.FibonacciTask;
import com.rk.grid.testing.TestHandler;
import com.rk.grid.testing.TestObserver;

/**
 * @author RK
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/ExecutionServerPrototype-context.xml"})
public class TaskExecutorsTest
{

	@Autowired
	private ITaskExecutor 	taskExecutor;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception
	{
	}

	/**
	 * Test method for {@link com.rk.grid.server.TaskExecutors#newFixedCluster(int, int, java.lang.String[])}.
	 * @throws Exception 
	 */
	@Test
	public void testNewFixedClusterIntIntStringArray() throws Exception
	{
		ITaskExecutor executor = TaskExecutors.newFixedCluster(3,1,"-Xms20m");
		executor.unPause();
    	int taskCount = 30;
    	Collection<ITask<String>> tasks = FibonacciTask.generateTasks(taskCount);
    	TestHandler resultHandler = new TestHandler("TestHelloWorld");

		executor.execute(tasks,resultHandler);		
		
		resultHandler.await();
		executor.shutdown();
		
		assertEquals(taskCount,resultHandler.getResultCount());
		assertEquals(0,resultHandler.getRejectedCount());
		assertEquals(0,resultHandler.getErrorCount());
	}

	/**
	 * Test method for {@link com.rk.grid.server.TaskExecutors#newFixedCluster(java.lang.String, int, int, java.lang.String[])}.
	 * @throws Exception 
	 */
	@Test
	public void testNewFixedClusterStringIntIntStringArray() throws Exception
	{
		ITaskExecutor executor = TaskExecutors.newFixedCluster("Task-Autowiring-Example-context.xml", 3,1,"-Xms20m");
		executor.unPause();

		int taskCount = 3;
    	Collection<ITask<String>> tasks = FibonacciTask.generateTasks(taskCount);
    	TestHandler resultHandler = new TestHandler("TestHelloWorld");

		executor.execute(tasks,resultHandler);		
		
		resultHandler.await();
		executor.shutdown();

		assertEquals(taskCount,resultHandler.getResultCount());
		assertEquals(0,resultHandler.getRejectedCount());
		assertEquals(0,resultHandler.getErrorCount());
	}

	/**
	 * Test method for {@link com.rk.grid.server.TaskExecutors#newFixedCluster(java.lang.String, int, int, java.lang.String[])}.
	 * @throws Exception 
	 */
	@Test
	public void testSpringContextLoading() throws Exception
	{

		this.taskExecutor.unPause();

    	int taskCount = 123;
		Collection<ITask<String>> tasks = FibonacciTask.generateTasks(taskCount);
    	TestHandler resultHandler = new TestHandler("TestHelloWorld");    	
    	ITaskObserver taskObserver = new TestObserver();

		this.taskExecutor.execute(tasks,resultHandler,taskObserver);		
		
		resultHandler.await();		
		this.taskExecutor.shutdown();
		
		assertEquals(taskCount,resultHandler.getResultCount());
		assertEquals(0,resultHandler.getRejectedCount());
		assertEquals(0,resultHandler.getErrorCount());		
	}

	/**
	 * Test method for {@link com.rk.grid.server.TaskExecutors#newFixedCluster(java.lang.String, int, int, java.lang.String[])}.
	 * @throws Exception 
	 */
	@Test
	public void testErrorReporting() throws Exception
	{
		ITaskExecutor executor = TaskExecutors.newFixedCluster(3,1,"-Xms20m");
		executor.unPause();
    	int taskCount = 30;
    	List<ITask<String>> tasks = FibonacciTask.generateTasks(taskCount);
    	
    	tasks.add(new ErrorTask());
    	
    	Collections.shuffle(tasks);
    	
    	TestHandler resultHandler = new TestHandler("TestHelloWorld");

		executor.execute(tasks,resultHandler);		
		
		resultHandler.await();
		executor.shutdown();
		
		assertEquals(taskCount,resultHandler.getResultCount());
		assertEquals(0,resultHandler.getRejectedCount());
		assertEquals(1,resultHandler.getErrorCount());
	}


}
