package restfulspring.utils;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Maps;

import lombok.SneakyThrows;

public class TextUtil {
	public static Pattern compile = Pattern.compile("(?<!\\\\)\"\\s*:", 0);

	
	public static boolean isTextEqualKey(String text1,String text2) {
		Set<String> allKeys = getAllKeys(text1);
		Set<String> allKeys2 = getAllKeys(text2);
		return allKeys.containsAll(allKeys2)&&allKeys.size()==allKeys2.size();
	}
	

    /**
     * 递归获取 JSON 对象中的所有 key
     */
    private static Set<String> getAllKeys(Object obj) {
        Set<String> keys = new HashSet<>();
        if (obj instanceof JSONObject) {
            // 如果当前对象是 JSONObject，则遍历其中所有的子节点
            JSONObject jsonObj = (JSONObject) obj;
            for (String key : jsonObj.keySet()) {
                keys.add(key);
                keys.addAll(getAllKeys(jsonObj.get(key)));
            }
        } else if (obj instanceof JSONArray) {
            // 如果当前对象是 JSONArray，则遍历其中所有的元素
            JSONArray jsonArray = (JSONArray) obj;
            for (Object subObj : jsonArray) {
                keys.addAll(getAllKeys(subObj));
            }
        }
        // 其它类型的对象不包含 key，直接返回空 Set
        return keys;
    }
    
	@SneakyThrows
	public static IMethod getCursorMethod(IEditorPart editorPart) {
		if (editorPart instanceof ITextEditor) {
		    ITextEditor textEditor = (ITextEditor) editorPart;
		    IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
		    ISelection selection = textEditor.getSelectionProvider().getSelection();
		    if (selection instanceof ITextSelection) {
		        ITextSelection textSelection = (ITextSelection) selection;
		        int offset = textSelection.getOffset();
		        // 获取与打开 Java 文件对应的 ICompilationUnit 对象
		        IFile file = textEditor.getEditorInput().getAdapter(IFile.class);
		        ICompilationUnit compilationUnit = JavaCore.createCompilationUnitFrom(file);
		        try {
		            int lineNumber = document.getLineOfOffset(offset);
		            IJavaElement element = compilationUnit.getElementAt(document.getLineOffset(lineNumber));
		            if (element != null && element instanceof IMethod) {
		                IMethod method = (IMethod) element;
		                return method;
		            }
		        } catch (BadLocationException e) {
		            e.printStackTrace();
		        }
		    }
		}
		return null;
	}

	@SneakyThrows
	public static int getCursorLineNumber(IEditorPart iep) {
		ITextEditor ite = null;
		TextSelection selection = null;
		IDocument document = null;
		ISelectionProvider selectionProvider = null;
		if (iep instanceof ITextEditor) {
			ite = (ITextEditor) iep;
			selectionProvider = ite.getSelectionProvider();
			selection = (TextSelection) selectionProvider.getSelection();
			document = ite.getDocumentProvider().getDocument(ite.getEditorInput());
			int offset = selection.getOffset();
	        int lineNumber = document.getLineOfOffset(offset);
	        return lineNumber;
		}
		return -1;
	}
	
	public static Map<String, String> parseHeaders(String head) {
		HashMap<String, String> kvMap = Maps.newLinkedHashMap();
		if (StringUtils.isBlank(head)) {
			return  kvMap;
		}
		String[] split = head.split("\r\n");
		for (int i = 0; i < split.length; i++) {
			String line = StringUtils.trimToEmpty(split[i]);
			if (StringUtils.isBlank(line)) {
				continue;
			}
			String[] kv = line.split(":");
			if (kv==null||kv.length!=2) {
				continue;
			}
			kvMap.put(StringUtils.trimToEmpty(kv[0]), StringUtils.trimToEmpty(kv[1]));
		}
		return kvMap;
	}
	
	@SneakyThrows
	public static String initGetParam(Map<String, Object> map) {
		if (CollectionUtils.isEmpty(map)) {
			return "";
		}
		StringBuffer sb = new StringBuffer("?");
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();
			Object value = Optional.ofNullable(entry.getValue()).orElse("");
			if (value instanceof JSONArray) {
				if (((JSONArray)value).size()>0) {
					value=((JSONArray)value).get(0);
				}else {
					value = "";
				}
				
			}else if (value instanceof JSONObject) {
				value = "";
			}
			String encodedParamValue = URLEncoder.encode(value+"", "UTF-8").replaceAll("\\+", "%20");
			sb.append(key).append("=").append(encodedParamValue).append("&");
		}
		sb = sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
	
	public static String getUrlParam(String url) {
		if (!StringUtils.contains(url, "?")) {
			return "";
		}
		String substringAfter = "?"+StringUtils.substringAfter(url, "?");
		return substringAfter;
	}
	
	public static String prettyJSON(Object parse) {
		String jsonString = parse+"";
		if (parse!=null) {
			if (parse instanceof JSON) {
				jsonString = JSON.toJSONString(parse,new SerializerFeature[] {
						SerializerFeature.WriteMapNullValue,
						SerializerFeature.PrettyFormat,
						SerializerFeature.SortField,
						SerializerFeature.MapSortField,});
			}else if(JSON.isValid(parse+"")) {
				Object parse2 = JSON.parse(parse+"");
				jsonString =  JSON.toJSONString(parse2,new SerializerFeature[] {
						SerializerFeature.WriteMapNullValue,
						SerializerFeature.PrettyFormat,
						SerializerFeature.SortField,
						SerializerFeature.MapSortField,	});
			}
			Matcher matcher = compile.matcher(jsonString);
			StringBuffer sb = new StringBuffer();
			while (matcher.find()) {
			    matcher.appendReplacement(sb, "$0"+" ");
			}
			matcher.appendTail(sb);
			jsonString = sb.toString();
		}
		return jsonString;
	}


}
