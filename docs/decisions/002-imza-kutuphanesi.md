# 002 – İmza kütüphanesi: MA3 API mi, DSS mi?

Durum: **Geçici karar – uygulamalı test gerekli**
Tarih: 2026-11-23

## Gereksinim (kaynaklı)
- İmza: P4, CAdES ES X-Long; mühür: P4, ES-A (Kamu SM EYP Ön Hazırlık Rehberi REH.05.02 Rev05).
- P4 politika OID'i `2.16.792.1.61.0.1.5070.3.3.1` (BTK Elektronik İmza Kullanım Profilleri Rehberi, 2012).
- P4 zorunlu imzasız öznitelikler: signature-time-stamp, complete-certificate-refs, complete-revocation-refs, certificate-values, revocation-values. İptal kontrolü OCSP.
- İmzalı öznitelik mime-type = `text/xml` (OID 0.4.0.1733.2.1).
- Politika özeti: gerçek kodda (MA3 çıktısı) Profiller Rehberi PDF'inin SHA-256'sı kullanılıyor (`ff39bd29…117a`). MA3'ün yazdığı SPURI doğrulanmadı.

## MA3 API
- 01.06.2026'dan itibaren **ücretli**; fiyat yayımlanmamış (başvuru: api@kamusm.gov.tr).
- Süreli, imzalı lisans dosyası; satış/alt lisans yazılı izne bağlı; "TÜBİTAK BİLGEM - MA3 API" ibaresi zorunlu.
- Maven'da yok. Java 21 ve macOS/Linux uyumu belgelenmemiş.
- BENI_OKU: "yalnızca Kamu SM zaman damgası ile çalışır".
- ES-XL ve ES-A hazır; Kamu SM uyum testi buna göre yazılmış.

## DSS (6.5.1, LGPL-2.1)
- Maven Central'da, Java 21 uyumlu. EPES (policy id + hash + SPURI) ve mime-type destekli.
- **CAdES-X-Long üretemiyor**: imzalamada yalnız BASELINE B/T/LT/LTA. LT, doğrulama verisini SignedData.certificates/crls'ye koyar; complete-*-refs ve *-values özniteliklerini üretmez.
- LTA, archive-time-stamp-v3 üretir; Kamu SM kabulü bilinmiyor.

## Karar (geçici)
DSS + BouncyCastle ile kendi X-Long katmanı:
1. DSS ile CAdES-EPES (B) + signature-time-stamp (T) üretilir.
2. refs ve values öznitelikleri BouncyCastle ile eklenir (ES-C → ES-X-Long).
3. Mühür için ES-A: önce DSS LTA (ATSv3) denenir; kabul edilmezse kendi uygulamamız.

MA3 yedek seçenektir. Kullanıcı fiyat teklifi almak isterse paralel yürütülür.

## Doğrulama testi (Faz 0 imza denemesi)
- SoftHSM2 test anahtarıyla tek dosya imzalanır; ASN.1 dökümü P4 öznitelik listesiyle karşılaştırılır.
- Gerçek AKİS kartla imza denemesi yapılır. Ardından e-Yazışma Test Platformu'nda doğrulanır.

## Açık sorular
1. MA3 için Kamu SM'den fiyat teklifi istenecek mi?

## Faz 0 imza denemesi sonucu (2026-11-24)
Kod: `spikes/cades-p4/` (DSS 6.5.1 + BouncyCastle 1.86, OpenJDK 21.0.13.1, SoftHSM2 2.7.0). Test PKI, TSA ve OCSP süreç içinde, yereldir.

**Başarılı**
- İmzacı anahtarı SoftHSM token'ında üretildi; özel anahtar token'dan çıkmadı. İmza DSS `Pkcs11SignatureToken` ile atıldı.
- DSS çıktısı (EPES + T, enveloping) üzerine BouncyCastle ile complete-certificate-refs, complete-revocation-refs, certificate-values ve revocation-values (OCSP) eklendi. CAdES-C zaman damgası (id-aa 25/26) yok, P4 beklentisiyle uyumlu.
- İmzalı öznitelikler: content-type, message-digest, signing-time, signing-certificate-v2, signature-policy-identifier (P4 OID + SHA-256 + SPURI), mime-type = text/xml.
- DSS doğrulaması `TOTAL_PASSED`: politika OID'i ve mime-type okundu; 3 sertifika referansı ve 1 iptal referansı eşleşti.
- Ders: `ocspRepHash` tam OCSPResponse üzerinden hesaplanmalı. Yalnız BasicOCSPResponse ile hesaplanınca referans eşleşmedi.

**Riskler / açık noktalar**
1. DSS bu imzayı "CAdES-C" olarak etiketliyor. DSS, XL etiketini yalnız ES-X zaman damgası (Type 1/2) varsa veriyor; ETSI 101 733'teki "ES-X-Long" (C + values) seviyesini ayrı tanımıyor. Kendi doğrulayıcımız seviyeyi öznitelik listesinden belirlemeli.
2. DSS ek olarak `cms-algorithm-protection` (1.2.840.113549.1.9.52) imzalı özniteliğini ekliyor. P4 listesinde yok; Kamu SM/MA3 doğrulayıcısının bunu kabul edip etmediği bilinmiyor.
3. SPURI değeri doğrulanmadı; gerçek bir MA3/EBYS imzasıyla karşılaştırılmalı.
4. Denenmedi: EC anahtar, ES-A (mühür), gerçek Kamu SM TSA, gerçek AKİS kart, e-Yazışma Test Platformu.

**Sonuç:** DSS + BouncyCastle ile P4 ES X-Long yapısı teknik olarak üretilebiliyor. Kesin karar için gerçek bir kamu .eyp imzasıyla öznitelik karşılaştırması ve Test Platformu doğrulaması gerekli.
