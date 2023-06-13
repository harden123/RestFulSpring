package restfulspring.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JDTMethodDTO {

	/**入参
	System.out.println("Parameter: " + parameter.getName().getIdentifier() + " (" + parameter.getType().toString() + ")");
	Parameter: baseRoleDTO (BaseRoleDTO)
	Parameter: url (String)
	Parameter: areaCodes (List<String>)
	 */
	private List<SingleVariableDeclaration> reqParams = Lists.newArrayList();
	
	/**
	 * <annoKey:<key,val>>
	 */
	private HashMap<String, Map<String, Object>> annotations;


}
