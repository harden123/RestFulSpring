package restfulspring.view.listener.restSpring;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import restfulspring.Activator;
import restfulspring.constant.RestConstant;
import restfulspring.dto.JDTMethodDTO;
import restfulspring.dto.RestParamDTO;
import restfulspring.handlers.RequestCacheHandlers;
import restfulspring.utils.AstUtil;
import restfulspring.utils.TextUtil;
import restfulspring.view.tab.restSpring.TabGroupDTO;
import restfulspring.view.tree.restSpring.MyTreeElement;

public class BodyResetItemListener implements SelectionListener{

	private TabGroupDTO tabGroupDTO;
	private Text urlText;

	public BodyResetItemListener(TabGroupDTO tabGroupDTO, Text urlText) {
		this.tabGroupDTO = tabGroupDTO;
		this.urlText = urlText;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void widgetSelected(SelectionEvent e) {
		MyTreeElement selectedTreeNode = tabGroupDTO.getSelectedTreeNode();
		JDTMethodDTO selectedJdtMethodDTO = selectedTreeNode.getJDTMethodDTO();
		String methodUrl = AstUtil.getMethodUrl(selectedTreeNode);
		RequestCacheHandlers.remove(RestConstant.BodyText,methodUrl);
		RestParamDTO computeParam = AstUtil.computeParam(selectedJdtMethodDTO);
		AtomicReference<String> bodyStr = computeParam.getBodyStr();
		
		RequestCacheHandlers.remove(RestConstant.UrlText,methodUrl);
		Map<String, Object> getParamKVMap = computeParam.getGetParamKVMap();
		String urlParam = TextUtil.initGetParam(getParamKVMap);
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				if (StringUtils.isNotBlank(bodyStr.get())) {
					tabGroupDTO.getBodyText().setText(bodyStr.get());
				}else {
					tabGroupDTO.getBodyText().setText("");
				}
				//url
				String UrlPrefix = Activator.getDefault().getPreferenceStore().getString(RestConstant.UrlPrefix);
				urlText.setText(UrlPrefix+methodUrl+StringUtils.trimToEmpty(urlParam));
				tabGroupDTO.getResetItem().setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
			}
		});

			
	
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		
	}

}
