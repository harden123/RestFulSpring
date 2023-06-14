package restfulspring.view.listener;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import com.google.common.collect.Maps;

import restfulspring.constant.RestTypeEnum;
import restfulspring.utils.OkHttpUtlis;
import restfulspring.view.tab.TabGroupDTO;

public class SendButtonListener implements SelectionListener {


    private Combo getCombo;
	private Text urlText;
	private TabGroupDTO tabGroupDTO;

	public SendButtonListener(Combo getCombo, Text urlText, TabGroupDTO tabGroupDTO) {
		this.getCombo = getCombo;
		this.urlText = urlText;
		this.tabGroupDTO = tabGroupDTO;
	}

	@Override
    public void widgetSelected(SelectionEvent e) {
		// 按钮被单击时执行的代码
		String url = StringUtils.trimToEmpty(urlText.getText());
		String body = StringUtils.trimToNull(tabGroupDTO.getBodyText().getText());
		HashMap<String, String> headers = Maps.newHashMap();
		String restType = getCombo.getText();
		AtomicReference<String> result = new AtomicReference<>();

		if (RestTypeEnum.GET.toString().equals(restType)) {
			result.set(OkHttpUtlis.doGet(null, headers, url));
		}else if(RestTypeEnum.POST.toString().equals(restType)) {
			result.set(OkHttpUtlis.doPostJSON(body, headers, url));
		}
		
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				tabGroupDTO.getFolder().setSelection(2);
				tabGroupDTO.getResponseText().setText(result.get());
			}
		});
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
    	// 如果按钮被默认选中，则执行的代码
		// 在大多数情况下，忽略此方法即可
    }
}
