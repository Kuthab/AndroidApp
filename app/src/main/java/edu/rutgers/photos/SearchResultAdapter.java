package edu.rutgers.photos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.photos.model.DataStore;
import edu.rutgers.photos.util.ImageLoader;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.VH> {

    public interface Listener {
        void onClick(DataStore.SearchHit hit);
    }

    private final Context context;
    private final Listener listener;
    private final List<DataStore.SearchHit> data = new ArrayList<>();

    public SearchResultAdapter(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void submit(List<DataStore.SearchHit> items) {
        data.clear();
        data.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_result, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        DataStore.SearchHit hit = data.get(position);
        h.tvName.setText(hit.photo.getDisplayName());
        h.tvAlbum.setText(context.getString(R.string.from_album, hit.album.getName()));
        ImageLoader.loadInto(h.ivThumb, hit.photo.getUri(),
                192, 192, R.drawable.ic_image_placeholder);
        h.itemView.setOnClickListener(v -> listener.onClick(hit));
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView ivThumb;
        final TextView tvName;
        final TextView tvAlbum;
        VH(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.ivThumb);
            tvName = itemView.findViewById(R.id.tvName);
            tvAlbum = itemView.findViewById(R.id.tvAlbum);
        }
    }
}
