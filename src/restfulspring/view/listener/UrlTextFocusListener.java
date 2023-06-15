package restfulspring.view.listener;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Text;

import restfulspring.constant.RestConstant;
import restfulspring.dto.JDTMethodDTO;
import restfulspring.dto.RestParamDTO;
import restfulspring.handlers.RequestCacheHandlers;
import restfulspring.utils.AstUtil;
import restfulspring.utils.TextUtil;
import restfulspring.view.tab.TabGroupDTO;
import restfulspring.view.tree.MyTreeElement;

public class UrlTextFocusListener implements FocusListener{

	private TabGroupDTO tabGroupDTO;

	public UrlTextFocusListener(TabGroupDTO tabGroupDTO) {
		this.tabGroupDTO = tabGroupDTO;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void focusGained(FocusEvent arg0) {
		
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void focusLost(FocusEvent e) {
		Object source = e.getSource();
		if (source instanceof Text) {
			Text urlText = (Text)source;
			String text = urlText.getText();
			MyTreeElement selectedTreeNode = tabGroupDTO.getSelectedTreeNode();
			JDTMethodDTO selectedJdtMethodDTO = selectedTreeNode.getJDTMethodDTO();
			String methodUrl = AstUtil.getMethodUrl(selectedTreeNode);
			String methodUrlParamText = RequestCacheHandlers.getBeyKey(RestConstant.UrlText,methodUrl);
			if (StringUtils.isBlank(methodUrlParamText)) {
				RestParamDTO computeParam = AstUtil.computeParam(selectedJdtMethodDTO);
				Map<String, Object> getParamKVMap = computeParam.getGetParamKVMap();
				methodUrlParamText = TextUtil.initGetParam(getParamKVMap);
			}
			String urlParamText = TextUtil.getUrlParam(text);
			if (!StringUtils.trimToEmpty(urlParamText).equals(StringUtils.trimToEmpty(methodUrlParamText))) {
				RequestCacheHandlers.put(RestConstant.UrlText,methodUrl,urlParamText);
			}
		}
		
	}



}
