package restfulspring.constant;

import lombok.Getter;

/**
 * RestType类型
 */
@Getter
public enum RestTypeEnum {

	GET(0, "GET"),
	POST(1, "POST"),
	
	;

	private Integer key;
	private String desc;

	RestTypeEnum(Integer key, String desc) {
		this.key = key;
		this.desc = desc;
	}

	public static String getDesc(Integer key) {
		for (RestTypeEnum auth : RestTypeEnum.values()) {
			if (auth.getKey().equals(key)) {
				return auth.getDesc();
			}
		}
		return null;
	}

	public static RestTypeEnum getByKey(Integer key) {
		if (key == null) {
			return null;
		}
		for (RestTypeEnum type : values()) {
			if (type.getKey().equals(key)) {
				return type;
			}
		}
		return null;
	}

}
