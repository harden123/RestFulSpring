package restfulspring.view.listener.restSpring;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import com.alibaba.fastjson.JSON;

import restfulspring.utils.TextUtil;
import restfulspring.view.tab.restSpring.TabGroupDTO;

public class BodyFormatItemListener implements SelectionListener{

	private TabGroupDTO tabGroupDTO;

	public BodyFormatItemListener(TabGroupDTO tabGroupDTO) {
		this.tabGroupDTO = tabGroupDTO;
	}


	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void widgetSelected(SelectionEvent arg0) {
		Text bodyText = tabGroupDTO.getBodyText();
		String text = bodyText.getText();
		if (StringUtils.isNotBlank(text)&&JSON.isValid(text)) {
			Object parse = TextUtil.parseJsonText(text);
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					tabGroupDTO.getBodyText().setText(TextUtil.prettyJSON(parse));
				}
			});
		}
		
		
	}


	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void widgetDefaultSelected(SelectionEvent arg0) {
		
	}
}
