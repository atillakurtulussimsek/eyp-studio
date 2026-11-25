# XSD, Rehber ve resmî API farkları

Kaynaklar: Teknik Rehber 2.1, resmî e-Yazışma API 2.1 (Java) ve XSD'ler (`docs/references/`).
Her madde bir testle belgelendi; test adı parantez içinde.

## XSD hataları (paketle gelen kopyada yamalandı)

| # | Yer | Sorun | Yama | Kanıt |
|---|---|---|---|---|
| 1 | EY-Tipler-2: ST_GUID, ST_TCKN, ST_KKK | Desenler `^…$` içeriyor. XSD regex'inde bunlar düz karakter, bu yüzden geçerli her değer reddediliyor. | Çapalar kaldırıldı | `SchemaPatchTest` |
| 2 | EY-Tipler-2: `schemaLocation="reference/..."` | Klasörün adı `Reference/`. Büyük/küçük harf duyarlı dosya sistemlerinde şema yüklenemiyor. | Klasör `reference/` yapıldı | — |
| 3 | EY-Tipler-2: CT_Guncelleme | `xsd:any` (##any), `GuvenlikKoduDegisiklikBilgisi` ile Unique Particle Attribution ihlali yaratıyor. Xerces şemayı derlemiyor. | `namespace="##other" processContents="lax"` | İlk test çalıştırması |
| 4 | EY-PaketOzeti-2 / NihaiOzet-2 / ParafOzeti-2: `Id` | Tip `xsd:ID` (NCName). K.34/K.44 bu alana BelgeId GUID'ini koyuyor; rakamla başlayan GUID NCName değil. | `xsd:string` | `PackageBuilderTest` |
| 5 | EY-KodGuvenlik-2 | 2.1 BENIOKU'ya göre OZL kaldırıldı, ama XSD'de hâlâ var. | Değişmedi; uygulama OZL sunmuyor | — |

## Resmî API (Java 2.1) davranışları

| Konu | Davranış | Bizim tercih |
|---|---|---|
| Okuma modu `Paket.ac(stream)` | İmza, Nihai Üstveri ve Nihai Özet yoksa paketi açmıyor. | İmzasız paket, `PaketModu.Guncelle` ile çapraz doğrulanıyor. |
| Varsayılan özet | SHA-384 + SHA-512 (SHA-512 önce yazılıyor) | SHA-512 + SHA-256 varsayılan; `--ikinci-ozet SHA384` ile değiştirilebilir (karar 007). |
| Core sürümü | Normal pakette "2.0" | Aynı |
| Core bileşen adı | POI: `/docProps/core.xml` | Rehber §10 örneği: `/package/services/metadata/core-properties/<guid>.psmdcp`. Okuyucu ikisini de kabul ediyor. |
| Nihai Özet'te Core | `nihaiOzetOlustur()` Core'u eklemiyor. API'nin test uygulaması sonradan ayrı bir yardımcıyla ekliyor; `nihaiOzetDogrula` Core özetini kontrol etmiyor. | Core, Nihai Özet'ten önce bir kez yazılıyor ve gerçek özeti ekleniyor (K.43). |
| `xsi:nil` | Boş `GuvenlikKoduGecerlilikTarihi` için `xsi:nil="true"` yazıyor. | Doğrulayıcı `xsi:nil`'i K.22 ihlali saymıyor (öğe XSD'de nillable). |
| Paket ilişki hedefleri | Göreli (`Ustveri/Ustveri.xml`) | Mutlak (`/Ustveri/Ustveri.xml`); ikisi de OPC'ye uygun. |
| `paketOzetiDogrula` | Üstveri/ÜstYazı varlık kontrolü String ile URI'yi karşılaştırdığı için hiç tetiklenmiyor. | Kendi doğrulayıcımız kapsamı ayrıca kontrol ediyor. |

Resmî API'nin ürettiği paket kendi doğrulayıcımızda yalnız K.43 (Nihai Özet'te Core yok) veriyor (`OfficialApiCrossCheckTest.ourValidatorOnOfficialApiOutput`).
