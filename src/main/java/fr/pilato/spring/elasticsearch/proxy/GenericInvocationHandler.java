package fr.pilato.spring.elasticsearch.proxy;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.Future;

public class GenericInvocationHandler<T> implements InvocationHandler {

	private volatile T bean;
	private Future<T> nodeFuture;

	public GenericInvocationHandler(Future<T> nodeFuture) {
		this.nodeFuture = nodeFuture;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (bean == null) {
			bean = nodeFuture.get();
			//release reference
			nodeFuture = null;
		}
		return ReflectionUtils.invokeMethod(method, bean, args);
	}
}
