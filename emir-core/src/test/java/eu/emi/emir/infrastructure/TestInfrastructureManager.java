/**
 * 
 */
package eu.emi.emir.infrastructure;

import static org.junit.Assert.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.emi.emir.core.Configuration;
import eu.emi.emir.infrastructure.EmptyIdentifierFailureException;
import eu.emi.emir.infrastructure.InfrastructureManager;
import eu.emi.emir.infrastructure.NullPointerFailureException;

/**
 * @author szigeti
 *
 */
public class TestInfrastructureManager {
	public static InfrastructureManager manager;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		manager = new InfrastructureManager(new Configuration(new Properties()));
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link eu.emi.emir.infrastructure.InfrastructureManager#InfrastructureManager()}.
	 */
	@Test
	public void testInfrastructureManager() {
		List<String> parents = manager.getParentsRoute();
		if (!parents.isEmpty()) fail("Can not clean the parents collection!");
		
		// Child collection check
		List<String> childs = manager.getChildDSRs();
		if (!childs.isEmpty()) fail("Can not clean the childs collection!");
		
		assertEquals(parents.isEmpty(),childs.isEmpty());
	}

	/**
	 * Test method for {@link eu.emi.emir.infrastructure.InfrastructureManager#setParent(java.util.List)}.
	 */
	@Test
	public void testSetParent() {
        // NULL pointer test
		try {
			manager.setParent(null);
			// if the SetParentsRoute don't throw exception, then it is failure
			fail("Don't throw NullPointerFailureException exception, because an input was a NULL pointer!");			
		} catch (EmptyIdentifierFailureException e) {
			e.printStackTrace();
			fail("An input was a NULL pointer and not an empty collection!");			
		} catch (NullPointerFailureException e) {
			assertTrue("Detecting NullPointerFailureException exception.",true);
		}

		String inputParent = new String();
		// empty input test
		try {
			manager.setParent(inputParent);
			// if the SetParentsRoute don't throw exception, then it is failure
			fail("Don't throw EmptyIdentifierFailureException exception, because an input list was empty!");			
		} catch (EmptyIdentifierFailureException e) {
			assertTrue("Detecting EmptyIdentifierFailureException exception.",true);
		} catch (NullPointerFailureException e) {
			fail("Input was a valid value and not NULL pointer!");			
		}
		
		inputParent = "test_parent";
		try {
			manager.setParent(inputParent);
		} catch (EmptyIdentifierFailureException e) {
			fail("Input parents list was not empty!");
		} catch (NullPointerFailureException e) {
			fail("Input parents list was not NULL pointer!");
		}
		
		assertEquals(inputParent, manager.getParent());
	}

	/**
	 * Test method for {@link eu.emi.emir.infrastructure.InfrastructureManager#setParentsRoute(java.util.List)}.
	 */
	@Test
	public void testSetParentsRoute() {
        // NULL pointer test
		try {
			manager.setParentsRoute(null);
			// if the SetParentsRoute don't throw exception, then it is failure
			fail("Don't throw NullPointerFailureException exception, because an input was a NULL pointer!");			
		} catch (EmptyIdentifierFailureException e) {
			e.printStackTrace();
			fail("An input was a NULL pointer and not an empty collection!");			
		} catch (NullPointerFailureException e) {
			assertTrue("Detecting NullPointerFailureException exception.",true);
		}

		List<String> inputParentsRoute = new ArrayList<String>();
		// empty input test
		try {
			manager.setParentsRoute(inputParentsRoute);
			// if the SetParentsRoute don't throw exception, then it is failure
			fail("Don't throw EmptyIdentifierFailureException exception, because an input list was empty!");			
		} catch (EmptyIdentifierFailureException e) {
			assertTrue("Detecting EmptyIdentifierFailureException exception.",true);
		} catch (NullPointerFailureException e) {
			fail("Input was a valid value and not NULL pointer!");			
		}
		
		inputParentsRoute.add("test_parent");
		try {
			manager.setParentsRoute(inputParentsRoute);
		} catch (EmptyIdentifierFailureException e) {
			fail("Input parents list was not empty!");
		} catch (NullPointerFailureException e) {
			fail("Input parents list was not NULL pointer!");
		}
		
		assertEquals(inputParentsRoute, manager.getParentsRoute());
	}

	/**
	 * Test method for {@link eu.emi.emir.infrastructure.InfrastructureManager#getParent()}.
	 */
	@Test
	public void testGetParent() {
		List<String> parents = new ArrayList<String>();
		parents.add("test_paretn1");
		parents.add("test_parent2");
		parents.add("test_parent3");
		try {
			manager.setParent(parents.get(0));
		} catch (EmptyIdentifierFailureException e) {
			fail("The parents list was not empty!");
		} catch (NullPointerFailureException e) {
			fail("Detecting NullPointerFailureException exception, but the input is valid.");
		}
		
		assertEquals(parents.get(0), manager.getParent());

		// set new parent
		try {
			manager.setParent(parents.get(1));
		} catch (EmptyIdentifierFailureException e) {
			fail("The parents list was not empty!");
		} catch (NullPointerFailureException e) {
			fail("Detecting NullPointerFailureException exception, but the input is valid.");
		}
		
		assertEquals(parents.get(1), manager.getParent());
	}

	/**
	 * Test method for {@link eu.emi.emir.infrastructure.InfrastructureManager#getParentsRoute()}.
	 */
	@Test
	public void testGetParentsRoute() {
		List<String> parents = new ArrayList<String>();
		parents.add("test_paretn1");
		parents.add("test_parent2");
		parents.add("test_parent3");
		try {
			manager.setParentsRoute(parents);
		} catch (EmptyIdentifierFailureException e) {
			fail("The parents list was not empty!");
		} catch (NullPointerFailureException e) {
			fail("Detecting NullPointerFailureException exception, but the input is valid.");
		}
		
		assertEquals(parents, manager.getParentsRoute());
	}

	/**
	 * Test method for {@link eu.emi.emir.infrastructure.InfrastructureManager#GetChildDSRs()}.
	 */
	@Test
	public void testGetChildDSRs() {
		List<String> childs = new ArrayList<String>();
		childs.add("test_child1");
		childs.add("test_child2");
		childs.add("test_child3");
		
		try {
			manager.addChildDSR("test_child1");
			manager.addChildDSR("test_child2");
			manager.addChildDSR("test_child3");
		} catch (EmptyIdentifierFailureException e) {
			fail("Detecting EmptyIdentifierFailureException exception.");
		} catch (NullPointerFailureException e) {
			fail("Detecting NullPointerFailureException exception.");
		}
		
		// Value checking
		//assertTrue(manager.getChildDSRs().containsAll(childs));
	}

	/**
	 * Test method for {@link eu.emi.emir.infrastructure.InfrastructureManager#addChildDSR(java.lang.String)}.
	 */
	@Test
	public void testAddChildDSR() {
		// NULL pointer test
		try {
			manager.addChildDSR(null);
		} catch (EmptyIdentifierFailureException e) {
			fail("Input was a NULL pointer and we catch EmptyIdentifierFailureException exception.");
		} catch (NullPointerFailureException e) {
			assertTrue("Detecting NullPointerFailureException exception.",true);
		}
		
		// empty input test
		try {
			manager.addChildDSR("");
		} catch (EmptyIdentifierFailureException e) {
			assertTrue("Detecting EmptyIdentifierFailureException exception.",true);
		} catch (NullPointerFailureException e) {
			fail("Child collection was emtpy and we catch NullPointerFailureException exception.");
		}
		
		// Duplicate value test
		try {
			manager.addChildDSR("test_child1");
			manager.addChildDSR("test_child1");
		} catch (EmptyIdentifierFailureException e) {
			fail("The input identifier is not empty but we catch EmptyIdentifierFailureException exception.");
		} catch (NullPointerFailureException e) {
			fail("The input identifier is not empty but we catch NullPointerFailureException exception.");
		}
		//assertEquals("test_child1", manager.getChildDSRs().get(0));
		
		try {
			manager.addChildDSR("test_child2");
			manager.addChildDSR("test_child3");
		} catch (EmptyIdentifierFailureException e) {
			fail("Every input identifier was not empty.");
		}catch (NullPointerFailureException e) {
			fail("Detecting NullPointerFailureException exception.");
		}
		// Value checking
		List<String> childs = new ArrayList<String>();
		childs.add("test_child1");
		childs.add("test_child2");
		childs.add("test_child3");
		//assertTrue(manager.getChildDSRs().containsAll(childs));
	}

}
