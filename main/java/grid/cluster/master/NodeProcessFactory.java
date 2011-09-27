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
package grid.cluster.master;
import grid.cluster.slave.NodeMain;
import grid.util.ProcessUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * @author rkehoe
 *
 */
public class NodeProcessFactory
{

    private static void log(String x)
    {
	    System.out.println(x);
    }

    public static Process createClusterNode(int port, String library, List<String> jvmNodeParams, String serviceName)
    {    	
    	int i = 0;
    	for (String string : jvmNodeParams)
        {
	        System.out.println("Process Cmd Line param"+(++i)+" : "+string);
        }
        try
        {
    		String classpath = System.getProperty("java.class.path");            
    		log("CLASSPATH: "+classpath);
        	ArrayList<String> cmdStrs = new ArrayList<String>();
        	
        	cmdStrs.add("java");
        	cmdStrs.addAll(jvmNodeParams);

        	cmdStrs.add("-cp");
        	cmdStrs.add(classpath);
        	cmdStrs.add(NodeMain.class.getName());        	
        	cmdStrs.add("-p");
        	cmdStrs.add(port+"");        	
        	cmdStrs.add("-b");        	
        	cmdStrs.add(serviceName);        	
        	        	
            return ProcessUtil.create(cmdStrs);          
        } 
        catch (Throwable e)
        {        	
            throw new RuntimeException(e);
        }
    }    
}
