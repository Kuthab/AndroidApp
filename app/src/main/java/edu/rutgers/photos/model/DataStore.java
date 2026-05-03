package edu.rutgers.photos.model;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

public final class DataStore implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String TAG = "DataStore";
    private static final String FILE_NAME = "photos.dat";

    private static volatile DataStore instance;

    private final List<Album> albums = new ArrayList<>();

    private DataStore() {}

    public static synchronized DataStore get(Context context) {
        if (instance == null) {
            instance = load(context);
        }
        return instance;
    }

    public List<Album> getAlbums() {
        sortAlbums();
        return Collections.unmodifiableList(albums);
    }

    public Album findAlbumById(String id) {
        for (Album a : albums) if (a.getId().equals(id)) return a;
        return null;
    }

    public Album findAlbumByName(String name) {
        if (name == null) return null;
        for (Album a : albums) if (a.nameEqualsIgnoreCase(name)) return a;
        return null;
    }

    public Album createAlbum(String name) {
        if (name == null) return null;
        String trimmed = name.trim();
        if (trimmed.isEmpty()) return null;
        if (findAlbumByName(trimmed) != null) return null;
        Album a = new Album(trimmed);
        albums.add(a);
        sortAlbums();
        return a;
    }

    public boolean renameAlbum(Album album, String newName) {
        if (album == null || newName == null) return false;
        String trimmed = newName.trim();
        if (trimmed.isEmpty()) return false;
        Album existing = findAlbumByName(trimmed);
        if (existing != null && existing != album) return false;
        album.setName(trimmed);
        sortAlbums();
        return true;
    }

    public boolean deleteAlbum(Album album) {
        boolean ok = albums.remove(album);
        return ok;
    }

    public boolean movePhoto(Photo photo, Album from, Album to) {
        if (photo == null || from == null || to == null || from == to) return false;
        if (to.getPhotos().contains(photo)) return false;
        if (!from.removePhoto(photo)) return false;
        to.addPhoto(photo);
        return true;
    }

    public List<Album> otherAlbums(Album exclude) {
        List<Album> out = new ArrayList<>();
        for (Album a : albums) if (a != exclude) out.add(a);
        sort(out);
        return out;
    }

    public Set<String> autocompleteValues(Tag.Type type, String prefix) {
        Set<String> out = new TreeSet<>(Comparator.naturalOrder());
        if (type == null) return out;
        String p = prefix == null ? "" : prefix.trim().toLowerCase(Locale.ROOT);
        Set<String> seen = new HashSet<>();
        for (Album a : albums) {
            for (Photo photo : a.getPhotos()) {
                for (Tag t : photo.getTags()) {
                    if (t.getType() != type) continue;
                    String v = t.getValue();
                    String key = v.toLowerCase(Locale.ROOT);
                    if (!seen.add(key)) continue;
                    if (p.isEmpty() || key.startsWith(p)) out.add(v);
                }
            }
        }
        return out;
    }

    public List<SearchHit> searchSingle(Tag.Type type, String prefix) {
        List<SearchHit> hits = new ArrayList<>();
        if (type == null) return hits;
        for (Album a : albums) {
            for (Photo p : a.getPhotos()) {
                if (p.hasMatchingTag(type, prefix)) hits.add(new SearchHit(a, p));
            }
        }
        return hits;
    }

    public List<SearchHit> searchAnd(Tag.Type t1, String v1, Tag.Type t2, String v2) {
        List<SearchHit> hits = new ArrayList<>();
        if (t1 == null || t2 == null) return hits;
        for (Album a : albums) {
            for (Photo p : a.getPhotos()) {
                if (p.hasMatchingTag(t1, v1) && p.hasMatchingTag(t2, v2)) {
                    hits.add(new SearchHit(a, p));
                }
            }
        }
        return hits;
    }

    public List<SearchHit> searchOr(Tag.Type t1, String v1, Tag.Type t2, String v2) {
        List<SearchHit> hits = new ArrayList<>();
        if (t1 == null || t2 == null) return hits;
        for (Album a : albums) {
            for (Photo p : a.getPhotos()) {
                if (p.hasMatchingTag(t1, v1) || p.hasMatchingTag(t2, v2)) {
                    hits.add(new SearchHit(a, p));
                }
            }
        }
        return hits;
    }

    public synchronized void save(Context context) {
        File f = new File(context.getFilesDir(), FILE_NAME);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
            oos.writeObject(albums);
        } catch (Exception e) {
            Log.e(TAG, "save failed", e);
        }
    }

    private static DataStore load(Context context) {
        DataStore d = new DataStore();
        File f = new File(context.getFilesDir(), FILE_NAME);
        if (!f.exists()) {
            seedSampleAlbums(d);
            d.save(context);
            return d;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            Object o = ois.readObject();
            if (o instanceof List) {
                @SuppressWarnings("unchecked")
                List<Album> read = (List<Album>) o;
                d.albums.addAll(read);
            }
        } catch (Exception e) {
            Log.e(TAG, "load failed; starting fresh", e);
        }
        return d;
    }

    private static void seedSampleAlbums(DataStore d) {
        d.albums.add(new Album("Stock"));
        d.albums.add(new Album("Travel"));
    }

    private void sortAlbums() { sort(albums); }

    private static void sort(List<Album> list) {
        Collections.sort(list, new Comparator<Album>() {
            @Override public int compare(Album a, Album b) {
                return a.getName().compareToIgnoreCase(b.getName());
            }
        });
    }

    public static final class SearchHit {
        public final Album album;
        public final Photo photo;
        public SearchHit(Album album, Photo photo) {
            this.album = album;
            this.photo = photo;
        }
    }
}
