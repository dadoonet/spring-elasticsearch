package fr.pilato.spring.elasticsearch.proxy;

import java.util.concurrent.Future;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.util.ReflectionUtils;

public class GenericInvocationHandler<T> implements MethodInterceptor {

	private volatile T bean;
	private Future<T> nodeFuture;

	public GenericInvocationHandler(Future<T> nodeFuture) {
		this.nodeFuture = nodeFuture;
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		if (bean == null) {
			bean = nodeFuture.get();
			//release reference
			nodeFuture = null;
		}
		return ReflectionUtils.invokeMethod(invocation.getMethod(), bean, invocation.getArguments());
	}

}
