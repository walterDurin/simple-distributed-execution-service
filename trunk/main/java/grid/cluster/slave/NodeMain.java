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
package grid.cluster.slave;

import grid.cluster.shared.GridConfig;
import grid.cluster.shared.IBroker;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;


/**
 * @author rkehoe
 * 
 */
public class NodeMain
{
	@SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException
	{
		String host = "localhost";
		String port = null;
		int indx = 0;
		String serviceName=null;
		System.out.println("Node bootstrap params: "+Arrays.asList(args));
		while (indx < args.length && args[indx].startsWith("-"))
		{
			if (args[indx].equalsIgnoreCase("-p"))
			{
				port = args[++indx];
				log("Port="+port);
			}
			else if (args[indx].equalsIgnoreCase("-h"))
			{
				host = args[++indx];
				log("Host="+host);
			}
			else if (args[indx].equalsIgnoreCase("-b"))
			{
				serviceName = args[++indx];
				log("Broker ServiceName="+serviceName);
			}
			indx++;
		}

		if (port == null)
		{
			throw new RuntimeException("Port not set");
		}

		if (serviceName == null)
		{
			throw new RuntimeException("Broker Service Name not set");
		}

		RmiProxyFactoryBean s = new RmiProxyFactoryBean();
		s.setServiceUrl("rmi://" + host + ":" + port + "/" + serviceName);
		s.setServiceInterface(IBroker.class);
		s.afterPropertiesSet();

		IBroker<Object> broker = (IBroker<Object>) s.getObject();

		RemoteExecutorNode<Object> remoteExecutor = new RemoteExecutorNode<Object>(broker);

		GridConfig gridConfig = broker.getBrokerInfo().getConfig();
		String libraryPath = gridConfig.getLibraryPath();
		ClassLoader loader = getClassLoader(libraryPath);
		Thread.currentThread().setContextClassLoader(loader);

		String injectionContext = gridConfig.getInjectionContext();
		ApplicationContext ctx = new ClassPathXmlApplicationContext(injectionContext);
		IInterceptor interceptor = new InjectionInterceptor(ctx);
		remoteExecutor.add(interceptor);
		remoteExecutor.start();
	}

    private static void log(String x)
    {
	    System.out.println(x);
    }

	/**
	 * @return
	 * @throws MalformedURLException 
	 */
	private static ClassLoader getClassLoader(String pathname) throws MalformedURLException
	{
//		{
//			URLClassLoader loader = new URLClassLoader(new URL[] { new URL("jar:file:/C:\\temp\\Caf1.jar!/") });
//		}
		{
			File file = new File(pathname);

			if(!file.exists())throw new RuntimeException("File not find: "+pathname);

			URL[] urls = { file.toURI().toURL() };
			final URLClassLoader urlClassLoader = URLClassLoader.newInstance(urls);
			return urlClassLoader;
		}
	}
}
