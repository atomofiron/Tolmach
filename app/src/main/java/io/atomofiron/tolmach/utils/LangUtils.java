package io.atomofiron.tolmach.utils;

import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import java.util.ArrayList;
import java.util.Locale;

public class LangUtils {

	public static String[] getArrayString(ArrayList<Lang> langs) {
		String[] list = new String[langs.size()];
		for (int i = 0; i < list.length; i++) {
			Lang lang = langs.get(i);
			list[i] = lang.name + (lang.country.isEmpty() ? "" : "-"+lang.country);
		}

		return list;
	}

	public static Lang getSrcLang(Resources resources, ArrayList<Lang> srcLangs, String def) {
		if (srcLangs == null || srcLangs.size() == 0)
			return null;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			LocaleList localeList = resources.getConfiguration().getLocales();
			for (int i = 0; i < localeList.size(); i++)
				for (Lang lang : srcLangs)
					if (lang.code.equals(localeList.get(i).getLanguage()))
						return lang;
		} else
			for (Lang lang : srcLangs)
				if (lang.code.equals(def))
					return lang;

		return srcLangs.get(0);
	}

	public static ArrayList<Lang> getSrcLangs(String[] srcCodes) {
		ArrayList<Lang> langs = new ArrayList<>();

		if (srcCodes == null || srcCodes.length == 0)
			return langs;

		for (String code : srcCodes)
			langs.add(new Lang(code, code));

		for (Locale locale : Locale.getAvailableLocales())
			for (Lang lang : langs)
				if (lang.code.equals(locale.getLanguage()))
					lang.name = locale.getDisplayLanguage();

		return langs;
	}

	public static ArrayList<Lang> getSrcLangs(ArrayList<CharSequence> srcCodes) {
		ArrayList<Lang> langs = new ArrayList<>();

		if (srcCodes == null || srcCodes.size() == 0)
			return langs;

		for (CharSequence code : srcCodes) {
			String[] parts = code.toString().split("-");
			if (parts.length >= 2)
				langs.add(new Lang(parts[0], parts[0], parts[parts.length - 1]));
		}

		for (Locale locale : Locale.getAvailableLocales())
			for (Lang lang : langs)
				if (lang.code.equals(locale.getLanguage()))
					lang.name = locale.getDisplayLanguage();

		return langs;
	}
}
