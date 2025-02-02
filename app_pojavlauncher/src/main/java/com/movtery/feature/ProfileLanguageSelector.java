package com.movtery.feature;

import com.movtery.ui.subassembly.customprofilepath.ProfilePathHome;
import com.movtery.utils.ZHTools;
import com.movtery.utils.stringutils.StringUtils;

import net.kdt.pojavlaunch.JMinecraftVersionList;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.utils.MCOptionUtils;
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile;

import java.io.IOException;
import java.util.regex.Pattern;

public class ProfileLanguageSelector {

    private static String getOlderLanguage(String lang) {
        StringBuilder builder = new StringBuilder(lang);
        int underscoreIndex = lang.indexOf('_');

        if (underscoreIndex != -1) {
            for (int i = underscoreIndex; i < lang.length(); i++) {
                builder.setCharAt(i, Character.toUpperCase(lang.charAt(i)));
            }
        }

        return builder.toString();
    }

    private static int getVersion(String versionId) throws NumberFormatException {
        int firstDotIndex = versionId.indexOf('.');
        int secondDotIndex = versionId.indexOf('.', firstDotIndex + 1);

        int version;
        if (firstDotIndex != -1) {
            if (secondDotIndex == -1) {
                version = Integer.parseInt(versionId.substring(firstDotIndex + 1));
            } else {
                version = Integer.parseInt(versionId.substring(firstDotIndex + 1, secondDotIndex));
            }
        } else {
            version = 12;
        }
        return version;
    }

    private static String getLanguage(String versionName, String rawLang) {
        if (versionName == null || rawLang == null) return null;
        String lang = rawLang;
        if ("system".equals(rawLang)) {
            lang = ZHTools.getSystemLanguage();
        }

        JMinecraftVersionList.Version version;
        try {
            version = Tools.GLOBAL_GSON.fromJson(
                    Tools.read(ProfilePathHome.getVersionsHome() + "/" + versionName + "/" + versionName + ".json"),
                    JMinecraftVersionList.Version.class
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String versionId = version.id;
        String regex = "^\\d+[a-zA-Z]\\d+[a-zA-Z]$";
        Pattern pattern = Pattern.compile(regex);
        boolean matches = pattern.matcher(versionId).matches();

        if (StringUtils.containsDot(versionId)) {
            try {
                int ver = getVersion(versionId);

                // 1.10 -
                if (ver < 11) {
                    return getOlderLanguage(lang);
                }

                return lang;
            } catch (NumberFormatException e) {
                return lang;
            }
        } else if (matches) { // Snapshot versions "24w09a" "16w20a"
            try {
                int[] result = StringUtils.extractNumbers(versionId, 2);

                if (result[0] < 16) {
                    return getOlderLanguage(lang);
                } else if (result[0] == 16 && result[1] <= 32) {
                    return getOlderLanguage(lang);
                }

                return lang;
            } catch (NumberFormatException e) {
                return lang;
            }
        }

        return lang;
    }

    public static void setGameLanguage(MinecraftProfile minecraftProfile, boolean overridden) {
        if (MCOptionUtils.containsKey("lang")) {
            if (!overridden) return;
        }
        String language = getLanguage(minecraftProfile.lastVersionId, LauncherPreferences.PREF_GAME_LANGUAGE);
        MCOptionUtils.set("lang", language);
    }
}