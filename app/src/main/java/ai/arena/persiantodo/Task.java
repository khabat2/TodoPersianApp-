package ai.arena.persiantodo;

import org.json.JSONException;
import org.json.JSONObject;

public class Task {
    public long id;
    public String title;
    public String note;
    public long dueAt;
    public String imageUri;
    public String audioUri;
    public boolean done;

    public Task(long id, String title, String note, long dueAt, String imageUri, String audioUri, boolean done) {
        this.id = id; this.title = title; this.note = note; this.dueAt = dueAt;
        this.imageUri = imageUri; this.audioUri = audioUri; this.done = done;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", id); o.put("title", title); o.put("note", note); o.put("dueAt", dueAt);
        o.put("imageUri", imageUri == null ? "" : imageUri);
        o.put("audioUri", audioUri == null ? "" : audioUri);
        o.put("done", done);
        return o;
    }

    public static Task fromJson(JSONObject o) {
        return new Task(
                o.optLong("id"), o.optString("title"), o.optString("note"), o.optLong("dueAt"),
                o.optString("imageUri", ""), o.optString("audioUri", ""), o.optBoolean("done", false)
        );
    }
}
