package com.wanted.backend.domain.community.application.command;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public record UpdatePostCommand(
        Long memberId,
        Long postId,
        Long subjectId,
        String title,
        String content,
        List<MultipartFile> files
) {

}