package com.lifelink.offer.controller;

import com.lifelink.common.Result;
import com.lifelink.offer.dto.OfferAccessResponse;
import com.lifelink.offer.dto.OfferCategoryRequest;
import com.lifelink.offer.dto.OfferImportRequest;
import com.lifelink.offer.dto.OfferImportResultResponse;
import com.lifelink.offer.dto.OfferQuestionBankRequest;
import com.lifelink.offer.dto.OfferQuestionPageResponse;
import com.lifelink.offer.dto.OfferQuestionRequest;
import com.lifelink.offer.dto.OfferStatisticsResponse;
import com.lifelink.offer.entity.OfferCategory;
import com.lifelink.offer.entity.OfferQuestion;
import com.lifelink.offer.entity.OfferQuestionBank;
import com.lifelink.offer.enums.OfferDifficulty;
import com.lifelink.offer.enums.OfferQuestionType;
import com.lifelink.offer.service.OfferAccessService;
import com.lifelink.offer.service.OfferQuestionBankService;
import com.lifelink.security.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/offer")
@RequiredArgsConstructor
public class OfferQuestionBankController {

    private final OfferAccessService accessService;
    private final OfferQuestionBankService offerService;

    @GetMapping("/access")
    public Result<OfferAccessResponse> access(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(accessService.getAccess(loginUser.getId()));
    }

    @GetMapping("/question-banks")
    public Result<List<OfferQuestionBank>> listQuestionBanks(@AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireMember(loginUser.getId());
        return Result.success(offerService.listQuestionBanks());
    }

    @GetMapping("/categories")
    public Result<List<OfferCategory>> listCategories(@RequestParam(required = false) Long bankId,
                                                       @RequestParam(required = false) OfferQuestionType type,
                                                       @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireMember(loginUser.getId());
        return Result.success(offerService.listCategories(bankId, type));
    }

    @GetMapping("/questions")
    public Result<OfferQuestionPageResponse> listQuestions(@RequestParam(required = false) Long bankId,
                                                           @RequestParam(required = false) OfferQuestionType type,
                                                           @RequestParam(required = false) Long categoryId,
                                                           @RequestParam(required = false) OfferDifficulty difficulty,
                                                           @RequestParam(required = false) String keyword,
                                                           @RequestParam(defaultValue = "1") long page,
                                                           @RequestParam(defaultValue = "20") long size,
                                                           @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireMember(loginUser.getId());
        return Result.success(offerService.listQuestions(bankId, type, categoryId, difficulty, keyword, page, size));
    }

    @GetMapping("/questions/{id}")
    public Result<OfferQuestion> getQuestion(@PathVariable Long id, @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireMember(loginUser.getId());
        return Result.success(offerService.getQuestion(id));
    }

    @GetMapping("/statistics")
    public Result<OfferStatisticsResponse> statistics(@AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireMember(loginUser.getId());
        return Result.success(offerService.statistics());
    }

    @PostMapping("/questions/import/preview")
    public Result<OfferImportResultResponse> previewImport(@Valid @RequestBody OfferImportRequest request,
                                                           @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireManager(loginUser.getId());
        return Result.success(offerService.previewImport(request));
    }

    @PostMapping("/questions/import")
    public Result<OfferImportResultResponse> importQuestions(@Valid @RequestBody OfferImportRequest request,
                                                             @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireManager(loginUser.getId());
        return Result.success(offerService.importQuestions(request));
    }

    @PostMapping("/question-banks")
    public Result<OfferQuestionBank> createQuestionBank(@Valid @RequestBody OfferQuestionBankRequest request,
                                                        @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireManager(loginUser.getId());
        return Result.success(offerService.createQuestionBank(request));
    }

    @PutMapping("/question-banks/{id}")
    public Result<OfferQuestionBank> updateQuestionBank(@PathVariable Long id, @Valid @RequestBody OfferQuestionBankRequest request,
                                                        @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireManager(loginUser.getId());
        return Result.success(offerService.updateQuestionBank(id, request));
    }

    @DeleteMapping("/question-banks/{id}")
    public Result<Void> deleteQuestionBank(@PathVariable Long id, @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireManager(loginUser.getId());
        offerService.deleteQuestionBank(id);
        return Result.success();
    }

    @PostMapping("/categories")
    public Result<OfferCategory> createCategory(@Valid @RequestBody OfferCategoryRequest request,
                                                @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireManager(loginUser.getId());
        return Result.success(offerService.createCategory(request));
    }

    @PutMapping("/categories/{id}")
    public Result<OfferCategory> updateCategory(@PathVariable Long id, @Valid @RequestBody OfferCategoryRequest request,
                                                @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireManager(loginUser.getId());
        return Result.success(offerService.updateCategory(id, request));
    }

    @DeleteMapping("/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id, @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireManager(loginUser.getId());
        offerService.deleteCategory(id);
        return Result.success();
    }

    @PostMapping("/questions")
    public Result<OfferQuestion> createQuestion(@Valid @RequestBody OfferQuestionRequest request,
                                                @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireManager(loginUser.getId());
        return Result.success(offerService.createQuestion(request));
    }

    @PutMapping("/questions/{id}")
    public Result<OfferQuestion> updateQuestion(@PathVariable Long id, @Valid @RequestBody OfferQuestionRequest request,
                                                @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireManager(loginUser.getId());
        return Result.success(offerService.updateQuestion(id, request));
    }

    @DeleteMapping("/questions/{id}")
    public Result<Void> deleteQuestion(@PathVariable Long id, @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireManager(loginUser.getId());
        offerService.deleteQuestion(id);
        return Result.success();
    }
}
