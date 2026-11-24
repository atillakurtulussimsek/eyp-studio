package cbddo.eYazisma.tipler;

public enum OzetModu {
    Yok(0),
    SHA256(1),
    SHA512(2),
    SHA384(3);
    private final int index;

    OzetModu(int index) {
        this.index = index;
    }

    public int index() {
        return index;
    }
}
