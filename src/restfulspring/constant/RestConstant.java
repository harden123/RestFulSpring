package restfulspring.constant;

import java.io.File;

public class RestConstant {
	public static String lineSeparator = System.lineSeparator();
	public static String fileSeparator = File.separator;

	/*----------------------------------restView --------------------------------------*/
	/*-----------------annos ------------------*/
	public static final String RequestMapping = "RequestMapping";
	public static final Object RestController = "RestController";
	public static final String FeignClient = "FeignClient";
	
	public static final String RequestParam = "RequestParam";
	public static final String RequestBody = "RequestBody";

	public static final String RequestMapping_Method = "method";
	public static final String RequestMapping_value = "value";
	

	
	/*----------------filterJavaFile ---------------*/

	public static final String Controller = "Controller";
	public static final String Service = "Service";

	/*----------------qualifiedName ----------------*/
	public static final String java_lang_String = "java.lang.String";
	public static final String java_lang_Integer = "java.lang.Integer";
	
	
	/*---------------sp -----------------------------*/

	public static final String UrlPrefix = "urlPrefix";
	public static final String Headers = "Headers";
	public static final String BodyText = "bodyText:";
	public static final String UrlText = "urlText:";


	/*----------------------------------sqlJSONView --------------------------------------*/

	/*-----------------sp --------------------*/
	public static final String SqlJson_ymd = "SqlJson_ymd";
	public static final String JsonSql_ymd = "JsonSql_ymd";

	
	/*----------------------------------quickAssist ----------------------------------------------*/

	public static final String TYPE_ROW_BOUNDS = "org.apache.ibatis.session.RowBounds";

	


}
