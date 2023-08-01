package restfulspring.view.listener.restSpring;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import restfulspring.Activator;
import restfulspring.constant.RestConstant;
import restfulspring.constant.RestTypeEnum;
import restfulspring.dto.JDTMethodDTO;
import restfulspring.dto.RestParamDTO;
import restfulspring.handlers.OpenEditorHandlers;
import restfulspring.handlers.RequestCacheHandlers;
import restfulspring.preference.MyPreferencesPage;
import restfulspring.utils.AstUtil;
import restfulspring.utils.TextUtil;
import restfulspring.view.tab.restSpring.TabGroupDTO;
import restfulspring.view.tree.restSpring.MyTreeElement;

public class TreeClickLinstener implements IDoubleClickListener,ISelectionChangedListener {

	private Combo getCombo;
	private Text urlText;
	private TabGroupDTO tabGroupDTO;
	private Combo urlPrfixCombo;

	public TreeClickLinstener(Combo getCombo, Text urlText, TabGroupDTO tabGroupDTO, Combo urlPrfixCombo) {
		this.getCombo = getCombo;
		this.urlText = urlText;
		this.tabGroupDTO = tabGroupDTO;
		this.urlPrfixCombo = urlPrfixCombo;
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
					method_type.set(RestTypeEnum.POST.getDesc());
				}
				Map<String, Object> getParamKVMap = computeParam.getGetParamKVMap();
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						boolean hasCache = false;
						if (RestTypeEnum.POST.getDesc().equals(method_type.get())) {
							getCombo.select(RestTypeEnum.POST.getKey());
						}else {
							getCombo.select(RestTypeEnum.GET.getKey());
						}
					
						//head
						String headers = Activator.getDefault().getPreferenceStore().getString(RestConstant.Headers);
						tabGroupDTO.getHeadText().setText(headers);
						
						//url
						String UrlPrefix = MyPreferencesPage.getUrlPrfixByCombo(urlPrfixCombo);
						String urlParamCache = RequestCacheHandlers.getBeyKey(RestConstant.UrlText,methodUrl);
						if (StringUtils.isBlank(urlParamCache)) {
							urlParamCache = TextUtil.initGetParam(getParamKVMap);
						}else {
							hasCache=true;
						}
						urlText.setText(UrlPrefix+methodUrl+StringUtils.trimToEmpty(urlParamCache));
						
						//body
						tabGroupDTO.getFolder().setSelection(1);
						String bodyStrCache = RequestCacheHandlers.getBeyKey(RestConstant.BodyText,methodUrl);
						if (StringUtils.isBlank(bodyStrCache)) {
							if (StringUtils.isNotBlank(bodyStr.get())) {
								bodyStrCache = bodyStr.get();
							}else {
								bodyStrCache="";
							}
						}else {
							hasCache = true;
						}
						tabGroupDTO.getBodyText().setText(bodyStrCache);

						
						if (clickTime==2) {
							OpenEditorHandlers.openEditor(node);
						}
						
						//如果有缓存，reset不同
						Color color = null;
						if (hasCache) {
							color = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
						}else {
							color = Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
						}
						tabGroupDTO.getResetItem().setForeground(color);
					}
				});
			}
		}
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
