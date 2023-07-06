package tool.utils;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolTip;

import lombok.SneakyThrows;

public class ViewUtil {

	@SneakyThrows
	public static void copyAndToolTip(String content) {
		// 创建一个 StringSelection 对象，将要复制的字符串作为参数传入
		StringSelection stringSelection = new StringSelection(content);
		// 获取系统剪贴板
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		// 将 StringSelection 对象设置为剪贴板的内容
		clipboard.setContents(stringSelection, null);
		

		Display display = Display.getCurrent();
		ToolTip toolTip = new ToolTip(display.getActiveShell(), SWT.BALLOON | SWT.ICON_INFORMATION);
		// 设置提示文本
		toolTip.setMessage(new String ("已复制到剪贴板".getBytes(),"UTF-8"));

		// 设置提示的标题
		toolTip.setText(new String ("提示".getBytes(),"UTF-8"));
        Point cursorLocation = display.getCursorLocation();
		toolTip.setLocation(cursorLocation.x-100, cursorLocation.y-100);
		toolTip.setAutoHide(true);
		toolTip.setVisible(true);
		
		// 定时关闭提示
		display.timerExec(300, new Runnable() {
		    public void run() {
		        toolTip.setVisible(false);
		        toolTip.dispose(); // 在不需要时手动释放资源
		    }
		});
	}
}
