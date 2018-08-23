package rpc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class RpcHelper {
	public static void writeJsonArray(HttpServletResponse response, JSONArray array) {
		PrintWriter out = null;
		try {
			out = response.getWriter();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		response.setContentType("application/json");
		response.addHeader("Access-Control-Allow-Origin", "*");
		out.print(array);
		out.close();
	}

	public static void writeJsonObject(HttpServletResponse response, JSONObject obj) {
		PrintWriter out = null;
		try {
			out = response.getWriter();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		response.setContentType("application/json");
		response.addHeader("Access-Control-Allow-Origin", "*");
		out.print(obj);
		out.close();
	}

	// Parses a JSONObject from http request.
	public static JSONObject readJSONObject(HttpServletRequest request) {
		StringBuilder sBuilder = new StringBuilder();
		try (BufferedReader reader = request.getReader()) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				sBuilder.append(line);
			}
			return new JSONObject(sBuilder.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}

		return new JSONObject();
	}

}