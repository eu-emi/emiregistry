/**
 * 
 */
package eu.emi.dsr.infrastructure;

import static org.junit.Assert.*;

import java.util.List;
import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author szigeti
 *
 */
public class TestChildrenManager {
	public static ChildrenManager manager;

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
		manager = new ChildrenManager();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link eu.emi.dsr.infrastructure.ChildrenManager#ChildrenManager()}.
	 */
	@Test
	public void testChildrenManager() {
		// Child collection check
		List<String> childs = manager.getChildDSRs();
		if (!childs.isEmpty()) fail("Can not clean the childs collection!");
		
		assertTrue(childs.isEmpty());
	}

	/**
	 * Test method for {@link eu.emi.dsr.infrastructure.ChildrenManager#GetChildDSRs()}.
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
		assertTrue(manager.getChildDSRs().containsAll(childs));
	}

	/**
	 * Test method for {@link eu.emi.dsr.infrastructure.ChildrenManager#addChildDSR(java.lang.String)}.
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
		assertEquals("test_child1", manager.getChildDSRs().get(0));
		
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
		assertTrue(manager.getChildDSRs().containsAll(childs));
	}

}
