package restfulspring.view.listener;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Text;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;

import restfulspring.constant.RestConstant;
import restfulspring.dto.JDTMethodDTO;
import restfulspring.dto.RestParamDTO;
import restfulspring.handlers.RequestCacheHandlers;
import restfulspring.utils.AstUtil;
import restfulspring.view.tab.TabGroupDTO;
import restfulspring.view.tree.MyTreeElement;

public class BodyTextFocusListener implements FocusListener{

	private TabGroupDTO tabGroupDTO;

	public BodyTextFocusListener(TabGroupDTO tabGroupDTO) {
		this.tabGroupDTO = tabGroupDTO;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void focusGained(FocusEvent e) {
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void focusLost(FocusEvent e) {
		Object source = e.getSource();
		if (source instanceof Text) {
			Text bodyText = (Text)source;
			String text = bodyText.getText();
			MyTreeElement selectedTreeNode = tabGroupDTO.getSelectedTreeNode();
			JDTMethodDTO selectedJdtMethodDTO = selectedTreeNode.getJDTMethodDTO();
			String methodUrl = AstUtil.getMethodUrl(selectedTreeNode);
			String methodBodyText = RequestCacheHandlers.getBeyKey(RestConstant.BodyText,methodUrl);
			if (StringUtils.isBlank(methodBodyText)) {
				RestParamDTO computeParam = AstUtil.computeParam(selectedJdtMethodDTO);
				AtomicReference<String> bodyStr = computeParam.getBodyStr();
				if (bodyStr!=null) {
					methodBodyText = bodyStr.get();
				}
			}
			boolean judgeJsonEquals = judgeJsonEquals(StringUtils.trim(text),StringUtils.trim(methodBodyText));
			if (!judgeJsonEquals) {
				RequestCacheHandlers.put(RestConstant.BodyText,methodUrl, text);
			}
		}
	}

	private boolean judgeJsonEquals(String textJson, String methodJson) {
		if (Objects.equals(textJson, methodJson)) {
			return true;
		}
		//json变化记录到本地
		if (methodJson!=null&&textJson==null) {
			return false;
		}else if (textJson!=null&&methodJson==null) {
			return false;
		}
		Object parse = JSON.parse(textJson,Feature.OrderedField);
		Object parse2 = JSON.parse(methodJson,Feature.OrderedField);
		if (parse.equals(parse2)) {
			return true;
		}
		return false;
	}

}
