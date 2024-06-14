package com.example.quiz.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.quiz.entity.Quiz;

@Repository
public interface QuizDao extends JpaRepository<Quiz, Integer> {

	public List<Quiz> findByNameContainingAndStartDateGreaterThanEqualAndEndDateLessThanEqual(String name, //
			LocalDate start, LocalDate end);

	@Query(value = "select * from quiz", nativeQuery = true)
	public List<Quiz> selectAll();
	
}
