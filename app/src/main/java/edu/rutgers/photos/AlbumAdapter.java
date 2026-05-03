package edu.rutgers.photos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.photos.model.Album;
import edu.rutgers.photos.model.Photo;
import edu.rutgers.photos.util.ImageLoader;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.VH> {

    public interface Listener {
        void onOpen(Album album);
        void onMore(Album album, View anchor);
    }

    private final Context context;
    private final Listener listener;
    private final List<Album> data = new ArrayList<>();

    public AlbumAdapter(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void submit(List<Album> items) {
        data.clear();
        data.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_album, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Album a = data.get(position);
        h.tvName.setText(a.getName());
        h.tvCount.setText(context.getString(R.string.album_count, a.size()));

        Photo cover = a.first();
        h.ivCover.setImageResource(R.drawable.ic_image_placeholder);
        if (cover != null) {
            ImageLoader.loadInto(h.ivCover, cover.getUri(),
                    192, 192, R.drawable.ic_image_placeholder);
        }

        h.itemView.setOnClickListener(v -> listener.onOpen(a));
        h.btnMore.setOnClickListener(v -> listener.onMore(a, v));
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView ivCover;
        final TextView tvName;
        final TextView tvCount;
        final ImageButton btnMore;
        VH(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivCover);
            tvName = itemView.findViewById(R.id.tvName);
            tvCount = itemView.findViewById(R.id.tvCount);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }
}
