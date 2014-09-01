package fr.pilato.spring.elasticsearch.xml;

import org.elasticsearch.node.Node;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test class for feature request #15 : https://github.com/dadoonet/spring-elasticsearch/issues/15
 * @author David
 *
 */
public class Settings15Test extends AbstractXmlContextModel {
    private String[] xmlBeans = {"models/settings-15/settings-15-context.xml"};

    @Override
    String[] xmlBeans() {
        return xmlBeans;
    }

	@Test
	public void test_node_clustername() {
		Node node = checkNode();
        assertThat(node.settings().get("cluster.name"), is("newclustername"));
	}
}
