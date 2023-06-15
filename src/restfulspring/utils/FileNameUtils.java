package restfulspring.utils;

import java.util.regex.Pattern;

public class FileNameUtils {
	/**
	 * 类Unix路径分隔符
	 */
	public static final char UNIX_SEPARATOR = CharUtil.SLASH;
	/**
	 * Windows路径分隔符
	 */
	public static final char WINDOWS_SEPARATOR = CharUtil.BACKSLASH;

	/**
	 * Windows下文件名中的无效字符
	 */
	private static final Pattern FILE_NAME_INVALID_PATTERN_WIN = Pattern.compile("[\\\\/:*?\"<>|]");
	/**
	 * 数组中元素未找到的下标，值为-1
	 */
	public static final int INDEX_NOT_FOUND = -1;

	// -------------------------------------------------------------------------------------------- name start


	/**
	 * 返回主文件名
	 *
	 * @param fileName 完整文件名
	 * @return 主文件名
	 * @see #mainName(String)
	 * @since 5.3.8
	 */
	public static String getPrefix(String fileName) {
		return mainName(fileName);
	}
	

	/**
	 * 获得文件后缀名，扩展名不带“.”
	 *
	 * @param fileName 文件名
	 * @return 扩展名
	 * @see #extName(String)
	 * @since 5.3.8
	 */
	public static String getSuffix(String fileName) {
		return extName(fileName);
	}
	/**
	 * 返回主文件名
	 *
	 * @param fileName 完整文件名
	 * @return 主文件名
	 */
	public static String mainName(String fileName) {
		if (null == fileName) {
			return null;
		}
		int len = fileName.length();
		if (0 == len) {
			return fileName;
		}
		if (isFileSeparator(fileName.charAt(len - 1))) {
			len--;
		}

		int begin = 0;
		int end = len;
		char c;
		for (int i = len - 1; i >= 0; i--) {
			c = fileName.charAt(i);
			if (len == end && CharUtil.DOT == c) {
				// 查找最后一个文件名和扩展名的分隔符：.
				end = i;
			}
			// 查找最后一个路径分隔符（/或者\），如果这个分隔符在.之后，则继续查找，否则结束
			if (CharUtil.isFileSeparator(c)) {
				begin = i + 1;
				break;
			}
		}

		return fileName.substring(begin, end);
	}
	
	/**
	 * 返回文件名
	 *
	 * @param filePath 文件
	 * @return 文件名
	 * @since 4.1.13
	 */
	public static String getName(String filePath) {
		if (null == filePath) {
			return null;
		}
		int len = filePath.length();
		if (0 == len) {
			return filePath;
		}
		if (isFileSeparator(filePath.charAt(len - 1))) {
			// 以分隔符结尾的去掉结尾分隔符
			len--;
		}

		int begin = 0;
		char c;
		for (int i = len - 1; i > -1; i--) {
			c = filePath.charAt(i);
			if (isFileSeparator(c)) {
				// 查找最后一个路径分隔符（/或者\）
				begin = i + 1;
				break;
			}
		}

		return filePath.substring(begin, len);
	}

	

	/**
	 * 是否为Windows或者Linux（Unix）文件分隔符<br>
	 * Windows平台下分隔符为\，Linux（Unix）为/
	 *
	 * @param c 字符
	 * @return 是否为Windows或者Linux（Unix）文件分隔符
	 * @since 4.1.11
	 */
	public static boolean isFileSeparator(char c) {
		return CharUtil.SLASH == c || CharUtil.BACKSLASH == c;
	}
	
	/**
	 * 获得文件的扩展名（后缀名），扩展名不带“.”
	 *
	 * @param fileName 文件名
	 * @return 扩展名
	 */
	public static String extName(String fileName) {
		if (fileName == null) {
			return null;
		}
		int index = fileName.lastIndexOf(StrUtil.DOT);
		if (index == -1) {
			return StrUtil.EMPTY;
		} else {
			String ext = fileName.substring(index + 1);
			// 扩展名中不能包含路径相关的符号
			return containsAny(ext, UNIX_SEPARATOR, WINDOWS_SEPARATOR) ? StrUtil.EMPTY : ext;
		}
	}
	
	public static boolean containsAny(CharSequence str, char... testChars) {
		if (false == isEmpty(str)) {
			int len = str.length();
			for (int i = 0; i < len; i++) {
				if (contains(testChars, str.charAt(i))) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * 数组中是否包含元素
	 *
	 * @param array 数组
	 * @param value 被检查的元素
	 * @return 是否包含
	 * @since 3.0.7
	 */
	public static boolean contains(char[] array, char value) {
		return indexOf(array, value) > INDEX_NOT_FOUND;
	}
	public static int indexOf(char[] array, char value) {
		if (null != array) {
			for (int i = 0; i < array.length; i++) {
				if (value == array[i]) {
					return i;
				}
			}
		}
		return INDEX_NOT_FOUND;
	}
	/**
	 * 查找指定字符串是否包含指定字符串列表中的任意一个字符串，如果包含返回找到的第一个字符串
	 *
	 * @param str      指定字符串
	 * @param testStrs 需要检查的字符串数组
	 * @return 被包含的第一个字符串
	 * @since 3.2.0
	 */
	public static String getContainsStr(CharSequence str, CharSequence... testStrs) {
		if (isEmpty(str) || isEmpty(testStrs)) {
			return null;
		}
		for (CharSequence checkStr : testStrs) {
			if (str.toString().contains(checkStr)) {
				return checkStr.toString();
			}
		}
		return null;
	}
	

	public static boolean isEmpty(CharSequence str) {
		return str == null || str.length() == 0;
	}
	
	/**
	 * 数组是否为空
	 *
	 * @param array 数组
	 * @return 是否为空
	 */
	public static boolean isEmpty(CharSequence[] array) {
		return array == null || array.length == 0;
	}
}
