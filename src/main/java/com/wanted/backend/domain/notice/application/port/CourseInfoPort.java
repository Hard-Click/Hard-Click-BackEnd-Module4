package com.wanted.backend.domain.notice.application.port;


import java.util.List;
import java.util.Map;

public interface CourseInfoPort {
    String getCourseNameByCourseId(Long courseId);
    Map<Long, String> getCourseNamesByCourseIds(List<Long> courseIds);
}