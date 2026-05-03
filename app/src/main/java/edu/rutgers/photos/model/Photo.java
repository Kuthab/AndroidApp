package edu.rutgers.photos.model;

import android.net.Uri;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Photo implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private String uriString;
    private String displayName;
    private final List<Tag> tags;

    public Photo(String uriString, String displayName) {
        this.id = UUID.randomUUID().toString();
        this.uriString = uriString;
        this.displayName = (displayName == null || displayName.isEmpty())
                ? deriveName(uriString) : displayName;
        this.tags = new ArrayList<>();
    }

    private static String deriveName(String uriString) {
        if (uriString == null) return "Photo";
        int slash = uriString.lastIndexOf('/');
        String tail = slash >= 0 ? uriString.substring(slash + 1) : uriString;
        int q = tail.indexOf('?');
        if (q >= 0) tail = tail.substring(0, q);
        return tail.isEmpty() ? "Photo" : tail;
    }

    public String getId() { return id; }

    public String getUriString() { return uriString; }

    public Uri getUri() { return Uri.parse(uriString); }

    public String getDisplayName() { return displayName; }

    public void setDisplayName(String displayName) {
        if (displayName != null && !displayName.trim().isEmpty()) {
            this.displayName = displayName.trim();
        }
    }

    public List<Tag> getTags() { return Collections.unmodifiableList(tags); }

    public boolean addTag(Tag tag) {
        if (tag == null) return false;
        if (tags.contains(tag)) return false;
        tags.add(tag);
        return true;
    }

    public boolean removeTag(Tag tag) {
        return tags.remove(tag);
    }

    public boolean hasMatchingTag(Tag.Type type, String startingSubstring) {
        if (type == null || startingSubstring == null) return false;
        for (Tag t : tags) {
            if (t.getType() == type && t.matchesValue(startingSubstring)) return true;
        }
        return false;
    }

    public Photo copyForAlbum() {
        Photo c = new Photo(uriString, displayName);
        c.tags.addAll(tags);
        return c;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Photo)) return false;
        Photo p = (Photo) o;
        return Objects.equals(uriString, p.uriString);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uriString);
    }
}
