package com.wanted.backend.domain.community.application.service;

import com.wanted.backend.domain.community.application.command.CreatePostCommand;
import com.wanted.backend.domain.community.application.usecase.PostCommandUseCase;
import com.wanted.backend.domain.community.domain.model.Post;
import com.wanted.backend.domain.community.domain.model.PostFile;
import com.wanted.backend.domain.community.domain.repository.PostFileRepository;
import com.wanted.backend.domain.community.domain.repository.PostRepository;
import com.wanted.backend.domain.community.infrastructure.file.FileUploadUtils;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class PostCommandService implements PostCommandUseCase {

    @Value("${community.image.post-dir}")
    private String postDir;

    @Value("${community.image.post-url}")
    private String postUrl;

    @Value("${community.image.max-size}")
    private long maxFileSize;

    private final PostRepository postRepository;
    private final PostFileRepository postFileRepository;

    public PostCommandService(PostRepository postRepository,
                              PostFileRepository postFileRepository) {
        this.postRepository = postRepository;
        this.postFileRepository = postFileRepository;
    }

    @Override
    public Long create(CreatePostCommand command) {

        int fileCount = command.files() != null ? command.files().size() : 0;

        Post post = Post.create(
                command.authorId(),
                command.boardType(),
                command.subjectId(),
                command.title(),
                command.content(),
                fileCount
        );

        Post saved = postRepository.save(post);

        if (fileCount > 0) {
            List<String> savedFileNames = new ArrayList<>();
            try {
                for (int i = 0; i < command.files().size(); i++) {
                    MultipartFile file = command.files().get(i);
                    String savedFileName = FileUploadUtils.saveFile(file, postDir, maxFileSize);
                    savedFileNames.add(savedFileName);
                    String fileUrl = postUrl + savedFileName;
                    postFileRepository.save(PostFile.create(saved.getId(), fileUrl, i + 1));
                }
            } catch (IOException e) {
                savedFileNames.forEach(name -> FileUploadUtils.deleteFile(postDir, name));
                throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
            }
        }

        return saved.getId();
    }
}