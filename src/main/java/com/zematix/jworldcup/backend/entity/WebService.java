package com.zematix.jworldcup.backend.entity;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the web_service database table.
 * 
 */
@Entity
@Table(name="web_service", uniqueConstraints=@UniqueConstraint(columnNames={"league_shortcut", "league_saison"}))
@NamedQuery(name="WebService.findAll", query="SELECT w FROM WebService w")
public class WebService implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="web_service_id", unique=true, nullable=false)
	private Long webServiceId;

	@Column(name="league_saison", nullable=false, length=10)
	private String leagueSaison;

	@Column(name="league_shortcut", nullable=false, length=64)
	private String leagueShortcut;

	@Column(nullable=false)
	private Byte priority;

	@Column(name="result_extra_label", nullable=false, length=64)
	private String resultExtraLabel;

	@Column(name="result_normal_extra_label", length=64)
	private String resultNormalExtraLabel;

	@Column(name="result_normal_label", nullable=false, length=64)
	private String resultNormalLabel;

	@Column(name="result_penalty_label", length=64)
	private String resultPenaltyLabel;

	//bi-directional many-to-one association to Event
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="event_id", nullable=false)
	private Event event;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((webServiceId == null) ? 0 : webServiceId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof WebService)) // use instanceof instead of getClass, due to hibernate creating proxys of subclasses that are lazy-loaded
			return false;
		WebService other = (WebService) obj;
		if (webServiceId == null) {
			if (other.getWebServiceId() != null) // remember to change to getter at other, simple property may result always null
				return false;
		} else if (!webServiceId.equals(other.getWebServiceId())) // remember to change to getter at other, simple property may result always null
			return false;
		return true;
	}

	public Long getWebServiceId() {
		return this.webServiceId;
	}

	public void setWebServiceId(Long webServiceId) {
		this.webServiceId = webServiceId;
	}

	public String getLeagueSaison() {
		return this.leagueSaison;
	}

	public void setLeagueSaison(String leagueSaison) {
		this.leagueSaison = leagueSaison;
	}

	public String getLeagueShortcut() {
		return this.leagueShortcut;
	}

	public void setLeagueShortcut(String leagueShortcut) {
		this.leagueShortcut = leagueShortcut;
	}

	public Byte getPriority() {
		return this.priority;
	}

	public void setPriority(Byte priority) {
		this.priority = priority;
	}

	public String getResultExtraLabel() {
		return this.resultExtraLabel;
	}

	public void setResultExtraLabel(String resultExtraLabel) {
		this.resultExtraLabel = resultExtraLabel;
	}

	public String getResultNormalExtraLabel() {
		return this.resultNormalExtraLabel;
	}

	public void setResultNormalExtraLabel(String resultNormalExtraLabel) {
		this.resultNormalExtraLabel = resultNormalExtraLabel;
	}

	public String getResultNormalLabel() {
		return this.resultNormalLabel;
	}

	public void setResultNormalLabel(String resultNormalLabel) {
		this.resultNormalLabel = resultNormalLabel;
	}

	public String getResultPenaltyLabel() {
		return this.resultPenaltyLabel;
	}

	public void setResultPenaltyLabel(String resultPenaltyLabel) {
		this.resultPenaltyLabel = resultPenaltyLabel;
	}

	public Event getEvent() {
		return this.event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

}