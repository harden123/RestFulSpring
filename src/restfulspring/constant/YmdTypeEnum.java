package restfulspring.constant;

import java.util.ArrayList;

import com.google.common.collect.Lists;

import lombok.Getter;

/**
 * YmdTypeEnum类型
 */
@Getter
public enum YmdTypeEnum {

	ymdhmsz(0, "ymdhmsz"),
	ymdhms(1, "ymdhms"),
	miliSec(2, "miliSec"),
	sec(3, "sec"),

	;

	private Integer key;
	private String desc;

	YmdTypeEnum(Integer key, String desc) {
		this.key = key;
		this.desc = desc;
	}

	public static String getDesc(Integer key) {
		for (YmdTypeEnum auth : YmdTypeEnum.values()) {
			if (auth.getKey().equals(key)) {
				return auth.getDesc();
			}
		}
		return null;
	}

	public static YmdTypeEnum getByKey(Integer key) {
		if (key == null) {
			return null;
		}
		for (YmdTypeEnum type : values()) {
			if (type.getKey().equals(key)) {
				return type;
			}
		}
		return null;
	}
	
	public static Integer getKeyByDesc(String desc) {
		for (YmdTypeEnum auth : YmdTypeEnum.values()) {
			if (auth.getDesc().equals(desc)) {
				return auth.getKey();
			}
		}
		return null;
	}

	public static String[] toDescArray() {
		ArrayList<String> arrayList = Lists.newArrayList();
		for (YmdTypeEnum auth : YmdTypeEnum.values()) {
			arrayList.add(auth.getDesc());
		}
        String[] stringArray = arrayList.stream().toArray(String[]::new);
		return stringArray;
	}

}
