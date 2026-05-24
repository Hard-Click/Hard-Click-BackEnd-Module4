package com.wanted.backend.domain.community.domain.repository;

import com.wanted.backend.domain.community.domain.model.PostFile;
import java.util.List;

public interface PostFileRepository {
    PostFile save(PostFile postFile);
}