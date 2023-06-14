package restfulspring.view.tree;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import com.google.common.collect.Maps;

import lombok.SneakyThrows;
import restfulspring.constant.RestConstant;
import restfulspring.dto.JDTMethodDTO;
import restfulspring.dto.VariableBindingField;
import restfulspring.utils.AstUtil;
import restfulspring.utils.CollectionUtils;
import restfulspring.view.tab.TabGroupDTO;

public class TreeDoubleClickLinstener implements IDoubleClickListener {

	private Combo getCombo;
	private Text urlText;
	private TabGroupDTO tabGroupDTO;

	public TreeDoubleClickLinstener(Combo getCombo, Text urlText, TabGroupDTO tabGroupDTO) {
		this.getCombo = getCombo;
		this.urlText = urlText;
		this.tabGroupDTO = tabGroupDTO;
	}

	@Override
	public void doubleClick(DoubleClickEvent event) {
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		Object element = selection.getFirstElement();
		if (element instanceof MyTreeElement) {
			MyTreeElement node = (MyTreeElement) element;
			JDTMethodDTO jdtMethodDTO = node.getJDTMethodDTO();
			if (jdtMethodDTO!=null) {
				Object type_url = null;//   /micro/accessory
				MyTreeElement parent = node.getParent();
				if (parent!=null&&parent.getJDTTypeDTO()!=null) {
					HashMap<String, Map<String, Object>> type_Annotations = parent.getJDTTypeDTO().getAnnotations();
					type_url = AstUtil.getValByAnoAndKey(type_Annotations, RestConstant.RequestMapping, RestConstant.RequestMapping_value);
				}
				HashMap<String, Map<String, Object>> m_annotations = jdtMethodDTO.getAnnotations();
				Object method_type = AstUtil.getValByAnoAndKey(m_annotations, RestConstant.RequestMapping, RestConstant.RequestMapping_Method);// POST
				Object method_url = AstUtil.getValByAnoAndKey(m_annotations, RestConstant.RequestMapping, RestConstant.RequestMapping_value);//  /add
				String url = normalizeReq(type_url)+normalizeReq(method_url);
				//body
				Map<String,Object> getParamKVMap = Maps.newHashMap();
//				String requestBody = null;
				AtomicReference<String> bodyStr = new AtomicReference<>();
				List<SingleVariableDeclaration> reqVariable = jdtMethodDTO.getReqParams();
				for (SingleVariableDeclaration singleVariableDeclaration : reqVariable) {
					Type type = singleVariableDeclaration.getType();
					List modifiers = singleVariableDeclaration.modifiers();
					String paramName = singleVariableDeclaration.getName().toString();
					HashMap<String, Map<String, Object>> retrieveAnnotations = AstUtil.retrieveAnnoByModifiers(modifiers);
					if (retrieveAnnotations.containsKey(RestConstant.RequestBody)) {
						VariableBindingField fields = AstUtil.getFields(type);
						bodyStr.set(initBodyStr(fields));
					}else if (retrieveAnnotations.containsKey(RestConstant.RequestParam)) {
						Object param = AstUtil.getValByAnoAndKey(retrieveAnnotations, RestConstant.RequestParam, RestConstant.RequestMapping_value);
						if (StringUtils.isBlank(Objects.toString(param, null))) {
							param = paramName;
						}
						getParamKVMap.put(param.toString(), initParamVal(param.toString(),type));
					}else {
						getParamKVMap.put(paramName, initParamVal(paramName,type));
					}
				}
			
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						if ("POST".equals(method_type)) {
							getCombo.select(1);
						}else {
							getCombo.select(0);
						}
						urlText.setText(url+initGetParam(getParamKVMap));
						if (StringUtils.isNotBlank(bodyStr.get())) {
							tabGroupDTO.getBodyText().setText(bodyStr.get());
						}
					}
					
				});
			}
		}

	}
	
	private String normalizeReq(Object type_url) {
		String string = StringUtils.trim(Objects.toString(type_url, ""));
		String removeEnd = StringUtils.removeEnd(string, "/");
		if (!StringUtils.startsWith(removeEnd, "/")) {
			removeEnd="/"+removeEnd;
		}
		return removeEnd;
	}
	
	
	@SneakyThrows
	protected String initGetParam(Map<String, Object> map) {
		if (CollectionUtils.isEmpty(map)) {
			return "";
		}
		StringBuffer sb = new StringBuffer("?");
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();
			String encodedParamValue = URLEncoder.encode(entry.getValue()+"", "UTF-8").replaceAll("+", "%20");
			sb.append(key).append("=").append(encodedParamValue).append("&");
		}
		sb = sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}

	/**
	 * TODO
	 */
	private String initBodyStr(VariableBindingField fields) {
		// TODO:hsl Auto-generated method stub
		return null;
	}

	/**
	 * TODO
	 */
	private Object initParamVal(String paramName, Type type) {
		ITypeBinding typeBinding = ((SimpleType) type).resolveBinding();
		String qualifiedName = typeBinding.getQualifiedName();
		System.out.println(qualifiedName);

		return null;
	}



	
}
