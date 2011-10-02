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

import grid.cluster.shared.IExecutable;
import grid.server.Inject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;


/**
 * @author rkehoe
 * 
 */
public class InjectionInterceptor implements IInterceptor
{

	private final Map<Class<?>, InjectionMetadata>	injectionMetadataCache	= new ConcurrentHashMap<Class<?>, InjectionMetadata>();
	private final ApplicationContext ctx;

	/**
	 * @param ctx
	 */
	public InjectionInterceptor(ApplicationContext ctx)
	{
		this.ctx = ctx;
	}

	/* (non-Javadoc)
     * @see grid.service.IInterceptor#process(grid.common.IExecutable)
     */
    @Override
    public void process(IExecutable<?> ex)
    {
    	processInjection(ex.getCallable());
    }

	public void processInjection(Object bean)
	{
		if(bean==null)return;
		InjectionMetadata metadata = findAutowiringMetadata(bean.getClass());
		metadata.injectFields(bean);
	}

    private static void log(String x)
    {
	    System.out.println(x);
    }

	private InjectionMetadata findAutowiringMetadata(final Class<?> clazz)
	{
		InjectionMetadata metadata = this.injectionMetadataCache.get(clazz);
		log("Generating Injection Metadata for Class: "+clazz);

		if (metadata == null)
		{
			synchronized (this.injectionMetadataCache)
			{
				metadata = this.injectionMetadataCache.get(clazz);
				if (metadata == null)
				{
					final InjectionMetadata newMetadata = new InjectionMetadata(clazz);
					
					ReflectionUtils.doWithFields(clazz, new ReflectionUtils.FieldCallback()
					{
						public void doWith(Field field)
						{
							log("Generating Injection Metadata for Field: "+field);
							Inject injectionAnnotation = field.getAnnotation(Inject.class);
							if (injectionAnnotation != null)
							{
								if (Modifier.isStatic(field.getModifiers()))
								{
									throw new IllegalStateException("Autowired annotation is not supported on static fields");
								}
								ReflectionUtils.makeAccessible(field);
								InjectedFieldElement injectedFieldElement = new InjectedFieldElement(field,injectionAnnotation);
								log("Found injected field: "+injectedFieldElement);
								newMetadata.addInjectedField(injectedFieldElement);
							}
						}
					});
					
					metadata = newMetadata;
					
					this.injectionMetadataCache.put(clazz, metadata);
				}
			}
		}
		return metadata;
	}

	private class InjectionMetadata
	{
		private Set<InjectedFieldElement> injectedFields = new LinkedHashSet<InjectedFieldElement>();
		@SuppressWarnings("unused")
        private final Class<?> clazz;

		/**
         * @param clazz
         */
        public InjectionMetadata(Class<?> clazz)
        {
			this.clazz = clazz;
        }

		/**
         * @param bean
         */
        public void injectFields(Object bean)
        {
	        for ( InjectedFieldElement f :this.injectedFields)
            {
	            f.setField(bean);
            }
        }

		/**
         * @param injectedFieldElement
         */
        public void addInjectedField(InjectedFieldElement injectedFieldElement)
        {
        	injectedFields.add(injectedFieldElement);	        
        }
		
	}
	
	private class InjectedFieldElement
	{
		private final Field field;
		private final Inject annotation;
		private Object bean;

		/**
         * @param field
         * @param injectionAnnotation
         */
        public InjectedFieldElement(Field field, Inject injectionAnnotation)
        {
			this.field = field;
			this.annotation = injectionAnnotation;
        }

		/**
         * @param bean
         */
        public void setField(Object target)
        {
        	try
            {
	            this.field.set(target, getBean());
            }
            catch (IllegalArgumentException e)
            {
	            throw new RuntimeException( e );
            }
            catch (IllegalAccessException e)
            {
	            throw new RuntimeException( e );
            }
        }
		
        /**
         * @return
         */
        private Object getBean()
        {
        	if(this.bean == null)
        	{ 
        		this.bean = ctx.getBean(this.annotation.name(),this.field.getType());
        	}
        	return this.bean;
        }

		/* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
	        return "InjectedFieldElement [annotation=" + this.annotation + ", bean=" + this.bean + ", field=" + this.field + "]";
        }
        
        
	}
}
