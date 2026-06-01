package com.lifelink.calendar.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifelink.accounting.entity.AccountBook;
import com.lifelink.accounting.entity.Transaction;
import com.lifelink.accounting.mapper.AccountBookMapper;
import com.lifelink.accounting.mapper.TransactionMapper;
import com.lifelink.anniversary.entity.Anniversary;
import com.lifelink.anniversary.mapper.AnniversaryMapper;
import com.lifelink.calendar.dto.CalendarDayItemResponse;
import com.lifelink.calendar.dto.CalendarDayResponse;
import com.lifelink.calendar.dto.CalendarMonthQueryRequest;
import com.lifelink.calendar.dto.CalendarMonthResponse;
import com.lifelink.calendar.dto.CreateCalendarEventRequest;
import com.lifelink.calendar.dto.UpdateCalendarEventRequest;
import com.lifelink.calendar.entity.CalendarEvent;
import com.lifelink.calendar.entity.HolidayCalendar;
import com.lifelink.calendar.mapper.CalendarEventMapper;
import com.lifelink.calendar.mapper.HolidayCalendarMapper;
import com.lifelink.calendar.service.CalendarService;
import com.lifelink.common.BusinessException;
import com.lifelink.cycle.dto.CycleCalendarEventResponse;
import com.lifelink.cycle.service.CycleCareCalendarService;
import com.lifelink.daily.entity.DailyPost;
import com.lifelink.daily.mapper.DailyPostMapper;
import com.lifelink.focus.dto.FocusSessionResponse;
import com.lifelink.focus.service.FocusService;
import com.lifelink.relationship.entity.RelationshipMember;
import com.lifelink.relationship.service.RelationshipPermissionService;
import com.lifelink.todo.entity.SpaceTodo;
import com.lifelink.todo.mapper.SpaceTodoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService {

    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String DELETED_STATUS = "DELETED";
    private static final String TODO_STATUS = "TODO";
    private static final String DONE_STATUS = "DONE";
    private static final String RELATIONSHIP_BOOK_TYPE = "RELATIONSHIP";
    private static final String INCOME_TYPE = "INCOME";
    private static final String EXPENSE_TYPE = "EXPENSE";
    private static final String CUSTOM_EVENT_TYPE = "CUSTOM";
    private static final String NONE_REPEAT = "NONE";
    private static final String YEARLY_REPEAT = "YEARLY";

    private final SpaceTodoMapper spaceTodoMapper;
    private final AnniversaryMapper anniversaryMapper;
    private final DailyPostMapper dailyPostMapper;
    private final AccountBookMapper accountBookMapper;
    private final TransactionMapper transactionMapper;
    private final CalendarEventMapper calendarEventMapper;
    private final HolidayCalendarMapper holidayCalendarMapper;
    private final RelationshipPermissionService relationshipPermissionService;
    private final CycleCareCalendarService cycleCareCalendarService;
    private final FocusService focusService;

    @Override
    public CalendarMonthResponse getMonthCalendar(CalendarMonthQueryRequest request, Long userId) {
        if (request.getRelationshipId() == null) {
            throw new BusinessException(400, "relationshipId is required");
        }
        int year = request.getYear() == null ? YearMonth.now().getYear() : request.getYear();
        int month = request.getMonth() == null ? YearMonth.now().getMonthValue() : request.getMonth();
        YearMonth yearMonth;
        try {
            yearMonth = YearMonth.of(year, month);
        } catch (Exception ex) {
            throw new BusinessException(400, "Invalid year or month");
        }

        Long relationshipId = request.getRelationshipId();
        relationshipPermissionService.requireActiveRelationshipMember(relationshipId, userId);

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.plusMonths(1).atDay(1);
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.atStartOfDay();
        Map<LocalDate, CalendarDayResponse> dayMap = initDays(relationshipId, yearMonth);

        if (enabled(request.getIncludeHolidays())) {
            fillHolidays(dayMap, startDate, endDate);
        }
        if (enabled(request.getIncludeTodos())) {
            fillTodos(dayMap, relationshipId, startTime, endTime);
        }
        if (enabled(request.getIncludeAnniversaries())) {
            fillAnniversaries(dayMap, relationshipId, startDate, endDate);
        }
        if (enabled(request.getIncludeDailyPosts())) {
            fillDailyPosts(dayMap, relationshipId, startTime, endTime);
        }
        if (enabled(request.getIncludeTransactions())) {
            fillTransactions(dayMap, relationshipId, startTime, endTime);
        }
        if (enabled(request.getIncludeCustomEvents())) {
            fillCustomEvents(dayMap, relationshipId, startTime, endTime);
        }
        if (enabled(request.getIncludeCycleCare())) {
            fillCycleCareEvents(dayMap, relationshipId, yearMonth, userId);
        }
        if (enabled(request.getIncludeFocus())) {
            fillFocusSessions(dayMap, relationshipId, yearMonth);
        }

        List<CalendarDayResponse> days = new ArrayList<CalendarDayResponse>(dayMap.values());
        for (CalendarDayResponse day : days) {
            day.getItems().sort(Comparator
                    .comparing((CalendarDayItemResponse item) -> item.getAllDay() != null && item.getAllDay() ? 0 : 1)
                    .thenComparing(item -> item.getStartTime() == null ? LocalDateTime.MIN : item.getStartTime())
                    .thenComparing(CalendarDayItemResponse::getType));
        }
        return new CalendarMonthResponse(yearMonth.getYear(), yearMonth.getMonthValue(), relationshipId, days);
    }

    @Override
    public CalendarDayResponse getDayCalendar(Long relationshipId, LocalDate date, Long userId) {
        if (date == null) {
            throw new BusinessException(400, "date is required");
        }
        CalendarMonthQueryRequest request = new CalendarMonthQueryRequest();
        request.setRelationshipId(relationshipId);
        request.setYear(date.getYear());
        request.setMonth(date.getMonthValue());
        CalendarMonthResponse month = getMonthCalendar(request, userId);
        for (CalendarDayResponse day : month.getDays()) {
            if (date.equals(day.getDate())) {
                return day;
            }
        }
        throw new BusinessException(404, "Calendar day not found");
    }

    @Override
    @Transactional
    public CalendarDayResponse createCalendarEvent(CreateCalendarEventRequest request, Long userId) {
        relationshipPermissionService.requireActiveRelationshipMember(request.getRelationshipId(), userId);
        validateEventTime(request.getStartTime(), request.getEndTime());
        LocalDateTime now = LocalDateTime.now();

        CalendarEvent event = new CalendarEvent();
        event.setRelationshipId(request.getRelationshipId());
        event.setTitle(request.getTitle().trim());
        event.setDescription(request.getDescription());
        event.setEventType(normalizeOrDefault(request.getEventType(), CUSTOM_EVENT_TYPE));
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setAllDay(Boolean.TRUE.equals(request.getAllDay()));
        event.setRepeatType(normalizeOrDefault(request.getRepeatType(), NONE_REPEAT));
        event.setReminderMinutes(request.getReminderMinutes());
        event.setColor(request.getColor());
        event.setCreatedBy(userId);
        event.setUpdatedBy(userId);
        event.setStatus(ACTIVE_STATUS);
        event.setCreatedAt(now);
        event.setUpdatedAt(now);
        calendarEventMapper.insert(event);
        return getDayCalendar(event.getRelationshipId(), event.getStartTime().toLocalDate(), userId);
    }

    @Override
    @Transactional
    public CalendarDayResponse updateCalendarEvent(Long eventId, UpdateCalendarEventRequest request, Long userId) {
        CalendarEvent event = requireActiveEvent(eventId);
        if (!event.getRelationshipId().equals(request.getRelationshipId())) {
            throw new BusinessException(400, "Cannot move event across relationship spaces");
        }
        RelationshipMember member = relationshipPermissionService.requireActiveRelationshipMember(event.getRelationshipId(), userId);
        requireEventEditable(event, member, userId);
        validateEventTime(request.getStartTime(), request.getEndTime());

        event.setTitle(request.getTitle().trim());
        event.setDescription(request.getDescription());
        event.setEventType(normalizeOrDefault(request.getEventType(), CUSTOM_EVENT_TYPE));
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setAllDay(Boolean.TRUE.equals(request.getAllDay()));
        event.setRepeatType(normalizeOrDefault(request.getRepeatType(), NONE_REPEAT));
        event.setReminderMinutes(request.getReminderMinutes());
        event.setColor(request.getColor());
        event.setUpdatedBy(userId);
        event.setUpdatedAt(LocalDateTime.now());
        calendarEventMapper.updateById(event);
        return getDayCalendar(event.getRelationshipId(), event.getStartTime().toLocalDate(), userId);
    }

    @Override
    @Transactional
    public void deleteCalendarEvent(Long eventId, Long userId) {
        CalendarEvent event = requireActiveEvent(eventId);
        RelationshipMember member = relationshipPermissionService.requireActiveRelationshipMember(event.getRelationshipId(), userId);
        requireEventEditable(event, member, userId);
        event.setStatus(DELETED_STATUS);
        event.setUpdatedBy(userId);
        event.setUpdatedAt(LocalDateTime.now());
        calendarEventMapper.updateById(event);
    }

    private Map<LocalDate, CalendarDayResponse> initDays(Long relationshipId, YearMonth yearMonth) {
        Map<LocalDate, CalendarDayResponse> dayMap = new HashMap<LocalDate, CalendarDayResponse>();
        LocalDate today = LocalDate.now();
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = yearMonth.atDay(day);
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            CalendarDayResponse response = new CalendarDayResponse();
            response.setDate(date);
            response.setDayOfWeek(dayOfWeek.getValue());
            response.setIsToday(date.equals(today));
            response.setIsWeekend(dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY);
            response.setIsHoliday(false);
            response.setIsWorkday(false);
            response.setHolidayNames(new ArrayList<String>());
            response.setSolarTermNames(new ArrayList<String>());
            response.setItems(new ArrayList<CalendarDayItemResponse>());
            response.setTodoCount(0);
            response.setDoneTodoCount(0);
            response.setDailyPostCount(0);
            response.setAnniversaryCount(0);
            response.setIncomeAmount(BigDecimal.ZERO);
            response.setExpenseAmount(BigDecimal.ZERO);
            dayMap.put(date, response);
        }
        return dayMap;
    }

    private void fillHolidays(Map<LocalDate, CalendarDayResponse> dayMap, LocalDate startDate, LocalDate endDate) {
        List<HolidayCalendar> holidays = holidayCalendarMapper.selectList(new LambdaQueryWrapper<HolidayCalendar>()
                .ge(HolidayCalendar::getDate, startDate)
                .lt(HolidayCalendar::getDate, endDate)
                .orderByAsc(HolidayCalendar::getDate)
                .orderByAsc(HolidayCalendar::getType));
        for (HolidayCalendar holiday : holidays) {
            CalendarDayResponse day = dayMap.get(holiday.getDate());
            if (day == null) {
                continue;
            }
            if (Boolean.TRUE.equals(holiday.getHoliday())) {
                day.setIsHoliday(true);
            }
            if (Boolean.TRUE.equals(holiday.getWorkday())) {
                day.setIsWorkday(true);
            }
            if ("SOLAR_TERM".equals(holiday.getType())) {
                day.getSolarTermNames().add(holiday.getNameZh());
            } else {
                day.getHolidayNames().add(holiday.getNameZh());
            }
            if (!StringUtils.hasText(day.getLunarText()) && StringUtils.hasText(holiday.getLunarDate())) {
                day.setLunarText(holiday.getLunarDate());
            }
            day.getItems().add(buildHolidayItem(holiday));
        }
    }

    private void fillTodos(Map<LocalDate, CalendarDayResponse> dayMap, Long relationshipId, LocalDateTime startTime, LocalDateTime endTime) {
        List<SpaceTodo> todos = spaceTodoMapper.selectList(new LambdaQueryWrapper<SpaceTodo>()
                .eq(SpaceTodo::getRelationshipId, relationshipId)
                .ne(SpaceTodo::getStatus, DELETED_STATUS)
                .and(wrapper -> wrapper
                        .ge(SpaceTodo::getDueTime, startTime).lt(SpaceTodo::getDueTime, endTime)
                        .or()
                        .ge(SpaceTodo::getCompletedAt, startTime).lt(SpaceTodo::getCompletedAt, endTime)));
        for (SpaceTodo todo : todos) {
            if (todo.getDueTime() != null) {
                LocalDate dueDate = todo.getDueTime().toLocalDate();
                CalendarDayResponse day = dayMap.get(dueDate);
                if (day != null) {
                    day.getItems().add(buildTodoItem(todo, dueDate, "TODO", todo.getDueTime()));
                    if (DONE_STATUS.equals(todo.getStatus())) {
                        day.setDoneTodoCount(day.getDoneTodoCount() + 1);
                    } else {
                        day.setTodoCount(day.getTodoCount() + 1);
                    }
                }
            }
            if (todo.getCompletedAt() != null) {
                LocalDate completedDate = todo.getCompletedAt().toLocalDate();
                CalendarDayResponse day = dayMap.get(completedDate);
                if (day != null) {
                    day.getItems().add(buildTodoItem(todo, completedDate, "TODO_DONE", todo.getCompletedAt()));
                    day.setDoneTodoCount(day.getDoneTodoCount() + 1);
                }
            }
        }
    }

    private void fillAnniversaries(Map<LocalDate, CalendarDayResponse> dayMap, Long relationshipId, LocalDate startDate, LocalDate endDate) {
        List<Anniversary> anniversaries = anniversaryMapper.selectList(new LambdaQueryWrapper<Anniversary>()
                .eq(Anniversary::getRelationshipId, relationshipId)
                .eq(Anniversary::getStatus, ACTIVE_STATUS));
        for (Anniversary anniversary : anniversaries) {
            LocalDate displayDate = resolveAnniversaryDate(anniversary, startDate, endDate);
            if (displayDate == null) {
                continue;
            }
            CalendarDayResponse day = dayMap.get(displayDate);
            if (day != null) {
                day.getItems().add(buildAnniversaryItem(anniversary, displayDate));
                day.setAnniversaryCount(day.getAnniversaryCount() + 1);
            }
        }
    }

    private void fillDailyPosts(Map<LocalDate, CalendarDayResponse> dayMap, Long relationshipId, LocalDateTime startTime, LocalDateTime endTime) {
        List<DailyPost> posts = dailyPostMapper.selectList(new LambdaQueryWrapper<DailyPost>()
                .eq(DailyPost::getRelationshipId, relationshipId)
                .eq(DailyPost::getStatus, ACTIVE_STATUS)
                .ge(DailyPost::getCreatedAt, startTime)
                .lt(DailyPost::getCreatedAt, endTime)
                .orderByDesc(DailyPost::getCreatedAt));
        for (DailyPost post : posts) {
            LocalDate date = post.getCreatedAt().toLocalDate();
            CalendarDayResponse day = dayMap.get(date);
            if (day != null) {
                day.getItems().add(buildDailyPostItem(post, date));
                day.setDailyPostCount(day.getDailyPostCount() + 1);
            }
        }
    }

    private void fillTransactions(Map<LocalDate, CalendarDayResponse> dayMap, Long relationshipId, LocalDateTime startTime, LocalDateTime endTime) {
        List<AccountBook> books = accountBookMapper.selectList(new LambdaQueryWrapper<AccountBook>()
                .eq(AccountBook::getRelationshipId, relationshipId)
                .eq(AccountBook::getType, RELATIONSHIP_BOOK_TYPE)
                .eq(AccountBook::getStatus, ACTIVE_STATUS));
        if (books.isEmpty()) {
            return;
        }
        List<Long> bookIds = new ArrayList<Long>();
        for (AccountBook book : books) {
            bookIds.add(book.getId());
        }
        List<Transaction> transactions = transactionMapper.selectList(new LambdaQueryWrapper<Transaction>()
                .in(Transaction::getAccountBookId, bookIds)
                .eq(Transaction::getStatus, ACTIVE_STATUS)
                .ge(Transaction::getTransactionTime, startTime)
                .lt(Transaction::getTransactionTime, endTime)
                .orderByDesc(Transaction::getTransactionTime));
        for (Transaction transaction : transactions) {
            LocalDate date = transaction.getTransactionTime().toLocalDate();
            CalendarDayResponse day = dayMap.get(date);
            if (day == null) {
                continue;
            }
            if (INCOME_TYPE.equals(transaction.getType())) {
                day.setIncomeAmount(day.getIncomeAmount().add(transaction.getAmount()));
            } else if (EXPENSE_TYPE.equals(transaction.getType())) {
                day.setExpenseAmount(day.getExpenseAmount().add(transaction.getAmount()));
            }
            day.getItems().add(buildTransactionItem(transaction, date, relationshipId));
        }
    }

    private void fillCustomEvents(Map<LocalDate, CalendarDayResponse> dayMap, Long relationshipId, LocalDateTime startTime, LocalDateTime endTime) {
        List<CalendarEvent> events = calendarEventMapper.selectList(new LambdaQueryWrapper<CalendarEvent>()
                .eq(CalendarEvent::getRelationshipId, relationshipId)
                .eq(CalendarEvent::getStatus, ACTIVE_STATUS)
                .ge(CalendarEvent::getStartTime, startTime)
                .lt(CalendarEvent::getStartTime, endTime)
                .orderByAsc(CalendarEvent::getStartTime));
        for (CalendarEvent event : events) {
            LocalDate date = event.getStartTime().toLocalDate();
            CalendarDayResponse day = dayMap.get(date);
            if (day != null) {
                day.getItems().add(buildCustomEventItem(event, date));
            }
        }
    }

    private void fillCycleCareEvents(Map<LocalDate, CalendarDayResponse> dayMap, Long relationshipId, YearMonth yearMonth, Long userId) {
        List<CycleCalendarEventResponse> events;
        try {
            events = cycleCareCalendarService.getRelationshipCalendarEvents(relationshipId, yearMonth, userId);
        } catch (BusinessException ex) {
            if (ex.getCode() != null && ex.getCode() == 403) {
                return;
            }
            throw ex;
        }
        for (CycleCalendarEventResponse event : events) {
            CalendarDayResponse day = dayMap.get(event.getDate());
            if (day != null) {
                day.getItems().add(buildCycleCareItem(event, relationshipId));
            }
        }
    }

    private void fillFocusSessions(Map<LocalDate, CalendarDayResponse> dayMap, Long relationshipId, YearMonth yearMonth) {
        List<FocusSessionResponse> sessions = focusService.listRelationshipCalendarSessions(relationshipId, yearMonth);
        for (FocusSessionResponse session : sessions) {
            if (session.getStartedAt() == null) {
                continue;
            }
            LocalDate date = session.getStartedAt().toLocalDate();
            CalendarDayResponse day = dayMap.get(date);
            if (day != null) {
                day.getItems().add(buildFocusItem(session, date));
            }
        }
    }

    private CalendarDayItemResponse buildTodoItem(SpaceTodo todo, LocalDate date, String itemType, LocalDateTime itemTime) {
        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put("completedAt", todo.getCompletedAt());
        metadata.put("dueTime", todo.getDueTime());
        return new CalendarDayItemResponse(
                todo.getId(),
                itemType,
                todo.getTitle(),
                todo.getContent(),
                date,
                itemTime,
                null,
                false,
                todo.getRelationshipId(),
                "SPACE_TODO",
                todo.getId(),
                todo.getStatus(),
                todo.getPriority(),
                null,
                null,
                null,
                null,
                "check-square",
                metadata
        );
    }

    private CalendarDayItemResponse buildAnniversaryItem(Anniversary anniversary, LocalDate date) {
        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put("anniversaryDate", anniversary.getAnniversaryDate());
        metadata.put("repeatType", anniversary.getRepeatType());
        metadata.put("daysFromOriginal", ChronoUnit.DAYS.between(anniversary.getAnniversaryDate(), date));
        return new CalendarDayItemResponse(
                anniversary.getId(),
                "ANNIVERSARY",
                anniversary.getTitle(),
                anniversary.getDescription(),
                date,
                date.atStartOfDay(),
                null,
                true,
                anniversary.getRelationshipId(),
                "ANNIVERSARY",
                anniversary.getId(),
                anniversary.getStatus(),
                null,
                null,
                null,
                null,
                "#ffadd2",
                "heart",
                metadata
        );
    }

    private CalendarDayItemResponse buildDailyPostItem(DailyPost post, LocalDate date) {
        String summary = summarize(post.getContent());
        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put("mood", post.getMood());
        metadata.put("visibility", post.getVisibility());
        return new CalendarDayItemResponse(
                post.getId(),
                "DAILY_POST",
                summary,
                post.getContent(),
                date,
                post.getCreatedAt(),
                null,
                false,
                post.getRelationshipId(),
                "DAILY_POST",
                post.getId(),
                post.getStatus(),
                null,
                null,
                null,
                null,
                null,
                "read",
                metadata
        );
    }

    private CalendarDayItemResponse buildTransactionItem(Transaction transaction, LocalDate date, Long relationshipId) {
        BigDecimal income = INCOME_TYPE.equals(transaction.getType()) ? transaction.getAmount() : null;
        BigDecimal expense = EXPENSE_TYPE.equals(transaction.getType()) ? transaction.getAmount() : null;
        return new CalendarDayItemResponse(
                transaction.getId(),
                "TRANSACTION",
                transaction.getTitle(),
                transaction.getNote(),
                date,
                transaction.getTransactionTime(),
                null,
                false,
                relationshipId,
                "TRANSACTION",
                transaction.getId(),
                transaction.getStatus(),
                null,
                transaction.getAmount(),
                income,
                expense,
                INCOME_TYPE.equals(transaction.getType()) ? "#95de64" : "#ff7875",
                "wallet",
                Map.of("transactionType", transaction.getType(), "accountBookId", transaction.getAccountBookId())
        );
    }

    private CalendarDayItemResponse buildCustomEventItem(CalendarEvent event, LocalDate date) {
        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put("eventType", event.getEventType());
        metadata.put("repeatType", event.getRepeatType());
        metadata.put("reminderMinutes", event.getReminderMinutes());
        metadata.put("createdBy", event.getCreatedBy());
        return new CalendarDayItemResponse(
                event.getId(),
                "CUSTOM_EVENT",
                event.getTitle(),
                event.getDescription(),
                date,
                event.getStartTime(),
                event.getEndTime(),
                Boolean.TRUE.equals(event.getAllDay()),
                event.getRelationshipId(),
                "CALENDAR_EVENT",
                event.getId(),
                event.getStatus(),
                null,
                null,
                null,
                null,
                event.getColor(),
                "calendar",
                metadata
        );
    }

    private CalendarDayItemResponse buildCycleCareItem(CycleCalendarEventResponse event, Long relationshipId) {
        Map<String, Object> metadata = new HashMap<String, Object>();
        if (event.getMetadata() != null) {
            metadata.putAll(event.getMetadata());
        }
        metadata.put("predicted", event.getPredicted());
        return new CalendarDayItemResponse(
                event.getId(),
                event.getType(),
                event.getTitle(),
                String.valueOf(metadata.getOrDefault("disclaimer", "周期关怀提醒")),
                event.getDate(),
                event.getDate().atStartOfDay(),
                null,
                true,
                relationshipId,
                "CYCLE_CARE",
                event.getId(),
                ACTIVE_STATUS,
                null,
                null,
                null,
                null,
                cycleCareColor(event.getType()),
                "heart",
                metadata
        );
    }

    private CalendarDayItemResponse buildFocusItem(FocusSessionResponse session, LocalDate date) {
        String title = session.getTodoTitle() == null
                ? "专注 " + session.getActualMinutes() + " 分钟"
                : session.getTodoTitle() + " · 专注 " + session.getActualMinutes() + " 分钟";
        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put("actualMinutes", session.getActualMinutes());
        metadata.put("plannedMinutes", session.getPlannedMinutes());
        metadata.put("phase", session.getPhase());
        metadata.put("sessionType", session.getSessionType());
        metadata.put("roomId", session.getRoomId());
        return new CalendarDayItemResponse(
                session.getSessionId(),
                "FOCUS_SESSION",
                title,
                "完成了一次专注",
                date,
                session.getStartedAt(),
                session.getEndedAt(),
                false,
                session.getSpaceId(),
                "FOCUS_SESSION",
                session.getSessionId(),
                session.getStatus(),
                null,
                null,
                null,
                null,
                "#6f8fdb",
                "focus",
                metadata
        );
    }

    private String cycleCareColor(String type) {
        if ("CYCLE_WARNING".equals(type)) {
            return "#df746c";
        }
        if ("CYCLE_PERIOD_PREDICTED".equals(type)) {
            return "#e9a34f";
        }
        if ("CYCLE_OVULATION_ESTIMATED".equals(type) || "CYCLE_FERTILE_WINDOW_ESTIMATED".equals(type)) {
            return "#56b39b";
        }
        if ("CYCLE_DAILY_REPORT".equals(type)) {
            return "#7487a8";
        }
        return "#c678dd";
    }

    private CalendarDayItemResponse buildHolidayItem(HolidayCalendar holiday) {
        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put("nameZh", holiday.getNameZh());
        metadata.put("nameEn", holiday.getNameEn());
        metadata.put("holidayType", holiday.getType());
        metadata.put("lunarDate", holiday.getLunarDate());
        metadata.put("isHoliday", holiday.getHoliday());
        metadata.put("isWorkday", holiday.getWorkday());
        metadata.put("descriptionZh", holiday.getDescriptionZh());
        metadata.put("descriptionEn", holiday.getDescriptionEn());
        String itemType = "SOLAR_TERM".equals(holiday.getType()) ? "SOLAR_TERM" : "HOLIDAY";
        return new CalendarDayItemResponse(
                holiday.getId(),
                itemType,
                holiday.getNameZh(),
                holiday.getDescriptionZh(),
                holiday.getDate(),
                holiday.getDate().atStartOfDay(),
                null,
                true,
                null,
                "HOLIDAY_CALENDAR",
                holiday.getId(),
                ACTIVE_STATUS,
                null,
                null,
                null,
                null,
                "SOLAR_TERM".equals(holiday.getType()) ? "#91caff" : "#ffd666",
                "calendar",
                metadata
        );
    }

    private LocalDate resolveAnniversaryDate(Anniversary anniversary, LocalDate startDate, LocalDate endDate) {
        LocalDate date = anniversary.getAnniversaryDate();
        if (date == null) {
            return null;
        }
        if (YEARLY_REPEAT.equals(anniversary.getRepeatType())) {
            int day = Math.min(date.getDayOfMonth(), YearMonth.of(startDate.getYear(), date.getMonth()).lengthOfMonth());
            LocalDate displayDate = LocalDate.of(startDate.getYear(), date.getMonth(), day);
            return !displayDate.isBefore(startDate) && displayDate.isBefore(endDate) ? displayDate : null;
        }
        return !date.isBefore(startDate) && date.isBefore(endDate) ? date : null;
    }

    private CalendarEvent requireActiveEvent(Long eventId) {
        CalendarEvent event = calendarEventMapper.selectById(eventId);
        if (event == null || !ACTIVE_STATUS.equals(event.getStatus())) {
            throw new BusinessException(404, "Calendar event not found");
        }
        return event;
    }

    private void requireEventEditable(CalendarEvent event, RelationshipMember member, Long userId) {
        boolean adminOrOwner = RelationshipPermissionService.OWNER_ROLE.equals(member.getRole())
                || RelationshipPermissionService.ADMIN_ROLE.equals(member.getRole());
        if (!userId.equals(event.getCreatedBy()) && !adminOrOwner) {
            throw new BusinessException(403, "No permission to edit this calendar event");
        }
    }

    private void validateEventTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null) {
            throw new BusinessException(400, "Start time is required");
        }
        if (endTime != null && endTime.isBefore(startTime)) {
            throw new BusinessException(400, "End time cannot be earlier than start time");
        }
    }

    private String normalizeOrDefault(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase() : defaultValue;
    }

    private boolean enabled(Boolean value) {
        return value == null || Boolean.TRUE.equals(value);
    }

    private String summarize(String content) {
        if (!StringUtils.hasText(content)) {
            return "Daily post";
        }
        String trimmed = content.trim().replaceAll("\\s+", " ");
        if (trimmed.length() <= 40) {
            return trimmed;
        }
        return trimmed.substring(0, 40) + "...";
    }
}
