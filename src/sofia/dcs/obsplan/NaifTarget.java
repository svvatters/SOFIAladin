package sofia.dcs.obsplan;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;

/**
 * @author bwarrington@sofia.usra.edu
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "NaifTarget")
@XmlType(propOrder = { "ObjectName", "Equinox", "NaifId", "MajorBody",
		"ApparentMagnitude" })
public class NaifTarget {

	@XmlAttribute(name = "ObjectName")
	private String ObjectName;
	@XmlAttribute(name = "Equinox")
	private String Equinox;
	@XmlAttribute(name = "NaifId")
	private String NaifId;
	@XmlAttribute(name = "MajorBody")
	private String MajorBody;
	@XmlAttribute(name = "ApparentMagnitude")
	private String ApparentMagnitude;

	public String getObjectName() {
		return ObjectName;
	}
	public void setObjectName(String s) {
		this.ObjectName = s;
	}

	public String getEquinox() {
		return Equinox;
	}
	public void setEquinox(String s) {
		this.Equinox = s;
	}

	public String getNaifId() {
		return NaifId;
	}
	public void setNaifId(String s) {
		this.NaifId = s;
	}

	public String getMajorBody() {
		return MajorBody;
	}
	public void setMajorBody(String s) {
		this.MajorBody = s;
	}

	public String getApparentMagnitude() {
		return ApparentMagnitude;
	}
	public void setApparentMagnitude(String s) {
		this.ApparentMagnitude = s;
	}
}
