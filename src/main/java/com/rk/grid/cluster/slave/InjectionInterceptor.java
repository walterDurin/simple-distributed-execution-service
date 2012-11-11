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
package com.rk.grid.cluster.slave;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.rk.grid.cluster.shared.IExecutable;

/**
 * @author rkehoe
 * 
 */
public class InjectionInterceptor implements IInterceptor
{
	private ClassPathXmlApplicationContext	context;

	public InjectionInterceptor(ClassPathXmlApplicationContext ctx)
	{
		this.context = ctx;
	}

	public InjectionInterceptor()
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see grid.service.IInterceptor#process(grid.common.IExecutable)
	 */
	@Override
	public void process(IExecutable<?> ex)
	{
		processInjection(ex.getCallable());
	}

	public void processInjection(Object bean)
	{
		if (context == null)
			return;
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		beanFactory.autowireBean(bean);
	}

	/**
	 * @param context
	 *            the context to set
	 */
	public void setContext(ClassPathXmlApplicationContext context)
	{
		if (this.context == null)
			this.context = context;
		else
			this.context.setParent(context);
	}

	public void addBean(String name, Object bean)
	{
		if (this.context == null)
			this.context = new ClassPathXmlApplicationContext();
		context.refresh();
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		beanFactory.registerSingleton(name, bean);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    @Override
    public String toString()
    {
	    return "InjectionInterceptor [context=" + this.context + "]";
    }
	
	
}
