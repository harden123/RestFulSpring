package restfulspring.view.listener;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import lombok.SneakyThrows;
import restfulspring.Activator;
import restfulspring.constant.RestConstant;
import restfulspring.constant.RestTypeEnum;
import restfulspring.dto.JDTMethodDTO;
import restfulspring.dto.RestParamDTO;
import restfulspring.utils.AstUtil;
import restfulspring.utils.CollectionUtils;
import restfulspring.view.tab.TabGroupDTO;
import restfulspring.view.tree.MyTreeElement;

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
				tabGroupDTO.setSelectedTreeNode(node);
				HashMap<String, Map<String, Object>> m_annotations = jdtMethodDTO.getAnnotations();
				Object method_type = AstUtil.getValByAnoAndKey(m_annotations, RestConstant.RequestMapping, RestConstant.RequestMapping_Method);// POST
				String methodUrl = AstUtil.getMethodUrl(node);
			
				RestParamDTO computeParam = AstUtil.computeParam(jdtMethodDTO);
				AtomicReference<String> bodyStr = computeParam.getBodyStr();
				Map<String, Object> getParamKVMap = computeParam.getGetParamKVMap();
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						if (RestTypeEnum.POST.toString().equals(method_type)) {
							getCombo.select(RestTypeEnum.POST.getKey());
						}else {
							getCombo.select(RestTypeEnum.GET.getKey());
						}
						String UrlPrefix = Activator.getDefault().getPreferenceStore().getString(RestConstant.UrlPrefix);
						urlText.setText(UrlPrefix+methodUrl+initGetParam(getParamKVMap));
						tabGroupDTO.getFolder().setSelection(1);
						
						String memBodyStr = Activator.MethodUrl2BodyTextCacheMap.get(methodUrl);
						if (StringUtils.isNotBlank(memBodyStr)) {
							tabGroupDTO.getBodyText().setText(memBodyStr);
						}else {
							if (StringUtils.isNotBlank(bodyStr.get())) {
								tabGroupDTO.getBodyText().setText(bodyStr.get());
							}else {
								tabGroupDTO.getBodyText().setText("");
							}
						}
					
					}
				});
			}
		}

	}
	
	

	

	
	
	@SneakyThrows
	protected String initGetParam(Map<String, Object> map) {
		if (CollectionUtils.isEmpty(map)) {
			return "";
		}
		StringBuffer sb = new StringBuffer("?");
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();
			Object value = Optional.ofNullable(entry.getValue()).orElse("");
			if (value instanceof JSONArray) {
				if (((JSONArray)value).size()>0) {
					value=((JSONArray)value).get(0);
				}else {
					value = "";
				}
				
			}else if (value instanceof JSONObject) {
				value = "";
			}
			String encodedParamValue = URLEncoder.encode(value+"", "UTF-8").replaceAll("\\+", "%20");
			sb.append(key).append("=").append(encodedParamValue).append("&");
		}
		sb = sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}





	
}
