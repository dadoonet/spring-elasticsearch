package fr.pilato.spring.elasticsearch;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * <ul>
 * A utiliser pour tester les DAO .<br>
 * <li>Permet de recuperer les declarations (fichiers xml) communes aux DAO en
 * test.<br>
 * <li>Les DAO de test doivent donc deriver de cette classe.<br>
 * </ul>
 * 
 * 
 * @author ROBET
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath:es-context.xml"
		})
public abstract class AbstractESTest {

}
