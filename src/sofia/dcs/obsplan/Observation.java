package sofia.dcs.obsplan;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
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
@XmlRootElement(name = "Observation")
@XmlType(propOrder = { "Id", "State", "Priority", "InstrumentId",
		"InstrumentConfigurations", "DurationInSeconds", "OverheadInSeconds",
		"FixedTargets", "NaifTargets" })
public class Observation {

	public Observation() {
		this.Id = "";
		this.State = "";
		this.Priority = "";
		this.InstrumentId = "";
		this.InstrumentConfigurations = "";
		this.DurationInSeconds = 0;
		this.OverheadInSeconds = 0;
		this.FixedTargets = new ArrayList<FixedTarget>();
		this.NaifTargets = new ArrayList<NaifTarget>();
	}

	public Observation(Observation another) {
		this.Id = another.Id;
		this.State = another.State;
		this.Priority = another.Priority;
		this.InstrumentId = another.InstrumentId;
		this.InstrumentConfigurations = another.InstrumentConfigurations;
		this.DurationInSeconds = another.DurationInSeconds;
		this.OverheadInSeconds = another.OverheadInSeconds;
		this.FixedTargets = new ArrayList<FixedTarget>(another.FixedTargets);
		this.NaifTargets = new ArrayList<NaifTarget>(another.NaifTargets);
	}

	@XmlAttribute(name = "Id")
	private String Id;
	@XmlAttribute(name = "State")
	private String State;
	@XmlAttribute(name = "Priority")
	private String Priority;
	@XmlAttribute(name = "InstrumentId")
	private String InstrumentId;
	@XmlAttribute(name = "InstrumentConfigurations")
	private String InstrumentConfigurations;
	@XmlAttribute(name = "DurationInSeconds")
	private int DurationInSeconds;
	@XmlAttribute(name = "OverheadInSeconds")
	private int OverheadInSeconds;
	@XmlElement(name = "FixedTarget")
	private ArrayList<FixedTarget> FixedTargets;
	@XmlElement(name = "NaifTarget")
	private ArrayList<NaifTarget> NaifTargets;

	public String getId() {
		return Id;
	}
	public void setId(String s) {
		this.Id = s;
	}

	public String getState() {
		return State;
	}
	public void setState(String s) {
		this.State = s;
	}

	public String getPriority() {
		return Priority;
	}
	public void setPriority(String s) {
		this.Priority = s;
	}

	public String getInstrumentId() {
		return InstrumentId;
	}
	public void setInstrumentId(String s) {
		this.InstrumentId = s;
	}

	public String getInstrumentConfigurations() {
		return InstrumentConfigurations;
	}
	public void setInstrumentConfigurations(String s) {
		this.InstrumentConfigurations = s;
	}

	public int getDurationInSeconds() {
		return DurationInSeconds;
	}
	public void setDurationInSeconds(int s) {
		this.DurationInSeconds = s;
	}

	public int getOverheadInSeconds() {
		return OverheadInSeconds;
	}
	public void setOverheadInSeconds(int s) {
		this.OverheadInSeconds = s;
	}

	public void setFixedTargets(ArrayList<FixedTarget> FixedTargets) {
		this.FixedTargets = FixedTargets;
	}
	public ArrayList<FixedTarget> getFixedTargets() {
		return FixedTargets;
	}

	public void setNaifTargets(ArrayList<NaifTarget> NaifTargets) {
		this.NaifTargets = NaifTargets;
	}
	public ArrayList<NaifTarget> getNaifTargets() {
		return NaifTargets;
	}
}
