package infra.core.listeners;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.testng.*;
import org.testng.annotations.Test;
import org.testng.internal.IResultListener2;



public class TestNGHook implements IResultListener2, ISuiteListener, IInvokedMethodListener2, TestCasesListener,
		TestIterationListener {

	private Map<String, Collection<ITestNGMethod>> testMethodsMap;

	@Override
	public void onTestStart(ITestResult result) {

	}

	@Override
	public void onTestSuccess(ITestResult result) {

	}

	@Override
	public void onTestFailure(ITestResult result) {

	}

	@Override
	public void onTestSkipped(ITestResult result) {

	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {

	}

	@Override
	public void onStart(ITestContext context) {

	}

	@Override
	public void onFinish(ITestContext context) {

	}

	@Override
	public void onConfigurationSuccess(ITestResult result) {

	}

	@Override
	public void onConfigurationFailure(ITestResult result) {

	}

	@Override
	public void onConfigurationSkip(ITestResult result) {

	}
	
	@Override
	public void beforeConfiguration(ITestResult result) {
		
		// if(!testMethodsMap.containsKey(result.getMethod().getConstructorOrMethod().getMethod().getName())

	}

	@Override
	public void onStart(ISuite suite) {
		testMethodsMap = suite.getMethodsByGroups();
	}

	@Override
	public void onFinish(ISuite suite) {

	}

	@Override
	public void validateTestMethodNode(String testWave, String testClass, String testMethod, Exception e) {

	}

	@Override
	public void validateTestClassNode(String testWaveXmlNode, String testClassXmlNode, Exception e) {

	}

	@Override
	public void validateTestNGMethodSignature(String testWaveXmlNode, String testClassXmlNode, String testMethodXmlNode,
			Exception e) {

	}

	@Override
	public void validateTestMandatoryParamsAlert(String testWaveXmlNode, String testClassXmlNode,
			String testMethodXmlNode, Exception e) {

	}

	@Override
	public void beforeInvocation(IInvokedMethod method, ITestResult testResult, ITestContext context) {

	}

	@Override
	public void afterInvocation(IInvokedMethod method, ITestResult testResult, ITestContext context) {

	}

	@Override
	public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {

	}

	@Override
	public void afterInvocation(IInvokedMethod method, ITestResult testResult) {

	}

	}
