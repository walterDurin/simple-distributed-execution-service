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
package grid.server;

import java.util.concurrent.ExecutorService;

/**
 * Defines an ExecutorService that is associated with a particular
 * invocation space (version of a Jar).
 * Also acts as Marker Interface to inform users that this service
 * is clustered (... and *may* execute on a remote VM).
 * 
 * @author rkehoe
 */
public interface IInvocationService extends ExecutorService
{

	/** 
	 * Synonymous with library/jar versions on classpath of this service.
	 *  
     * @return namespace
     */
    String getNamespace();

    void start() throws Exception;

    void pause();

	/**
     * 
     */
    void unPause();
}
