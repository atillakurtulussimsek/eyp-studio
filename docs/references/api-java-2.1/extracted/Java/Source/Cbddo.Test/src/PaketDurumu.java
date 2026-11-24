import java.util.ArrayList;
import java.util.List;

public class PaketDurumu {

    private PaketDurumuEnum paketDurumuEnum;

    public PaketDurumuEnum getPaketDurumuEnum() {
        return paketDurumuEnum;
    }

    public void setPaketDurumuEnum(PaketDurumuEnum paketDurumuEnum) {
        this.paketDurumuEnum = paketDurumuEnum;
    }

    public List<AksiyonEnum> getAksiyonlar() {
        return aksiyonlar;
    }

    public void setAksiyonlar(List<AksiyonEnum> aksiyonlar) {
        this.aksiyonlar = aksiyonlar;
    }

    private List<AksiyonEnum> aksiyonlar;

    public PaketDurumu() {
        aksiyonlar = new ArrayList<>();
    }
}
