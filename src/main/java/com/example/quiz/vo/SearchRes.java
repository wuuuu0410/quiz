package com.example.quiz.vo;

import java.util.List;

import com.example.quiz.entity.Quiz;

public class SearchRes extends BasicRes {

	private List<Quiz> quizList;

	public SearchRes() {
		super();
	}

	public SearchRes(int statusCode, String message, List<Quiz> quizList) {
		super(statusCode, message);
		this.quizList = quizList;
	}

	public List<Quiz> getQuizList() {
		return quizList;
	}


	
	
}
