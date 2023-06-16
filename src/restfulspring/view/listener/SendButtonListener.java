package restfulspring.view.listener;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import restfulspring.constant.RestTypeEnum;
import restfulspring.utils.OkHttpUtlis;
import restfulspring.utils.TextUtil;
import restfulspring.view.tab.TabGroupDTO;

public class SendButtonListener implements SelectionListener {
	private static final ExecutorService executor = new ThreadPoolExecutor(2, 4, 30L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(4),
			new ThreadPoolExecutor.CallerRunsPolicy());

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
		String head = StringUtils.trimToNull(tabGroupDTO.getHeadText().getText());
		Map<String,String> headers = TextUtil.parseHeaders(head);
		String url = StringUtils.trimToEmpty(urlText.getText());
		String body = StringUtils.trimToNull(tabGroupDTO.getBodyText().getText());
		String restType = getCombo.getText();
		
		if (RestTypeEnum.GET.toString().equals(restType)) {
			executor.execute(()->{
				String r = OkHttpUtlis.doGet(null, headers, url);
				showResult(r);
			});
		}else if(RestTypeEnum.POST.toString().equals(restType)) {
			executor.execute(()->{
				String r = OkHttpUtlis.doPostJSON(body, headers, url);
				showResult(r);
			});
		}
		
    }



	private void showResult(String result) {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				tabGroupDTO.getFolder().setSelection(2);
				tabGroupDTO.getResponseText().setText(result);
			}
		});
	}

	@Override
    public void widgetDefaultSelected(SelectionEvent e) {
    	// 如果按钮被默认选中，则执行的代码
		// 在大多数情况下，忽略此方法即可
    }
}
