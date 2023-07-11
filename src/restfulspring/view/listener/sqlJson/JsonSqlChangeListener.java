package restfulspring.view.listener.sqlJson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.hutool.core.util.StrUtil;
import restfulspring.constant.RestConstant;
import restfulspring.constant.YmdTypeEnum;
import restfulspring.utils.TextUtil;

public class JsonSqlChangeListener implements SelectionListener{

	private StyledText sqlText;
	private StyledText resultText;
	private Combo toSqlDateCombo;

	public JsonSqlChangeListener(StyledText sqlText, StyledText resultText, Combo toSqlDateCombo) {
		this.sqlText = sqlText;
		this.resultText = resultText;
		this.toSqlDateCombo = toSqlDateCombo;
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
			String toSqlDateText = toSqlDateCombo.getText();
			String result = doParse(jsontext,toSqlDateText);
			
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					resultText.setText(result);
				}
			});
		}
		
	}

	private String doParse(String jsontext, String toSqlDateText) {
		if (!JSON.isValid(jsontext)) {
			return null;
		}
		jsontext = jsontext.replaceAll("(\r\n|\r|\n|\n\r|↵|')","");
		StringBuffer sb = new StringBuffer();
		Object parse = TextUtil.parseJsonText(jsontext);
		if (parse instanceof JSONObject) {
			JSONObject jsonObj = ((JSONObject) parse);
			String sql = parseJSONObject(jsonObj,toSqlDateText);
			sb.append(sql);
			sb.append(RestConstant.lineSeparator);
		}else if (parse instanceof JSONArray) {
			JSONArray jsonArray = ((JSONArray)parse);
			for (Object object : jsonArray) {
				JSONObject target = ((JSONObject) object);
				String sql = parseJSONObject(target,toSqlDateText);
				sb.append(sql);
				sb.append(RestConstant.lineSeparator);
			}
			
		}
		return sb.toString();
	}

	private String parseJSONObject(JSONObject obj, String toSqlDateText) {
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
				val  = changeDate(key, val, toSqlDateText);
				if (val instanceof String) {
					sb.append("'").append(val).append("'");
				}else {
					sb.append(val);
				}
			}
			sb.append(",");
		}
		sb = TextUtil.chopLast(sb, ',');
		sb.append(");");
		return sb.toString();
	}

	private Object changeDate(String k, Object v, String toSqlDateText) {
		if (v instanceof Boolean) {
			if (((Boolean)v)) {
				return 1;
			}else {
				return 0;
			}
		}
		if (!StringUtils.startsWith(k, "gmt")&&!StringUtils.endsWithIgnoreCase(k, "Date")) {
			return v;
		}
		Date date = null;
		if (v instanceof Long) {
			try {
				long time = Long.valueOf(v+"");
				date = new Date(time);
			} catch (Exception e) {
			}
		}else if (v instanceof Integer&&(v+"").length()<=10) {
			try {
				long time = Long.valueOf(v+"000");
				date = new Date(time);
			} catch (Exception e) {
			}
		}
		if (date == null) {
			return v;
		}
		if (YmdTypeEnum.ymdhmsz.getDesc().equals(toSqlDateText)) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
	        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));  // 设置时区为东八区
	        return sdf.format(date);
		}else if (YmdTypeEnum.ymdhms.getDesc().equals(toSqlDateText)) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));  // 设置时区为东八区
	        return sdf.format(date);
		}else if (YmdTypeEnum.miliSec.getDesc().equals(toSqlDateText)) {
			return date.getTime();
		}else if (YmdTypeEnum.sec.getDesc().equals(toSqlDateText)) {
			return date.getTime()/1000;
		}
		return v;
	}
	
	public static void main(String[] args) {
		Object i =1689436799000L;
		System.out.println(i instanceof Integer);
		System.out.println(i instanceof Long);
	}
	

}
