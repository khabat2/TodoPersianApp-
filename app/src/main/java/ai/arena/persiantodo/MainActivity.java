package ai.arena.persiantodo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends android.app.Activity {
    private static final int PICK_IMAGE = 10;
    private static final int PICK_AUDIO = 11;
    private ArrayList<Task> tasks;
    private LinearLayout list;
    private String dialogImageUri = "";
    private String dialogAudioUri = "";
    private TextView imageLabel, audioLabel;
    private Task editingTask = null;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(16,24,39));
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 5);
        tasks = TaskStore.load(this);
        draw();
    }

    private void draw() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        root.setBackgroundColor(Color.rgb(245,247,251));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(22), dp(24), dp(22), dp(20));
        GradientDrawable hg = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{Color.rgb(16,24,39), Color.rgb(0,191,166)});
        hg.setCornerRadii(new float[]{0,0,0,0,0,0,dp(34),dp(34)});
        header.setBackground(hg);
        TextView title = txt("کارهای روزمره من", 28, Color.WHITE, true);
        TextView sub = txt("مدیریت فارسی کارها، تاریخ شمسی، تصویر، صدا و آلارم", 14, 0xffe6fffb, false);
        header.addView(title); header.addView(sub);
        root.addView(header, new LinearLayout.LayoutParams(-1, -2));

        Button add = button("＋ افزودن کار جدید", Color.rgb(0,191,166), Color.WHITE);
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(-1, dp(54));
        ap.setMargins(dp(18), dp(16), dp(18), dp(8));
        root.addView(add, ap);
        add.setOnClickListener(v -> showTaskDialog(null));

        ScrollView sv = new ScrollView(this);
        list = new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); list.setPadding(dp(14), dp(4), dp(14), dp(22));
        sv.addView(list);
        root.addView(sv, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
        refreshList();
    }

    private void refreshList() {
        list.removeAllViews();
        if (tasks.isEmpty()) {
            TextView empty = txt("هنوز کاری ثبت نشده است. روی دکمه افزودن بزنید.", 16, 0xff6b7280, false);
            empty.setGravity(Gravity.CENTER); empty.setPadding(dp(20), dp(50), dp(20), dp(20));
            list.addView(empty, new LinearLayout.LayoutParams(-1, -2));
            return;
        }
        for (Task t : tasks) addTaskCard(t);
    }

    private void addTaskCard(Task t) {
        LinearLayout card = new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setPadding(dp(16), dp(14), dp(16), dp(12));
        GradientDrawable bg = new GradientDrawable(); bg.setColor(Color.WHITE); bg.setCornerRadius(dp(22)); bg.setStroke(dp(1), 0xffe5e7eb); card.setBackground(bg); card.setElevation(dp(3));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2); cp.setMargins(0, dp(8), 0, dp(10));
        list.addView(card, cp);

        LinearLayout row = new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL); row.setOrientation(LinearLayout.HORIZONTAL);
        CheckBox cb = new CheckBox(this); cb.setChecked(t.done); row.addView(cb);
        TextView ttl = txt(t.title, 19, t.done ? 0xff9ca3af : 0xff111827, true); row.addView(ttl, new LinearLayout.LayoutParams(0, -2, 1));
        card.addView(row);
        if (t.note != null && !t.note.isEmpty()) card.addView(txt(t.note, 14, 0xff4b5563, false));
        PersianDate pd = PersianDate.fromMillis(t.dueAt);
        card.addView(txt("⏰ " + pd.format(), 14, 0xff00a693, true));

        LinearLayout media = new LinearLayout(this); media.setOrientation(LinearLayout.HORIZONTAL); media.setGravity(Gravity.CENTER_VERTICAL);
        if (t.imageUri != null && !t.imageUri.isEmpty()) {
            ImageView img = new ImageView(this); img.setImageURI(Uri.parse(t.imageUri)); img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            GradientDrawable ib = new GradientDrawable(); ib.setCornerRadius(dp(14)); img.setBackground(ib); img.setClipToOutline(true);
            media.addView(img, new LinearLayout.LayoutParams(dp(70), dp(70)));
        }
        if (t.audioUri != null && !t.audioUri.isEmpty()) media.addView(txt("   🔊 صدای اختصاصی دارد", 13, 0xff6b7280, false));
        if (media.getChildCount() > 0) card.addView(media);

        LinearLayout actions = new LinearLayout(this); actions.setOrientation(LinearLayout.HORIZONTAL); actions.setGravity(Gravity.LEFT);
        Button edit = smallButton("ویرایش", 0xffeef2ff, 0xff4338ca);
        Button del = smallButton("حذف", 0xffffeeee, 0xffdc2626);
        actions.addView(edit); actions.addView(del); card.addView(actions);

        cb.setOnClickListener(v -> { t.done = cb.isChecked(); if (t.done) AlarmScheduler.cancel(this, t.id); else AlarmScheduler.schedule(this, t); saveAndRefresh(); });
        edit.setOnClickListener(v -> showTaskDialog(t));
        del.setOnClickListener(v -> new AlertDialog.Builder(this).setTitle("حذف کار").setMessage("این کار حذف شود؟")
                .setPositiveButton("بله", (d,w)->{ AlarmScheduler.cancel(this, t.id); tasks.remove(t); saveAndRefresh(); })
                .setNegativeButton("خیر", null).show());
    }

    private void showTaskDialog(Task edit) {
        editingTask = edit;
        dialogImageUri = edit == null ? "" : edit.imageUri;
        dialogAudioUri = edit == null ? "" : edit.audioUri;
        PersianDate initial = edit == null ? PersianDate.fromMillis(System.currentTimeMillis() + 3600_000) : PersianDate.fromMillis(edit.dueAt);

        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(18), dp(8), dp(18), 0); box.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        EditText title = input("عنوان کار"); if (edit != null) title.setText(edit.title); box.addView(title);
        EditText note = input("توضیحات اختیاری"); note.setMinLines(2); if (edit != null) note.setText(edit.note); box.addView(note);

        TextView dateTitle = txt("تاریخ و ساعت یادآور شمسی", 15, 0xff111827, true); dateTitle.setPadding(0, dp(12), 0, dp(4)); box.addView(dateTitle);
        LinearLayout pickers = new LinearLayout(this); pickers.setOrientation(LinearLayout.HORIZONTAL); pickers.setGravity(Gravity.CENTER);
        NumberPicker year = picker(initial.year - 1, initial.year + 5, initial.year);
        NumberPicker month = picker(1, 12, initial.month);
        NumberPicker day = picker(1, 31, initial.day);
        NumberPicker hour = picker(0, 23, initial.hour);
        NumberPicker min = picker(0, 59, initial.minute);
        pickers.addView(year); pickers.addView(month); pickers.addView(day); pickers.addView(hour); pickers.addView(min);
        box.addView(pickers);
        TextView hint = txt("از راست به چپ: سال، ماه، روز، ساعت، دقیقه", 12, 0xff6b7280, false); box.addView(hint);

        Button imgBtn = button("انتخاب تصویر", 0xffeefcf9, 0xff008f7d);
        Button audBtn = button("انتخاب صدای آلارم", 0xffeef2ff, 0xff4338ca);
        imageLabel = txt(dialogImageUri.isEmpty() ? "تصویری انتخاب نشده" : "تصویر انتخاب شده است", 12, 0xff6b7280, false);
        audioLabel = txt(dialogAudioUri.isEmpty() ? "صدایی انتخاب نشده" : "صدا انتخاب شده است", 12, 0xff6b7280, false);
        box.addView(imgBtn); box.addView(imageLabel); box.addView(audBtn); box.addView(audioLabel);
        imgBtn.setOnClickListener(v -> pick("image/*", PICK_IMAGE));
        audBtn.setOnClickListener(v -> pick("audio/*", PICK_AUDIO));

        AlertDialog dlg = new AlertDialog.Builder(this)
                .setTitle(edit == null ? "افزودن کار" : "ویرایش کار")
                .setView(box)
                .setPositiveButton("ذخیره", null)
                .setNegativeButton("انصراف", null)
                .create();
        dlg.setOnShowListener(di -> dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String ttl = title.getText().toString().trim();
            if (ttl.isEmpty()) { title.setError("عنوان را وارد کنید"); return; }
            int dmax = maxDay(month.getValue()); if (day.getValue() > dmax) day.setValue(dmax);
            PersianDate pd = new PersianDate(year.getValue(), month.getValue(), day.getValue(), hour.getValue(), min.getValue());
            long due = pd.toMillis();
            if (edit == null) tasks.add(new Task(System.currentTimeMillis() % Integer.MAX_VALUE, ttl, note.getText().toString().trim(), due, dialogImageUri, dialogAudioUri, false));
            else { edit.title = ttl; edit.note = note.getText().toString().trim(); edit.dueAt = due; edit.imageUri = dialogImageUri; edit.audioUri = dialogAudioUri; edit.done = false; AlarmScheduler.cancel(this, edit.id); }
            saveAndRefresh(); dlg.dismiss();
        }));
        dlg.show();
    }

    private void saveAndRefresh() {
        TaskStore.save(this, tasks);
        tasks = TaskStore.load(this);
        for (Task t : tasks) AlarmScheduler.schedule(this, t);
        refreshList();
    }

    private void pick(String type, int code) {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE); i.setType(type);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(i, code);
    }

    @Override protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (res == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try { getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); } catch (Exception ignored) {}
            if (req == PICK_IMAGE) { dialogImageUri = uri.toString(); if (imageLabel != null) imageLabel.setText("تصویر انتخاب شد"); }
            if (req == PICK_AUDIO) { dialogAudioUri = uri.toString(); if (audioLabel != null) audioLabel.setText("صدای آلارم انتخاب شد"); }
        }
    }

    private EditText input(String hint) { EditText e = new EditText(this); e.setHint(hint); e.setTextDirection(View.TEXT_DIRECTION_RTL); e.setSingleLine(false); return e; }
    private NumberPicker picker(int min, int max, int val) { NumberPicker p = new NumberPicker(this); p.setMinValue(min); p.setMaxValue(max); p.setValue(val); p.setWrapSelectorWheel(false); return p; }
    private int maxDay(int m) { return m <= 6 ? 31 : (m <= 11 ? 30 : 29); }

    private TextView txt(String s, int sp, int color, boolean bold) { TextView t = new TextView(this); t.setText(PersianDate.digits(s)); t.setTextSize(sp); t.setTextColor(color); t.setGravity(Gravity.RIGHT); if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD); t.setPadding(0, dp(3), 0, dp(3)); return t; }
    private Button button(String s, int bg, int fg) { Button b = new Button(this); b.setText(PersianDate.digits(s)); b.setTextColor(fg); b.setTextSize(15); b.setTypeface(Typeface.DEFAULT, Typeface.BOLD); GradientDrawable g = new GradientDrawable(); g.setColor(bg); g.setCornerRadius(dp(18)); b.setBackground(g); return b; }
    private Button smallButton(String s, int bg, int fg) { Button b = button(s, bg, fg); LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(96), dp(42)); lp.setMargins(dp(6), dp(10), 0, 0); b.setLayoutParams(lp); return b; }
    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density + 0.5f); }
}
