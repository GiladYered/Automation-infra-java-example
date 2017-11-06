package infra.core.objectmodels;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


public class TestMethod {

	private String name;
	private List<Sut> sut;

	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElement
	public List<Sut> getSut() {
		return sut;
	}

	public void setSut(List<Sut> suts) {
		this.sut = suts;
	}

	public List<Iteration> getAllIterationsAndSetSutParent() {
		List<Iteration> iterationsList = new LinkedList<>();
		sut.stream().forEach(sut ->{
			sut.getIteration().forEach(iteration -> {
				iteration.setSut(sut.getName());});
			iterationsList.addAll(sut.getIteration());
		});
		return iterationsList;
	}
	
	public int getTestMethodIterationsCount(){
		AtomicInteger runCount = new AtomicInteger(0);
		 sut.stream().forEach(sut ->{
			 runCount.getAndAdd(sut.getIterationsCount());
		} );
		 return runCount.get();
	}
	
}