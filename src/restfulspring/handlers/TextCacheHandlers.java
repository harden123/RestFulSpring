package restfulspring.handlers;

import restfulspring.Activator;
import restfulspring.constant.RestConstant;

public class TextCacheHandlers {

	private static boolean inMemory;
	
	
	public static String getBeyKey(String methodUrl) {
		String decorateKey = getDecorateKey(methodUrl);
		if (inMemory) {
			return  Activator.MethodUrl2BodyTextCacheMap.get(decorateKey);
		}else {
			return Activator.getDefault().getPreferenceStore().getString(decorateKey);
		}
	}
	
	public static void put(String methodUrl,String text) {
		String decorateKey = getDecorateKey(methodUrl);
		if (inMemory) {
			Activator.MethodUrl2BodyTextCacheMap.put(decorateKey, text);
		}else {
			Activator.getDefault().getPreferenceStore().putValue(decorateKey, text);
		}

	}

	public static void remove(String methodUrl) {
		String decorateKey = getDecorateKey(methodUrl);
		if (inMemory) {
			Activator.MethodUrl2BodyTextCacheMap.remove(decorateKey);
		}else {
			Activator.getDefault().getPreferenceStore().setToDefault(decorateKey);
		}
	}
	
	private static String getDecorateKey(String methodUrl) {
		return RestConstant.BodyText+methodUrl;

	}
}
