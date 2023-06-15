package restfulspring.view.listener;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import restfulspring.dto.JDTMethodDTO;
import restfulspring.dto.RestParamDTO;
import restfulspring.handlers.TextCacheHandlers;
import restfulspring.utils.AstUtil;
import restfulspring.view.tab.TabGroupDTO;
import restfulspring.view.tree.MyTreeElement;

public class BodyResetItemListener implements SelectionListener{

	private TabGroupDTO tabGroupDTO;

	public BodyResetItemListener(TabGroupDTO tabGroupDTO) {
		this.tabGroupDTO = tabGroupDTO;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void widgetSelected(SelectionEvent e) {
		MyTreeElement selectedTreeNode = tabGroupDTO.getSelectedTreeNode();
		JDTMethodDTO selectedJdtMethodDTO = selectedTreeNode.getJDTMethodDTO();
		String methodUrl = AstUtil.getMethodUrl(selectedTreeNode);
		TextCacheHandlers.remove(methodUrl);
		RestParamDTO computeParam = AstUtil.computeParam(selectedJdtMethodDTO);
		AtomicReference<String> bodyStr = computeParam.getBodyStr();
		if (StringUtils.isNotBlank(bodyStr.get())) {
			tabGroupDTO.getBodyText().setText(bodyStr.get());
		}else {
			tabGroupDTO.getBodyText().setText("");
		}
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		
	}

}
