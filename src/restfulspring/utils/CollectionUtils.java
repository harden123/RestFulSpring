package restfulspring.utils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import restfulspring.dto.JDTTypeDTO;

public class CollectionUtils {

	/**
     * Null-safe check if the specified collection is empty.
     * <p>
     * Null returns true.
     * 
     * @param coll  the collection to check, may be null
     * @return true if empty or null
     * @since Commons Collections 3.2
     */
    public static boolean isEmpty(Collection coll) {
        return (coll == null || coll.isEmpty());
    }

	public static boolean isNotEmpty(List<JDTTypeDTO> list) {
		return !isEmpty(list);
	}

	public static boolean isNotEmpty(Map map) {
		return map!=null&&!map.isEmpty();
	}

	public static boolean isEmpty(Map map) {
		return map==null||map.isEmpty();
	}
}
