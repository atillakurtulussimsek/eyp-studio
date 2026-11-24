package cbddo.eYazisma.tipler;

public class OzetDogrulamaHatasi {
	private String _Uri;
	private String _Hata;
	private OzetDogrulamaHataKodu _HataKodu;
	private Exception _InnerException;

	public String getUri() {
		return _Uri;
	}

	public void setUri(String value) {
		_Uri = value;
	}

	public String getHata() {
		return _Hata;
	}

	public void setHata(String value) {
		_Hata = value;
	}

	public OzetDogrulamaHataKodu getHataKodu() {
		return _HataKodu;
	}

	public void setHataKodu(OzetDogrulamaHataKodu value) {
		_HataKodu = value;
	}

	public Exception getInnerException(){
		return _InnerException;
	}

	public void setInnerException(Exception value){
		_InnerException = value;
	}

	OzetDogrulamaHatasi(String hata, OzetDogrulamaHataKodu hataKodu) {
		_Hata = hata;
		_HataKodu = hataKodu;
	}

	OzetDogrulamaHatasi(String hata, OzetDogrulamaHataKodu hataKodu, String uri) {
		_Hata = hata;
		_HataKodu = hataKodu;
		_Uri = uri;
	}

	OzetDogrulamaHatasi(String hata, OzetDogrulamaHataKodu hataKodu, String uri, Exception innerException) {
		_Hata = hata;
		_HataKodu = hataKodu;
		_Uri = uri;
		_InnerException=innerException;
	}
}
