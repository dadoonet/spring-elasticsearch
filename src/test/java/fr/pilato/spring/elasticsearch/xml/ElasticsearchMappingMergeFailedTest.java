package fr.pilato.spring.elasticsearch.xml;

import static org.junit.Assert.*;

import org.elasticsearch.index.mapper.MergeMappingException;
import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * We try to merge non merging mapping.
 * An exception should be raised.
 * @author David Pilato aka dadoonet
 *
 */
public class ElasticsearchMappingMergeFailedTest {
	
	@Test(expected=BeanCreationException.class)
	public void test_transport_client() {
		try {
			new ClassPathXmlApplicationContext("fr/pilato/spring/elasticsearch/xml/es-mapping-failed-test-context.xml");
		} catch (BeanCreationException e) {
			assertEquals(MergeMappingException.class, e.getCause().getClass());
			throw e;
		}
	}
}
