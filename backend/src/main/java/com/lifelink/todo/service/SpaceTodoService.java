package com.lifelink.todo.service;

import com.lifelink.todo.dto.CreateSpaceTodoRequest;
import com.lifelink.todo.dto.SpaceTodoResponse;
import com.lifelink.todo.dto.UpdateSpaceTodoRequest;

import java.util.List;

public interface SpaceTodoService {

    SpaceTodoResponse createTodo(Long relationshipId, CreateSpaceTodoRequest request, Long userId);

    List<SpaceTodoResponse> listTodos(Long relationshipId, String status, String keyword, Integer page, Integer size, Long userId);

    SpaceTodoResponse getTodoDetail(Long relationshipId, Long todoId, Long userId);

    SpaceTodoResponse updateTodo(Long relationshipId, Long todoId, UpdateSpaceTodoRequest request, Long userId);

    SpaceTodoResponse toggleTodoStatus(Long relationshipId, Long todoId, Long userId);

    void deleteTodo(Long relationshipId, Long todoId, Long userId);
}
