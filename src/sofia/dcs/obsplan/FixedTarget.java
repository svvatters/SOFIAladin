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
@XmlRootElement(name = "FixedTarget")
@XmlType(propOrder = { "ObjectName", "Equinox", "RightAscension",
		"Declination", "ApparentMagnitude" })
public class FixedTarget {
	
	@XmlAttribute(name = "ObjectName")
	private String ObjectName;
	@XmlAttribute(name = "Equinox")
	private String Equinox;
	@XmlAttribute(name = "RightAscension")
	private String RightAscension;
	@XmlAttribute(name = "Declination")
	private String Declination;
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

	public String getRightAscension() {
		return RightAscension;
	}
	public void setRightAscension(String s) {
		this.RightAscension = s;
	}

	public String getDeclination() {
		return Declination;
	}
	public void setDeclination(String s) {
		this.Declination = s;
	}

	public String getApparentMagnitude() {
		return ApparentMagnitude;
	}
	public void setApparentMagnitude(String s) {
		this.ApparentMagnitude = s;
	}
}
