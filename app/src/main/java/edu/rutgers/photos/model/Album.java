package edu.rutgers.photos.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class Album implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private String name;
    private final List<Photo> photos;

    public Album(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name == null ? "" : name.trim();
        this.photos = new ArrayList<>();
    }

    public String getId() { return id; }

    public String getName() { return name; }

    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
    }

    public List<Photo> getPhotos() { return Collections.unmodifiableList(photos); }

    public int size() { return photos.size(); }

    public Photo first() { return photos.isEmpty() ? null : photos.get(0); }

    public boolean addPhoto(Photo photo) {
        if (photo == null) return false;
        if (photos.contains(photo)) return false;
        photos.add(photo);
        return true;
    }

    public boolean removePhoto(Photo photo) {
        return photos.remove(photo);
    }

    public Photo findById(String photoId) {
        for (Photo p : photos) {
            if (p.getId().equals(photoId)) return p;
        }
        return null;
    }

    public int indexOf(Photo p) {
        return photos.indexOf(p);
    }

    public boolean nameEqualsIgnoreCase(String other) {
        return other != null && name.toLowerCase(Locale.ROOT)
                .equals(other.trim().toLowerCase(Locale.ROOT));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Album)) return false;
        Album a = (Album) o;
        return Objects.equals(id, a.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
