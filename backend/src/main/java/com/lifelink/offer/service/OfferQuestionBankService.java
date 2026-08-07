package com.lifelink.offer.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifelink.common.BusinessException;
import com.lifelink.offer.dto.OfferCategoryRequest;
import com.lifelink.offer.dto.OfferQuestionBankRequest;
import com.lifelink.offer.dto.OfferQuestionPageResponse;
import com.lifelink.offer.dto.OfferQuestionRequest;
import com.lifelink.offer.dto.OfferStatisticsResponse;
import com.lifelink.offer.entity.OfferCategory;
import com.lifelink.offer.entity.OfferQuestion;
import com.lifelink.offer.entity.OfferQuestionBank;
import com.lifelink.offer.enums.OfferDifficulty;
import com.lifelink.offer.enums.OfferQuestionType;
import com.lifelink.offer.mapper.OfferCategoryMapper;
import com.lifelink.offer.mapper.OfferQuestionBankMapper;
import com.lifelink.offer.mapper.OfferQuestionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OfferQuestionBankService {

    private final OfferQuestionBankMapper questionBankMapper;
    private final OfferCategoryMapper categoryMapper;
    private final OfferQuestionMapper questionMapper;

    public List<OfferQuestionBank> listQuestionBanks() {
        return questionBankMapper.selectList(new LambdaQueryWrapper<OfferQuestionBank>()
                .orderByAsc(OfferQuestionBank::getSort)
                .orderByAsc(OfferQuestionBank::getId));
    }

    public List<OfferCategory> listCategories(Long bankId, OfferQuestionType type) {
        return categoryMapper.selectList(new LambdaQueryWrapper<OfferCategory>()
                .eq(bankId != null, OfferCategory::getBankId, bankId)
                .eq(type != null, OfferCategory::getType, type)
                .orderByAsc(OfferCategory::getSort)
                .orderByAsc(OfferCategory::getId));
    }

    public OfferQuestionPageResponse listQuestions(Long bankId, OfferQuestionType type, Long categoryId,
                                                   OfferDifficulty difficulty, String keyword, long page, long size) {
        long safePage = Math.max(page, 1L);
        long safeSize = Math.min(Math.max(size, 1L), 100L);
        LambdaQueryWrapper<OfferQuestion> wrapper = new LambdaQueryWrapper<OfferQuestion>()
                .eq(bankId != null, OfferQuestion::getBankId, bankId)
                .eq(type != null, OfferQuestion::getType, type)
                .eq(categoryId != null, OfferQuestion::getCategoryId, categoryId)
                .eq(difficulty != null, OfferQuestion::getDifficulty, difficulty)
                .and(StringUtils.hasText(keyword), condition -> condition
                        .like(OfferQuestion::getTitle, keyword.trim())
                        .or().like(OfferQuestion::getContent, keyword.trim())
                        .or().like(OfferQuestion::getAnswer, keyword.trim()))
                .orderByDesc(OfferQuestion::getUpdatedAt)
                .orderByDesc(OfferQuestion::getId);
        Page<OfferQuestion> result = questionMapper.selectPage(new Page<OfferQuestion>(safePage, safeSize), wrapper);
        return new OfferQuestionPageResponse(result.getRecords(), result.getTotal(), safePage, safeSize);
    }

    public OfferQuestion getQuestion(Long id) {
        OfferQuestion question = questionMapper.selectById(id);
        if (question == null) {
            throw new BusinessException(404, "Question not found");
        }
        return question;
    }

    public OfferStatisticsResponse statistics() {
        return new OfferStatisticsResponse(
                questionMapper.selectCount(null),
                questionMapper.selectCount(new LambdaQueryWrapper<OfferQuestion>().eq(OfferQuestion::getType, OfferQuestionType.THEORY)),
                questionMapper.selectCount(new LambdaQueryWrapper<OfferQuestion>().eq(OfferQuestion::getType, OfferQuestionType.ALGORITHM)),
                categoryMapper.selectCount(null));
    }

    @Transactional
    public OfferQuestionBank createQuestionBank(OfferQuestionBankRequest request) {
        ensureQuestionBankUnique(request.getName(), request.getCode(), null);
        OfferQuestionBank bank = new OfferQuestionBank();
        apply(request, bank);
        bank.setCreatedAt(LocalDateTime.now());
        bank.setUpdatedAt(LocalDateTime.now());
        questionBankMapper.insert(bank);
        return bank;
    }

    @Transactional
    public OfferQuestionBank updateQuestionBank(Long id, OfferQuestionBankRequest request) {
        OfferQuestionBank bank = getQuestionBank(id);
        ensureQuestionBankUnique(request.getName(), request.getCode(), id);
        apply(request, bank);
        bank.setUpdatedAt(LocalDateTime.now());
        questionBankMapper.updateById(bank);
        return bank;
    }

    @Transactional
    public void deleteQuestionBank(Long id) {
        getQuestionBank(id);
        if (categoryMapper.selectCount(new LambdaQueryWrapper<OfferCategory>().eq(OfferCategory::getBankId, id)) > 0) {
            throw new BusinessException("Question bank still contains categories");
        }
        questionBankMapper.deleteById(id);
    }

    @Transactional
    public OfferCategory createCategory(OfferCategoryRequest request) {
        ensureQuestionBank(request.getBankId());
        ensureCategoryUnique(request.getBankId(), request.getType(), request.getName(), null);
        OfferCategory category = new OfferCategory();
        apply(request, category);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        categoryMapper.insert(category);
        return category;
    }

    @Transactional
    public OfferCategory updateCategory(Long id, OfferCategoryRequest request) {
        OfferCategory category = getCategory(id);
        if ((!category.getBankId().equals(request.getBankId()) || category.getType() != request.getType())
                && questionMapper.selectCount(new LambdaQueryWrapper<OfferQuestion>().eq(OfferQuestion::getCategoryId, id)) > 0) {
            throw new BusinessException("Cannot change the bank or type of a category that has questions");
        }
        ensureQuestionBank(request.getBankId());
        ensureCategoryUnique(request.getBankId(), request.getType(), request.getName(), id);
        apply(request, category);
        category.setUpdatedAt(LocalDateTime.now());
        categoryMapper.updateById(category);
        return category;
    }

    @Transactional
    public void deleteCategory(Long id) {
        getCategory(id);
        if (questionMapper.selectCount(new LambdaQueryWrapper<OfferQuestion>().eq(OfferQuestion::getCategoryId, id)) > 0) {
            throw new BusinessException("Category still contains questions");
        }
        categoryMapper.deleteById(id);
    }

    @Transactional
    public OfferQuestion createQuestion(OfferQuestionRequest request) {
        validateCategory(request.getCategoryId(), request.getBankId(), request.getType());
        ensureQuestionUnique(request.getBankId(), request.getType(), request.getTitle(), null);
        OfferQuestion question = new OfferQuestion();
        apply(request, question);
        question.setCreatedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());
        questionMapper.insert(question);
        return question;
    }

    @Transactional
    public OfferQuestion updateQuestion(Long id, OfferQuestionRequest request) {
        OfferQuestion question = getQuestion(id);
        validateCategory(request.getCategoryId(), request.getBankId(), request.getType());
        ensureQuestionUnique(request.getBankId(), request.getType(), request.getTitle(), id);
        apply(request, question);
        question.setUpdatedAt(LocalDateTime.now());
        questionMapper.updateById(question);
        return question;
    }

    @Transactional
    public void deleteQuestion(Long id) {
        if (questionMapper.deleteById(id) == 0) {
            throw new BusinessException(404, "Question not found");
        }
    }

    private OfferQuestionBank getQuestionBank(Long id) {
        OfferQuestionBank bank = questionBankMapper.selectById(id);
        if (bank == null) {
            throw new BusinessException(404, "Question bank not found");
        }
        return bank;
    }

    private OfferCategory getCategory(Long id) {
        OfferCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(404, "Category not found");
        }
        return category;
    }

    private void ensureQuestionBank(Long id) {
        getQuestionBank(id);
    }

    private void ensureQuestionBankUnique(String name, String code, Long excludedId) {
        long count = questionBankMapper.selectCount(new LambdaQueryWrapper<OfferQuestionBank>()
                .and(condition -> condition.eq(OfferQuestionBank::getName, name.trim())
                        .or().eq(OfferQuestionBank::getCode, code.trim().toUpperCase()))
                .ne(excludedId != null, OfferQuestionBank::getId, excludedId));
        if (count > 0) {
            throw new BusinessException("Question bank name or code already exists");
        }
    }

    private void ensureCategoryUnique(Long bankId, OfferQuestionType type, String name, Long excludedId) {
        long count = categoryMapper.selectCount(new LambdaQueryWrapper<OfferCategory>()
                .eq(OfferCategory::getBankId, bankId)
                .eq(OfferCategory::getType, type)
                .eq(OfferCategory::getName, name.trim())
                .ne(excludedId != null, OfferCategory::getId, excludedId));
        if (count > 0) {
            throw new BusinessException("Category already exists in this question bank and type");
        }
    }

    private void ensureQuestionUnique(Long bankId, OfferQuestionType type, String title, Long excludedId) {
        long count = questionMapper.selectCount(new LambdaQueryWrapper<OfferQuestion>()
                .eq(OfferQuestion::getBankId, bankId)
                .eq(OfferQuestion::getType, type)
                .eq(OfferQuestion::getTitle, title.trim())
                .ne(excludedId != null, OfferQuestion::getId, excludedId));
        if (count > 0) {
            throw new BusinessException("Question title already exists in this question bank and type");
        }
    }

    private void validateCategory(Long categoryId, Long bankId, OfferQuestionType type) {
        OfferCategory category = getCategory(categoryId);
        if (!category.getBankId().equals(bankId) || category.getType() != type) {
            throw new BusinessException("Category does not match the selected question bank and type");
        }
    }

    private void apply(OfferQuestionBankRequest request, OfferQuestionBank bank) {
        bank.setName(request.getName().trim());
        bank.setCode(request.getCode().trim().toUpperCase());
        bank.setSort(request.getSort() == null ? 0 : request.getSort());
    }

    private void apply(OfferCategoryRequest request, OfferCategory category) {
        category.setBankId(request.getBankId());
        category.setName(request.getName().trim());
        category.setType(request.getType());
        category.setSort(request.getSort() == null ? 0 : request.getSort());
    }

    private void apply(OfferQuestionRequest request, OfferQuestion question) {
        question.setTitle(request.getTitle().trim());
        question.setType(request.getType());
        question.setBankId(request.getBankId());
        question.setCategoryId(request.getCategoryId());
        question.setDifficulty(request.getDifficulty());
        question.setContent(request.getContent().trim());
        question.setAnswer(request.getAnswer().trim());
        question.setSource(trimToNull(request.getSource()));
        question.setRemark(trimToNull(request.getRemark()));
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
