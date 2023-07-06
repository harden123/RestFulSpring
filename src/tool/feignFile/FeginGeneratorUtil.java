package tool.feignFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.hutool.core.io.FileUtil;
import lombok.SneakyThrows;

public class FeginGeneratorUtil {

	public static boolean headReq=true;
	
	private static String desktop = FileUtil.getUserHomePath()+"\\Desktop";
	
	public static String dtoSuffix = "";
//	public static String dtoSuffix = "Feign";

	@SneakyThrows
	public static void generator(String filePath,String feignClientName) {
		String requestMappingHead = null; // "/fc/configBaisc"
		File file = new File(filePath);
		List<String> serviceLines = FileUtil.readUtf8Lines(file);
		List<String> list = new ArrayList<String>();
		ServiceImplFacade serviceImplFacade = new ServiceImplFacade(filePath);
		requestMappingHead = serviceImplFacade.getRequestMappingHeadFromImpl();
		for (String line : serviceLines) {
			String requestMapping = serviceImplFacade.buildRequestMapping(line);
			if (requestMapping!=null) {
				//方法上加requestMapping
				list.add(requestMapping);
				//处理方法参数
				list.add(serviceImplFacade.handleFunctionLine(line));
				continue;
			}
			String feignClient =  serviceImplFacade.getFeignClientLine(line,feignClientName);
			if(feignClient!=null) {
				list.add(feignClient);
				if (headReq) {
					list.add("@RequestMapping(value = \""+requestMappingHead+"\")");
				}
				String serviceLine = serviceImplFacade.getServiceLine(line);
				if (serviceLine!=null) {
					list.add(serviceLine);
				}
				continue;
			}
			list.add(line);
			
		}
		list.add(2,"import org.springframework.cloud.netflix.feign.FeignClient;");
		if(serviceImplFacade.hasRequestMapping||serviceImplFacade.hasRequestBody) {
			list.add(2, "import org.springframework.web.bind.annotation.*;");
		}
		String dir = desktop+"\\feignOutput";
		FileUtil.mkdir(dir);
		String newFilePath = dir+"\\"+serviceImplFacade.getfileName()+dtoSuffix+".java";
		FileUtil.writeUtf8Lines(list, new File(newFilePath));
	}
	
}
