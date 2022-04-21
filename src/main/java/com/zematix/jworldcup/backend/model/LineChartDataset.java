package com.zematix.jworldcup.backend.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LineChartDataset {
	private String label;
	private List<Integer> data = new ArrayList<>();
}
