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
package com.rk.grid.server;

import com.rk.grid.cluster.shared.ITaskMonitor;


/**
 * Observer for messages from {@link ITaskMonitor} sent from
 * remotely executing {@link ITask}.
 * 
 * @author rkehoe
 */
public interface ITaskObserver
{
	/**
	 * Accept message from remote executing task.
	 * @param taskID
	 * @param arg
	 */
	void update(String taskID, Object arg);	
}
