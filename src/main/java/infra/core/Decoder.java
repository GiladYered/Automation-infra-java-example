package infra.core;



import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Pattern;

import infra.core.webdriver.enums.EBrowser;


public class Decoder {

	public static Queue<String> decode(String paramKey,String paramValue) {
		Queue<String> decodedParams = new LinkedList<String>();
			if(paramValue.equalsIgnoreCase("all")){
					switch(paramKey.toLowerCase()) {
						case "browser" :
						case "browsers" : { decodedParams.addAll(Arrays.asList(EBrowser.toArray())); }; break;
						case "environment" :
						case "environments" : { //TODO : create env enum }; break;
					}
				}
			}
			else if (Pattern.compile("[^\\w\\s]").matcher(paramValue).find()) {
			if (!paramValue.startsWith("[") || !paramValue.endsWith("]"))
				throw new RuntimeException(
						"testng.xml array parameters must be start and end with square brackets [ ] ");
			String mergedTokens = paramValue.substring(1, paramValue.length() - 1);
			String[] tokens = mergedTokens.split(",");
			for (String token : tokens)
				decodedParams.add(token);
			} else
				decodedParams.add(paramValue);
		return decodedParams;
	}
}

