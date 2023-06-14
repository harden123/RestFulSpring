package restfulspring.dto;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.Maps;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RestParamDTO {
	Map<String,Object> getParamKVMap = Maps.newHashMap();
//	String requestBody = null;
	AtomicReference<String> bodyStr = new AtomicReference<>();
}
