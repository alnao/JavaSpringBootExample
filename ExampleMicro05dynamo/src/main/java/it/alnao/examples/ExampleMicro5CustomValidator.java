package it.alnao.examples;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ExampleMicro5CustomValidator implements ConstraintValidator<ExampleMicro5CustomValidatorAnnotation,String>{

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		Pattern pattern=Pattern.compile("^\\d*[02468]$");
		if (value==null || !pattern.matcher(value).matches() ){
			return false;
		}
		return true;
	}	
}