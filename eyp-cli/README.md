# eyp-cli

```bash
eyp olustur belge.json [-o KLASOR] [--ikinci-ozet SHA256|SHA384]   # imzasız [BelgeId].eyp
eyp incele PAKET.eyp [-x KLASOR]                                  # bileşenler, ilişkiler, üstveri; -x ile dışa aktarım
eyp dogrula PAKET.eyp                                             # K.xx kurallarına göre yapısal doğrulama
```

Çıkış kodları: 0 başarılı, 1 doğrulama hatası, 2 girdi/dosya hatası.

## belge.json

Dosya yolları JSON dosyasına göre çözülür. Dosya adları pakette ASCII'ye çevrilir (Türkçe harfler, boşluklar).

```json
{
  "konu": "Sözleşme hk.",
  "guvenlikKodu": "YOK",
  "olusturan": {"tuzelSahis": {"id": "1234567890", "idTuru": "VKN", "adi": "Şirket A.Ş.",
                "iletisim": {"kepAdresi": "sirket@hs01.kep.tr", "adres": "...", "il": "Ankara"}}},
  "dagitimlar": [{"kurumKurulus": {"kkk": "14 hane hex KKK", "adi": "Kurum adı"},
                  "ivedilik": "NRM", "dagitimTuru": "GRG"}],
  "ustYazi": "ustyazi.pdf",
  "ekler": [{"dosya": "ek1.pdf", "ad": "Ek adı", "imzali": true}],
  "ilgiler": [{"etiket": "a", "belgeNo": "E-123", "tarih": "2026-10-01"}],
  "sdp": [{"kod": "045.02", "ad": "Dosya planı adı"}],
  "dogrulamaAdresi": "https://..."
}
```

Taraf türleri: `kurumKurulus` (kkk, adi, birimKkk), `tuzelSahis` (id, idTuru, adi), `gercekSahis` (ad, soyad, tckn, gorev, unvan).
Değer listeleri: guvenlikKodu YOK/HZO/GZL/CGZ, ivedilik NRM/ACL/GNL, dagitimTuru BLG/GRG.
