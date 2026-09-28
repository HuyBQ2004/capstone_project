package com.sfms.mf10.service;

import com.sfms.common.enums.RoleKey;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf10.domain.Feedback;
import com.sfms.mf10.dto.FeedbackDtos.ClassAggregate;
import com.sfms.mf10.dto.FeedbackDtos.FeedbackWeeklyAggregate;
import com.sfms.mf10.dto.FeedbackDtos.TagCount;
import com.sfms.mf10.error.FbaExceptions;
import com.sfms.mf10.repository.FeedbackRepository;
import com.sfms.mf10.repository.FeedbackResponseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Hiện thực {@link FeedbackDashboardService}. TDS-MF10_Backend.md §8.1, UC-164, BR-141/145 (ADR-M10-5 — ẨN DANH). */
@Service
public class FeedbackDashboardServiceImpl implements FeedbackDashboardService {

    private final FeedbackRepository feedbackRepository;
    private final FeedbackResponseRepository responseRepository;

    public FeedbackDashboardServiceImpl(FeedbackRepository feedbackRepository,
                                         FeedbackResponseRepository responseRepository) {
        this.feedbackRepository = feedbackRepository;
        this.responseRepository = responseRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public FeedbackWeeklyAggregate weeklyAggregate(AuthUser actor, LocalDate weekStart) {
        if (actor.role() != RoleKey.SCHOOL_ADMIN) {
            throw FbaExceptions.accessDenied();
        }
        var schoolId = actor.schoolId();
        LocalDate start = weekStart != null ? weekStart : LocalDate.now().minusDays(6);
        LocalDate end = start.plusDays(6);

        List<Feedback> rows = feedbackRepository.findAll((root, cq, cb) -> cb.and(
                cb.equal(root.get("schoolId"), schoolId),
                cb.isNull(root.get("deletedAt")),
                cb.between(root.get("mealDate"), start, end)));

        long total = rows.size();
        double averageRating = rows.stream().mapToInt(Feedback::getRating).average().orElse(0);
        Map<Integer, Long> ratingDistribution = new LinkedHashMap<>();
        for (int star = 1; star <= 5; star++) {
            int s = star;
            ratingDistribution.put(s, rows.stream().filter(f -> f.getRating() == s).count());
        }
        long negativeCount = rows.stream().filter(f -> f.getRating() < 3).count();
        long illnessFlaggedCount = rows.stream().filter(Feedback::isIllnessKeyword).count();
        long respondedCount = rows.stream().filter(f -> responseRepository.existsByFeedbackId(f.getId())).count();
        double respondedRate = total == 0 ? 0 : (double) respondedCount / total;

        Map<String, Long> tagCounts = new LinkedHashMap<>();
        for (Feedback f : rows) {
            for (String tag : f.getTags()) {
                tagCounts.merge(tag, 1L, Long::sum);
            }
        }
        List<TagCount> topTags = tagCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> new TagCount(e.getKey(), e.getValue()))
                .toList();

        Map<String, List<Feedback>> byClassMap = new LinkedHashMap<>();
        for (Feedback f : rows) {
            byClassMap.computeIfAbsent(f.getClassName(), k -> new java.util.ArrayList<>()).add(f);
        }
        List<ClassAggregate> byClass = byClassMap.entrySet().stream()
                .map(e -> new ClassAggregate(e.getKey(), e.getValue().size(),
                        e.getValue().stream().mapToInt(Feedback::getRating).average().orElse(0)))
                .sorted(Comparator.comparing(ClassAggregate::clazz))
                .toList();

        return new FeedbackWeeklyAggregate(start, end, total, averageRating, ratingDistribution,
                negativeCount, illnessFlaggedCount, respondedRate, topTags, byClass);
    }
}
