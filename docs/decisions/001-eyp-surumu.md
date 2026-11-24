# 001 – Özel şirketler için EYP sürümü

Durum: **Açık – kullanıcı kararı/saha verisi bekleniyor**
Tarih: 2026-11-23

## Bulgular
- Rehber 2.1 (resmiyazisma.gov.tr, PDF 14.07.2026 baskısı): 2.x pakette "mutlaka bir tane Elektronik Mühür bileşeni bulunmalıdır". Özel gönderici istisnası yok. Rehber kapsamı "kamu kurum ve kuruluşları arasında" (§4).
- Kamu SM e-mühür sertifikası yalnızca DETSİS'te kayıtlı kamu kurumlarına verilir (KSM_EMUHUR_SI_v4, 20.10.2022). Özel şirket Kamu SM mühürü alamaz.
- Ticari ESHS'den alınan nitelikli e-mühürün kamu EBYS'lerince kabulü: **doğrulanamadı**.
- Resmî Yazışma Yönetmeliği (RG 31151) yalnız kamu idarelerini kapsar; md.30/4 ret hakkını "başka bir idareden" gelen belgeye tanır. Özel sektörün EYP göndermesini düzenleyen hüküm bulunamadı.
- 1.3 kabulü: Rehber 2.x, kurumların eski paketleri "bir süre daha" kabul etmesini ister; bitiş tarihi yok. TÜRKKEP 1.3 ve 2.0 desteklediğini söylüyor.
- Resmî API 2.1 `Kapat()` mühürsüz 2.x paketi de kapatıyor (teknik olarak üretilebilir, uygunluğu ayrı konu).
- Resmî API 2.x, 2.0'dan küçük paketleri reddediyor; 1.3 için ayrı API gerekir.
- PTT KEP Webmail kılavuzu (2021) yalnızca `.eyp` uzantısı istiyor, sürüm/mühür şartı yazmıyor.

## Karar (geçici)
- Uygulama iki modu destekler: **1.3 (yalnız e-imza)** ve **2.x (e-imza + e-mühür)**.
- Varsayılan, aşağıdaki sorular cevaplanana kadar belirlenmez.

## Açık sorular
1. Şirketin ticari ESHS'den (ör. TÜRKKEP, E-Tuğra) nitelikli e-mühür alma planı var mı?
2. İade eden kurum(lar)dan biri hangi sürümü bekliyor? Gelen kamu yazılarındaki .eyp sürümleri (Core version) neler?
3. TÜRKKEP vb. sağlayıcıların özel şirketler için ürettiği EYP'lerden bir örnek temin edilebilir mi?
