package com.sfms.mf10.service;

import com.sfms.iam.security.AuthUser;
import com.sfms.mf10.dto.FeedbackDtos.FeedbackWeeklyAggregate;

import java.time.LocalDate;

/** @version 1.0 — UC-164, BR-141/145. TDS-MF10_Backend.md §8.1. */
public interface FeedbackDashboardService {

    /** Tổng hợp ẨN DANH theo tuần: avgRating, phân bố sao, top tags, số phản hồi bệnh lý. */
    FeedbackWeeklyAggregate weeklyAggregate(AuthUser actor, LocalDate weekStart);
}
