package restfulspring.dto;

import java.util.List;

import org.eclipse.jdt.core.dom.IVariableBinding;

import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VariableBindingField {

	private List<IVariableBinding> cur = Lists.newArrayList();
	private List<IVariableBinding> child = Lists.newArrayList();
}
