package cbddo.eYazisma.tipler;


import urn.cbddo.eyazisma.schema.xsd.belgeHedef.CTBelgeHedef;
import urn.cbddo.eyazisma.schema.xsd.tipler.CTHedef;
import urn.cbddo.eyazisma.schema.xsd.tipler.CTHedefListesi;

import java.util.List;

class BelgeHedefInternal extends BelgeHedef {
	CTBelgeHedef _CTBelgeHedef;

	public BelgeHedefInternal() {
		_CTBelgeHedef = new CTBelgeHedef();
	}

	BelgeHedefInternal(CTBelgeHedef belgeHedef) {
		_CTBelgeHedef = belgeHedef;
	}

	@Override
	public CTBelgeHedef getCTBelgeHedef() {
		return _CTBelgeHedef;
	}

	@Override
	public void hedefEkle(CTHedef hedef) {
		if (_CTBelgeHedef.getHedefListesi() == null)
			_CTBelgeHedef.setHedefListesi(new CTHedefListesi());
		_CTBelgeHedef.getHedefListesi().getHedef().add(hedef);
	}

	@Override
	public List<CTHedef> hedefleriAl() {
		if (_CTBelgeHedef.getHedefListesi() == null)
			return null;
		return this._CTBelgeHedef.getHedefListesi().getHedef();
	}

	@Override
	public void kontrolEt() {
		XsdAraclar.kontrolEt(_CTBelgeHedef);
	}
}
