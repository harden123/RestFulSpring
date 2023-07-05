package restfulspring.utils;

import java.util.function.Predicate;

public class StrUtil {
	public static final int INDEX_NOT_FOUND = -1;

	/**
	 * 字符常量：空格符 {@code ' '}
	 */
	public static final char C_SPACE = CharUtil.SPACE;

	/**
	 * 字符常量：制表符 {@code '\t'}
	 */
	public static final char C_TAB = CharUtil.TAB;

	/**
	 * 字符常量：点 {@code '.'}
	 */
	public static final char C_DOT = CharUtil.DOT;

	/**
	 * 字符常量：斜杠 {@code '/'}
	 */
	public static final char C_SLASH = CharUtil.SLASH;

	/**
	 * 字符常量：反斜杠 {@code '\\'}
	 */
	public static final char C_BACKSLASH = CharUtil.BACKSLASH;

	/**
	 * 字符常量：回车符 {@code '\r'}
	 */
	public static final char C_CR = CharUtil.CR;

	/**
	 * 字符常量：换行符 {@code '\n'}
	 */
	public static final char C_LF = CharUtil.LF;

	/**
	 * 字符常量：下划线 {@code '_'}
	 */
	public static final char C_UNDERLINE = CharUtil.UNDERLINE;

	/**
	 * 字符常量：逗号 {@code ','}
	 */
	public static final char C_COMMA = CharUtil.COMMA;

	/**
	 * 字符常量：花括号（左） <code>'{'</code>
	 */
	public static final char C_DELIM_START = CharUtil.DELIM_START;

	/**
	 * 字符常量：花括号（右） <code>'}'</code>
	 */
	public static final char C_DELIM_END = CharUtil.DELIM_END;

	/**
	 * 字符常量：中括号（左） {@code '['}
	 */
	public static final char C_BRACKET_START = CharUtil.BRACKET_START;

	/**
	 * 字符常量：中括号（右） {@code ']'}
	 */
	public static final char C_BRACKET_END = CharUtil.BRACKET_END;

	/**
	 * 字符常量：冒号 {@code ':'}
	 */
	public static final char C_COLON = CharUtil.COLON;

	/**
	 * 字符常量：艾特 <code>'@'</code>
	 */
	public static final char C_AT = CharUtil.AT;


	/**
	 * 字符串常量：空格符 {@code " "}
	 */
	public static final String SPACE = " ";

	/**
	 * 字符串常量：制表符 {@code "\t"}
	 */
	public static final String TAB = "	";

	/**
	 * 字符串常量：点 {@code "."}
	 */
	public static final String DOT = ".";

	/**
	 * 字符串常量：双点 {@code ".."} <br>
	 * 用途：作为指向上级文件夹的路径，如：{@code "../path"}
	 */
	public static final String DOUBLE_DOT = "..";

	/**
	 * 字符串常量：斜杠 {@code "/"}
	 */
	public static final String SLASH = "/";

	/**
	 * 字符串常量：反斜杠 {@code "\\"}
	 */
	public static final String BACKSLASH = "\\";

	/**
	 * 字符串常量：空字符串 {@code ""}
	 */
	public static final String EMPTY = "";

	/**
	 * 字符串常量：{@code "null"} <br>
	 * 注意：{@code "null" != null}
	 */
	public static final String NULL = "null";

	/**
	 * 字符串常量：回车符 {@code "\r"} <br>
	 * 解释：该字符常用于表示 Linux 系统和 MacOS 系统下的文本换行
	 */
	public static final String CR = "\r";

	/**
	 * 字符串常量：换行符 {@code "\n"}
	 */
	public static final String LF = "\n";

	/**
	 * 字符串常量：Windows 换行 {@code "\r\n"} <br>
	 * 解释：该字符串常用于表示 Windows 系统下的文本换行
	 */
	public static final String CRLF = "\r\n";

	/**
	 * 字符串常量：下划线 {@code "_"}
	 */
	public static final String UNDERLINE = "_";

	/**
	 * 字符串常量：减号（连接符） {@code "-"}
	 */
	public static final String DASHED = "-";

	/**
	 * 字符串常量：逗号 {@code ","}
	 */
	public static final String COMMA = ",";

	/**
	 * 字符串常量：花括号（左） <code>"{"</code>
	 */
	public static final String DELIM_START = "{";

	/**
	 * 字符串常量：花括号（右） <code>"}"</code>
	 */
	public static final String DELIM_END = "}";

	/**
	 * 字符串常量：中括号（左） {@code "["}
	 */
	public static final String BRACKET_START = "[";

	/**
	 * 字符串常量：中括号（右） {@code "]"}
	 */
	public static final String BRACKET_END = "]";

	/**
	 * 字符串常量：冒号 {@code ":"}
	 */
	public static final String COLON = ":";

	/**
	 * 字符串常量：艾特 <code>"@"</code>
	 */
	public static final String AT = "@";


	/**
	 * 字符串常量：HTML 空格转义 {@code "&nbsp;" -> " "}
	 */
	public static final String HTML_NBSP = "&nbsp;";

	/**
	 * 字符串常量：HTML And 符转义 {@code "&amp;" -> "&"}
	 */
	public static final String HTML_AMP = "&amp;";

	/**
	 * 字符串常量：HTML 双引号转义 {@code "&quot;" -> "\""}
	 */
	public static final String HTML_QUOTE = "&quot;";

	/**
	 * 字符串常量：HTML 单引号转义 {@code "&apos" -> "'"}
	 */
	public static final String HTML_APOS = "&apos;";

	/**
	 * 字符串常量：HTML 小于号转义 {@code "&lt;" -> "<"}
	 */
	public static final String HTML_LT = "&lt;";

	/**
	 * 字符串常量：HTML 大于号转义 {@code "&gt;" -> ">"}
	 */
	public static final String HTML_GT = "&gt;";

	/**
	 * 字符串常量：空 JSON <code>"{}"</code>
	 */
	public static final String EMPTY_JSON = "{}";


	// ------------------------------------------------------------------------ Blank
	/**
	 * 除去字符串头部的空白，如果字符串是{@code null}，则返回{@code null}。
	 *
	 * <p>
	 * 注意，和{@link String#trim()}不同，此方法使用{@link CharUtil#isBlankChar(char)} 来判定空白， 因而可以除去英文字符集之外的其它空白，如中文空格。
	 *
	 * <pre>
	 * trimStart(null)         = null
	 * trimStart(&quot;&quot;)           = &quot;&quot;
	 * trimStart(&quot;abc&quot;)        = &quot;abc&quot;
	 * trimStart(&quot;  abc&quot;)      = &quot;abc&quot;
	 * trimStart(&quot;abc  &quot;)      = &quot;abc  &quot;
	 * trimStart(&quot; abc &quot;)      = &quot;abc &quot;
	 * </pre>
	 *
	 * @param str 要处理的字符串
	 * @return 除去空白的字符串，如果原字串为{@code null}或结果字符串为{@code ""}，则返回 {@code null}
	 */
	public static String trimStart(CharSequence str) {
		return trim(str, -1);
	}
	
	/**
	 * 除去字符串头尾部的空白符，如果字符串是{@code null}，依然返回{@code null}。
	 *
	 * @param str  要处理的字符串
	 * @param mode {@code -1}表示trimStart，{@code 0}表示trim全部， {@code 1}表示trimEnd
	 * @return 除去指定字符后的的字符串，如果原字串为{@code null}，则返回{@code null}
	 */
	public static String trim(CharSequence str, int mode) {
		return trim(str, mode, CharUtil::isBlankChar);
	}
	/**
	 * 按照断言，除去字符串头尾部的断言为真的字符，如果字符串是{@code null}，依然返回{@code null}。
	 *
	 * @param str       要处理的字符串
	 * @param mode      {@code -1}表示trimStart，{@code 0}表示trim全部， {@code 1}表示trimEnd
	 * @param predicate 断言是否过掉字符，返回{@code true}表述过滤掉，{@code false}表示不过滤
	 * @return 除去指定字符后的的字符串，如果原字串为{@code null}，则返回{@code null}
	 * @since 5.7.4
	 */
	public static String trim(CharSequence str, int mode, Predicate<Character> predicate) {
		String result;
		if (str == null) {
			result = null;
		} else {
			int length = str.length();
			int start = 0;
			int end = length;// 扫描字符串头部
			if (mode <= 0) {
				while ((start < end) && (predicate.test(str.charAt(start)))) {
					start++;
				}
			}// 扫描字符串尾部
			if (mode >= 0) {
				while ((start < end) && (predicate.test(str.charAt(end - 1)))) {
					end--;
				}
			}
			if ((start > 0) || (end < length)) {
				result = str.toString().substring(start, end);
			} else {
				result = str.toString();
			}
		}

		return result;
	}
}
