package sanity;


import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import infra.bases.MasterBaseTest;
import infra.core.objectmodels.Iteration;

/**
 * Add description to every test split into classes create waves
 * 
 * @author ben mark
 *
 */
public class DataDrivenTest extends MasterBaseTest {

	@Test(dataProvider = "ParallelDataProvider", groups="Sanity")
	public void drivenTestMethod(Iteration iteration) throws InterruptedException {
		report.log("some title0", "Some message");
		report.log("print parameter1", param1);
		report.log("print parameter2", param2);
		report.log("print sut parameter", sutFileName);
	}


	

}
