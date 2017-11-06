package infra.core.listeners;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.annotations.Test;
import org.testng.internal.collections.Pair;

import infra.bases.MasterBaseTest;
import infra.core.Decoder;




public class MethodInterceptor implements IMethodInterceptor {
	
	public String methodName ;
	
	@Override
	public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
		int methodCloneNum = 1;
		Map<String, String> decodedParamsMap = new HashMap<String, String>();
		Map<String, String> xmlParamsMap = context.getCurrentXmlTest().getAllParameters();

		Queue<Map<String, String>> testParamsQueue = new ConcurrentLinkedQueue<Map<String, String>>();
		
		for (String paramKey : xmlParamsMap.keySet()) {
			Queue<String> decodedQueue = Decoder.decode(paramKey, xmlParamsMap.get(paramKey));
			for(String value: decodedQueue) {
				//decodedParamsMap.put(paramKey, value)
			}
			methodCloneNum *= decodedQueue.size();
			// decodedParamsMap.put(paramKey, decodedSet);
		}

		// int methodCloneNum = decodedParamsMap.
		// for (String key : decodedParamsMap.keySet()) {
		// for (String s : decodedParamsMap.get(key)) {
		// System.out.println("key: "+key+" value:"+s);
		// }
		// }

		//IXmlParamsCB callBack = new MasterBaseTest();
		//callBack.getXmlParameters(testParamsQueue);

		context.setAttribute("testParams", testParamsQueue);

		int methCount = methods.size();
		List<IMethodInstance> result = new ArrayList<IMethodInstance>();

		for (int i = 0; i < methCount; i++) {
			IMethodInstance instns = methods.get(i);
			for (int j = 0; j < methodCloneNum; j++)
				result.add(instns);

		}

		return result;
	}
}