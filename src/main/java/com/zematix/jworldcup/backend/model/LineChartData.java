package com.zematix.jworldcup.backend.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LineChartData {
	private List<LocalDateTime> matchDates = new ArrayList<>(); // labels
	private List<LineChartDataset> datasets = new ArrayList<>();
}
