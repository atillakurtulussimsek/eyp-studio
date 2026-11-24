package cbddo.eYazisma.tipler;

public enum PaketTipi {
    /**
     * Normal e-Yazışma Paketi
     */
    NormalPaket(1),
    /**
     * Şifreli e-Yazışma Paketi
     */
    SifreliPaket(2),
    /**
     * Güncelleme Paketi
     */
    GuncellemePaketi(3);

    private final int index;

    PaketTipi(int index) {
        this.index = index;
    }

    public int index() {
        return index;
    }

}
