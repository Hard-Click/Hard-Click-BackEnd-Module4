package com.wanted.backend.domain.learning_activity.application.policy;

import com.wanted.backend.domain.learning_activity.domain.model.VideoAccessInfo;
import org.springframework.stereotype.Component;

@Component
public class VideoAccessPolicy {

    /* comment.
    *   순수 조건 판단만 한다.
    *   ex) 공개 강의이고, 무료 강의이거나, 미리보기 영상이거나, 수강권이 있거나, 구독권이 있으면 재생 가능
    *   Policy 는 DB 모름
    *   Repository 도 모름
    *   그냥 값만 받아서 판단
    * */

    public boolean canPlay(VideoAccessInfo accessInfo, boolean enrolled, boolean subscribed) {
        if (!accessInfo.isPublishedCourse()) {
            return false;
        }

        return accessInfo.isPreview()
                || accessInfo.isFreeCourse()
                || enrolled
                || subscribed;
    }
}
