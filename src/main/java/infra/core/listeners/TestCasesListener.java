package infra.core.listeners;

public interface TestCasesListener {
	void validateTestMethodNode(String testWaveXmlNode,String testClassXmlNode, String testMethodXmlNode, Exception e);
	void validateTestClassNode(String testWaveXmlNode,String testClassXmlNode, Exception e );
	void validateTestNGMethodSignature(String testWaveXmlNode,String testClassXmlNode, String testMethodXmlNode,Exception e );
}
