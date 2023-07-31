package br.com.aspenmc.utils.json;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.utils.FileUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;
import org.bson.BsonInvalidOperationException;
import org.bson.Document;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class JsonUtils {

	public static JsonObject jsonTree(Object src) {
		return CommonConst.GSON.toJsonTree(src).getAsJsonObject();
	}

	public static Object elementToBson(JsonElement element) {
		if (element.isJsonPrimitive()) {
			JsonPrimitive primitive = element.getAsJsonPrimitive();
			if (primitive.isString()) {
				return primitive.getAsString();
			} else if (primitive.isNumber()) {
				return primitive.getAsNumber();
			} else if (primitive.isBoolean()) {
				return primitive.getAsBoolean();
			}
		} else if (element.isJsonArray()) {
			return CommonConst.GSON.fromJson(element, List.class);
		}

		try {
			return Document.parse(CommonConst.GSON.toJson(element));
		} catch (BsonInvalidOperationException ex) {
			return JsonParser.parseString(CommonConst.GSON.toJson(element));
		}
	}

	public static String elementToString(JsonElement element) {
		if (element.isJsonPrimitive()) {
			JsonPrimitive primitive = element.getAsJsonPrimitive();

			if (primitive.isString()) {
				return primitive.getAsString();
			}
		}
		return CommonConst.GSON.toJson(element);
	}

	public static <T> T mapToObject(Map<String, String> map, Class<T> clazz) {
		JsonObject obj = new JsonObject();

		for (Entry<String, String> entry : map.entrySet()) {
			try {
				obj.add(entry.getKey(), JsonParser.parseString(entry.getValue()));
			} catch (Exception e) {
				obj.addProperty(entry.getKey(), entry.getValue());
			}
		}

		return CommonConst.GSON.fromJson(obj, clazz);
	}

	public static Map<String, String> objectToMap(Object src) {
		Map<String, String> map = new HashMap<>();
		JsonObject obj =
				src instanceof JsonObject ? (JsonObject) src : CommonConst.GSON.toJsonTree(src).getAsJsonObject();

		for (Entry<String, JsonElement> entry : obj.entrySet()) {
			map.put(entry.getKey(), CommonConst.GSON.toJson(entry.getValue()));
		}

		return map;
	}

	public static JsonElement fileToJson(File file) {
		JsonObject jsonObject = null;

		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);

			JsonReader jsonReader = new JsonReader(inputStreamReader);
			jsonObject = JsonParser.parseReader(jsonReader).getAsJsonObject();

			jsonReader.close();
			inputStreamReader.close();
			fileInputStream.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return jsonObject;
	}

	public static void saveJsonAsFile(String json, String fileName, String directory) {
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(FileUtils.createFile(fileName, directory, true));
			OutputStreamWriter outputStreamReader = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);

			BufferedWriter bufferedWriter = new BufferedWriter(outputStreamReader);

			bufferedWriter.write(json);

			bufferedWriter.flush();
			bufferedWriter.close();
			fileOutputStream.close();
			outputStreamReader.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
