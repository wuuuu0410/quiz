package com.example.quiz.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DeleteReq {

	@JsonProperty("id_list")
	private List<Integer> idList;

	public DeleteReq() {
		super();
		System.out.println("�w�]�غc��k");
	}

	public DeleteReq(List<Integer> idList) {
		this.idList = idList;
		System.out.println("���Ѽƪ��غc��k");
	}

	public List<Integer> getIdList() {
		return idList;
	}

	public void setIdList(List<Integer> idList) {
		this.idList = idList;
	}
	
	
}
