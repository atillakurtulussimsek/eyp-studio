package cbddo.eYazisma.tipler;


import urn.cbddo.eyazisma.schema.xsd.tipler.CTSifreliIcerikBilgisi;

public class SifreliIcerikBilgisiInternal extends SifreliIcerikBilgisi {
	private CTSifreliIcerikBilgisi _CTSifreliIcerikBilgisi;

	public SifreliIcerikBilgisiInternal() {
		_CTSifreliIcerikBilgisi = new CTSifreliIcerikBilgisi();
	}

	SifreliIcerikBilgisiInternal(CTSifreliIcerikBilgisi sifreliIcerikBilgisi) {
		_CTSifreliIcerikBilgisi = sifreliIcerikBilgisi;
	}

	@Override
	public CTSifreliIcerikBilgisi getSifreliIcerikBilgisi() {
		return _CTSifreliIcerikBilgisi;
	}

	@Override
	public void kontrolEt() {}
}
