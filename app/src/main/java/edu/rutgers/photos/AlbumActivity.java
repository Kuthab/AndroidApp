package edu.rutgers.photos;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.photos.model.Album;
import edu.rutgers.photos.model.DataStore;
import edu.rutgers.photos.model.Photo;

public class AlbumActivity extends AppCompatActivity implements PhotoGridAdapter.Listener {

    public static final String EXTRA_ALBUM_ID = "album_id";

    private DataStore store;
    private Album album;
    private PhotoGridAdapter adapter;
    private TextView tvSubtitle;
    private TextView tvEmpty;

    private ActivityResultLauncher<String[]> picker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        store = DataStore.get(this);
        String id = getIntent().getStringExtra(EXTRA_ALBUM_ID);
        album = store.findAlbumById(id);
        if (album == null) {
            finish();
            return;
        }

        TextView tvName = findViewById(R.id.tvAlbumName);
        tvName.setText(album.getName());
        tvSubtitle = findViewById(R.id.tvAlbumSubtitle);
        tvEmpty = findViewById(R.id.tvEmpty);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.rvPhotos);
        int span = 3;
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int cell = dm.widthPixels / span;
        rv.setLayoutManager(new GridLayoutManager(this, span));
        adapter = new PhotoGridAdapter(this, this, cell);
        rv.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> picker.launch(new String[]{"image/*"}));

        picker = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                this::onImagePicked);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        adapter.submit(album.getPhotos());
        tvSubtitle.setText(getString(R.string.album_count, album.size()));
        tvEmpty.setVisibility(album.getPhotos().isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void onImagePicked(Uri uri) {
        if (uri == null) return;
        try {
            getContentResolver().takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException ignored) {}

        String displayName = queryDisplayName(uri);
        Photo photo = new Photo(uri.toString(), displayName);
        if (!album.addPhoto(photo)) {
            Toast.makeText(this, R.string.dup_photo, Toast.LENGTH_SHORT).show();
            return;
        }
        store.save(this);
        refresh();
    }

    private String queryDisplayName(Uri uri) {
        try (android.database.Cursor c = getContentResolver().query(
                uri, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int idx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) return c.getString(idx);
            }
        } catch (Exception ignored) {}
        return uri.getLastPathSegment();
    }

    @Override
    public void onClick(Photo photo, int position) {
        Intent i = new Intent(this, PhotoActivity.class);
        i.putExtra(PhotoActivity.EXTRA_ALBUM_ID, album.getId());
        i.putExtra(PhotoActivity.EXTRA_PHOTO_ID, photo.getId());
        startActivity(i);
    }

    @Override
    public boolean onLongClick(Photo photo, int position, View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, R.string.open_photo);
        popup.getMenu().add(0, 2, 1, R.string.move_photo);
        popup.getMenu().add(0, 3, 2, R.string.remove_photo);
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1: onClick(photo, position); break;
                case 2: showMoveDialog(photo); break;
                case 3: confirmRemove(photo); break;
            }
            return true;
        });
        popup.show();
        return true;
    }

    private void confirmRemove(Photo photo) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.remove_photo)
                .setMessage(R.string.remove_photo)
                .setPositiveButton(R.string.yes, (d, w) -> {
                    album.removePhoto(photo);
                    store.save(this);
                    refresh();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void showMoveDialog(Photo photo) {
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
                    if (!store.movePhoto(photo, album, dest)) {
                        Toast.makeText(this, R.string.dup_photo,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    store.save(this);
                    refresh();
                })
                .show();
    }
}
