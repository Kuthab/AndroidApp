package edu.rutgers.photos;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import edu.rutgers.photos.model.Album;
import edu.rutgers.photos.model.DataStore;

public class MainActivity extends AppCompatActivity implements AlbumAdapter.Listener {

    private DataStore store;
    private AlbumAdapter adapter;
    private RecyclerView rv;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        store = DataStore.get(this);

        rv = findViewById(R.id.rvAlbums);
        tvEmpty = findViewById(R.id.tvEmpty);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AlbumAdapter(this, this);
        rv.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabNew);
        fab.setOnClickListener(v -> showCreateDialog());

        ImageButton btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(v ->
                startActivity(new Intent(this, SearchActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        adapter.submit(store.getAlbums());
        tvEmpty.setVisibility(store.getAlbums().isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onOpen(Album album) {
        Intent i = new Intent(this, AlbumActivity.class);
        i.putExtra(AlbumActivity.EXTRA_ALBUM_ID, album.getId());
        startActivity(i);
    }

    @Override
    public void onMore(Album album, View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, R.string.rename_album);
        popup.getMenu().add(0, 2, 1, R.string.delete_album);
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) showRenameDialog(album);
            else if (item.getItemId() == 2) confirmDelete(album);
            return true;
        });
        popup.show();
    }

    private void showCreateDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_album_name, null);
        TextInputEditText et = view.findViewById(R.id.etName);
        new AlertDialog.Builder(this)
                .setTitle(R.string.new_album)
                .setView(view)
                .setPositiveButton(R.string.ok, (d, w) -> {
                    String name = et.getText() == null ? "" : et.getText().toString();
                    if (name.trim().isEmpty()) {
                        Toast.makeText(this, R.string.empty_name, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (store.findAlbumByName(name) != null) {
                        Toast.makeText(this, R.string.dup_album, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    store.createAlbum(name);
                    store.save(this);
                    refresh();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showRenameDialog(Album album) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_album_name, null);
        TextInputEditText et = view.findViewById(R.id.etName);
        et.setText(album.getName());
        et.setSelection(et.getText() == null ? 0 : et.getText().length());
        new AlertDialog.Builder(this)
                .setTitle(R.string.rename_album)
                .setView(view)
                .setPositiveButton(R.string.ok, (d, w) -> {
                    String name = et.getText() == null ? "" : et.getText().toString();
                    if (name.trim().isEmpty()) {
                        Toast.makeText(this, R.string.empty_name, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!store.renameAlbum(album, name)) {
                        Toast.makeText(this, R.string.dup_album, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    store.save(this);
                    refresh();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmDelete(Album album) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_album)
                .setMessage(getString(R.string.delete_album) + ": " + album.getName() + "?")
                .setPositiveButton(R.string.yes, (d, w) -> {
                    store.deleteAlbum(album);
                    store.save(this);
                    refresh();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
}
