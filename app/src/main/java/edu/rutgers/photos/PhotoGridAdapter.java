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

import edu.rutgers.photos.model.Photo;
import edu.rutgers.photos.util.ImageLoader;

public class PhotoGridAdapter extends RecyclerView.Adapter<PhotoGridAdapter.VH> {

    public interface Listener {
        void onClick(Photo photo, int position);
        boolean onLongClick(Photo photo, int position, View anchor);
    }

    private final Context context;
    private final Listener listener;
    private final List<Photo> data = new ArrayList<>();
    private final int cellSizePx;

    public PhotoGridAdapter(Context context, Listener listener, int cellSizePx) {
        this.context = context;
        this.listener = listener;
        this.cellSizePx = cellSizePx;
    }

    public void submit(List<Photo> items) {
        data.clear();
        data.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Photo p = data.get(position);
        h.tvCaption.setText(p.getDisplayName());
        ImageLoader.loadInto(h.ivPhoto, p.getUri(),
                cellSizePx, cellSizePx, R.drawable.ic_image_placeholder);
        h.itemView.setOnClickListener(v -> listener.onClick(p, h.getBindingAdapterPosition()));
        h.itemView.setOnLongClickListener(v ->
                listener.onLongClick(p, h.getBindingAdapterPosition(), v));
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView ivPhoto;
        final TextView tvCaption;
        VH(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            tvCaption = itemView.findViewById(R.id.tvCaption);
        }
    }
}
