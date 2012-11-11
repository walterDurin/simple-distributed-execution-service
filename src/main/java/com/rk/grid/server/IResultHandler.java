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
package com.rk.grid.server;

import java.util.Collection;
import java.util.Map;


/**
 * @author rkehoe
 *
 */
public interface IResultHandler<T>
{
    void onStarted();
    void onResult(ITaskResult<T> r);
    void onResult(IResultError r);
    void onError(Throwable e);
    void onCompleted();
    void onRejection(Collection<ITask<T>> rejectedTasks);
    Map<String, Integer> getThreadCounts();
	/**
	 * @return
	 */
    int getRejectedCount();
	/**
	 * @return
	 */
    int getErrorCount();
	/**
	 * @return
	 */
    long getElapsedTimeMillis();
	/**
	 * @return
	 */
    int getResultCount();
}
