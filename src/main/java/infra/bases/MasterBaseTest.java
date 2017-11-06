package infra.bases;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.springframework.context.annotation.DependsOn;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.beust.jcommander.internal.Sets;
import com.jayway.awaitility.core.ConditionFactory;

import il.co.topq.difido.ReportDispatcher;
import il.co.topq.difido.ReportManager;
import il.co.topq.difido.ReportManagerHook;
import infra.core.listeners.MethodInterceptor;
import infra.core.listeners.TestNGHook;
import infra.core.objectmodels.Iteration;
import infra.core.objectmodels.Scenarios;


@Listeners({ReportManagerHook.class, TestNGHook.class, MethodInterceptor.class})
public abstract class MasterBaseTest implements ITest {
	
	private String testMethodName;
	protected ConditionFactory wait;
	protected static ReportDispatcher report;
	private static Scenarios _scenarios;
	protected String sutFileName;
	
	protected String param1;// = "param1-value";
	protected String param2;//= "param2-value";
	protected String param3;//= "param3-value";
	
	@Override
	public String getTestName() {
		return Stream.of(param1,param2).allMatch(p -> p == null) ?
				testMethodName : param1 + "  " + param2;
	}

	@BeforeSuite(alwaysRun=true)
	public void suitePreConfigure(ITestContext context) throws Exception {
		report = ReportManager.getInstance();
		if (_scenarios == null) {
			try {
				JAXBContext jc = JAXBContext.newInstance(Scenarios.class);
				Unmarshaller unmarshaller = jc.createUnmarshaller();
				String relativeProjectPath = System.getProperty("user.dir");
				File xml = new File(relativeProjectPath+"\\testcases.xml");
				_scenarios = (Scenarios) unmarshaller.unmarshal(xml);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@BeforeMethod(alwaysRun=true)
	public void setMethodName(Method method) {
		testMethodName = method.getName();
	}
	
	@BeforeMethod(alwaysRun=true)
	@SuppressWarnings({ "rawtypes" })
	protected void setUp( Object... params) throws Exception {
		Iteration iteration;
		try {
		    iteration = ((Iteration) params[0]);
		}
		catch(Exception e) { return; }
		sutFileName = iteration.getSut();
		List<Field> fields = new LinkedList<>();
		for (Class<?> c = this.getClass(); c != Object.class; c = c.getSuperclass())
			Collections.addAll(fields, c.getDeclaredFields());
		Stream.of(fields.toArray(new Field[0])).forEach(field -> {
			if (iteration.containsParameter(field.getName())) {
				try {
					field.setAccessible(true);
					Class<?> fieldTypeArg =  field.getType();
					if (fieldTypeArg.isEnum()) {
						Method enumValueOf = fieldTypeArg.getMethod("valueOf", String.class);
						field.set(this,(Enum) enumValueOf.invoke(fieldTypeArg, iteration.getParamValue(field.getName())));
					}
					if (fieldTypeArg.isAssignableFrom(String.class))
						field.set(this,(String) iteration.getParamValue(field.getName()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		iteration.setTestName(getTestName());
	}

	/**
	 * This method is the first to be called from within testNG.xml execution, to
	 * this method , all existing testMethods From testng.xml will call it once, and
	 * return with their set of iterations.
	 * 
	 * Reflection verifications: - TestCases.xml contains executed test classes and
	 * test methods, if fails, posts it on the difido reporter - @MandatoryParams
	 * exists above the current test method, do the mandatory parameters match the
	 * parameters inside testCases.xml - Test-Method legible signature (Iteration
	 * iteration)
	 * 
	 * Reflection configurations: - @EnableRetry if exists, initialize the
	 * ITestNGMethod retryAnalyzer - Test method's name/description - Test method's
	 * sut/environment
	 * 
	 * Right after every method calls once the dataprovider, every iteration starts
	 * calling the @BeforeMethod.
	 * 
	 * @param ctx
	 * @param method
	 * @return
	 * @throws Exception
	 */
	@DataProvider(name = "ParallelDataProvider", parallel = false)
	public Iterator<Object[]> fileDataProvider(ITestContext ctx, ITestNGMethod method) throws Exception {

		Set<Object[]> set = Sets.newLinkedHashSet();

		String className = method.getTestClass().getRealClass().getSimpleName();
		String methodName = method.getConstructorOrMethod().getMethod().getName();
		//testMethodName = Optional.of(methodName);
		Method testMethod = method.getConstructorOrMethod().getMethod();
		_scenarios.testMethodExistInTestCasesXML(method);
		_scenarios.testMethodSignatureValidation(method);
		
		Test testAnnotation = testMethod.getAnnotation(Test.class);

//		if (testMethod.getAnnotation(EnableRetry.class) != null)
//			method.setRetryAnalyzer(new RetryAnalyzer());
		List<Iteration> iterations = _scenarios.getTestMethodIterations(methodName, className);

		Set<String> allSuts = iterations.stream().map((iter) -> iter.getSut()).collect(Collectors.toSet());

		method.setDescription(testAnnotation.description());
		
		iterations.stream().forEach(iteration -> {
			//iteration.containsMandatoryParams(mandatoryParamsAnnotation.params(), method);
			set.add(new Object[] { iteration });
		});
		return set.iterator();
	}

	/**
	 * Can set the thread count to the number of existing iterations in the
	 * testCases.xml Or manually set programmatically set the
	 * data-provider-thread-count value.
	 * 
	 * @param context
	 * @param threadCount
	 * @throws Exception
	 */

	@AfterMethod()
	protected void tearDown() throws Exception {
		report.log("tear down in base test");
	}



	

}
