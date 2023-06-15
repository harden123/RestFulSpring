package restfulspring.view.listener;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import restfulspring.view.tab.TabGroupDTO;

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
			Object parse = JSON.parse(text);
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					tabGroupDTO.getBodyText().setText(JSON.toJSONString(parse,new SerializerFeature[] {
							SerializerFeature.WriteMapNullValue,
							SerializerFeature.PrettyFormat,
							SerializerFeature.SortField,
							SerializerFeature.MapSortField})
							);
				}
			});
		}
		
		
	}


	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void widgetDefaultSelected(SelectionEvent arg0) {
		// TODO:hsl Auto-generated method stub
		
	}
}
