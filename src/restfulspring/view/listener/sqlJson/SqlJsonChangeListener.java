package restfulspring.view.listener.sqlJson;

import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Display;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;

import restfulspring.utils.TextUtil;

public class SqlJsonChangeListener implements SelectionListener{
	public static final Pattern compile = Pattern.compile("(\\(.+\\))\\s*values\\s*(\\(.+\\))",Pattern.CASE_INSENSITIVE);
	public static final Pattern fields = Pattern.compile("(?<!\\\\)`(.+?)(?<!\\\\)`\\s*,",Pattern.CASE_INSENSITIVE);
	public static final Pattern values = Pattern.compile("(?<!\\\\)'(.+?)(?<!\\\\)'|(?<=,)\\s*(NULL)\\s*(?=[,\\)])|(?<=[\\(,])\\s*(.+?)\\s*(?=[\\),])",Pattern.CASE_INSENSITIVE);

	private StyledText sqlText;
	private StyledText resultText;

	public SqlJsonChangeListener(StyledText sqlText, StyledText resultText) {
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
		String text = sqlText.getText();
		if (StringUtils.isNotBlank(text)) {
			
			JSON  result = doParse(text);
			
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					resultText.setText(TextUtil.prettyJSON(result));
				}
			});
		}
	}

	
	private JSON doParse(String text) {
		String[] split = text.split(";");
		JSONArray array = new JSONArray();
//		ArrayList<JSONObject> objs = Lists.newArrayList();
		for (String sql : split) {
			String replaceAll = sql.replaceAll("(\r\n|\r|\n|\n\r|↵|')","");
			Matcher matcher = compile.matcher(replaceAll);
			ArrayList<String> fieldList = Lists.newArrayList();
			ArrayList<Object> valueList = Lists.newArrayList();
			if (matcher.find()) {
//				(`id`, `user_id`, `area_code`, `live_type`, `expire_date`, `area_address`, `extend`, `live_reason`, `cert_image_first`, `cert_image_end`, `company_id`, `leader`, `gmt_create`, `creator`, `gmt_modify`, `modifier`, `deleted`, `manager`, `outside`, `outside_tag_code`)
				String fieldtext = matcher.group(1).trim();
				Matcher matcher2 = fields.matcher(fieldtext);
				while(matcher2.find()) {
					String group = matcher2.group(1);
					fieldList.add(group);
				}
//				('00001c4ac78911edbaf5005056b63bdb', '9aef83d0bcc611edbaf5005056b63bdb', '0033000100050001000300010001001000020002', 'H004001001', '2024-03-21 00:00:00', '大象国际中心1幢10单元2层202室', '0', NULL, NULL, NULL, NULL, '1', '2023-03-21 09:37:54', 'sysadmin', '2023-04-27 16:43:22', 'sysadmin7', '1', '0', '0', NULL)
				String valuetext = matcher.group(2).trim();
				Matcher matcher3 = values.matcher(valuetext);
				while(matcher3.find()) {
					String group = Optional.ofNullable(matcher3.group(1)).orElse(matcher3.group(2));
					group = Optional.ofNullable(group).orElse(matcher3.group(3));
					if (StringUtils.equalsIgnoreCase(group, "null")) {
						valueList.add(null);
					}else if ("0".equals(group)|"1".equals(group)|"2".equals(group)) {
						valueList.add(Integer.parseInt(group));
					}else{
						valueList.add(group);
					}
				}
				JSONObject o = new JSONObject();
				for (int i = 0; i < fieldList.size(); i++) {
					String k = fieldList.get(i);
					Object v = valueList.get(i);
					o.put(k, v);
				}
				array.add(o);
			}
			
		}
		if (array.size()==1) {
			return (JSON) array.get(0);
		}
		return array;
	}
	public static void main(String[] args) {
//		Matcher matcher = compile.matcher("INSERT INTO `wit_test_b`.`uar_visitor` (`id`, `user_id`, `area_code`, `live_type`, `expire_date`, `area_address`, `extend`, `live_reason`, `cert_image_first`, `cert_image_end`, `company_id`, `leader`, `gmt_create`, `creator`, `gmt_modify`, `modifier`, `deleted`, `manager`, `outside`, `outside_tag_code`) VALUES (ebb1f246027811edbaf5005056b63bdb, 'eb99f59c027811edbaf5005056b63bdb', '0033000100010001000100010001000100010001', 'H004001003', '2023-07-15 23:59:59', '和茂大厦1幢1单元1层101室\\',水电费', '0', NULL, NULL, NULL, NULL, '0', '2022-07-13 14:56:30', 'b766f9007e7e11ecb9fa005056b63bdb', '2022-07-13 14:56:30', 'b766f9007e7e11ecb9fa005056b63bdb', '0', '0', '0', NULL);\r\n"
//				+ "");
//		while (matcher.find()) {
//			System.out.println(matcher.group());
//			System.out.println(matcher.group(1));
//			System.out.println(matcher.group(2));
//			Matcher matcher2 = fields.matcher(matcher.group(1));
//			while(matcher2.find()) {
//				String group = matcher2.group(1);
//				System.out.println(group);
//				
//			}
//			Matcher matcher3 = values.matcher(matcher.group(2));
//			while(matcher3.find()) {
//				String group = Optional.ofNullable(matcher3.group(1)).orElse(matcher3.group(2));
//				System.out.println(group);
//				
//				
//			}
//		}
		
		String s = "(00001c4ac78911edbaf5005056b63bdb, 9aef83d0bcc611edbaf5005056b63bdb, 0033000100050001000300010001001000020002, H004001001, 2024-03-21 00:00:00, 大象国际中心1幢10单元2层202室, 0, NULL, NULL, NULL, NULL, 1, 2023-03-21 09:37:54, sysadmin, 2023-04-27 16:43:22, sysadmin7, 1, 0, 0, NULL)";
		Matcher matcher3 = values.matcher(s);
		while(matcher3.find()) {
			String group = Optional.ofNullable(matcher3.group(1)).orElse(matcher3.group(2));
			group = Optional.ofNullable(group).orElse(matcher3.group(3));
			System.out.println(group);
			
			
		}
	}

}
