package cbddo.eYazisma.tipler;

import urn.cbddo.eyazisma.schema.xsd.guncellemeturu.STGuncellemeTuru;
import urn.cbddo.eyazisma.schema.xsd.tipler.CTGuncelleme;
import urn.cbddo.eyazisma.schema.xsd.tipler.CTGuncellemeBilgisi;
import urn.cbddo.eyazisma.schema.xsd.tipler.CTGuvenlikKoduDegisiklikBilgisi;

import java.util.ArrayList;
import java.util.List;

public class GuncellemeBilgisiInternal extends GuncellemeBilgisi {
    private CTGuncellemeBilgisi _guncellemeBilgisi;

    GuncellemeBilgisiInternal() {
        _guncellemeBilgisi = new CTGuncellemeBilgisi();
    }

    GuncellemeBilgisiInternal(CTGuncellemeBilgisi guncellemeBilgisi) {
        _guncellemeBilgisi = guncellemeBilgisi;
    }


    @Override
    public CTGuncellemeBilgisi getGuncellemeBilgisi() {
        return _guncellemeBilgisi;
    }

    @Override
    public void guncellemeEkle(STGuncellemeTuru guncellemeTuru, Object guncelleme) throws Exception {
        if (_guncellemeBilgisi == null) {
            _guncellemeBilgisi = new CTGuncellemeBilgisi();
        }

        if (_guncellemeBilgisi.getGuncellemeler() == null) {
            _guncellemeBilgisi.setGuncellemeler(new ArrayList<>());
        }

        if (guncellemeTuru == STGuncellemeTuru.GuvenlikKodu) {
            if (!(guncelleme instanceof CTGuvenlikKoduDegisiklikBilgisi)) {
                throw new Exception("GuncellemeTuru GuvenlikKodu seçildiğinde Güncelleme verisi \"CT_GuvenlikKoduDegisiklikBilgisi\" olabilir");
            }

            CTGuvenlikKoduDegisiklikBilgisi degisiklikBilgisi = (CTGuvenlikKoduDegisiklikBilgisi) guncelleme;

            if (Araclar.isNullOrWhitespace(degisiklikBilgisi.getAciklama())) {
                throw new Exception("Aciklama boş olamaz");
            }
            if (Araclar.isNullOrWhitespace(degisiklikBilgisi.getKomisyonKarariBelgeId())) {
                throw new Exception("KomisyonKarariBelgeId boş olamaz");
            }
            if (Araclar.isNullOrWhitespace(degisiklikBilgisi.getKomisyonKarariBelgeNo())) {
                throw new Exception("KomisyonKarariBelgeNo boş olamaz");
            }
            if (!degisiklikBilgisi.getDegistirmeTarihi().isValid()) {
                throw new Exception("DegistirmeTarihi geçerli bir tarih değil");
            }

            List<CTGuncelleme> guncellemeler = _guncellemeBilgisi.getGuncellemeler();
            CTGuncelleme ctGuncelleme = new CTGuncelleme();
            ctGuncelleme.setGuncellemeTuru(guncellemeTuru);
            ctGuncelleme.setGuvenlikKoduDegisiklikBilgisi(degisiklikBilgisi);
            guncellemeler.add(ctGuncelleme);
            _guncellemeBilgisi.setGuncellemeler(guncellemeler);
        }
    }

    @Override
    public List<CTGuncelleme> guncellemeleriAl() {
        return _guncellemeBilgisi.getGuncellemeler();
    }

    @Override
    public void kontrolEt() throws Exception {
        if (_guncellemeBilgisi == null) {
            throw new Exception("CT_GuncellemeBilgisi nesnesi boş");
        }
        if (_guncellemeBilgisi.getGuncellemeler() == null || _guncellemeBilgisi.getGuncellemeler().isEmpty()) {
            throw new Exception("Güncellemeler eklenmemiş");
        }
        for (CTGuncelleme item : _guncellemeBilgisi.getGuncellemeler()) {
            if (item.getGuncellemeTuru() == STGuncellemeTuru.GuvenlikKodu) {
                final CTGuvenlikKoduDegisiklikBilgisi degisiklikBilgisi = item.getGuvenlikKoduDegisiklikBilgisi();
                if (degisiklikBilgisi == null) {
                    throw new Exception("GuvenlikKoduDegisiklikBilgisi boş");
                }
                if (Araclar.isNullOrWhitespace(degisiklikBilgisi.getAciklama())) {
                    throw new Exception("Aciklama boş olamaz");
                }
                if (Araclar.isNullOrWhitespace(degisiklikBilgisi.getKomisyonKarariBelgeId())) {
                    throw new Exception("KomisyonKarariBelgeId boş olamaz");
                }
                if (Araclar.isNullOrWhitespace(degisiklikBilgisi.getKomisyonKarariBelgeNo())) {
                    throw new Exception("KomisyonKarariBelgeNo boş olamaz");
                }
                if (!degisiklikBilgisi.getDegistirmeTarihi().isValid()) {
                    throw new Exception("DegistirmeTarihi geçerli bir tarih değil");
                }

            } else {
                throw new Exception("Eklenen değişikliğin DegisiklikTuru belirlenemedi");
            }
        }
    }
}
