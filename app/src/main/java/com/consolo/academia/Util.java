package com.consolo.academia;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

public class Util {

    public static final String DD_MM_YYYY = "dd/MM/yyyy";

    public static void setFirebaseInstanceId(Context context, String InstanceId) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor;
        editor = sharedPreferences.edit();
        editor.putString(context.getString(R.string.pref_firebase_instance_id_key),InstanceId);
        editor.apply();
    }

    public static String getFirebaseInstanceId(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String key = context.getString(R.string.pref_firebase_instance_id_key);
        String default_value = context.getString(R.string.pref_firebase_instance_id_default_key);
        return sharedPreferences.getString(key, default_value);
    }

    public static String dataParaString(Date date) {
        return dataParaString(date, DD_MM_YYYY);
    }

    @SuppressLint("SimpleDateFormat")
    public static String dataParaString(Date date, String format) {
        if (isNullOrEmpty(date) || isNullOrEmpty(format)) return "";
        return new SimpleDateFormat(format).format(date);
    }

    public static boolean isNullOrEmpty(Integer o) {
        return o == null || o == 0;
    }

    public static boolean isNullOrEmpty(String o) {
        return TextUtils.isEmpty(o);
    }

    public static boolean isNullOrEmpty(Collection o) {
        return o == null || o.isEmpty();
    }

    public static boolean isNullOrEmpty(Object o) {
        return o == null;
    }

    public static String formatarSegundos(Integer seconds) {
        String prefixo;
        String formato;
        DecimalFormat formatter = new DecimalFormat("00");
        int[] horario = recuperarHorasMinutosSegundos(seconds);
        if (seconds < 60) {
            formato = formatter.format(horario[2]);
            prefixo = " segundos";
        } else if (seconds >= 60 && seconds < 3600) {
            formato = formatter.format(horario[1]) + ":" + formatter.format(horario[2]);
            prefixo = " minutos";
        } else {
            formato = formatter.format(horario[0]) + ":" + formatter.format(horario[1]) + ":" + formatter.format(horario[2]);
            prefixo = " horas";
        }

        return formato + prefixo;
    }

    public static int[] recuperarHorasMinutosSegundos(int seconds) {
        int[] horario = new int[3];
        if (seconds < 60) {
            horario[0] = 0;
            horario[1] = 0;
            horario[2] = seconds;
        } else if (seconds < 3600) {
            int remainder = seconds % 3600;
            horario[0] = 0;
            horario[1] = remainder / 60;
            horario[2] = remainder % 60;
        } else {
            int remainder = seconds % 3600;
            horario[0] = seconds / 3600;
            horario[1] = remainder / 60;
            horario[2] = remainder % 60;
        }
        return horario;
    }
}
