package restfulspring.view.listener.sqlJson;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Display;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.hutool.core.util.StrUtil;
import restfulspring.constant.RestConstant;
import restfulspring.utils.TextUtil;

public class JsonSqlChangeListener implements SelectionListener{

	private StyledText sqlText;
	private StyledText resultText;

	public JsonSqlChangeListener(StyledText sqlText, StyledText resultText) {
		this.sqlText = sqlText;
		this.resultText = resultText;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void widgetDefaultSelected(SelectionEvent arg0) {
		
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void widgetSelected(SelectionEvent arg0) {
		String jsontext = sqlText.getText();
		if (StringUtils.isNotBlank(jsontext)) {
			String result = doParse(jsontext);
			
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					resultText.setText(result);
				}
			});
		}
		
	}

	private static String doParse(String jsontext) {
		if (!JSON.isValid(jsontext)) {
			return null;
		}
		jsontext = jsontext.replaceAll("(\r\n|\r|\n|\n\r|↵|')","");
		StringBuffer sb = new StringBuffer();
		Object parse = TextUtil.parseJsonText(jsontext);
		if (parse instanceof JSONObject) {
			JSONObject jsonObj = ((JSONObject) parse);
			String sql = parseJSONObject(jsonObj);
			sb.append(sql);
			sb.append(RestConstant.lineSeparator);
		}else if (parse instanceof JSONArray) {
			JSONArray jsonArray = ((JSONArray)parse);
			for (Object object : jsonArray) {
				JSONObject target = ((JSONObject) object);
				String sql = parseJSONObject(target);
				sb.append(sql);
				sb.append(RestConstant.lineSeparator);
			}
			
		}
		return sb.toString();
	}

	private static String parseJSONObject(JSONObject obj) {
		StringBuffer sb = new StringBuffer("INSERT INTO `` (");
		for (String key : obj.keySet()) {
			sb.append("`").append(StrUtil.toUnderlineCase(key)).append("`,");
		}
		sb = TextUtil.chopLast(sb, ',');
		sb.append(") VALUES (");
		for (String key : obj.keySet()) {
			Object val = obj.get(key);
			if (val==null) {
				sb.append("null");
			}else {
				sb.append("'").append(val).append("'");
			}
			sb.append(",");
		}
		sb = TextUtil.chopLast(sb, ',');
		sb.append(");");
		return sb.toString();
	}
	

}
