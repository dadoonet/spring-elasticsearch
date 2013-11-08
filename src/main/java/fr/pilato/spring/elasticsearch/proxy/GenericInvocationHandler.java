package fr.pilato.spring.elasticsearch.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.Future;

import org.springframework.util.ReflectionUtils;

/**
 * @author labrot
 * Date: 10/7/13
 * Time: 4:19 PM
 */
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
