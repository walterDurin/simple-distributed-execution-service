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
package grid.cluster.shared;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * Tethers satellite JVMs
 * 
 * @author rkehoe
 *
 */
public interface IBroker<V> extends Serializable
{
	//public final static String SERVICE_NAME = "clustered-executor-broker";
		
	public IRemoteResultsHandler<V> getCallback() throws RemoteException;

	public IWorkQueue<V> getWorkQueue() throws RemoteException;
	
	public BrokerInfo getBrokerInfo() throws RemoteException;

//	/**
//     * @param fwq
//     */
//    @Federation
//    public void offerFederatedWorkQueue(IFederatedWorkQueue<Object> fwq) throws RemoteException;

	public void unPause() throws RemoteException;
	public void pause() throws RemoteException;
	
	/**
     * @return
     */
    public Integer getConnectionID() throws RemoteException;
}
