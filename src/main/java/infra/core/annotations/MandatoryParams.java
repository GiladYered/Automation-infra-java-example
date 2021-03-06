package infra.core.annotations;



import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Test(dataProvider="ParallelDataProvider")
public @interface MandatoryParams {

	public String [] params();
	public String testDescription() default "No test name entered into annotation";

}
