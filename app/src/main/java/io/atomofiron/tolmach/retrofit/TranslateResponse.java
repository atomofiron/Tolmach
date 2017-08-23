package io.atomofiron.tolmach.retrofit;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/** Для чтения данных, полученных от API Яндекс.Переводчика. */
public class TranslateResponse {

	@SerializedName("code")
	@Expose
	private Integer code;
	@SerializedName("lang")
	@Expose
	private String lang;
	@SerializedName("text")
	@Expose
	private List<String> text = null;

	public Integer getCode() {
		return code;
	}

	public String getLang() {
		return lang;
	}

	public List<String> getText() {
		return text;
	}

}