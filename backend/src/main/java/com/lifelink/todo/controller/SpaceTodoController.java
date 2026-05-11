package com.lifelink.todo.controller;

import com.lifelink.common.Result;
import com.lifelink.security.LoginUser;
import com.lifelink.todo.dto.CreateSpaceTodoRequest;
import com.lifelink.todo.dto.SpaceTodoResponse;
import com.lifelink.todo.dto.UpdateSpaceTodoRequest;
import com.lifelink.todo.service.SpaceTodoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/relationships/{relationshipId}/todos")
@RequiredArgsConstructor
public class SpaceTodoController {

    private final SpaceTodoService spaceTodoService;

    @PostMapping
    public Result<SpaceTodoResponse> createTodo(@PathVariable Long relationshipId,
                                                @Valid @RequestBody CreateSpaceTodoRequest request,
                                                @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(spaceTodoService.createTodo(relationshipId, request, loginUser.getId()));
    }

    @GetMapping
    public Result<List<SpaceTodoResponse>> listTodos(@PathVariable Long relationshipId,
                                                     @RequestParam(required = false) String status,
                                                     @RequestParam(required = false) String keyword,
                                                     @RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "20") Integer size,
                                                     @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(spaceTodoService.listTodos(relationshipId, status, keyword, page, size, loginUser.getId()));
    }

    @GetMapping("/{todoId}")
    public Result<SpaceTodoResponse> getTodoDetail(@PathVariable Long relationshipId,
                                                   @PathVariable Long todoId,
                                                   @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(spaceTodoService.getTodoDetail(relationshipId, todoId, loginUser.getId()));
    }

    @PutMapping("/{todoId}")
    public Result<SpaceTodoResponse> updateTodo(@PathVariable Long relationshipId,
                                                @PathVariable Long todoId,
                                                @Valid @RequestBody UpdateSpaceTodoRequest request,
                                                @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(spaceTodoService.updateTodo(relationshipId, todoId, request, loginUser.getId()));
    }

    @PatchMapping("/{todoId}/toggle")
    public Result<SpaceTodoResponse> toggleTodoStatus(@PathVariable Long relationshipId,
                                                      @PathVariable Long todoId,
                                                      @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(spaceTodoService.toggleTodoStatus(relationshipId, todoId, loginUser.getId()));
    }

    @DeleteMapping("/{todoId}")
    public Result<Void> deleteTodo(@PathVariable Long relationshipId,
                                   @PathVariable Long todoId,
                                   @AuthenticationPrincipal LoginUser loginUser) {
        spaceTodoService.deleteTodo(relationshipId, todoId, loginUser.getId());
        return Result.success();
    }
}
