package tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class TestUnicode {
	private static final char[] DIGITS = {'\u96F6', '一', '壹', '二', '贰', '三', '叁', '四', '肆', '五', '伍',
			'六', '陆', '七', '柒', '八', '捌', '九', '玖'};

	Pattern pattern = Pattern.compile("(')([^\\x00-\\x7F]+)(')");


	@Test
	public void testChangeUnicode() throws Exception {
		String s = "private static final char[] DIGITS = {'\\u96F6', '一', '壹', '二', '贰', '三', '叁', '四', '肆', '五', '伍',\r\n"
				+ "			'六', '陆', '七', '柒', '八', '捌', '九', '玖'}";
		StringBuffer output = new StringBuffer();
		Matcher matcher = pattern.matcher(s);
		while (matcher.find()) {
		    String chineseText = matcher.group(2);
		    String unicodeText = unicodeEncode(chineseText);
		    matcher.appendReplacement(output, "$1" + "\\\\u"+unicodeText+"$3");
		}
		matcher.appendTail(output);
		String replacedContent = output.toString();
		System.out.println(replacedContent);
	}

	@Test
	public void replaceChineseFolder() throws Exception {
		String folderPath = "C:\\env\\wit\\RestFulSpring\\src\\cn\\hutool\\core";
		File folder = new File(folderPath);
		processFiles(folder);
	}
	
	
	private void replaceChinese(File file) {
	    try (BufferedReader reader = new BufferedReader(new FileReader(file))){
	        String line;
	        StringBuilder content = new StringBuilder();
	        boolean find =false;
	        while ((line = reader.readLine()) != null) {
	            // 使用正则表达式替换单引号中的中文字符
	        	StringBuffer output = new StringBuffer();
	    		Matcher matcher = pattern.matcher(line);
	    		while (matcher.find()) {
	    			find=true;
	    		    String chineseText = matcher.group(2);
	    		    String unicodeText = unicodeEncode(chineseText);
	    		    matcher.appendReplacement(output, "$1" + "\\\\u"+unicodeText+"$3");
	    		    System.out.println(chineseText);

	    		}
	    		matcher.appendTail(output);
	    		String replacedLine = output.toString();
	            content.append(replacedLine).append("\n");
	        }
	        if (find) {
	        	  // 将替换后的内容写回文件
	        	System.out.println(file.getAbsolutePath());
		        FileWriter writer = new FileWriter(file);
		        writer.write(content.toString());
		        writer.close();
			}
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	private void processFiles(File folder) {
	    if (folder.isDirectory()) {
	        File[] files = folder.listFiles();
	        for (File file : files) {
	            if (file.isDirectory()) {
	                processFiles(file);  // 递归处理子文件夹
	            } else {
	                replaceChinese(file);  // 替换当前文件中的中文字符
	            }
	        }
	    }
	}
	
	private String unicodeEncode(String chinese) {
		StringBuilder unicode = new StringBuilder();
		for (char ch : chinese.toCharArray()) {
			unicode.append(Integer.toHexString(ch));
		}
		return unicode.toString();
	}
}
