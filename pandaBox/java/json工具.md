``` JAVA
package com.utry.common.util;

import com.fasterxml.jackson.databind.JavaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * json工具类
 * 
 */
public final class JsonUtils {
	private static final Logger LOG = LoggerFactory.getLogger(JsonUtils.class);

	private JsonUtils() {
	}
	public static final ObjectMapper OBJECTMAPPER = new ObjectMapper();

	static {
		OBJECTMAPPER.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        OBJECTMAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        OBJECTMAPPER.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        OBJECTMAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        OBJECTMAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        OBJECTMAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public static <T> T decode(String jsonStr, TypeReference<T> ref) {
		T obj = null;
		try {
			obj = OBJECTMAPPER.readValue(jsonStr, ref);
		} catch (Exception e) {
			LOG.error("Failed to parse json string.", e);
		}

		return obj;
	}

	public static <T> T decode(String jsonStr, Class<T> clazz) {
		T obj = null;
		try {
			obj = OBJECTMAPPER.readValue(jsonStr, clazz);
		} catch (Exception e) {
			LOG.error("Failed to parse json string.", e);
		}

		return obj;
	}

	public static String encode(Object obj) {
		String str = null;
		try {
			str = OBJECTMAPPER.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			LOG.error("Failed to generate json string.", e);
		}

		return str;
	}

	public static List<Map<String, Object>> decodeList(String jsonStr) {
		JavaType jt = OBJECTMAPPER.getTypeFactory().constructParametricType(List.class, Map.class);
		List<Map<String, Object>> list = null;
		try {
			list = OBJECTMAPPER.readValue(jsonStr, jt);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}

	public static Map<String, Object> decodeMap(String jsonStr) {
		JavaType jvt = OBJECTMAPPER.getTypeFactory().constructParametricType(Map.class,String.class,Object.class);
		Map<String, Object> map = null;
		try {
			map = OBJECTMAPPER.readValue(jsonStr, jvt);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}

}

```

