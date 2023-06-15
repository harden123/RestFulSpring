package restfulspring.view.listener;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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
import restfulspring.handlers.OpenEditorHandlers;
import restfulspring.handlers.TextCacheHandlers;
import restfulspring.utils.AstUtil;
import restfulspring.utils.CollectionUtils;
import restfulspring.view.tab.TabGroupDTO;
import restfulspring.view.tree.MyTreeElement;

public class TreeClickLinstener implements IDoubleClickListener,ISelectionChangedListener {

	private Combo getCombo;
	private Text urlText;
	private TabGroupDTO tabGroupDTO;

	public TreeClickLinstener(Combo getCombo, Text urlText, TabGroupDTO tabGroupDTO) {
		this.getCombo = getCombo;
		this.urlText = urlText;
		this.tabGroupDTO = tabGroupDTO;
	}

	@Override
	public void doubleClick(DoubleClickEvent event) {
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		Object element = selection.getFirstElement();
		doSelect(element,2);

	}

	private void doSelect(Object element, int clickTime) {
		if (element!=null&&element instanceof MyTreeElement) {
			MyTreeElement node = (MyTreeElement) element;
			JDTMethodDTO jdtMethodDTO = node.getJDTMethodDTO();
			if (jdtMethodDTO!=null) {
				tabGroupDTO.setSelectedTreeNode(node);
				HashMap<String, Map<String, Object>> m_annotations = jdtMethodDTO.getAnnotations();
				AtomicReference<Object> method_type = new AtomicReference<>();
				method_type.set(AstUtil.getValByAnoAndKey(m_annotations, RestConstant.RequestMapping, RestConstant.RequestMapping_Method));;// POST
				String methodUrl = AstUtil.getMethodUrl(node);
			
				RestParamDTO computeParam = AstUtil.computeParam(jdtMethodDTO);
				AtomicReference<String> bodyStr = computeParam.getBodyStr();
				if (StringUtils.isNotBlank(bodyStr.get())) {
					method_type.set(RestTypeEnum.POST.toString());
				}
				Map<String, Object> getParamKVMap = computeParam.getGetParamKVMap();
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						if (RestTypeEnum.POST.toString().equals(method_type.get())) {
							getCombo.select(RestTypeEnum.POST.getKey());
						}else {
							getCombo.select(RestTypeEnum.GET.getKey());
						}
						String UrlPrefix = Activator.getDefault().getPreferenceStore().getString(RestConstant.UrlPrefix);
						urlText.setText(UrlPrefix+methodUrl+initGetParam(getParamKVMap));
						tabGroupDTO.getFolder().setSelection(1);
						
						String memBodyStr = TextCacheHandlers.getBeyKey(methodUrl);
						if (StringUtils.isNotBlank(memBodyStr)) {
							tabGroupDTO.getBodyText().setText(memBodyStr);
						}else {
							if (StringUtils.isNotBlank(bodyStr.get())) {
								tabGroupDTO.getBodyText().setText(bodyStr.get());
							}else {
								tabGroupDTO.getBodyText().setText("");
							}
						}
						if (clickTime==2) {
							OpenEditorHandlers.openEditor(node);
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

	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent e) {
		  IStructuredSelection selection = (IStructuredSelection) e.getSelection();
		  Object firstElement = selection.getFirstElement();
		  doSelect(firstElement,1);

	}





	
}