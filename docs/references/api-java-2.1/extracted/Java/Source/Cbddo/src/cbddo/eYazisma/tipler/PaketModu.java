package cbddo.eYazisma.tipler;

public enum PaketModu {
    Olustur(1),
    Ac(2),
    Guncelle(3);
    private final int index;

    PaketModu(int index) {
        this.index = index;
    }

    public int index() {
        return index;
    }
}
