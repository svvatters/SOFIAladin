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
@XmlRootElement(name = "ObservingPlan")
@XmlType(propOrder = { "Id", "CycleId", "LastModificationTime", "TacGrade",
		"IsTargetOfOpportunity", "IsSurvey", "AwardedTimeInSeconds",
		"IsMustDo", "Title", "PrimaryInvestigator", "Observations" })
public class ObservingPlan {

	@XmlAttribute(name = "Id")
	private String Id;
	@XmlAttribute(name = "CycleId")
	private String CycleId;
	@XmlAttribute(name = "LastModificationTime")
	private String LastModificationTime;
	@XmlAttribute(name = "TacGrade")
	private String TacGrade;
	@XmlAttribute(name = "IsTargetOfOpportunity")
	private String IsTargetOfOpportunity;
	@XmlAttribute(name = "IsSurvey")
	private String IsSurvey;
	@XmlAttribute(name = "AwardedTimeInSeconds")
	private String AwardedTimeInSeconds;
	@XmlAttribute(name = "IsMustDo")
	private String IsMustDo;
	@XmlAttribute(name = "Title")
	private String Title;
	@XmlAttribute(name = "PrimaryInvestigator")
	private String PrimaryInvestigator;
	@XmlElement(name = "Observation")
	private ArrayList<Observation> Observations;

	public String getId() {
		return Id;
	}
	public void setId(String s) {
		this.Id = s;
	}

	public String getCycleId() {
		return CycleId;
	}
	public void setCycleId(String s) {
		this.CycleId = s;
	}

	public String getLastModificationTime() {
		return LastModificationTime;
	}
	public void setLastModificationTime(String s) {
		this.LastModificationTime = s;
	}

	public String getTacGrade() {
		return TacGrade;
	}
	public void setTacGrade(String s) {
		this.TacGrade = s;
	}

	public String getIsTargetOfOpportunity() {
		return IsTargetOfOpportunity;
	}
	public void setIsTargetOfOpportunity(String s) {
		this.IsTargetOfOpportunity = s;
	}

	public String getIsSurvey() {
		return IsSurvey;
	}
	public void setIsSurvey(String s) {
		this.IsSurvey = s;
	}

	public String getAwardedTimeInSeconds() {
		return AwardedTimeInSeconds;
	}
	public void setAwardedTimeInSeconds(String s) {
		this.AwardedTimeInSeconds = s;
	}

	public String getIsMustDo() {
		return IsMustDo;
	}
	public void setIsMustDo(String s) {
		this.IsMustDo = s;
	}

	public String getTitle() {
		return Title;
	}
	public void setTitle(String s) {
		this.Title = s;
	}

	public String getPrimaryInvestigator() {
		return PrimaryInvestigator;
	}
	public void setPrimaryInvestigator(String s) {
		this.PrimaryInvestigator = s;
	}

	public void setObservations(ArrayList<Observation> Observations) {
		this.Observations = Observations;
	}
	public ArrayList<Observation> getObservations() {
		return Observations;
	}
}
