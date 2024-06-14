package com.example.quiz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.quiz.entity.Quiz;
import com.example.quiz.repository.QuizDao;

//@SpringBootTest

class QuizApplicationTests {

	@Autowired
	private QuizDao quizDao;

	@Test
	void contextLoads() {

	}

	@Test
	public void test() {
		List<String> list = List.of("Ab", "B", "C", "D", "E");//option 
		Map<String, Integer> statisticsMap = new TreeMap<>();
		String str = "AAAABBBBCCCDDDAAAEEECC";
		String newStr = "";
		for (String item : list) {
			newStr = str.replace(item, "");
			System.out.println(newStr);
			int x = str.length() - newStr.length();
			statisticsMap.put(item + "的出現次數是", x);
			System.out.println(item + "的出現次數是" + x);
			str = newStr;
			newStr = "";
		}
		System.out.println(statisticsMap);
	}
	
	
}
