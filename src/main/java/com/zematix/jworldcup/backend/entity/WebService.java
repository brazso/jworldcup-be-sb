package com.zematix.jworldcup.backend.entity;

import java.io.Serializable;
import javax.persistence.*;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;


/**
 * The persistent class for the web_service database table.
 * 
 */
@Entity
@Table(name="web_service", uniqueConstraints=@UniqueConstraint(columnNames={"league_shortcut", "league_saison"}))
@NamedQuery(name="WebService.findAll", query="SELECT w FROM WebService w")
@Getter @Setter @EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class WebService implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="web_service_id", unique=true, nullable=false)
	@EqualsAndHashCode.Include
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
}