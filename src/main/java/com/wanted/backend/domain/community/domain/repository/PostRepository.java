package com.wanted.backend.domain.community.domain.repository;

import com.wanted.backend.domain.community.domain.model.Post;

public interface PostRepository {
    Post save(Post post);
}