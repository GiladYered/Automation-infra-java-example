package sanity;


import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import il.co.topq.difido.model.Enums.Status;
import infra.bases.MasterBaseTest;

/**
 * Add description to every test split into classes create waves
 * 
 * @author ben mark
 *
 */
final public class SimpleTests extends MasterBaseTest {

	private String param1 = "param1";// = "param1-value";
	private String param2 = "param2";//= "param2-value";
	
	@BeforeMethod()
	public void setUp() throws Exception {
		report.startLevel("setUp");
		super.setUp(param1);
		if (param1 != null) {
			if (param1.equals("failOnBeforeMethod"))
				throw new Exception("My failure at before method");
		}
		report.log("Test class before method");
		report.endLevel();
	}

	@AfterMethod()
	public void tearDown() throws Exception {
		super.tearDown();
		report.startLevel("tearDown");
		if (param2 == "failOnAfterMethod")
			throw new Exception("My failure at tear down");
		report.log("Test class before method");
		report.endLevel();
	}


	@Test(groups="Sanity")
	public void simpleTest() {
		report.log("some title0", "Some message", Status.success);
		report.log("print parameter1", param1);
		report.log("print parameter2", param2);
		report.log("print sut parameter", sutFileName);
	}
	
	@Test(groups="Sanity")
	public void simpleTest2() {
		report.log("some title0", "Some message", Status.success);
		report.log("print2 parameter1", param1);
		report.log("print2 parameter2", param2);
		report.log("print2 sut parameter", sutFileName);
	}

	

}
