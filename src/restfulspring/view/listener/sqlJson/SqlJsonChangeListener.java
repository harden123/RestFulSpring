package restfulspring.view.listener.sqlJson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;

import cn.hutool.core.util.StrUtil;
import restfulspring.constant.YmdTypeEnum;
import restfulspring.utils.TextUtil;

public class SqlJsonChangeListener implements SelectionListener{
	public static final Pattern sqlsCompile = Pattern.compile("\\)\\s*;\\s*(?=INSERT)*",Pattern.CASE_INSENSITIVE);
	public static final Pattern compile = Pattern.compile("(\\(.+\\))\\s*values\\s*(\\(.+\\))",Pattern.CASE_INSENSITIVE);
	public static final Pattern fields = Pattern.compile("(?<!\\\\)`(.+?)(?<!\\\\)`\\s*,",Pattern.CASE_INSENSITIVE);
	public static final Pattern values = Pattern.compile("(?<!\\\\)'(.+?)(?<!\\\\)'|(?<=,)\\s*(NULL)\\s*(?=[,\\)])|(?<=[\\(,])\\s*(.+?)\\s*(?=[\\),])",Pattern.CASE_INSENSITIVE);


	private StyledText sqlText;
	private StyledText resultText;
	private Combo ymdCombo;

	public SqlJsonChangeListener(StyledText sqlText, StyledText resultText, Combo ymdCombo) {
		this.sqlText = sqlText;
		this.resultText = resultText;
		this.ymdCombo = ymdCombo;
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
		String ymdComboText = ymdCombo.getText();
		List<String> split = Lists.newArrayList();
		Matcher sqlsMatcher = sqlsCompile.matcher(text);
		int startIndex = 0;
        while (sqlsMatcher.find()) {
            String statement = text.substring(startIndex, sqlsMatcher.end()).trim();
            split.add(statement);
            startIndex = sqlsMatcher.end();
        }
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
					String camelCase = StrUtil.toCamelCase(group);
					fieldList.add(camelCase);
				}
//				('00001c4ac78911edbaf5005056b63bdb', '9aef83d0bcc611edbaf5005056b63bdb', '0033000100050001000300010001001000020002', 'H004001001', '2024-03-21 00:00:00', '大象国际中心1幢10单元2层202室', '0', NULL, NULL, NULL, NULL, '1', '2023-03-21 09:37:54', 'sysadmin', '2023-04-27 16:43:22', 'sysadmin7', '1', '0', '0', NULL)
				String valuetext = matcher.group(2).trim();
				Matcher matcher3 = values.matcher(valuetext);
				while(matcher3.find()) {
					String group = Optional.ofNullable(matcher3.group(1)).orElse(matcher3.group(2));
					group = Optional.ofNullable(group).orElse(matcher3.group(3));
					if (StringUtils.startsWith(group, "'")&&StringUtils.endsWith(group, "'")) {
						group = StringUtils.removeStart(group, "'");
						group = StringUtils.removeEnd(group, "'");
					}
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
					//TODO:hsl 2023/07/11-changeDate
					v = changeDate(k,v,ymdComboText);
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
	/**
	 * 转换日期
	 * @param getComboText 
	 */
	private Object changeDate(String k, Object v, String ymdComboText) {
		if (!StringUtils.startsWith(k, "gmt")&&!StringUtils.endsWithIgnoreCase(k, "Date")) {
			return v;
		}
		if (YmdTypeEnum.ymdhms.getDesc().equals(ymdComboText)) {
			return v;
		}
		String val = v+"";
		if (StringUtils.isNotBlank(val)) {
			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    	try {
				Date date = ft.parse(val);
				if (YmdTypeEnum.ymdhmsz.getDesc().equals(ymdComboText)) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
			        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));  // 设置时区为东八区
			        return sdf.format(date);
				}else if (YmdTypeEnum.miliSec.getDesc().equals(ymdComboText)) {
					return date.getTime();
				}else if (YmdTypeEnum.sec.getDesc().equals(ymdComboText)) {
					return date.getTime()/1000;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return v;
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
		
//		String s = "(00001c4ac78911edbaf5005056b63bdb, 9aef83d0bcc611edbaf5005056b63bdb, 0033000100050001000300010001001000020002, H004001001, 2024-03-21 00:00:00, 大象国际中心1幢10单元2层202室, 0, NULL, NULL, NULL, NULL, 1, 2023-03-21 09:37:54, sysadmin, 2023-04-27 16:43:22, sysadmin7, 1, 0, 0, NULL)";
//		Matcher matcher3 = values.matcher(s);
//		while(matcher3.find()) {
//			String group = Optional.ofNullable(matcher3.group(1)).orElse(matcher3.group(2));
//			group = Optional.ofNullable(group).orElse(matcher3.group(3));
//			System.out.println(group);
//		}
		
		
//		String s ="INSERT INTO `wit_test_b`.`notice_manage` (`id`, `title`, `intro`, `content`, `gmt_start`, `gmt_end`, `type`, `top`, `hot`, `sort`, `status`, `status_desc`, `gmt_issue`, `issuer`, `draft`, `link_type`, `link_url`, `range_type`, `creator`, `modifier`, `gmt_create`, `gmt_modify`, `deleted`) VALUES ('283912b941c811ecb9fa005056b63bdb', '测试公共1的风格是大法官是大法官恢诡谲怪监管就会高锦库', NULL, '<p>沙发范德萨范德萨ffsdafasdf</p>\\n<p>&nbsp;</p>\\n<p>sdafsdfsdafasdfsdfsaf</p>\\n<p>发色</p>', '2021-12-14 00:00:00', '2025-01-31 00:00:00', '1', NULL, NULL, '1', 'show', NULL, '2021-11-10 00:00:00', 'sysadmin3', '0', NULL, NULL, '1', 'sysadmin3', 'sysadmin4', '2021-11-10 09:47:27', '2023-02-13 10:28:12', '0');  "
//				+ "";
//		Matcher matcher = sqlsCompile.matcher(s);
//		int startIndex = 0;
//        while (matcher.find()) {
//            String statement = s.substring(startIndex, matcher.end()).trim();
//            System.out.println(statement);
//            startIndex = matcher.end();
//        }
	        
	}

}
