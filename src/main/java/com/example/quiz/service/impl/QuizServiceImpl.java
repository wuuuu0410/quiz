package com.example.quiz.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.example.quiz.constants.OptionType;
import com.example.quiz.constants.ResMessage;
import com.example.quiz.entity.Quiz;
import com.example.quiz.repository.QuizDao;
import com.example.quiz.service.ifs.QuizService;
import com.example.quiz.vo.BasicRes;
import com.example.quiz.vo.CreateOrUpdateReq;
import com.example.quiz.vo.DeleteReq;
import com.example.quiz.vo.Question;
import com.example.quiz.vo.SearchReq;
import com.example.quiz.vo.SearchRes;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class QuizServiceImpl implements QuizService {

	@Autowired
	private QuizDao quizDao;

	@Override
	public BasicRes createOrUpdate(CreateOrUpdateReq req) {
		// 檢查參數
		BasicRes checkResult = checkParams(req);
		// checkResult == null 時，表示參數檢查都正確
		if (checkResult != null) {
			return checkResult;
		}
		// 因為Quiz 中questions的資料格式是 String， 所以要將 req 的 List<Question> 轉成 String
		// 透過ObjectMapper，可以把物件(類別)轉成JSON格式字串
		ObjectMapper mapper = new ObjectMapper();
		try {
			String questionStr = mapper.writeValueAsString(req.getQuestionList());
			
			// 若req中的id，表示更新已存在的資料:若 id = 0;則表示要新增
			if (req.getId() > 0) {
				//
//				// 以下兩種方式擇一
//				// 使用方法1，透過fibdById，若有資料，就會回傳一整筆的資料(可能資料量較大)
//				// 使用方法2 因為透過existsById來判斷是否存在所以回傳的資料永遠都只會是一個bit(0或1)
//				// 方法1.透過findById
//				Optional<Quiz> op = quizDao.findById(req.getId());
//				// 判斷是否有資料
//				if (op.isEmpty()) {// op.isEmpty():表示沒資料
//					return new BasicRes(ResMessage.UPDATE_ID_NOT_FOUND.getCode(), //
//							ResMessage.UPDATE_ID_NOT_FOUND.getMessage());
//				}
//				Quiz quiz = op.get();
//				// 設定新值(值從req來)
//				// 將req中的新值設定到舊的quiz中，不設定id，因為id一樣
//				quiz.setName(req.getName());
//				quiz.setDescription(req.getDescription());
//				quiz.setStartDate(req.getStartDate());
//				quiz.setEndDate(req.getEndDate());
//				quiz.setQuestions(questionStr);
//				quiz.setPublished(req.isPublished());
				// 方法2 透過existsById:回傳一個bit的值
				//這邊要判斷從req帶進來的id是否真的有存在於DB中
				//因為若id不存在，後續程式碼在呼叫JPA的SAVE方法時，會變成新增
				boolean boo = quizDao.existsById(req.getId());
				if (!boo) {
					return new BasicRes(ResMessage.UPDATE_ID_NOT_FOUND.getCode(), //
							ResMessage.UPDATE_ID_NOT_FOUND.getMessage());
				}
				// 把id放進去
//				Quiz quiz = new Quiz(req.getId(),req.getName(), req.getDescription(), req.getStartDate(), req.getEndDate(),
//						questionStr, req.isPublished());
				
			}
			//上述一整段 if 程式碼可以縮減成以下這段
//			if (req.getId() > 0||!quizDao.existsById(req.getId())) {
//				return new BasicRes(ResMessage.UPDATE_ID_NOT_FOUND.getCode(), //
//						ResMessage.UPDATE_ID_NOT_FOUND.getMessage());
//			}
//			Quiz quiz = new Quiz(req.getName(),req.getDescription(),req.getStartDate(),req.getEndDate(),questionStr,req.isPublished());
//			quizDao.save(quiz);
			// 因為quiz只使用一次，因此可以使用匿名類別方式撰寫(不需要變數接)
			//new Quiz()中帶入req.getId()是PK，再呼叫save時，會先去檢查pk是否有存在於DB中，
			//若存在-->更新;不存在 -->新增
			//req中沒有在該欄位時，預設是0，因為id的資料型態是int
			quizDao.save(new Quiz(req.getId(),req.getName(), req.getDescription(), req.getStartDate(), req.getEndDate(),
					questionStr, req.isPublished()));
		} catch (JsonProcessingException e) {
			return new BasicRes(ResMessage.JSON_PROCESSING_EXCEPTION.getCode(), //
					ResMessage.JSON_PROCESSING_EXCEPTION.getMessage());
		}
		return new BasicRes(ResMessage.SUCCESS.getCode(), //
				ResMessage.SUCCESS.getMessage());
	}

	private BasicRes checkParams(CreateOrUpdateReq req) {
		// 檢查問卷參數
		// StringUtils.hasText(字串) : 會檢查字串是否為 null、空字串、空白字串，若是符合其中1種，會回false
		// 前面加個驚嘆號表示反向的意思，若字串檢查結果是false的話，就會進到if的實作區
		// !StringUtils.hasText(account) 等同於 StringUtils.hasText(account)==false
		// 有驚嘆號 沒驚嘆號
		if (!StringUtils.hasText(req.getName())) {
			return new BasicRes(ResMessage.PARAM_QUIZ_NAME_ERROR.getCode(), //
					ResMessage.PARAM_QUIZ_NAME_ERROR.getMessage());
		}
		if (!StringUtils.hasText(req.getDescription())) {
			return new BasicRes(ResMessage.PARAM_DESCRIPTION_ERROR.getCode(), //
					ResMessage.PARAM_DESCRIPTION_ERROR.getMessage());
		}
		// LocalDate.now(): 取得系統當前時間
		// req.getStartDate().isAfter(LocalDate.now()): 若 req 中的開始時間比當前時間晚，會得到 true
		// !req.getStartDate().isAfter(LocalDate.now()): 前面有加驚嘆號，表示會得到相反的結果，就是開始時間
		// 會等於小於當前時間
		if (req.getStartDate() == null || !req.getStartDate().isAfter(LocalDate.now())) {
			return new BasicRes(ResMessage.PARAM_START_DATE_ERROR.getCode(), //
					ResMessage.PARAM_START_DATE_ERROR.getMessage());
		}
		// 程式碼有執行到這行時，表示開始時間一定大於當前時間
		// 所以後續檢查結束時間時，只要確定結束時間是大於等於開始時間即可，因為只要結束時間是大於等於開始時間，
		// 就一定會是大於等於當前時間
		// 1. 結束時間不能小於等於當前時間 2. 結束時間不能小於開始間
		if (req.getEndDate() == null || req.getEndDate().isBefore(req.getStartDate())) {
			return new BasicRes(ResMessage.PARAM_END_DATE_ERROR.getCode(), //
					ResMessage.PARAM_END_DATE_ERROR.getMessage());
		}

		// 檢查問題參數
		if (CollectionUtils.isEmpty(req.getQuestionList())) {
			return new BasicRes(ResMessage.PARAM_QUESTION_List_NOT_FOUND.getCode(), //
					ResMessage.PARAM_QUESTION_List_NOT_FOUND.getMessage());
		}
		// 一張問卷可能有多個問題，所以要逐筆檢查每一題的參數
		for (Question item : req.getQuestionList()) {
			if (item.getId() <= 0) {
				return new BasicRes(ResMessage.PARAM_QUESTION_ID_ERROR.getCode(), //
						ResMessage.PARAM_QUESTION_ID_ERROR.getMessage());
			}
			if (!StringUtils.hasText(item.getTitle())) {
				return new BasicRes(ResMessage.PARAM_TITLE_ERROR.getCode(), //
						ResMessage.PARAM_TITLE_ERROR.getMessage());
			}

			if (!StringUtils.hasText(item.getType())) {
				return new BasicRes(ResMessage.PARAM_TYPE_ERROR.getCode(), //
						ResMessage.PARAM_TYPE_ERROR.getMessage());
			}
			// 當 option_type 是單選題時，options就不能是空字串
			// 反之，option_type是文字時，options 允許是空字串
			// 以下條件檢查:當option_type是單選或多選時，且 options是空字串，返回錯誤
			if (item.getType().equalsIgnoreCase(OptionType.SINGLE_CHOICE.getType())
					|| item.getType().equalsIgnoreCase(OptionType.MULTI_CHOICE.getType())) {
				if (!StringUtils.hasText(item.getOptions())) {
					return new BasicRes(ResMessage.PARAM_OPTIONS_ERROR.getCode(), //
							ResMessage.PARAM_OPTIONS_ERROR.getMessage());
				}
			}
			// 以下是上述兩個 if 合併寫法:(條件1||條件2)&&條件3
			// 第一個 if 條件式 && 第二個 if 條件式
//			if((item.getType().equalsIgnoreCase(OptionType.SINGLE_CHOICE.getType())
//					|| item.getType().equalsIgnoreCase(OptionType.MULTI_CHOICE.getType()))
//					&&!StringUtils.hasText(item.getOptions())) {
//				return new BasicRes(ResMessage.PARAM_OPTIONS_ERROR.getCode(), //
//						ResMessage.PARAM_OPTIONS_ERROR.getMessage());
//			}
		}
		return null;
	}

	@Override
	public SearchRes search(SearchReq req) {
		String name = req.getName();
		LocalDate start = req.getStartDate();
		LocalDate end = req.getEndDate();
		// 假設 name 是 null 或是全空白的字串，可以視為沒有輸入條件值，也就是沒有輸入條件值，就表示要取得全部
		// JPA 的 containing 方法，條件值是空字串時，會搜尋全部
		// 所以要把 name 的值是 null 或全空白字串時， 轉換成空字串
		if (!StringUtils.hasText(name)) {
			name = "";
		}
		if (start == null) {
			start = LocalDate.of(1970, 1, 1);
		}
		if (end == null) {
			end = LocalDate.of(2999, 12, 31);
		}
		return new SearchRes(ResMessage.SUCCESS.getCode(), //
				ResMessage.SUCCESS.getMessage(), //
				quizDao.findByNameContainingAndStartDateGreaterThanEqualAndEndDateLessThanEqual(name, //
						start, end));
	}

	@Override
	public BasicRes delete(DeleteReq req) {
		// 參數檢查
		if (!CollectionUtils.isEmpty(req.getIdList())) {
			// 刪除問卷
			try {
				quizDao.deleteAllById(req.getIdList());
			} catch (Exception e) {
				// 當 deleteAllById 方法中，id 的值不存在時，JPA 會報錯
				// 因為在刪除之前，JPA 會先搜尋帶入的 id 值，若沒結果就會報錯
				// 由於實際上沒刪除任何資料，因此就不需要對這個 Exception 作處理
			}

		}
		return new BasicRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage());
	}

	@Override
	public SearchRes findAll() {
		return new SearchRes(ResMessage.SUCCESS.getCode(), //
				ResMessage.SUCCESS.getMessage(), //
				quizDao.selectAll());
	}
}
