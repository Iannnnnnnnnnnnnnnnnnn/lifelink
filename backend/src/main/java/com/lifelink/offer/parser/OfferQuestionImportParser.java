package com.lifelink.offer.parser;

import com.lifelink.offer.enums.OfferDifficulty;
import com.lifelink.offer.enums.OfferQuestionType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses explicit marker blocks only, preserving Markdown and code blocks unchanged. */
@Component
public class OfferQuestionImportParser {

    private static final Pattern BLOCK = Pattern.compile("(?ms)^===QUESTION===\\s*\\R(.*?)^===END===\\s*(?=^===QUESTION===|\\z)");
    private static final Pattern HEADER = Pattern.compile("^([a-zA-Z]+):\\s*(.*)$");

    public List<ParsedOfferQuestion> parse(String raw) {
        List<ParsedOfferQuestion> result = new ArrayList<>();
        Matcher matcher = BLOCK.matcher(normalize(raw));
        int index = 1;
        while (matcher.find()) {
            result.add(parseBlock(index++, matcher.group(1)));
        }
        if (result.isEmpty()) {
            ParsedOfferQuestion invalid = new ParsedOfferQuestion();
            invalid.setIndex(1);
            invalid.addError("未找到完整题目块，请检查 ===QUESTION=== 和 ===END=== 是否独占一行");
            result.add(invalid);
        }
        return result;
    }

    private ParsedOfferQuestion parseBlock(int index, String block) {
        ParsedOfferQuestion question = new ParsedOfferQuestion();
        question.setIndex(index);
        int contentAt = markerIndex(block, "CONTENT");
        int answerAt = markerIndex(block, "ANSWER");
        if (contentAt < 0) {
            question.addError("缺少 ===CONTENT=== 标记");
        }
        if (answerAt < 0) {
            question.addError("缺少 ===ANSWER=== 标记");
        }
        if (contentAt >= 0 && answerAt >= 0 && contentAt > answerAt) {
            question.addError("===CONTENT=== 必须在 ===ANSWER=== 之前");
        }
        if (!question.isValid()) {
            return question;
        }

        String headerPart = block.substring(0, contentAt).trim();
        String content = block.substring(contentAt + markerLength(block, contentAt), answerAt).trim();
        String answer = block.substring(answerAt + markerLength(block, answerAt)).trim();
        for (String line : headerPart.split("\\R")) {
            if (line.isBlank()) {
                continue;
            }
            Matcher header = HEADER.matcher(line.trim());
            if (!header.matches()) {
                question.addError("Header 格式错误：" + line);
                continue;
            }
            String key = header.group(1).toLowerCase(Locale.ROOT);
            String value = header.group(2).trim();
            switch (key) {
                case "type" -> question.setType(parseType(value, question));
                case "bank" -> question.setBank(value);
                case "category" -> question.setCategory(value);
                case "difficulty" -> question.setDifficulty(parseDifficulty(value, question));
                case "title" -> question.setTitle(value);
                case "source" -> question.setSource(value);
                default -> question.addError("不支持的 Header：" + key);
            }
        }
        if (question.getType() == null) {
            question.addError("缺少或错误的 type（THEORY / ALGORITHM）");
        }
        if (question.getBank() == null || question.getBank().isBlank()) {
            question.setBank("Java");
        }
        if (question.getCategory() == null || question.getCategory().isBlank()) {
            question.addError("缺少 category");
        }
        if (question.getDifficulty() == null) {
            question.addError("缺少或错误的 difficulty（EASY / MEDIUM / HARD）");
        }
        if (question.getTitle() == null || question.getTitle().isBlank()) {
            question.addError("缺少 title");
        }
        if (content.isBlank()) {
            question.addError("题目正文不能为空");
        }
        if (answer.isBlank()) {
            question.addError("答案不能为空");
        }
        question.setContent(content);
        question.setAnswer(answer);
        return question;
    }

    private int markerIndex(String text, String name) {
        Matcher matcher = Pattern.compile("(?m)^===" + name + "===$").matcher(text);
        return matcher.find() ? matcher.start() : -1;
    }

    private int markerLength(String text, int index) {
        int end = text.indexOf('\n', index);
        return (end < 0 ? text.length() : end + 1) - index;
    }

    private String normalize(String raw) {
        return raw == null ? "" : raw.replace("\r\n", "\n").replace('\r', '\n');
    }

    private OfferQuestionType parseType(String value, ParsedOfferQuestion question) {
        try {
            return OfferQuestionType.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            question.addError("type 必须为 THEORY 或 ALGORITHM");
            return null;
        }
    }

    private OfferDifficulty parseDifficulty(String value, ParsedOfferQuestion question) {
        try {
            return OfferDifficulty.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            question.addError("difficulty 必须为 EASY、MEDIUM 或 HARD");
            return null;
        }
    }
}
