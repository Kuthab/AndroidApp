package edu.rutgers.photos;

import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.photos.model.Album;
import edu.rutgers.photos.model.DataStore;
import edu.rutgers.photos.model.Photo;
import edu.rutgers.photos.model.Tag;
import edu.rutgers.photos.util.ImageLoader;

public class PhotoActivity extends AppCompatActivity {

    public static final String EXTRA_ALBUM_ID = "album_id";
    public static final String EXTRA_PHOTO_ID = "photo_id";

    private DataStore store;
    private Album album;
    private int index;

    private ImageView ivPhoto;
    private TextView tvCaption;
    private TextView tvIndex;
    private ChipGroup cgTags;
    private TextView tvNoTags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        store = DataStore.get(this);

        String albumId = getIntent().getStringExtra(EXTRA_ALBUM_ID);
        String photoId = getIntent().getStringExtra(EXTRA_PHOTO_ID);
        album = store.findAlbumById(albumId);
        if (album == null || album.getPhotos().isEmpty()) {
            finish();
            return;
        }

        Photo p = album.findById(photoId);
        index = p == null ? 0 : album.indexOf(p);
        if (index < 0) index = 0;

        ivPhoto = findViewById(R.id.ivPhoto);
        tvCaption = findViewById(R.id.tvCaption);
        tvIndex = findViewById(R.id.tvIndex);
        cgTags = findViewById(R.id.cgTags);
        tvNoTags = findViewById(R.id.tvNoTags);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        ImageButton btnPrev = findViewById(R.id.btnPrev);
        ImageButton btnNext = findViewById(R.id.btnNext);
        btnPrev.setOnClickListener(v -> step(-1));
        btnNext.setOnClickListener(v -> step(+1));

        MaterialButton btnAddTag = findViewById(R.id.btnAddTag);
        btnAddTag.setOnClickListener(v -> showAddTagDialog());

        MaterialButton btnMove = findViewById(R.id.btnMove);
        btnMove.setOnClickListener(v -> showMoveDialog());

        MaterialButton btnRemove = findViewById(R.id.btnRemove);
        btnRemove.setOnClickListener(v -> confirmRemove());

        bind();
    }

    private void step(int delta) {
        if (album.getPhotos().isEmpty()) return;
        int n = album.size();
        index = ((index + delta) % n + n) % n;
        bind();
    }

    private Photo current() {
        if (album.getPhotos().isEmpty()) return null;
        if (index < 0 || index >= album.size()) index = 0;
        return album.getPhotos().get(index);
    }

    private void bind() {
        Photo p = current();
        if (p == null) {
            finish();
            return;
        }
        tvCaption.setText(p.getDisplayName());
        tvIndex.setText((index + 1) + " / " + album.size());

        DisplayMetrics dm = getResources().getDisplayMetrics();
        ImageLoader.loadInto(ivPhoto, p.getUri(),
                Math.min(dm.widthPixels, 1600),
                Math.min(dm.heightPixels, 1600),
                R.drawable.ic_image_placeholder);

        renderTags(p);
    }

    private void renderTags(Photo p) {
        cgTags.removeAllViews();
        List<Tag> tags = new ArrayList<>(p.getTags());
        if (tags.isEmpty()) {
            tvNoTags.setVisibility(View.VISIBLE);
            return;
        }
        tvNoTags.setVisibility(View.GONE);

        for (Tag t : tags) {
            Chip chip = new Chip(this);
            chip.setText(t.toString());
            chip.setCloseIconVisible(true);
            chip.setCloseIconResource(R.drawable.ic_close);
            int bgColor = t.getType() == Tag.Type.PERSON
                    ? R.color.chip_person_bg : R.color.chip_location_bg;
            int fgColor = t.getType() == Tag.Type.PERSON
                    ? R.color.chip_person_fg : R.color.chip_location_fg;
            chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(this, bgColor)));
            chip.setTextColor(ContextCompat.getColor(this, fgColor));
            chip.setChipStrokeWidth(0f);
            chip.setOnCloseIconClickListener(v -> confirmDeleteTag(p, t));
            cgTags.addView(chip);
        }
    }

    private void showAddTagDialog() {
        Photo p = current();
        if (p == null) return;
        Context ctx = this;
        View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_add_tag, null);
        RadioGroup rg = view.findViewById(R.id.rgType);
        TextInputEditText et = view.findViewById(R.id.etValue);

        new AlertDialog.Builder(ctx)
                .setTitle(R.string.add_tag)
                .setView(view)
                .setPositiveButton(R.string.ok, (d, w) -> {
                    String value = et.getText() == null ? "" : et.getText().toString();
                    if (value.trim().isEmpty()) {
                        Toast.makeText(ctx, R.string.empty_name,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Tag.Type type = rg.getCheckedRadioButtonId() == R.id.rbLocation
                            ? Tag.Type.LOCATION : Tag.Type.PERSON;
                    Tag tag = new Tag(type, value);
                    if (!p.addTag(tag)) {
                        Toast.makeText(ctx, R.string.dup_tag,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    store.save(ctx);
                    renderTags(p);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmDeleteTag(Photo p, Tag tag) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_tag)
                .setMessage(tag.toString())
                .setPositiveButton(R.string.yes, (d, w) -> {
                    p.removeTag(tag);
                    store.save(this);
                    renderTags(p);
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void showMoveDialog() {
        Photo p = current();
        if (p == null) return;
        List<Album> others = store.otherAlbums(album);
        if (others.isEmpty()) {
            Toast.makeText(this, R.string.no_other_album, Toast.LENGTH_SHORT).show();
            return;
        }
        List<String> names = new ArrayList<>();
        for (Album a : others) names.add(a.getName());
        new AlertDialog.Builder(this)
                .setTitle(R.string.pick_album)
                .setItems(names.toArray(new String[0]), (d, which) -> {
                    Album dest = others.get(which);
                    if (!store.movePhoto(p, album, dest)) {
                        Toast.makeText(this, R.string.dup_photo,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    store.save(this);
                    if (album.getPhotos().isEmpty()) {
                        finish();
                        return;
                    }
                    if (index >= album.size()) index = album.size() - 1;
                    bind();
                })
                .show();
    }

    private void confirmRemove() {
        Photo p = current();
        if (p == null) return;
        new AlertDialog.Builder(this)
                .setTitle(R.string.remove_photo)
                .setMessage(p.getDisplayName())
                .setPositiveButton(R.string.yes, (d, w) -> {
                    album.removePhoto(p);
                    store.save(this);
                    if (album.getPhotos().isEmpty()) {
                        finish();
                        return;
                    }
                    if (index >= album.size()) index = album.size() - 1;
                    bind();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
}
