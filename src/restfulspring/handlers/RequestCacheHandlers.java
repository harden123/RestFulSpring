package restfulspring.handlers;

import restfulspring.Activator;

public class RequestCacheHandlers {

	private static boolean inMemory;
	
	
	public static String getBeyKey(String prefix,String methodUrl) {
		String decorateKey = prefix+methodUrl;
		if (inMemory) {
			return  Activator.MethodUrl2BodyTextCacheMap.get(decorateKey);
		}else {
			return Activator.getDefault().getPreferenceStore().getString(decorateKey);
		}
	}
	
	public static void put(String prefix,String methodUrl,String text) {
		String decorateKey = prefix+methodUrl;
		if (inMemory) {
			Activator.MethodUrl2BodyTextCacheMap.put(decorateKey, text);
		}else {
			Activator.getDefault().getPreferenceStore().putValue(decorateKey, text);
		}

	}

	public static void remove(String prefix,String methodUrl) {
		String decorateKey = prefix+methodUrl;
		if (inMemory) {
			Activator.MethodUrl2BodyTextCacheMap.remove(decorateKey);
		}else {
			Activator.getDefault().getPreferenceStore().setToDefault(decorateKey);
		}
	}
	
}
