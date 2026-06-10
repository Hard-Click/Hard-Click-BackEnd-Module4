package com.wanted.backend.domain.community.application.command;

import com.wanted.backend.domain.community.domain.model.BoardType;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public record CreatePostCommand(
        Long authorId,
        BoardType boardType,
        Long subjectId,
        String title,
        String content,
        List<MultipartFile> files
) {}