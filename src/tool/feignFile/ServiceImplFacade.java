package tool.feignFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import cn.hutool.core.io.FileUtil;

public class ServiceImplFacade {
	String requestMappingTemp = "    @RequestMapping(value = \"$1/$2\")";
	String requestParamTemp = "@RequestParam(value=\"$1\",required=false)";
	public boolean hasRequestMapping = false;
	public boolean hasRequestBody = false;
	static Pattern general = Pattern.compile("[a-zA-Z0-9_]");

	private String filePath;
	private String requestMappingHead;//  /fc/configBaisc

	public ServiceImplFacade(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * find ServiceImpl @RequestMapping
	 */
	public String getRequestMappingHeadFromImpl() throws IOException  {
		requestMappingHead = null;
		String servicePath = filePath;
		for (int i = 0; i < 4; i++) {
			servicePath = servicePath.substring(0, servicePath.lastIndexOf("\\"));
		}
        List<File> listFiles = FileUtil.loopFiles(new File(servicePath), file -> file.getName().endsWith(".java"));
		File implFile = null;
		String implFileName = getfileName();
		if (implFileName.endsWith("Impl")) {
			implFileName = implFileName+".java";
		}else {
			implFileName = implFileName+"Impl.java";
		}
		for (File file : listFiles) {
			String name = file.getName();
			if(name.equalsIgnoreCase(implFileName)) {
				implFile=file;
				break;
			}
		}
		if(implFile!=null) {
			List<String> lines = FileUtil.readUtf8Lines(implFile);
			Pattern p = Pattern.compile("^\\s*@RequestMapping.+\"(.*)\".+", Pattern.CASE_INSENSITIVE);
			for (String line : lines) {
				Matcher mathcer = p.matcher(line);
				boolean find = mathcer.find();
				if(find) {
					requestMappingHead = mathcer.group(1);
					break;
				}

			}
		}
		if(requestMappingHead==null) {
			System.out.println("找不到"+getfileName()+"Impl.java实现类或者实现类上未加requestMapping注解");
			throw new RuntimeException("找不到"+getfileName()+"Impl.java实现类或者实现类上未加requestMapping注解");
		}
		return requestMappingHead;
	}
	
	public String getfileName() {
		return filePath.substring(filePath.lastIndexOf("\\")+1, filePath.lastIndexOf("."));
	}
	
	/**
	 * if function line return  RequestMapping line ,else null
	 * @param line
	 * @return
	 */
	public String buildRequestMapping(String line) {
		String replaceAll = null;
		line = line.trim();
		//是否是接口方法
		Pattern p = Pattern.compile("^\\b(public\\s+)?.+\\s+(\\w+)\\s?\\(", Pattern.CASE_INSENSITIVE);
		Matcher mathcer = p.matcher(line);
		boolean find = mathcer.find();
		if(find) {
			String functionName = mathcer.group(2);
			if (FeginGeneratorUtil.headReq) {
				replaceAll= requestMappingTemp.replaceAll("\\$1", "");
			}else {
				replaceAll= requestMappingTemp.replaceAll("\\$1", requestMappingHead);
			}
			replaceAll = replaceAll.replaceAll("\\$2", functionName);
		}
		return replaceAll;
	}
	
	/**
	 * append @RequestBody or @RequestParam to request params
	 * @param line
	 * @return
	 * @throws Exception
	 */
	public String handleFunctionLine(String line) throws Exception {
		Pattern p = Pattern.compile("\\((.*)\\)", Pattern.CASE_INSENSITIVE);
		Matcher mathcer = p.matcher(line);
		boolean find = mathcer.find();
		String result = null;
		StringBuffer sb = new StringBuffer();
		if(find) {
			String params = mathcer.group(1).trim();//"int pageNum, int pageSize" or  "" or " "
			StringBuffer newParams = new StringBuffer("(");
			String[] paramArr = params.split(",");
			for (String param : paramArr) {
				if(param.trim().equals("")) {
					continue;
				}
				String newParam = handleParam(param.trim());
				newParams.append(newParam).append(", ");
			}
			//delete last no use commma
			int lastIndexOf = newParams.lastIndexOf(", ");
			if(lastIndexOf!=-1&&lastIndexOf==newParams.length()-2) {
				newParams = newParams.deleteCharAt(newParams.length()-1);
				newParams = newParams.deleteCharAt(newParams.length()-1);
			}
			newParams.append(")");
			mathcer.appendReplacement(sb, newParams.toString());
			sb = mathcer.appendTail(sb);
			result = sb.toString();
		}
		return result;
	}
	
	
	
	/**
	 * append @RequestBody or @RequestParam to request param
	 * @param param
	 * @return
	 */
	public String handleParam(String param) {
		String[] split = param.split("\\s+");
		String requestAttr="";
		String type = split[0];
		String paramName = split[1];
		if(isPrimitive(type)) {
			requestAttr = requestParamTemp.replaceAll("\\$1", paramName);
			hasRequestMapping=true;
		}else {
			requestAttr="@RequestBody";
			hasRequestBody=true;
		}
		return requestAttr+" "+param;
		
	}
	
	
	
	private static boolean isPrimitive(String type) {
		ArrayList<String> list= new ArrayList<String>();
		list.add("byte");
		list.add("Byte");
		list.add("short");
		list.add("Short");
		list.add("long");
		list.add("Long");
		list.add("int");
		list.add("Integer");
		list.add("String");
		list.add("float");
		list.add("Float");
		list.add("double");
		list.add("Double");
		list.add("boolean");
		list.add("Boolean");
		list.add("char");
		list.add("Character");
		if(list.contains(type)) {
			return true;
		}
		if(type.endsWith("Enum")) {
			return true;
		}
		if(type.startsWith("ArryList")||type.startsWith("List")) {
			return false;
		}
		return false;
	}
	
	
	/**
	 * if interface line return  @FeignClient line ,else null
	 * @param feignClientName 
	 */
	public String getFeignClientLine(String line, String feignClientName) {
		Pattern p = Pattern.compile("^\\s*public\\s+interface", Pattern.CASE_INSENSITIVE);
		Matcher mathcer = p.matcher(line);
		boolean find = mathcer.find();
		String feignClient = null;
		if(find) {
			int secBackslash = requestMappingHead.indexOf("/", 1);
			if(secBackslash>0) {
				String module = requestMappingHead.substring(1, secBackslash);
				if (StringUtils.isNotBlank(feignClientName)) {
					feignClient="@FeignClient(value = \""+feignClientName+"\")";

				}else if(module.equals("service")) {
					feignClient="@FeignClient(value = \"service\")";
				}else {
					feignClient="@FeignClient(value = \"service-"+module+"\")";
				}
			}
		}
		return feignClient;
	}

	/**
	 * if interface line return interface
	 */
	public String getServiceLine(String line) {
		Pattern p = Pattern.compile("^\\s*public\\s+interface", Pattern.CASE_INSENSITIVE);
		Matcher mathcer = p.matcher(line);
		boolean find = mathcer.find();
		if(find) {
			if (StringUtils.contains(line, " extends ")) {
				line = StringUtils.substringBefore(line, " extends ")+" {";
			}
		}
		if (StringUtils.isNotBlank(FeginGeneratorUtil.dtoSuffix)) {
			line = insertSuffix(line,FeginGeneratorUtil.dtoSuffix);
		}
		return line;
	}
	
	/**
	 * 插入尾巴
	 */
	private String insertSuffix(String input, String dtoSuffix) {
		Matcher m = general.matcher(input);
		int end = -1;
		while(m.find()) { 
		     System.out.println(m.group()); 
		     end = m.end();
		} 
		if (end<=0) {
			return input;
		}
		String a = StringUtils.substring(input, 0,end);
		String b = StringUtils.substring(input, end);
		return a+dtoSuffix+b;
	}

}
