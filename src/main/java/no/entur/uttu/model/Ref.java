package no.entur.uttu.model;

public class Ref {

    public String id;

    public String version;

    public Ref(String id, String version) {
        this.id = id;
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Ref ref = (Ref) o;

        if (id != null ? !id.equals(ref.id) : ref.id != null) return false;
        return version != null ? version.equals(ref.version) : ref.version == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }
}
