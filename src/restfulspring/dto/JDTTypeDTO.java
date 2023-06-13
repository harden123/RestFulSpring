package restfulspring.dto;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.TypeDeclaration;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
public class JDTTypeDTO {

	private TypeDeclaration type;
	/**
	 * <annoKey:<key,val>>
	 */
	private HashMap<String, Map<String, Object>> annotations;
	
	private HashMap<String, JDTMethodDTO> methodName2DTOMap ;
}
