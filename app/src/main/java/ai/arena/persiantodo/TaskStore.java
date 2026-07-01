package ai.arena.persiantodo;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TaskStore {
    private static final String PREF = "tasks_pref";
    private static final String KEY = "tasks";

    public static ArrayList<Task> load(Context c) {
        ArrayList<Task> list = new ArrayList<>();
        String raw = c.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY, "[]");
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) list.add(Task.fromJson(arr.getJSONObject(i)));
        } catch (Exception ignored) {}
        Collections.sort(list, Comparator.comparingLong(t -> t.dueAt));
        return list;
    }

    public static void save(Context c, ArrayList<Task> list) {
        JSONArray arr = new JSONArray();
        try { for (Task t : list) arr.put(t.toJson()); } catch (Exception ignored) {}
        SharedPreferences.Editor e = c.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit();
        e.putString(KEY, arr.toString()); e.apply();
    }

    public static Task find(Context c, long id) {
        for (Task t : load(c)) if (t.id == id) return t;
        return null;
    }
}
