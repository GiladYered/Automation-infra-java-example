package infra.core.objectmodels;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;


public class Sut {

	private String name;

	private List<Iteration> iteration;

	@XmlElement
	public List<Iteration> getIteration() {
		return iteration;
	}

	public void setIteration(List<Iteration> iteration) {
		this.iteration = iteration;
	}

	@XmlAttribute
	public String getName(){
		return name;
	}

	public void setName(String machineName) {
		this.name = machineName;
	}
	
	public int getIterationsCount(){
		return (int) iteration.stream().count();
	}

}