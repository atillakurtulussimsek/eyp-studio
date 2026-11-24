# 004 – Zaman damgası (TSA)

Durum: **Kısmen karar verildi**
Tarih: 2026-11-23

## Bulgular
- Kamu SM: üretim `http://zd.kamusm.gov.tr`, test `tzd.kamusm.gov.tr` (5070'e uygun değil). Uyum testlerinde `zds*.test3.kamusm.gov.tr`.
- Test kullanıcısı ücretsiz: bilgi@kamusm.gov.tr, konu "Zamane test kullanıcı talebi".
- Protokol: RFC 3161 + özel `identity` HTTP başlığı (müşteri no + PBKDF2/AES ile şifreli istek özeti). Resmî spesifikasyon yok; açık kaynak iki uygulamadan (mersel-dss, eimza-go) çıkarıldı. Bir başka proje Basic auth kullanıyor → testte netleşecek.
- Kontör paketleri: 10 bin–10 milyon; Kamu SM fiyatı sayfada görünmüyor.
- E-Tuğra (KDV dahil): 1.000 = ₺3.779, 10.000 = ₺10.899. Endpoint/auth doğrulanmadı.
- TÜRKTRUST, E-Güven, TÜRKKEP: bilgi bulunamadı.
- Kamu SM TSA EC P-384'e geçiyor (duyuru 25.08.2026).
- P4: zaman damgası signing-time'dan en geç 2 saat sonra alınmalı.

## Karar
- TSA istemcisi kendi yazılır; iki kimlik doğrulama modu: Kamu SM `identity` ve HTTP Basic.
- URL ve kullanıcı bilgisi kod dışında; parola işletim sistemi güvenli deposunda.
- Faz 2 öncesi Kamu SM test kullanıcısı istenir.

## Açık sorular
1. Üretimde hangi TSA kullanılacak (Kamu SM mi, ticari mi)? Şirketin mevcut zaman damgası kontörü var mı?
